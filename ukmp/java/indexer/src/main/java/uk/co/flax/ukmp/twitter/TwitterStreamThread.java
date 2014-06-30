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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;

import twitter4j.StatusListener;
import twitter4j.conf.Configuration;
import uk.co.flax.ukmp.IndexerConfiguration;
import uk.co.flax.ukmp.index.IndexerSearchEngine;

/**
 * @author Matt Pearce
 */
public class TwitterStreamThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamThread.class);

	private final IndexerConfiguration indexConfig;
	private final Configuration twitterConfig;
	private final IndexerSearchEngine searchEngine;
	private final PartyListHandler partyListHandler;

	private final BlockingQueue<String> queue;
	private boolean running;

	public TwitterStreamThread(IndexerConfiguration indexConfig, Configuration twitterConfig, IndexerSearchEngine searchEngine, PartyListHandler partyList) {
		this.indexConfig = indexConfig;
		this.twitterConfig = twitterConfig;
		this.searchEngine = searchEngine;
		this.partyListHandler = partyList;

		this.queue = new LinkedBlockingQueue<>(indexConfig.getMessageQueueSize());
		if (partyListHandler.isTimeToUpdate()) {
			partyListHandler.refreshLists();
		}
	}


	@Override
	public void run() {
		// Create the authentication object
		Authentication auth = new OAuth1(twitterConfig.getOAuthConsumerKey(), twitterConfig.getOAuthConsumerSecret(),
				twitterConfig.getOAuthAccessToken(), twitterConfig.getOAuthAccessTokenSecret());

		// Define the endpoint
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
		endpoint.followings(partyListHandler.getAllPartyMemberIds());

		// Create the client
		BasicClient client = new ClientBuilder().hosts(Constants.STREAM_HOST)
				.endpoint(endpoint).authentication(auth)
				.processor(new StringDelimitedProcessor(queue))
				.build();

		// Create the listeners
		List<StatusListener> listeners = new ArrayList<>(indexConfig.getNumListeners());
		for (int i = 0; i < indexConfig.getNumListeners(); i ++) {
			listeners.add(new UKMPStatusStreamHandler("Handler" + i, searchEngine, partyListHandler));
		}

		int numProcessingThreads = indexConfig.getNumThreads();
		ExecutorService service = Executors.newFixedThreadPool(numProcessingThreads);

		// Wrap the basic client with the Twitter4j client
		Twitter4jStatusClient t4jClient = new Twitter4jStatusClient(client, queue, listeners, service);

		// Establish a connection
		t4jClient.connect();
		// Start the processor threads
		for (int i = 0; i < numProcessingThreads; i ++) {
			t4jClient.process();
		}

		running = true;

		while (running) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOGGER.error("Sleep interrupted: {}", e.getMessage());
			}
		}

		t4jClient.stop();
	}


}
