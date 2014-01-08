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
package uk.co.flax.ukmp.api;

import java.util.List;
import java.util.Map;

/**
 * POJO representing the current search state.
 */
public class SearchState {

	private final String query;
	private final String sortField;
	private final boolean sortAscending;

	private final int pageNumber;

	/** The facets that can be applied to this results list */
	private final Map<String, FacetList> facets;

	/** The facet queries that can be applied to this results list */
	private final List<FacetQuery> facetQueries;

	/** The filters already applied */
	private final Map<String, List<String>> appliedFilters;

	public SearchState(String q, String sort, boolean sortAsc, int page, Map<String, FacetList> facets,
			List<FacetQuery> facetQueries, Map<String, List<String>> appliedFilters) {
		this.query = q;
		this.sortField = sort;
		this.sortAscending = sortAsc;
		this.pageNumber = page;
		this.facets = facets;
		this.facetQueries = facetQueries;
		this.appliedFilters = appliedFilters;
	}

	/**
	 * @return the query searched.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the field results are sorted on.
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * @return <code>true</code> if the results are sorted in ascending order.
	 */
	public boolean isSortAscending() {
		return sortAscending;
	}

	/**
	 * @return the pageNumber of the results.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * @return the facets available to be applied to the results.
	 */
	public Map<String, FacetList> getFacets() {
		return facets;
	}

	/**
	 * @return the facetQueries
	 */
	public List<FacetQuery> getFacetQueries() {
		return facetQueries;
	}

	/**
	 * @return the filters applied to the results.
	 */
	public Map<String, List<String>> getAppliedFilters() {
		return appliedFilters;
	}

}
