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

/**
 * @author Matt Pearce
 */
public class PartyConfiguration {

	private String displayName;
	private String twitterScreenName;
	private String twitterListSlug;

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the twitterOwnerId
	 */
	public String getTwitterScreenName() {
		return twitterScreenName;
	}

	/**
	 * @param twitterOwnerId the twitterOwnerId to set
	 */
	public void setTwitterScreenName(String twitterOwnerId) {
		this.twitterScreenName = twitterOwnerId;
	}

	/**
	 * @return the twitterSlug
	 */
	public String getTwitterListSlug() {
		return twitterListSlug;
	}

	/**
	 * @param twitterSlug the twitterSlug to set
	 */
	public void setTwitterListSlug(String twitterSlug) {
		this.twitterListSlug = twitterSlug;
	}

}
