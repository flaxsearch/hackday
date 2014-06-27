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
package uk.co.flax.ukmp.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.search.Query;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

/**
 * Resource handler for browsing tweets.
 */
@Path("/browse")
public class BrowseResource {

	private final SearchEngine searchEngine;

	public BrowseResource(SearchEngine engine) {
		this.searchEngine = engine;
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SearchResults handleGet(@QueryParam("p") int page, @QueryParam("q") String queryString,
			@QueryParam("fq") List<String> filters, @QueryParam("sortby") String sortField,
			@QueryParam("sortasc") boolean sortAsc, @QueryParam("hl") String hl) {
		SearchResults results;

		try {
			String qString = queryString;
			if (StringUtils.isBlank(qString)) {
				qString = Query.DEFAULT_SEARCH;
			}
			if (StringUtils.isBlank(sortField)) {
				sortField = Query.DEFAULT_SORT_FIELD;
				sortAsc = false;
			}

			// Set highlight value - default should be true
			boolean highlight = true;
			if (StringUtils.isNotBlank(hl)) {
				highlight = Boolean.parseBoolean(hl);
			}

			Query query = new Query(qString, filters, sortField, sortAsc, Query.DEFAULT_PAGE_SIZE, page, highlight);
			results = searchEngine.search(query);
		} catch (SearchEngineException e) {
			results = new SearchResults(e.getMessage());
		}

		return results;
	}

}
