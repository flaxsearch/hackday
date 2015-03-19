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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import uk.co.flax.ukmp.config.TwitterConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.services.EntityExtractionService;

/**
 * Created by mlp on 14/03/15.
 */
public class ManagedTwitterClient extends AbstractTwitterClient implements Managed, TwitterListListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedTwitterClient.class);

    private final TwitterConfiguration config;
    private final SearchEngine searchEngine;

    private final Queue<Status> statusQueue;
    private final Queue<StatusDeletionNotice> deletionQueue;

    private TwitterStream stream;
    private List<Long> filterIds;

    private TweetDeletionThread deletionThread;
    private TweetUpdateThread updateThread;

	public ManagedTwitterClient(TwitterConfiguration config, SearchEngine search,
			EntityExtractionService entityExtraction, TwitterListManager listManager) {
        this.config = config;
        this.searchEngine = search;

        this.statusQueue = new ConcurrentLinkedQueue<>();
        this.deletionQueue = new ConcurrentLinkedQueue<>();

        this.deletionThread = new TweetDeletionThread(searchEngine, deletionQueue, config.getDeletionBatchSize());
        this.updateThread = new TweetUpdateThread(searchEngine, statusQueue, config.getStatusBatchSize(), entityExtraction);

        // Register ourselves as a listener with the list manager
        listManager.registerListener(this);
    }

    @Override
    public void start() throws Exception {
        Configuration authConfig = buildConfiguration();

        TwitterStreamFactory tsf = new TwitterStreamFactory(authConfig);
        stream = tsf.getInstance();

        StatusListener statusListener = new UKMPStatusListener(statusQueue, deletionQueue);
        stream.addListener(statusListener);

        // Start the update and delete threads
        deletionThread.start();
        updateThread.start();
    }

    @Override
    public void stop() throws Exception {
        stream.cleanUp();
        stream.shutdown();
        deletionThread.closeDown();
        updateThread.closeDown();
    }

    @Override
	protected TwitterConfiguration getConfig() {
    	return config;
    }

	@Override
	public void notify(Map<String, List<Long>> listIds) {
		List<Long> ids = new ArrayList<>();
		listIds.keySet().forEach(party -> ids.addAll(listIds.get(party)));
		updateFilterIds(ids);

		// Invert the map for the update thread
		Map<Long, String> userPartyMap = new HashMap<>();
		for (String party : listIds.keySet()) {
			listIds.get(party).forEach(userId -> userPartyMap.put(userId, party));
		}
		updateThread.setPartyListIds(userPartyMap);
	}

	private void updateFilterIds(List<Long> updatedIds) {
		if (!updatedIds.equals(filterIds)) {
			LOGGER.debug("Updating filter IDs");
			if (this.filterIds != null) {
				// Stop the stream listener
				stream.cleanUp();
			}

			// Convert filterIds to long[]
			long[] ids = new long[updatedIds.size()];
			for (int i = 0; i < updatedIds.size(); i ++) {
				ids[i] = updatedIds.get(i);
			}
			FilterQuery filterQuery = new FilterQuery().follow(ids);
			// Start filtering by the new query
			stream.filter(filterQuery);

			this.filterIds = updatedIds;
		}
	}

}
