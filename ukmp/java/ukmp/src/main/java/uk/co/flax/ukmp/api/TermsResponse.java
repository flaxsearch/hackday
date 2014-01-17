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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from a call to the terms component resource handler.
 */
public class TermsResponse {

	@JsonProperty("terms")
	private final List<Term> terms;

	private final String error;

	public TermsResponse(List<Term> terms, String error) {
		this.terms = terms;
		this.error = error;
	}

	public TermsResponse(List<Term> terms) {
		this(terms, null);
	}

	public TermsResponse(String error) {
		this(null, error);
	}

	/**
	 * @return the terms
	 */
	public List<Term> getTerms() {
		return terms;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

}
