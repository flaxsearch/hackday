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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import uk.co.flax.ukmp.index.IndexerSearchEngine;

import com.twitter.hbc.twitter4j.handler.StatusStreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;

/**
 * @author Matt Pearce
 */
public class UKMPStatusStreamHandler implements StatusStreamHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UKMPStatusStreamHandler.class);

	private final String name;
	private final IndexerSearchEngine searchEngine;
	private final PartyListHandler partyListHandler;

	private boolean disconnected;
	private boolean stalled;

	public UKMPStatusStreamHandler(String name, IndexerSearchEngine searchEngine, PartyListHandler partyList) {
		this.name = name;
		this.searchEngine = searchEngine;
		this.partyListHandler = partyList;
	}

	@Override
	public void onStatus(Status status) {
		String party = partyListHandler.getMemberParties().get(status.getUser().getId());
		searchEngine.storeStatus(status, party);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		LOGGER.debug("{} : handling status deletion for user {}, status ID {}", name, statusDeletionNotice.getUserId(), statusDeletionNotice.getStatusId());
		searchEngine.deleteStatus(statusDeletionNotice.getUserId(), statusDeletionNotice.getStatusId());
	}

	/* (non-Javadoc)
	 * @see twitter4j.StatusListener#onTrackLimitationNotice(int)
	 */
	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		LOGGER.warn("{} : received stall warning: {}", name, warning.getMessage());
		stalled = true;
	}

	@Override
	public void onException(Exception ex) {
		LOGGER.error("{} : Caught exception: {}", name, ex.getMessage());
	}

	@Override
	public void onDisconnectMessage(DisconnectMessage message) {
		LOGGER.warn("{} : received disconnect message: {}", name, message.getDisconnectReason());
		disconnected = true;
	}

	@Override
	public void onStallWarningMessage(StallWarningMessage warning) {
		LOGGER.warn("{} : received stall warning message: {}", name, warning.getMessage());
		stalled = true;
	}

	@Override
	public void onUnknownMessageType(String msg) {
		LOGGER.warn("{} : unknown message type received: {}", name, msg);
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public boolean isStalled() {
		return stalled;
	}

}
