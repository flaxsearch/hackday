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

import uk.co.flax.ukmp.config.EntityConfiguration;
import uk.co.flax.ukmp.config.SolrConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

/**
 * Configuration details for the UKMP application/indexer.
 */
public class UKMPConfiguration extends Configuration {

	@JsonProperty("solr")
	private SolrConfiguration solrConfiguration;
	@JsonProperty("entity")
	private EntityConfiguration entityConfiguration;

	public SolrConfiguration getSolrConfiguration() {
		return solrConfiguration;
	}

	/**
	 * @return the entityConfiguration
	 */
	public EntityConfiguration getEntityConfiguration() {
		return entityConfiguration;
	}

}
