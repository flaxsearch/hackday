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
package uk.co.flax.ukmp;

import java.util.Map;

import uk.co.flax.ukmp.config.PartyConfiguration;


/**
 * @author Matt Pearce
 */
public class IndexerConfiguration {

	private String authenticationFile;
	private String dataDirectory;
	private String archiveDirectory;
	private int numListeners;
	private int numThreads;
	private int messageQueueSize;

	private Map<String, PartyConfiguration> parties;

	/**
	 * @return the authenticationFile
	 */
	public String getAuthenticationFile() {
		return authenticationFile;
	}

	/**
	 * @param authenticationFile the authenticationFile to set
	 */
	public void setAuthenticationFile(String authenticationFile) {
		this.authenticationFile = authenticationFile;
	}

	/**
	 * @return the parties
	 */
	public Map<String, PartyConfiguration> getParties() {
		return parties;
	}

	/**
	 * @param parties the parties to set
	 */
	public void setParties(Map<String, PartyConfiguration> parties) {
		this.parties = parties;
	}

	/**
	 * @return the dataDirectory
	 */
	public String getDataDirectory() {
		return dataDirectory;
	}

	/**
	 * @param dataDirectory the dataDirectory to set
	 */
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	/**
	 * @return the archiveDirectory
	 */
	public String getArchiveDirectory() {
		return archiveDirectory;
	}

	/**
	 * @param archiveDirectory the archiveDirectory to set
	 */
	public void setArchiveDirectory(String archiveDirectory) {
		this.archiveDirectory = archiveDirectory;
	}

	/**
	 * @return the numListeners
	 */
	public int getNumListeners() {
		return numListeners;
	}

	/**
	 * @param numListeners the numListeners to set
	 */
	public void setNumListeners(int numListeners) {
		this.numListeners = numListeners;
	}

	/**
	 * @return the messageQueueSize
	 */
	public int getMessageQueueSize() {
		return messageQueueSize;
	}

	/**
	 * @param messageQueueSize the messageQueueSize to set
	 */
	public void setMessageQueueSize(int messageQueueSize) {
		this.messageQueueSize = messageQueueSize;
	}

	/**
	 * @return the numThreads
	 */
	public int getNumThreads() {
		return numThreads;
	}

	/**
	 * @param numThreads the numThreads to set
	 */
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

}
