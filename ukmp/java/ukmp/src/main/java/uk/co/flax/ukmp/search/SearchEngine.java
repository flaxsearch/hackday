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

	/**
	 * Test the readiness of the search engine.
	 * @return <code>true</code> if the search engine is available,
	 * <code>false</code> if not.
	 * @throws SearchEngineException if a problem occurs while testing the
	 * search engine. This does not include the search engine being off-line.
	 */
	public boolean isServerReady() throws SearchEngineException;

}
