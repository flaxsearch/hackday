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
package uk.co.flax.ukmp.api;

import java.util.List;
import java.util.Map;

/**
 * Set of results returned from a search.
 */
public class SearchResults {

	private final int start;
	private final long numResults;
	private final int pageSize;
	private final String query;
	private final String sortField;
	private final boolean sortAscending;

	/** The facets that can be applied to this results list */
	private final Map<String, List<Facet>> facets;

	/** The filters already applied */
	private final Map<String, List<String>> appliedFilters;

	private final List<Tweet> tweets;
	private final String errorMessage;

	public SearchResults(int start, long numResults, int pageSize, List<Tweet> results, String query,
			Map<String, List<String>> filters, String sortOrder, boolean sortAsc, Map<String, List<Facet>> facets) {
		this(start, numResults, pageSize, results, query, filters, sortOrder, sortAsc, facets, null);
	}

	public SearchResults(String error) {
		this(0, -1, 0, null, null, null, null, false, null, error);
	}

	public SearchResults(int start, long numResults, int pageSize, List<Tweet> results, String query,
			Map<String, List<String>> filters, String sortOrder, boolean sortAsc, Map<String, List<Facet>> facets, String error) {
		this.start = start;
		this.numResults = numResults;
		this.pageSize = pageSize;
		this.tweets = results;
		this.query = query;
		this.appliedFilters = filters;
		this.sortField = sortOrder;
		this.sortAscending = sortAsc;
		this.facets = facets;
		this.errorMessage = error;
	}

	/**
	 * @return the index of the first result in the set.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return the total number of results for the search.
	 */
	public long getNumResults() {
		return numResults;
	}

	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @return the query string for the search.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the filters applied to the search results.
	 */
	public Map<String, List<String>> getAppliedFilters() {
		return appliedFilters;
	}

	/**
	 * @return the sort order of the search results.
	 */
	public String getSortField() {
		return sortField;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	/**
	 * @return the tweets returned by the search.
	 */
	public List<Tweet> getTweets() {
		return tweets;
	}

	/**
	 * @return the facets that may be applied to this search.
	 */
	public Map<String, List<Facet>> getFacets() {
		return facets;
	}

	/**
	 * @return any error message that was returned by the search.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
