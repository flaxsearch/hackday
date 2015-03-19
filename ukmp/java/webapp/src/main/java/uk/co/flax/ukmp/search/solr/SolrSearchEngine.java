/**
 * Copyright (c) 2013 Lemur Consulting Ltd.
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
package uk.co.flax.ukmp.search.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.ukmp.api.Facet;
import uk.co.flax.ukmp.api.FacetList;
import uk.co.flax.ukmp.api.FacetQuery;
import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.api.SearchState;
import uk.co.flax.ukmp.api.Sentiment;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.config.SolrConfiguration;
import uk.co.flax.ukmp.config.TermsConfiguration;
import uk.co.flax.ukmp.search.Query;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

/**
 * Solr implementation of the search engine interface.
 */
public class SolrSearchEngine implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSearchEngine.class);

	public static final String ID_FIELD = "id";
	public static final String TEXT_FIELD = "text";
	public static final String PARTY_FIELD = "party";
	public static final String CREATED_FIELD = "created_at";
	public static final String COUNTRY_FIELD = "place_country";
	public static final String PLACE_NAME = "place_full_name";
	public static final String USER_SCREEN_NAME = "user_screen_name";
	public static final String USER_FULL_NAME = "user_full_name";
	public static final String SENTIMENT_FIELD = "sentiment";
	public static final String RETWEET_COUNT_FIELD = "retweet_count";
	public static final String FAVOURITE_COUNT_FIELD = "favorite_count";

	/**
	 * Static instance of the Solr server - should be only one of these per
	 * running application.
	 */
	private static SolrServer server;
	/** The Solr configuration details */
	private SolrConfiguration config;

	public SolrSearchEngine(SolrConfiguration config) {
		initialiseServer(config);
		this.config = config;
	}

	/**
	 * Unit testing constructor.
	 * @param server mock server instance.
	 */
	SolrSearchEngine(SolrServer server, SolrConfiguration config) {
		SolrSearchEngine.server = server;
		this.config = config;
	}

	/**
	 * Initialise the single server connection.
	 */
	private static void initialiseServer(SolrConfiguration config) {
		// Instantiate the server link
		server = new HttpSolrServer(config.getBaseUrl());
	}

	@Override
	public boolean isServerReady() throws SearchEngineException {
		boolean ready = false;

		try {
			SolrPingResponse response = server.ping();
			ready = (response != null && response.getStatus() == 0);

			if (!ready) {
				if (response == null) {
					LOGGER.error("Search engine returned null response from ping()");
				} else {
					LOGGER.error("Search engine is not ready: ", response.getResponse());
				}
			}
		} catch (SolrServerException e) {
			LOGGER.error("Server exception from ping(): {}", e.getMessage());
		} catch (IOException e) {
			LOGGER.error("IO exception when calling server: {}", e.getMessage());
		}

		return ready;
	}

	@Override
	public SearchResults search(Query query) throws SearchEngineException {
		SearchResults results;

		SolrQuery sQuery = new SolrQuery(query.getQuery());
		sQuery.setRows(query.getPageSize());
		int start = query.getPageSize() * query.getPageNumber();
		sQuery.setStart(start);
		sQuery.setSort(query.getSortField(), query.isSortAscending() ? ORDER.asc : ORDER.desc);

		if (query.getFilters() != null) {
			for (String fq : query.getFilters()) {
				sQuery.addFilterQuery(fq);
			}
		}

		// Set the request handler
		sQuery.setRequestHandler(config.getQueryHandler());

		try {
			QueryResponse response = server.query(sQuery);
			SolrDocumentList docs = response.getResults();

			Map<String, FacetList> availableFilters = extractAvailableFilters(response);
			availableFilters.putAll(extractFacetQueries(response));
			Map<String, FacetList> appliedFilters = extractAppliedFilters(query);
			trimAvailableFilters(availableFilters, appliedFilters);

			SearchState search = new SearchState(query.getQuery(), query.getSortField(), query.isSortAscending(),
					query.getPageNumber(), availableFilters, null,
					appliedFilters, query.isHighlightingEnabled());
			List<Tweet> tweets = extractTweets(docs, response.getHighlighting(), query.isHighlightingEnabled());

			results = new SearchResults(start, docs.getNumFound(), query.getPageSize(), tweets, search);
		} catch (SolrServerException e) {
			LOGGER.error("Server exception caught for query {}: {}", sQuery.toString(), e.getMessage());
			throw new SearchEngineException(e);
		}

		return results;
	}

	private List<Tweet> extractTweets(SolrDocumentList docs, Map<String, Map<String, List<String>>> highlightMap, boolean highlighting) {
		List<Tweet> tweets;

		if (docs == null) {
			tweets = new ArrayList<Tweet>();
		} else {
			tweets = new ArrayList<Tweet>(docs.size());
			for (SolrDocument doc : docs) {
				Map<String, Object> fieldMap = doc.getFieldValueMap();

				Tweet tweet = new Tweet();
				String id = (String)fieldMap.get(ID_FIELD);
				tweet.setId(id);

				String text = (String)fieldMap.get(TEXT_FIELD);
				if (highlighting) {
					text = getHighlight(text, TEXT_FIELD, highlightMap.get(id));
				}

				tweet.setText(text);
				tweet.setCreated((Date)fieldMap.get(CREATED_FIELD));
				tweet.setCountry((String)fieldMap.get(COUNTRY_FIELD));
				tweet.setPlaceName((String)fieldMap.get(PLACE_NAME));
				tweet.setUserScreenName((String)fieldMap.get(USER_SCREEN_NAME));
				tweet.setUserName((String)fieldMap.get(USER_FULL_NAME));
				tweet.setParty((String)fieldMap.get(PARTY_FIELD));
				if (fieldMap.containsKey(SENTIMENT_FIELD)) {
					tweet.setSentiment((Integer)fieldMap.get(SENTIMENT_FIELD));
				} else {
					// Default sentiment value to neutral
					tweet.setSentiment(Sentiment.SENTIMENT_NEUTRAL);
				}
				if (fieldMap.containsKey(RETWEET_COUNT_FIELD)) {
					tweet.setRetweetCount((Integer)fieldMap.get(RETWEET_COUNT_FIELD));
				}
				if (fieldMap.containsKey(FAVOURITE_COUNT_FIELD)) {
					tweet.setFavouriteCount((Integer)fieldMap.get(FAVOURITE_COUNT_FIELD));
				}
				tweets.add(tweet);
			}
		}

		return tweets;
	}

	private String getHighlight(String base, String fieldName, Map<String, List<String>> itemHighlights) {
		String ret = base;

		if (itemHighlights != null && itemHighlights.containsKey(fieldName)) {
			ret = itemHighlights.get(fieldName).get(0);
		}

		return ret;
	}

	private Map<String, FacetList> extractAppliedFilters(Query query) {
		Map<String, FacetList> applied = new HashMap<String, FacetList>();
		Map<String, List<String>> filters = new HashMap<String, List<String>>();

		if (query.getFilters() != null) {
			for (String fq : query.getFilters()) {
				String[] fqParts = fq.split(":");
				if (!filters.containsKey(fqParts[0])) {
					filters.put(fqParts[0], new ArrayList<String>());
				}
				String value = fqParts[1];
				if (value.startsWith("\"")) {
					// Need to strip quotes from around the value
					value = fqParts[1].substring(1, fqParts[1].length() - 1);
				}
				filters.get(fqParts[0]).add(value);
			}

			for (String field : filters.keySet()) {
				List<String> fList = filters.get(field);
				List<Facet> facets = new ArrayList<Facet>(fList.size());
				for (String value : fList) {
					String label = value;
					if (value.startsWith("[")) {
						// This is a facetquery - need to get display label
						label = config.getFacetQueryFields().get(field).get(value);
					}
					facets.add(new FacetQuery(field, value, 0, label));
				}

				applied.put(field, new FacetList(field, getFacetLabel(field), facets));
			}
		}

		return applied;
	}

	private Map<String, FacetList> extractAvailableFilters(QueryResponse response) {
		Map<String, FacetList> facets = new HashMap<String, FacetList>();

		if (response.getFacetFields() != null) {
			for (FacetField ff : response.getFacetFields()) {
				String field = ff.getName();
				List<Facet> facetList = new ArrayList<Facet>(ff.getValueCount());

				for (Count c : ff.getValues()) {
					if (c.getCount() > 0) {
						Facet f = new Facet(ff.getName(), c.getName(), c.getCount());
						facetList.add(f);
					}
				}

				facets.put(ff.getName(), new FacetList(field, getFacetLabel(field), facetList));
			}
		}

		return facets;
	}

	private String getFacetLabel(String field) {
		String ret = config.getFacetLabels().get(field);
		return ret == null ? field : ret;
	}

	/**
	 * Trim filters that have already been applied from the set of available
	 * filters. This is done in place on the passed availableFilters map.
	 * @param availableFilters the available filters.
	 * @param appliedFilters the filters already applied.
	 */
	private void trimAvailableFilters(Map<String, FacetList> availableFilters, Map<String, FacetList> appliedFilters) {
		// Loop through the fields in the available filters map
		for (Iterator<String> fieldIter = availableFilters.keySet().iterator(); fieldIter.hasNext(); ) {
			String field = fieldIter.next();
			if (appliedFilters.containsKey(field)) {
				FacetList facetList = availableFilters.get(field);
				List<Facet> values = facetList.getFacets();
				FacetList applied = appliedFilters.get(field);
				// Loop through each value, checking against the list of currently applied filters
				for (Iterator<Facet> valueIter = values.iterator(); valueIter.hasNext(); ) {
					Facet facet = valueIter.next();
					if (applied.containsValue(facet.getValue())) {
						// Filter has been applied - remove from the list
						valueIter.remove();
					}
				}
			}
		}
	}

	private Map<String, FacetList> extractFacetQueries(QueryResponse response) {
		Map<String, FacetList> fQuery = new HashMap<String, FacetList>();

		Map<String, Integer> facetQuery = response.getFacetQuery();
		if (facetQuery != null) {
			Map<String, List<Facet>> facetMap = new HashMap<String, List<Facet>>();

			for (String query : facetQuery.keySet()) {
				// Split into field, query
				String[] fqParts = query.split(":");

				if (!facetMap.containsKey(fqParts[0])) {
					facetMap.put(fqParts[0], new ArrayList<Facet>());
				}

				Map<String, String> facetLabels = config.getFacetQueryFields().get(fqParts[0]);
				String label = facetLabels.get(fqParts[1]);
				if (label != null) {
					Facet fq = new FacetQuery(fqParts[0], fqParts[1], facetQuery.get(query), label);
					facetMap.get(fqParts[0]).add(fq);
				}
			}

			for (String field : facetMap.keySet()) {
				FacetList fl = new FacetList(field, getFacetLabel(field), facetMap.get(field));
				fQuery.put(field, fl);
			}
		}

		return fQuery;
	}

	@Override
	public SearchResults getTextBatch(int batchNum) throws SearchEngineException {
		SearchResults results;
		TermsConfiguration termsConfig = config.getTermsConfiguration();

		SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler(termsConfig.getHandler());
		query.setFields(termsConfig.getField());
		query.setRows(termsConfig.getBatchSize());
		query.setStart(batchNum * termsConfig.getBatchSize());
		query.addFilterQuery(termsConfig.getFilters().toArray(new String[0]));

		try {
			QueryResponse response = server.query(query);
			SolrDocumentList docs = response.getResults();

			// Attempt to ensure we always return at least one batch of results
			if (docs.getNumFound() == 0) {
				LOGGER.debug("No tweets found in text batch - running again without filters");
				docs = runQueryWithoutFilters(query, termsConfig.getFilters());
			}

			List<Tweet> tweets = new ArrayList<Tweet>(docs.size());
			for (SolrDocument doc : docs) {
				Tweet tweet = new Tweet();
				tweet.setText((String) doc.getFieldValue(termsConfig.getField()));
				tweets.add(tweet);
			}

			results = new SearchResults(query.getStart(), docs.getNumFound(), query.getRows(), tweets, null);
		} catch (SolrServerException e) {
			LOGGER.error("Server exception caught getting text batch {}: {}", batchNum, e.getMessage());
			throw new SearchEngineException(e);
		}

		return results;
	}

	/**
	 * Run the given query, removing the given list of filters from the query itself.
	 * @param query the query to be run.
	 * @param filters the list of filters to remove.
	 * @return a list of documents returned from the query.
	 * @throws SolrServerException if a problem occurs accessing Solr.
	 */
	private SolrDocumentList runQueryWithoutFilters(SolrQuery query, List<String> filters) throws SolrServerException {
		for (String fq : filters) {
			// Re-query without the filter queries set
			query.removeFilterQuery(fq);
		}
		QueryResponse response = server.query(query);
		SolrDocumentList docs = response.getResults();

		// Set numFound to the results list size, avoid reading every tweet in
		// the search engine
		if (docs.getNumFound() > 0) {
			docs.setNumFound(docs.size());
		}

		return docs;
	}

	@Override
	public void indexTweets(List<Tweet> tweets) throws SearchEngineException {
		try {
			server.addBeans(tweets);
			server.commit();
		} catch (SolrServerException e) {
			throw new SearchEngineException(e);
		} catch (IOException e) {
			throw new SearchEngineException(e);
		}
	}

	@Override
	public void deleteTweets(List<Long> deleteIds) throws SearchEngineException {
		// Convert longs to strings
		List<String> ids = deleteIds.stream().map(id -> id.toString()).collect(Collectors.toList());

		try {
			server.deleteById(ids);
			server.commit();
		} catch (SolrServerException e) {
			throw new SearchEngineException(e);
		} catch (IOException e) {
			throw new SearchEngineException(e);
		}
	}

}
