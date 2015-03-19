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
package uk.co.flax.ukmp;

import io.dropwizard.Configuration;
import uk.co.flax.ukmp.config.SolrConfiguration;
import uk.co.flax.ukmp.config.StanfordConfiguration;
import uk.co.flax.ukmp.config.TwitterConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration details for the UKMP application/indexer.
 */
public class UKMPConfiguration extends Configuration {

	@JsonProperty("solr")
	private SolrConfiguration solrConfiguration;
	@JsonProperty("stanford")
	private StanfordConfiguration stanfordConfiguration;
	@JsonProperty("twitter")
	private TwitterConfiguration twitterConfiguration;

	/**
	 * @return the Solr configuration details.
	 */
	public SolrConfiguration getSolrConfiguration() {
		return solrConfiguration;
	}

	/**
	 * @return the Stanford configuration details.
	 */
	public StanfordConfiguration getStanfordConfiguration() {
		return stanfordConfiguration;
	}

	/**
	 * @return the Twitter configuration details.
	 */
	public TwitterConfiguration getTwitterConfiguration() {
		return twitterConfiguration;
	}

}
