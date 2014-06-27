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

/**
 * POJO representing a term, as returned from the terms component search.
 */
public class Term implements Comparable<Term> {

	private final String term;
	private final long count;

	public Term(String term, long count) {
		this.term = term;
		this.count = count;
	}

	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	@Override
	public int compareTo(Term t) {
		int ret = Long.compare(count, t.count);
		if (ret == 0) {
			ret = term.compareTo(t.term);
		}

		return ret;
	}

	@Override
	public String toString() {
		return term + " [" + count + "]";
	}

}
