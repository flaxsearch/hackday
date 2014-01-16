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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing data returned from the Stanford processors.
 */
public class StanfordData {

	@JsonProperty("entities")
	private final Map<String, List<String>> entities;
	@JsonProperty("sentiment")
	private final int sentiment;

	public StanfordData(Map<String, List<String>> entities, int sentiment) {
		this.entities = entities;
		this.sentiment = sentiment;
	}

	/**
	 * @return the entities
	 */
	public Map<String, List<String>> getEntities() {
		return entities;
	}

	/**
	 * @return the sentiment
	 */
	public int getSentiment() {
		return sentiment;
	}

}
