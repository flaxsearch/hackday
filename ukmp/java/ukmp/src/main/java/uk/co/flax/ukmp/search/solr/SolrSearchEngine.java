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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.ukmp.config.SolrConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

/**
 * Solr implementation of the search engine interface.
 */
public class SolrSearchEngine implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSearchEngine.class);

	/**
	 * Static instance of the Solr server - should be only one of these per
	 * running application.
	 */
	private static SolrServer server;

	public SolrSearchEngine(SolrConfiguration config) {
		initialiseServer(config);
	}

	/**
	 * Unit testing constructor.
	 * @param server mock server instance.
	 */
	SolrSearchEngine(SolrServer server) {
		SolrSearchEngine.server = server;
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

}
