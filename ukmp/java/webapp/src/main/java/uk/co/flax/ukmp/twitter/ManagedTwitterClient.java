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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import uk.co.flax.ukmp.config.TwitterConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.services.EntityExtractionService;

/**
 * Created by mlp on 14/03/15.
 */
public class ManagedTwitterClient implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedTwitterClient.class);

    private static final String CONSUMER_KEY = "consumer_key";
    private static final String CONSUMER_SECRET = "consumer_secret";
    private static final String ACCESS_TOKEN = "access_token_key";
    private static final String ACCESS_SECRET = "access_token_secret";

    private final TwitterConfiguration config;
    private final SearchEngine searchEngine;
    private final EntityExtractionService entityExtraction;

    private final Queue<Status> statusQueue;
    private final Queue<StatusDeletionNotice> deletionQueue;

    private TwitterStream stream;
    private List<String> filters;

    private TweetDeletionThread deletionThread;
    private TweetUpdateThread updateThread;

    public ManagedTwitterClient(TwitterConfiguration config, SearchEngine search, EntityExtractionService entityExtraction) {
        this.config = config;
        this.searchEngine = search;
        this.entityExtraction = entityExtraction;

        this.statusQueue = new ConcurrentLinkedQueue<>();
        this.deletionQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void start() throws Exception {
        Configuration authConfig = buildConfiguration();

        TwitterStreamFactory tsf = new TwitterStreamFactory(authConfig);
        stream = tsf.getInstance();

        StatusListener statusListener = new UKMPStatusListener(statusQueue, deletionQueue);
        stream.addListener(statusListener);

//        updateFilters();

        // Start the update and delete threads
        this.deletionThread = new TweetDeletionThread(searchEngine, deletionQueue, config.getDeletionBatchSize());
        deletionThread.start();
        this.updateThread = new TweetUpdateThread(searchEngine, statusQueue, config.getStatusBatchSize(), entityExtraction);
        updateThread.start();
    }

    @Override
    public void stop() throws Exception {
        stream.cleanUp();
        stream.shutdown();
        deletionThread.closeDown();
        updateThread.closeDown();
    }

    private Configuration buildConfiguration() throws IOException {
        Map<String, String> authMap = readAuthConfiguration();

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthAccessToken(authMap.get(ACCESS_TOKEN));
        cb.setOAuthAccessTokenSecret(authMap.get(ACCESS_SECRET));
        cb.setOAuthConsumerKey(authMap.get(CONSUMER_KEY));
        cb.setOAuthConsumerSecret(authMap.get(CONSUMER_SECRET));

        return cb.build();
    }

    private Map<String, String> readAuthConfiguration() throws IOException {
        Map<String, String> ret = new HashMap<>();

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(config.getAuthConfigFile()));
            String line;

            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotBlank(line.trim()) && !line.startsWith("#")) {
                    String[] parts = line.split(":");
                    ret.put(parts[0].trim(), parts[1].trim());
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return ret;
    }

}
