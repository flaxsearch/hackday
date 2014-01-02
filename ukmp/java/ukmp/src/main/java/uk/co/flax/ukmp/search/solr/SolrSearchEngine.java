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
import java.util.List;
import java.util.Map;

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
import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.config.SolrConfiguration;
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

	/**
	 * Static instance of the Solr server - should be only one of these per
	 * running application.
	 */
	private static SolrServer server;
	/** The default request handler for queries. */
	private String queryHandler;

	public SolrSearchEngine(SolrConfiguration config) {
		initialiseServer(config);
		this.queryHandler = config.getQueryHandler();
	}

	/**
	 * Unit testing constructor.
	 * @param server mock server instance.
	 */
	SolrSearchEngine(SolrServer server, String queryHandler) {
		SolrSearchEngine.server = server;
		this.queryHandler = queryHandler;
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
		sQuery.setRequestHandler(queryHandler);

		try {
			QueryResponse response = server.query(sQuery);
			SolrDocumentList docs = response.getResults();
			results = new SearchResults(start, docs.getNumFound(), query.getPageSize(), extractTweets(docs),
					query.getQuery(), extractAppliedFilters(query), query.getSortField(), query.isSortAscending(),
					extractAvailableFilters(response));
		} catch (SolrServerException e) {
			LOGGER.error("Server exception caught for query {}: {}", sQuery.toString(), e.getMessage());
			throw new SearchEngineException(e);
		}

		return results;
	}

	private List<Tweet> extractTweets(SolrDocumentList docs) {
		List<Tweet> tweets;

		if (docs == null) {
			tweets = new ArrayList<Tweet>();
		} else {
			tweets = new ArrayList<Tweet>(docs.size());
			for (SolrDocument doc : docs) {
				Tweet tweet = new Tweet();
				tweet.setId((String)doc.getFieldValue(ID_FIELD));
				tweet.setText((String)doc.getFieldValue(TEXT_FIELD));
				tweet.setCreated((Date)doc.getFieldValue(CREATED_FIELD));
				tweet.setCountry((String)doc.getFieldValue(COUNTRY_FIELD));
				tweet.setPlaceName((String)doc.getFieldValue(PLACE_NAME));
				tweet.setUserScreenName((String)doc.getFieldValue(USER_SCREEN_NAME));
				tweet.setUserName((String)doc.getFieldValue(USER_FULL_NAME));
				tweet.setParty((String)doc.getFieldValue(PARTY_FIELD));
				tweets.add(tweet);
			}
		}

		return tweets;
	}

	private Map<String, List<String>> extractAppliedFilters(Query query) {
		Map<String, List<String>> filters = new HashMap<String, List<String>>();

		if (query.getFilters() != null) {
			for (String fq : query.getFilters()) {
				String[] fqParts = fq.split(":");
				if (!filters.containsKey(fqParts[0])) {
					filters.put(fqParts[0], new ArrayList<String>());
				}
				filters.get(fqParts[0]).add(fqParts[1]);
			}
		}

		return filters;
	}

	private Map<String, List<Facet>> extractAvailableFilters(QueryResponse response) {
		Map<String, List<Facet>> facets = new HashMap<String, List<Facet>>();

		if (response.getFacetFields() != null) {
			for (FacetField ff : response.getFacetFields()) {
				facets.put(ff.getName(), new ArrayList<Facet>(ff.getValueCount()));
				for (Count c : ff.getValues()) {
					if (c.getCount() > 0) {
						Facet f = new Facet(ff.getName(), c.getName(), c.getCount());
						facets.get(ff.getName()).add(f);
					}
				}
			}
		}

		return facets;
	}

}
