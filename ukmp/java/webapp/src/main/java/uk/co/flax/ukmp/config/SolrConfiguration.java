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
package uk.co.flax.ukmp.config;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Solr configuration details.
 */
public class SolrConfiguration {

	@Valid @NotNull
	private String baseUrl;

	@Valid @NotNull
	private String queryHandler;

	private Map<String, String> facetLabels;

	private Map<String, Map<String, String>> facetQueryFields;

	@JsonProperty("terms")
	private TermsConfiguration termsConfiguration;

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @return the default query handler to use.
	 */
	public String getQueryHandler() {
		return queryHandler;
	}

	public Map<String, String> getFacetLabels() {
		return facetLabels;
	}

	public Map<String, Map<String, String>> getFacetQueryFields() {
		return facetQueryFields;
	}

	public TermsConfiguration getTermsConfiguration() {
		return termsConfiguration;
	}

}
