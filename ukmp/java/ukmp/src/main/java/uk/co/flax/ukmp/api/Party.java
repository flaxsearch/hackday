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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO holding party details.
 */
public class Party {

	@JsonProperty("twitter_screen_name")
	private String ownerScreenName;
	@JsonProperty("twitter_list_slug")
	private String listName;

	/**
	 * @return the ownerScreenName
	 */
	public String getOwnerScreenName() {
		return ownerScreenName;
	}

	/**
	 * @return the listName
	 */
	public String getListName() {
		return listName;
	}

}
