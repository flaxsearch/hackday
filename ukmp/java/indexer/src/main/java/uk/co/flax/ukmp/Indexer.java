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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import uk.co.flax.ukmp.twitter.PartyListHandler;

/**
 * @author Matt Pearce
 */
public class Indexer {

	private final IndexerConfiguration config;
	private final Configuration twitterConfig;

	public Indexer(String configFile) throws Exception {
		// Read the configuration details
		config = readConfiguration(configFile);
		twitterConfig = readTwitterConfiguration(config.getAuthenticationFile());
	}

	private IndexerConfiguration readConfiguration(String configFile) throws IOException {
		IndexerConfiguration ret = null;
		BufferedReader br = null;

		try {
			Yaml yaml = new Yaml();
			br = new BufferedReader(new FileReader(configFile));
			ret = yaml.loadAs(br, IndexerConfiguration.class);
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return ret;
	}


	private Configuration readTwitterConfiguration(String twitterConfigFile) throws IOException {
		Configuration twitterConfig = null;
		InputStream is = null;

		try {
			is = new FileInputStream(config.getAuthenticationFile());
			twitterConfig = new PropertyConfiguration(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		return twitterConfig;
	}


	public void run() {
		PartyListHandler listHandler = new PartyListHandler(twitterConfig, config.getParties());
		listHandler.refreshLists();
	}


	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage:");
			System.out.println("  java Indexer <configFile>");
			System.exit(1);
		} else {
			try {
				new Indexer(args[0]).run();
			} catch (Exception e) {
				System.err.println("Exception caught from Indexer: " + e.getMessage());
				System.exit(1);
			}
		}
	}

}
