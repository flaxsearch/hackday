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

/**
 * POJO wrapping around a list of facets available for a single field.
 */
public class FacetList {

	private final String field;
	private final String label;
	private final List<Facet> facets;

	public FacetList(String field, String label, List<Facet> facets) {
		this.field = field;
		this.label = label;
		this.facets = facets;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the facets
	 */
	public List<Facet> getFacets() {
		return facets;
	}

}
