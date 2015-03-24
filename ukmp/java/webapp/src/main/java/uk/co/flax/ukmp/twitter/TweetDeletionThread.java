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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.StatusDeletionNotice;
import uk.co.flax.ukmp.api.SearchResults;
import uk.co.flax.ukmp.api.Tweet;
import uk.co.flax.ukmp.search.Query;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.SearchEngineException;

public class TweetDeletionThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(TweetDeletionThread.class);

	private static final long QUEUE_CHECK_TIME = 60000;

	private final SearchEngine searchEngine;
	private final Queue<StatusDeletionNotice> deleteQueue;
	private final int batchSize;

	private boolean running;

	public TweetDeletionThread(SearchEngine searchEngine, Queue<StatusDeletionNotice> deleteQueue, int batchSize) {
		this.searchEngine = searchEngine;
		this.deleteQueue = deleteQueue;
		this.batchSize = batchSize;
	}

	@Override
	public void run() {
		LOGGER.info("Starting twitter deletion thread");
		running = true;
		List<StatusDeletionNotice> deleteList = new ArrayList<>();

		while (running) {
			while (deleteList.size() < batchSize && !deleteQueue.isEmpty()) {
				deleteList.add(deleteQueue.remove());
			}

			if (!deleteList.isEmpty()) {
				List<String> deletedIds = deleteTweets(deleteList);
				removeDeletedFromQueue(deletedIds, deleteList);
			}

			// Sleep...
			try {
				Thread.sleep(QUEUE_CHECK_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("Delete thread sleep interrupted: {}", e.getMessage());
			}
		}
	}

	public void closeDown() {
		// Set running to false
		running = false;

		LOGGER.debug("Shutting down twitter deletion thread");

		if (!deleteQueue.isEmpty()) {
			// Try to clear the queue
			LOGGER.debug("Trying to clear {} items from delete queue", deleteQueue.size());

			List<StatusDeletionNotice> deletions = new ArrayList<>(deleteQueue);
			List<String> deletedIds = deleteTweets(deletions);

			LOGGER.debug("Deleted {} / {} tweets", deletedIds.size(), deletions.size());
		}

		LOGGER.info("Twitter deletion thread shut down.");
	}

	private List<String> deleteTweets(List<StatusDeletionNotice> deleteList) {
		List<String> deletedIds = new ArrayList<>();

		try {
			// Get the statuses that have actually been stored
			Set<String> deleteIds = getStringIds(deleteList);
			Set<String> statusIds = getStoredTweetIds(deleteIds);

			if (statusIds.size() > 0) {
				// Try to delete them
				searchEngine.deleteTweets(new ArrayList<String>(statusIds));
				LOGGER.debug("Deleted {} tweets", statusIds.size());

				// Get the statuses which are still in the index, if any
				Set<String> notDeletedIds = getStoredTweetIds(statusIds);

				// Remove the not deleted IDs from the status list
				statusIds.removeAll(notDeletedIds);

				deletedIds = new ArrayList<>(statusIds);
			}
		} catch (SearchEngineException e) {
			LOGGER.error("Could not delete {} IDs - {}", deleteList.size(), e.getMessage());
		}

		return deletedIds;
	}

	private Set<String> getStringIds(List<StatusDeletionNotice> deleteList) {
		Set<String> ids = new HashSet<>(deleteList.size());
		deleteList.forEach(sdn -> ids.add("" + sdn.getStatusId()));
		return ids;
	}

	private Set<String> getStoredTweetIds(Collection<String> statusIds) throws SearchEngineException {
		Query query = new Query(Query.DEFAULT_SEARCH, Arrays.asList(buildIdFilter(statusIds)));
		SearchResults results = searchEngine.search(query);

		Set<String> storedIds = results.getTweets().stream().map(Tweet::getId).collect(Collectors.toSet());

		return storedIds;
	}

	private String buildIdFilter(Collection<String> ids) {
		StringBuilder filterBuilder = new StringBuilder();

		int count = 0;
		for (String id : ids) {
			if (count > 0) {
				filterBuilder.append(" OR ");
			}
			filterBuilder.append(SearchEngine.ID_FIELD).append(":").append(id);
			count ++;
		}

		return filterBuilder.toString();
	}

	private void removeDeletedFromQueue(List<String> deletedIds, List<StatusDeletionNotice> deleteList) {
		// Remove the deleted IDs from the delete list
		for (Iterator<StatusDeletionNotice> it = deleteList.iterator(); it.hasNext(); ) {
			StatusDeletionNotice sdn = it.next();
			if (deletedIds.contains("" + sdn.getStatusId())) {
				it.remove();
			}
		}
	}

}
