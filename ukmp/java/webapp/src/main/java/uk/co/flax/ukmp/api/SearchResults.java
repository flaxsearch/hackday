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

/**
 * Set of results returned from a search.
 */
public class SearchResults {

	private final int start;
	private final long numResults;
	private final int pageSize;

	private final SearchState searchState;

	private final List<Tweet> tweets;
	private final String errorMessage;

	public SearchResults(int start, long numResults, int pageSize, List<Tweet> results, SearchState search) {
		this(start, numResults, pageSize, results, search, null);
	}

	public SearchResults(String error) {
		this(0, -1, 0, null, null, error);
	}

	public SearchResults(int start, long numResults, int pageSize, List<Tweet> results, SearchState search, String error) {
		this.start = start;
		this.numResults = numResults;
		this.pageSize = pageSize;
		this.tweets = results;
		this.searchState = search;
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

	/**
	 * @return the number of results per page.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @return the current search state.
	 */
	public SearchState getSearchState() {
		return searchState;
	}

	/**
	 * @return the tweets returned by the search.
	 */
	public List<Tweet> getTweets() {
		return tweets;
	}

	/**
	 * @return any error message that was returned by the search.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
