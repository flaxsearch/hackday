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
 * Class representing a Twitter user.
 */
public class User {

	private long id;
	@JsonProperty("screen_name")
	private String screenName;
	private String name;
	private String description;
	@JsonProperty("profile_image_url")
	private String profileImageUrl;

	private boolean verified;
	private String lang;
	private String url;
	private String location;
	@JsonProperty("created_at")
	private Date createdAt;
	@JsonProperty("time_zone")
	private String timeZone;
	@JsonProperty("protected")
	private boolean userProtected;

	@JsonProperty("followers_count")
	private long followersCount;
	@JsonProperty("listed_count")
	private long listedCount;
	@JsonProperty("statuses_count")
	private long statusesCount;
	@JsonProperty("friends_count")
	private long friendsCount;
	@JsonProperty("favourites_count")
	private long favouritesCount;

	@JsonProperty("sidebar_fill_color")
	private String sidebarFillColour;
	@JsonProperty("profile_text_color")
	private String profileTextColour;
	@JsonProperty("profile_background_color")
	private String profileBackgroundColour;
	@JsonProperty("profile_link_color")
	private String profileLinkColour;
	@JsonProperty("profile_background_tile")
	private boolean profileBackgroundTile;
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the screenName
	 */
	public String getScreenName() {
		return screenName;
	}
	/**
	 * @param screenName the screenName to set
	 */
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the profileImageUrl
	 */
	public String getProfileImageUrl() {
		return profileImageUrl;
	}
	/**
	 * @param profileImageUrl the profileImageUrl to set
	 */
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}
	/**
	 * @return the verified
	 */
	public boolean isVerified() {
		return verified;
	}
	/**
	 * @param verified the verified to set
	 */
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}
	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}
	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}
	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	/**
	 * @return the userProtected
	 */
	public boolean isUserProtected() {
		return userProtected;
	}
	/**
	 * @param userProtected the userProtected to set
	 */
	public void setUserProtected(boolean userProtected) {
		this.userProtected = userProtected;
	}
	/**
	 * @return the followersCount
	 */
	public long getFollowersCount() {
		return followersCount;
	}
	/**
	 * @param followersCount the followersCount to set
	 */
	public void setFollowersCount(long followersCount) {
		this.followersCount = followersCount;
	}
	/**
	 * @return the listedCount
	 */
	public long getListedCount() {
		return listedCount;
	}
	/**
	 * @param listedCount the listedCount to set
	 */
	public void setListedCount(long listedCount) {
		this.listedCount = listedCount;
	}
	/**
	 * @return the statusesCount
	 */
	public long getStatusesCount() {
		return statusesCount;
	}
	/**
	 * @param statusesCount the statusesCount to set
	 */
	public void setStatusesCount(long statusesCount) {
		this.statusesCount = statusesCount;
	}
	/**
	 * @return the friendsCount
	 */
	public long getFriendsCount() {
		return friendsCount;
	}
	/**
	 * @param friendsCount the friendsCount to set
	 */
	public void setFriendsCount(long friendsCount) {
		this.friendsCount = friendsCount;
	}
	/**
	 * @return the favouritesCount
	 */
	public long getFavouritesCount() {
		return favouritesCount;
	}
	/**
	 * @param favouritesCount the favouritesCount to set
	 */
	public void setFavouritesCount(long favouritesCount) {
		this.favouritesCount = favouritesCount;
	}
	/**
	 * @return the sidebarFillColour
	 */
	public String getSidebarFillColour() {
		return sidebarFillColour;
	}
	/**
	 * @param sidebarFillColour the sidebarFillColour to set
	 */
	public void setSidebarFillColour(String sidebarFillColour) {
		this.sidebarFillColour = sidebarFillColour;
	}
	/**
	 * @return the profileTextColour
	 */
	public String getProfileTextColour() {
		return profileTextColour;
	}
	/**
	 * @param profileTextColour the profileTextColour to set
	 */
	public void setProfileTextColour(String profileTextColour) {
		this.profileTextColour = profileTextColour;
	}
	/**
	 * @return the profileBackgroundColour
	 */
	public String getProfileBackgroundColour() {
		return profileBackgroundColour;
	}
	/**
	 * @param profileBackgroundColour the profileBackgroundColour to set
	 */
	public void setProfileBackgroundColour(String profileBackgroundColour) {
		this.profileBackgroundColour = profileBackgroundColour;
	}
	/**
	 * @return the profileLinkColour
	 */
	public String getProfileLinkColour() {
		return profileLinkColour;
	}
	/**
	 * @param profileLinkColour the profileLinkColour to set
	 */
	public void setProfileLinkColour(String profileLinkColour) {
		this.profileLinkColour = profileLinkColour;
	}
	/**
	 * @return the profileBackgroundTile
	 */
	public boolean isProfileBackgroundTile() {
		return profileBackgroundTile;
	}
	/**
	 * @param profileBackgroundTile the profileBackgroundTile to set
	 */
	public void setProfileBackgroundTile(boolean profileBackgroundTile) {
		this.profileBackgroundTile = profileBackgroundTile;
	}

}
