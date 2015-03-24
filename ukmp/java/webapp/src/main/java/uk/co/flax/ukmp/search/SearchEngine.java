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
package uk.co.flax.ukmp.search;

import java.util.List;

import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.config.TermsConfiguration;


/**
 * Interface defining search engine functionality.
 */
public interface SearchEngine {

	/** Enumeration to indicate return states from store/update ops */
	public enum OperationStatus {
		SUCCESS,
		FAILURE,
		NO_OPERATION
	}

	public static final String ID_FIELD = "id";

	/**
	 * Test the readiness of the search engine.
	 * @return <code>true</code> if the search engine is available,
	 * <code>false</code> if not.
	 * @throws SearchEngineException if a problem occurs while testing the
	 * search engine. This does not include the search engine being off-line.
	 */
	public boolean isServerReady() throws SearchEngineException;

	/**
	 * Carry out a search and return the results.
	 * @param query the query details.
	 * @return the results of the search.
	 * @throws SearchEngineException if there are problems executing the
	 * search.
	 */
	public SearchResults search(Query query) throws SearchEngineException;

	/**
	 * Get a batch of tweets from the search engine, using a minimal handler
	 * that only returns the tweet text. This is expected to be used to generate
	 * the list of terms for the word cloud.
	 *
	 * <p>Most search parameters will be filled in from the {@link TermsConfiguration},
	 * with only the batch number required to set the search start point.</p>
	 * @param batchNum the number of the batch to look up, starting from 0.
	 * @return a list of search results, comprising tweets with text only.
	 * @throws SearchEngineException if there are problems executing the search.
	 */
	public SearchResults getTextBatch(int batchNum) throws SearchEngineException;

    public void indexTweets(List<Tweet> tweets) throws SearchEngineException;

    public void deleteTweets(List<String> deleteIds) throws SearchEngineException;

}
