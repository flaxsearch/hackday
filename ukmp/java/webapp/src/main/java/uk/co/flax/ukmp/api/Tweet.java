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
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java bean representing a single tweet.
 */
public class Tweet {

	@Field("id")
	private String id;

	@Field("text")
	private String text;

	@Field("party")
	private String party;

	@Field("created_at")
	private Date created;

	@Field("place_country")
	private String country;

	@Field("place_full_name")
	private String placeName;

	@Field("user_screen_name")
	private String userScreenName;

	@Field("user_full_name")
	private String userName;

	@JsonProperty("reply")
	private String inReplyTo;

	@Field("sentiment")
	private int sentiment;

	@Field("retweet_count")
	private int retweetCount;

	@Field("favorite_count")
	private int favouriteCount;

	@Field("*_ner")
	private Map<String, Object> entities;

	@Field("ent_mentions_screen_name")
	private List<String> mentionScreenNames;

	@Field("ent_mentions_full_name")
	private List<String> mentionFullNames;

	@Field("ent_hashtags")
	private List<String> hashtags;

	@Field("ent_urls")
	private List<String> urls;

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

	/**
	 * @return the entities
	 */
	public Map<String, Object> getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(Map<String, Object> entities) {
		this.entities = entities;
	}

	/**
	 * @return the mentionScreenNames
	 */
	public List<String> getMentionScreenNames() {
		return mentionScreenNames;
	}

	/**
	 * @param mentionScreenNames the mentionScreenNames to set
	 */
	public void setMentionScreenNames(List<String> mentionScreenNames) {
		this.mentionScreenNames = mentionScreenNames;
	}

	/**
	 * @return the mentionFullNames
	 */
	public List<String> getMentionFullNames() {
		return mentionFullNames;
	}

	/**
	 * @param mentionFullNames the mentionFullNames to set
	 */
	public void setMentionFullNames(List<String> mentionFullNames) {
		this.mentionFullNames = mentionFullNames;
	}

	/**
	 * @return the hashtags
	 */
	public List<String> getHashtags() {
		return hashtags;
	}

	/**
	 * @param hashtags the hashtags to set
	 */
	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}

	/**
	 * @return the urls
	 */
	public List<String> getUrls() {
		return urls;
	}

	/**
	 * @param urls the urls to set
	 */
	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

}
