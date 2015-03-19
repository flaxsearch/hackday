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

import io.dropwizard.lifecycle.Managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import uk.co.flax.ukmp.config.TwitterConfiguration;
import uk.co.flax.ukmp.config.TwitterListConfiguration;

/**
 * Manager class for keeping track of twitter lists.
 *
 * @author Matt Pearce
 */
public class TwitterListManager extends AbstractTwitterClient implements Managed {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterListManager.class);

	private final TwitterConfiguration config;
	private final long sleepTime;

	private ListUpdateThread updateThread;
	private Map<String, List<Long>> twitterLists = new HashMap<>();

	private Set<TwitterListListener> listeners = new HashSet<>();

	public TwitterListManager(TwitterConfiguration config) {
		this.config = config;
		// Convert update check delay into milliseconds
		this.sleepTime = config.getUpdateCheckHours() * 60 * 60 * 1000;
	}

	@Override
	protected TwitterConfiguration getConfig() {
		return config;
	}

	@Override
	public void start() throws Exception {
        Configuration authConfig = buildConfiguration();
        TwitterFactory factory = new TwitterFactory(authConfig);
        Twitter twitter = factory.getInstance();

        updateThread = new ListUpdateThread(twitter);
        updateThread.start();
	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Shutting down list update thread...");
		updateThread.shutdown();
		while (updateThread.running) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error("Interrupted while waiting for update thread to shut down: {}", e.getMessage());
			}
		}
		LOGGER.info("... list update thread stopped");
	}

	public void registerListener(TwitterListListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners() {
		listeners.forEach(listener -> listener.notify(twitterLists));
	}

	private class ListUpdateThread extends Thread {

		private boolean running;
		private Twitter twitter;

		ListUpdateThread(Twitter twitter) {
			this.twitter = twitter;
		}

		void shutdown() {
			this.running = false;
		}

		@Override
		public void run() {
			LOGGER.info("Starting list update thread");
			long lastUpdate = 0;
			running = true;

			while (running) {
				if (System.currentTimeMillis() - sleepTime > lastUpdate) {
					// Update the lists
					for (TwitterListConfiguration listConfig : config.getLists()) {
						List<Long> ids = getListUserIds(listConfig);
						if (ids != null) {
							twitterLists.put(listConfig.getDisplayName(), ids);
						}
					}

					notifyListeners();
					lastUpdate = System.currentTimeMillis();
				}

				// Sleep for 5 secs, check again
				delay(5000);
			}
		}

		private void delay(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				LOGGER.error("Sleep interrupted! {}", e.getMessage());
			}
		}

		private List<Long> getListUserIds(TwitterListConfiguration listConfig) {
			LOGGER.debug("Looking up user IDs for {} list", listConfig.getSlug());
			List<Long> userIds = new ArrayList<>();

			try {
				long cursor = -1;
				PagableResponseList<User> users;
				do {
					users = twitter.getUserListMembers(listConfig.getScreenName(), listConfig.getSlug(), cursor);
					userIds.addAll(users.stream().map(User::getId).collect(Collectors.toList()));
					cursor = users.getNextCursor();
				} while (users.hasNext());
			} catch (TwitterException e) {
				LOGGER.error("Could not get user list members: {}", e.getMessage());
				userIds = null;
			}

			LOGGER.debug("Got {} ids for {}", (userIds == null ? 0 : userIds.size()), listConfig.getSlug());

			return userIds;
		}

	}

}
