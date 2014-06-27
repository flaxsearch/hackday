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
package uk.co.flax.ukmp.twitter;

import java.awt.print.Pageable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import uk.co.flax.ukmp.config.PartyConfiguration;

/**
 * @author Matt Pearce
 */
public class TwitterPartyListHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterPartyListHandler.class);

	private final Configuration twitterConfig;
	private final Map<String, PartyConfiguration> parties;

	private boolean hasChanged;


	private Map<String, Set<Long>> partyMemberIds = new HashMap<>();

	public TwitterPartyListHandler(Configuration twitterConfig, Map<String, PartyConfiguration> parties) {
		this.twitterConfig = twitterConfig;
		this.parties = parties;
	}


	public void refreshLists() {
		Map<String, Set<Long>> memberIds = new HashMap<>();

		Authorization auth = AuthorizationFactory.getInstance(twitterConfig);
		TwitterFactory tf = new TwitterFactory(twitterConfig);
		Twitter twitter = tf.getInstance(auth);
		for (PartyConfiguration pc : parties.values()) {
			Set<Long> ids = readPartyIds(twitter, pc.getTwitterScreenName(), pc.getTwitterListSlug(), pc.getDisplayName());
			if (!ids.isEmpty()) {
				memberIds.put(pc.getDisplayName(), ids);
			}
		}

		synchronized(partyMemberIds) {
			for (String party : memberIds.keySet()) {
				if (!partyMemberIds.containsKey(party) || Sets.symmetricDifference(memberIds.get(party), partyMemberIds.get(party)).size() > 0) {
					hasChanged = true;
					partyMemberIds.put(party, memberIds.get(party));
					LOGGER.debug("Updated list for {} with {} ids", party, memberIds.get(party).size());
				}
			}
		}
	}

	private Set<Long> readPartyIds(Twitter twitter, String screenName, String slug, String party) {
		Set<Long> ids = new HashSet<>();

		try {
			long cursor = -1;
			PagableResponseList<User> response = null;
			do {
				response = twitter.getUserListMembers(screenName, slug, cursor);
				for (User user : response) {
					LOGGER.debug("Read id for user @{}", user.getScreenName());
					ids.add(user.getId());
				}
				cursor = response.getNextCursor();
			} while (response != null && response.hasNext());
		} catch (TwitterException e) {
			LOGGER.error("Twitter exception updating {} party list : {}", party, e.getMessage());
		}

		return ids;
	}


	public boolean listsChanged() {
		return hasChanged;
	}

}
