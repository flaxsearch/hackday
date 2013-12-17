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


/**
 * Class representing an entity extracted from an incoming document.
 */
public class Entity {

	/** Suffix appended to types - taken from Stanford NER */
	static final String TYPE_SUFFIX = "_ner";

	/** The type of the entity */
	private final String type;
	/** The entity's value */
	private final String value;

	/**
	 * Construct an entity with separate type and value fields.
	 * @param type the type of entity.
	 * @param value the entity's value.
	 */
	public Entity(String type, String value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Get the field name from the entity type for Solr.
	 * @return the entity type, followed by a suffix, or <code>null</code> if no type
	 * is set.
	 */
	public String getFieldName() {
		String ret = null;
		if (type != null) {
			ret = type.toLowerCase() + TYPE_SUFFIX;
		}
		return ret;
	}

	/**
	 * @return the entity type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the entity's value.
	 */
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Entity)) {
			return false;
		}
		Entity other = (Entity) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return type + "::" + value;
	}

}
