/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
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
package uk.co.flax.ukmp.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.UserMentionEntity;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.config.TwitterConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;
import uk.co.flax.ukmp.services.EntityExtractionService;

public class TweetUpdateThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(TweetUpdateThread.class);

	private static final DateFormat DATA_DIR_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private static final long QUEUE_CHECK_TIME = 500;

	private final SearchEngine searchEngine;
	private final Queue<Status> statusQueue;
	private final int batchSize;
	private final EntityExtractionService entityExtraction;
	private final TwitterConfiguration config;

	private boolean running;

	private Map<Long, String> partyListIds;

	public TweetUpdateThread(SearchEngine searchEngine, Queue<Status> statusQueue, int batchSize,
			EntityExtractionService ees, TwitterConfiguration config) {
		this.searchEngine = searchEngine;
		this.statusQueue = statusQueue;
		this.batchSize = batchSize;
		this.entityExtraction = ees;
		this.config = config;
	}

	@Override
	public void run() {
		LOGGER.info("Starting twitter update thread.");
		while (partyListIds == null) {
			try {
				LOGGER.debug("No party list IDs - waiting...");
				Thread.sleep(QUEUE_CHECK_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("Interrupted waiting for party list IDs: {}", e.getMessage());
			}
		}
		running = true;

		while (running) {
			List<Status> updates = new ArrayList<>(batchSize);
			while (!statusQueue.isEmpty() && updates.size() < batchSize) {
				updates.add(statusQueue.remove());
			}

			// Index the statuses
			storeUpdates(updates);

			// Sleep...
			try {
				Thread.sleep(QUEUE_CHECK_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("Update thread sleep interrupted: {}", e.getMessage());
			}
		}
	}

	public void closeDown() {
		// Set running to false
		running = false;

		LOGGER.debug("Shutting down twitter update thread");

		if (!statusQueue.isEmpty()) {
			// Try to clear the queue
			LOGGER.debug("Trying to clear {} items from update queue", statusQueue.size());

			List<Status> updates = new ArrayList<>(statusQueue);
			storeUpdates(updates);
		}

		LOGGER.info("Twitter update thread shut down.");
	}

	private void storeUpdates(List<Status> updates) {
		if (!updates.isEmpty()) {
			try {
				List<Tweet> tweets = new ArrayList<>(updates.size());
				updates.forEach(update -> tweets.add(buildTweetFromStatus(update)));

				searchEngine.indexTweets(tweets);
				LOGGER.debug("Indexed {} tweets", updates.size());

				// Store the tweets
				storeTweets(tweets);
			} catch (SearchEngineException e) {
				LOGGER.error("Could not index {} tweets: {}", updates.size(), e.getMessage());
			} catch (IOException e) {
				LOGGER.error("Could not store tweets: {}", e.getMessage());
			}
		}
	}

	private Tweet buildTweetFromStatus(Status status) {
		String text = status.getText();

		Tweet tweet = new Tweet();
		tweet.setId("" + status.getId());
		tweet.setText(text);
		tweet.setUserScreenName(status.getUser().getScreenName());
		tweet.setUserName(status.getUser().getName());
		tweet.setCreated(status.getCreatedAt());
		if (status.getPlace() != null) {
			tweet.setPlaceName(status.getPlace().getFullName());
			tweet.setCountry(status.getPlace().getCountry());
		}
		tweet.setRetweetCount(status.getRetweetCount());
		tweet.setFavouriteCount(status.getFavoriteCount());
		tweet.setParty(partyListIds.get(status.getUser().getId()));

		if (status.getUserMentionEntities() != null) {
			List<String> screenNames = new ArrayList<>(status.getUserMentionEntities().length);
			List<String> fullNames = new ArrayList<>(status.getUserMentionEntities().length);

			for (UserMentionEntity ent : status.getUserMentionEntities()) {
				screenNames.add(ent.getScreenName());
				if (StringUtils.isNotBlank(ent.getName())) {
					fullNames.add(ent.getName());
				}
			}
			tweet.setMentionScreenNames(screenNames);
			tweet.setMentionFullNames(fullNames);
		}

		if (status.getHashtagEntities().length > 0) {
			List<String> hashtags = new ArrayList<>(status.getHashtagEntities().length);
			for (HashtagEntity ht : status.getHashtagEntities()) {
				hashtags.add(ht.getText());
			}
			tweet.setHashtags(hashtags);
		}

		// Call the entity extraction service
		Map<String, List<String>> entities = entityExtraction.getEntities(text);
		if (entities != null && !entities.isEmpty()) {
			Map<String, Object> tweetEntities = new HashMap<String, Object>();
			entities.keySet().forEach(type -> tweetEntities.put(type, entities.get(type)));
			tweet.setEntities(tweetEntities);
		}

		return tweet;
	}

	private void storeTweets(List<Tweet> tweets) throws IOException {
		File dataDir = new File(config.getDataDirectory());
		ObjectMapper mapper = new ObjectMapper();

		for (Tweet tweet : tweets) {
			OutputStream out = null;

			File tweetDir = new File(dataDir, DATA_DIR_FORMAT.format(tweet.getCreated()));
			if (!tweetDir.exists()) {
				if (!tweetDir.mkdirs()) {
					LOGGER.error("Could not create data directory {}", tweetDir);
				}
			}

			try {
				File tweetFile = new File(tweetDir, tweet.getId() + ".json");
				out = new FileOutputStream(tweetFile);

				mapper.writeValue(out, tweet);

				out.flush();
			} catch (JsonGenerationException e) {
				LOGGER.error("Could not generate JSON for tweet: {}", e.getMessage());
			} catch (JsonMappingException e) {
				LOGGER.error("JSON mapping issue for tweet: {}", e.getMessage());
			} catch (IOException e) {
				throw e;
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						LOGGER.error("Could not close tweet output stream: {}", e.getMessage());
						throw e;
					}
				}
			}
		}
	}

	/**
	 * @param partyListIds the partyListIds to set
	 */
	public void setPartyListIds(Map<Long, String> partyListIds) {
		this.partyListIds = partyListIds;
	}

}
