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
package uk.co.flax.ukmp.config;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration for the TermsComponent.
 */
public class TermsConfiguration {

	@Valid @NotNull
	private String handler;
	@Valid @NotNull
	private String field;
	@Valid @NotNull
	private int limit;

	private String sortOrder;

	@Valid @NotNull
	private int batchSize;
	@Valid @NotNull
	private List<String> filters;
	@Valid @NotNull
	private String stopWordsFile;
	@Valid @NotNull
	private int refreshMinutes;


	/**
	 * @return the handler
	 */
	public String getHandler() {
		return handler;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * @return the batchSize
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * @return the filters
	 */
	public List<String> getFilters() {
		return filters;
	}

	/**
	 * @return the stopWordsFile
	 */
	public String getStopWordsFile() {
		return stopWordsFile;
	}

	/**
	 * @return the refreshMinutes
	 */
	public int getRefreshMinutes() {
		return refreshMinutes;
	}

}
