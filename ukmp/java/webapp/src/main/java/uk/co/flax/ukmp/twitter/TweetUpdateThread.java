/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.UserMentionEntity;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;
import uk.co.flax.ukmp.services.EntityExtractionService;

public class TweetUpdateThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(TweetUpdateThread.class);

	private static final long QUEUE_CHECK_TIME = 500;

	private final SearchEngine searchEngine;
	private final Queue<Status> statusQueue;
	private final int batchSize;
	private final EntityExtractionService entityExtractionService;

	private boolean running;

	public TweetUpdateThread(SearchEngine searchEngine, Queue<Status> statusQueue, int batchSize, EntityExtractionService ees) {
		this.searchEngine = searchEngine;
		this.statusQueue = statusQueue;
		this.batchSize = batchSize;
		this.entityExtractionService = ees;
	}

	@Override
	public void run() {
		LOGGER.info("Starting twitter update thread.");
		running = true;

		while (running) {
			List<Status> updates = new ArrayList<>(batchSize);
			while (!statusQueue.isEmpty() && updates.size() < batchSize) {
				updates.add(statusQueue.remove());
			}

			// Index the statuses
			storeUpdates(updates);

			// Sleep...
			try {
				Thread.sleep(QUEUE_CHECK_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("Update thread sleep interrupted: {}", e.getMessage());
			}
		}
	}

	public void closeDown() {
		// Set running to false
		running = false;

		LOGGER.debug("Shutting down twitter update thread");

		if (!statusQueue.isEmpty()) {
			// Try to clear the queue
			LOGGER.debug("Trying to clear {} items from update queue", statusQueue.size());

			List<Status> updates = new ArrayList<>(statusQueue);
			storeUpdates(updates);
		}

		LOGGER.info("Twitter update thread shut down.");
	}

	private void storeUpdates(List<Status> updates) {
		if (!updates.isEmpty()) {
			try {
				List<Tweet> tweets = new ArrayList<>(updates.size());
				updates.forEach(update -> tweets.add(buildTweetFromStatus(update)));

				searchEngine.indexTweets(tweets);
				LOGGER.debug("Indexed {} tweets", updates.size());
			} catch (SearchEngineException e) {
				LOGGER.error("Could not index {} tweets: {}", updates.size(), e.getMessage());
			}
		}
	}

	private Tweet buildTweetFromStatus(Status status) {
		String text = status.getText();

		Tweet tweet = new Tweet();
		tweet.setText(text);
		tweet.setUserScreenName(status.getUser().getScreenName());
		tweet.setUserName(status.getUser().getName());
		tweet.setCreated(status.getCreatedAt());
		tweet.setPlaceName(status.getPlace().getFullName());
		tweet.setCountry(status.getPlace().getCountry());
		tweet.setRetweetCount(status.getRetweetCount());
		tweet.setFavouriteCount(status.getFavoriteCount());

		if (status.getUserMentionEntities() != null) {
			List<String> screenNames = new ArrayList<>(status.getUserMentionEntities().length);
			List<String> fullNames = new ArrayList<>(status.getUserMentionEntities().length);

			for (UserMentionEntity ent : status.getUserMentionEntities()) {
				screenNames.add(ent.getScreenName());
				if (StringUtils.isNotBlank(ent.getName())) {
					fullNames.add(ent.getName());
				}
			}
			tweet.setMentionScreenNames(screenNames);
			tweet.setMentionFullNames(fullNames);
		}

		if (status.getHashtagEntities().length > 0) {
			List<String> hashtags = new ArrayList<>(status.getHashtagEntities().length);
			for (HashtagEntity ht : status.getHashtagEntities()) {
				hashtags.add(ht.getText());
			}
			tweet.setHashtags(hashtags);
		}

		// Call the entity extraction service
		Map<String, List<String>> entities = entityExtractionService.getEntities(text);
		if (entities != null && !entities.isEmpty()) {
			Map<String, Object> tweetEntities = new HashMap<String, Object>();
			for (String type : entities.keySet()) {
				String nerType = type.toLowerCase() + "_ner";
				tweetEntities.put(nerType, entities.get(type));
			}
			tweet.setEntities(tweetEntities);
		}

		return tweet;
	}

}
