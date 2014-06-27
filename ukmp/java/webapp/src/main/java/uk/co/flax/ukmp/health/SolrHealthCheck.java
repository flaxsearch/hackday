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
package uk.co.flax.ukmp.health;

import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

import com.yammer.metrics.core.HealthCheck;

/**
 * Health check to see if Solr server is up and running.
 */
public class SolrHealthCheck extends HealthCheck {

	private final SearchEngine searchEngine;

	/**
	 * Default constructor.
	 */
	public SolrHealthCheck(SearchEngine engine) {
		super("solr");
		this.searchEngine = engine;
	}

	@Override
	protected Result check() {
		try {
			if (searchEngine.isServerReady()) {
				return Result.healthy();
			} else {
				return Result.unhealthy("Server is not available - check log for details");
			}
		} catch (SearchEngineException e) {
			return Result.unhealthy(e.getMessage());
		}
	}

}
