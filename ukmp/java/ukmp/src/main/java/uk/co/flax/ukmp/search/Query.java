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
package uk.co.flax.ukmp.search;

import java.util.List;

/**
 * Class representing a query to be handled by the search engine.
 */
public class Query {

	/** Default search string - for browsing */
	public static final String DEFAULT_SEARCH = "*";
	/** Default sort order */
	public static final String DEFAULT_SORT_FIELD = "created_at";
	/** Default page size */
	public static final int DEFAULT_PAGE_SIZE = 10;

	private final String query;
	private final List<String> filters;
	private final String sortField;
	private final boolean sortAscending;
	private final int pageSize;
	private final int pageNumber;
	private final boolean highlightingEnabled;

	public Query(String query) {
		this(query, null, DEFAULT_SORT_FIELD, false, DEFAULT_PAGE_SIZE, 0, true);
	}

	public Query(String query, List<String> filters, String sort, boolean asc, int pgSize, int pgNum, boolean hl) {
		this.query = query;
		this.filters = filters;
		this.sortField = sort;
		this.sortAscending = asc;
		this.pageSize = pgSize;
		this.pageNumber = pgNum;
		this.highlightingEnabled = hl;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the filters
	 */
	public List<String> getFilters() {
		return filters;
	}

	/**
	 * @return the sortOrder
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * @return <code>true</code> if the results should be returned in ascending
	 * order.
	 */
	public boolean isSortAscending() {
		return sortAscending;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * @return the highlightingEnabled
	 */
	public boolean isHighlightingEnabled() {
		return highlightingEnabled;
	}

}
