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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a single tweet.
 */
public class Tweet {

	private String id;
	private String text;
	private String party;
	private Date created;
	private String country;
	private String placeName;
	private String userScreenName;
	private String userName;
	@JsonProperty("reply")
	private String inReplyTo;
	private int sentiment;
	private int retweetCount;
	private int favouriteCount;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the party
	 */
	public String getParty() {
		return party;
	}

	/**
	 * @param party the party to set
	 */
	public void setParty(String party) {
		this.party = party;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return the inReplyTo
	 */
	public String getInReplyTo() {
		return inReplyTo;
	}

	/**
	 * @param inReplyTo the inReplyTo to set
	 */
	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the placeName
	 */
	public String getPlaceName() {
		return placeName;
	}

	/**
	 * @param placeName the placeName to set
	 */
	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	/**
	 * @return the userScreenName
	 */
	public String getUserScreenName() {
		return userScreenName;
	}

	/**
	 * @param userScreenName the userScreenName to set
	 */
	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the sentiment
	 */
	public int getSentiment() {
		return sentiment;
	}

	/**
	 * @param sentiment the sentiment to set
	 */
	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}

	/**
	 * @return the retweetCount
	 */
	public int getRetweetCount() {
		return retweetCount;
	}

	/**
	 * @param retweetCount the retweetCount to set
	 */
	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	/**
	 * @return the favouriteCount
	 */
	public int getFavouriteCount() {
		return favouriteCount;
	}

	/**
	 * @param favouriteCount the favouriteCount to set
	 */
	public void setFavouriteCount(int favouriteCount) {
		this.favouriteCount = favouriteCount;
	}

}
