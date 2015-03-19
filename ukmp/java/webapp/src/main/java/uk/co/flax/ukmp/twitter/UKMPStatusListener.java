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

import java.util.Queue;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * Created by mlp on 14/03/15.
 */
public class UKMPStatusListener implements StatusListener {

    private final Queue<Status> statusQueue;
    private final Queue<StatusDeletionNotice> deletionQueue;

    public UKMPStatusListener(Queue<Status> sQueue, Queue<StatusDeletionNotice> delQueue) {
        this.statusQueue = sQueue;
        this.deletionQueue = delQueue;
    }

    @Override
    public void onStatus(Status status) {
        // Skip any DMs or RTs
        if (!status.getText().startsWith("@") && !status.isRetweet()) {
            statusQueue.offer(status);
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        deletionQueue.offer(statusDeletionNotice);
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {

    }

    @Override
    public void onStallWarning(StallWarning warning) {

    }

    @Override
    public void onException(Exception ex) {

    }
}
