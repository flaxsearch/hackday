/**
 * Copyright (c) 2014 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.flax.ukmp.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.api.Term;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.config.TermsConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

/**
 * Thread handling the terms management.
 */
public class TermsManagerThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermsManagerThread.class);

	static final long THREAD_SLEEP_MS = 500;

	/** The stopwords list - initialised at startup */
	private final Map<String, Integer> stopWords;
	/** The search engine */
	private final SearchEngine searchEngine;
	/** The configuration */
	private final TermsConfiguration termsConfig;
	/** Refresh interval */
	private final long refreshInterval;

	/** Is the thread running? */
	private boolean running;
	/** Last update run time */
	private long lastRunTime;

	/** The list of terms */
	private List<Term> terms;


	public TermsManagerThread(SearchEngine engine, TermsConfiguration config) throws Exception {
		this.searchEngine = engine;
		this.termsConfig = config;
		this.stopWords = new HashMap<String, Integer>();
		initialiseStopWords(termsConfig.getStopWordsFile());
		refreshInterval = config.getRefreshMinutes() * 60 * 1000;
		lastRunTime = 0;
		this.terms = new ArrayList<Term>();
	}


	/**
	 * Initialise the stopwords map.
	 * @param swFilePath path to the stopwords file.
	 * @throws IOException if the file cannot be read.
	 */
	private void initialiseStopWords(String swFilePath) throws IOException {
		BufferedReader br = null;
		try {
			File stopwordsFile = new File(swFilePath);
			br = new BufferedReader(new FileReader(stopwordsFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
					stopWords.put(line.trim().toLowerCase(), 1);
				}
			}
		} catch (IOException ioe) {
			LOGGER.error("Exception initialising stopwords: {}", ioe.getMessage());
			throw ioe;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error("Exception thrown closing file reader: {}", e.getMessage());
				}
			}
		}
	}


	@Override
	public void run() {
		LOGGER.info("TermsManagerThread starting...");
		running = true;

		while (running) {
			if (isRefreshTime()) {
				LOGGER.debug("Updating terms list");

				try {
					// Refresh the word list
					List<Tweet> tweets = getTweets();
					List<Term> termList = extractTerms(tweets);

					// Update the public term list - needs to be locked
					synchronized (terms) {
						// Check the endpoint, in case there aren't enough terms
						int end = termsConfig.getLimit();
						if (termList.size() < end) {
							end = termList.size();
						}

						terms = termList.subList(0, end);
					}

					lastRunTime = System.currentTimeMillis();
				} catch (SearchEngineException e) {
					LOGGER.error("Terms update failed - not updating terms list: {}", e.getMessage());
				}
			}

			try {
				Thread.sleep(THREAD_SLEEP_MS);
			} catch (InterruptedException e) {
				LOGGER.error("Thread sleep interrupted: {}", e.getMessage());
			}
		}

		LOGGER.info("TermsManagerThread stopped");
	}


	private boolean isRefreshTime() {
		return System.currentTimeMillis() > (lastRunTime + refreshInterval);
	}


	/**
	 * Get a list of tweets from the search engine, containing only the
	 * text of the tweet.
	 * @return the tweets.
	 * @throws SearchEngineException if the search engine throws an exception.
	 */
	private List<Tweet> getTweets() throws SearchEngineException {
		// Refresh the word list
		List<Tweet> tweets = new ArrayList<Tweet>(termsConfig.getBatchSize());
		long numResults;
		int batch = 0;
		do {
			LOGGER.trace("Fetching batch {}", batch);
			SearchResults results = searchEngine.getTextBatch(batch);
			numResults = results.getNumResults();
			tweets.addAll(results.getTweets());
			batch ++;

			LOGGER.trace("Fetched {}/{} results", tweets.size(), numResults);
		} while (tweets.size() < numResults);

		return tweets;
	}


	/**
	 * Extract a list of terms, sorted by popularity (descending) from a list
	 * of tweets.
	 * @param tweets the tweets whose text should be analysed.
	 * @return a complete list of terms in the tweets, filtered by the stopwords
	 * list, and with usernames, links and words with punctuation filtered out.
	 */
	private List<Term> extractTerms(List<Tweet> tweets) {
		Map<String, Integer> termMap = new HashMap<String, Integer>();

		for (Tweet tweet : tweets) {
			// Split text into words, breaking on whitespace
			String[] words = tweet.getText().split("\\s+");
			for (String w : words) {
				// Skip all Twitter handles by default
				if (!w.startsWith("@")) {
					// Split individual words by punctuation (except hyphens)
					String[] unpunctuated = w.split("[^-A-Za-z0-9]");
					// Ignore anything that has split into more than one term - should
					// cut out URLs
					if (unpunctuated.length == 1) {
						// Force word into lower case
						String word = unpunctuated[0].toLowerCase();
						if (!isWordInStopWords(word)) {
							if (!termMap.containsKey(word)) {
								termMap.put(word, 0);
							}

							termMap.put(word, termMap.get(word) + 1);
						}
					}
				}
			}
		}

		LOGGER.trace("Extracted {} terms from {} tweets", termMap.size(), tweets.size());

		// Convert the map into a set of terms in reverse order (ie. most popular first)
		Set<Term> termSet = new TreeSet<Term>(Collections.reverseOrder());
		for (String word : termMap.keySet()) {
			Term term = new Term(word, termMap.get(word));
			termSet.add(term);
		}

		// Convert the set into a List and return it
		return new ArrayList<Term>(termSet);
	}


	/**
	 * @param word word to be checked.
	 * @return <code>true</code> if the word is in the stopwords list.
	 */
	private boolean isWordInStopWords(String word) {
		return stopWords.containsKey(word);
	}


	/**
	 * Stop the thread. There may be a short delay before the thread is actually
	 * stopped.
	 * @param running
	 */
	public void shutdown() {
		this.running = false;
	}

	/**
	 * @return <code>true</code> if the thread is running.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Get the current list of terms.
	 * @return the list of terms, sorted in descending frequency order.
	 */
	public List<Term> getTerms() {
		return terms;
	}

}
