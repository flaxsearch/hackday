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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import uk.co.flax.ukmp.config.TwitterConfiguration;

/**
 * Base class for the twitter handlers.
 *
 * @author Matt Pearce
 */
public abstract class AbstractTwitterClient {

    private static final String CONSUMER_KEY = "consumer_key";
    private static final String CONSUMER_SECRET = "consumer_secret";
    private static final String ACCESS_TOKEN = "access_token_key";
    private static final String ACCESS_SECRET = "access_token_secret";

	protected abstract TwitterConfiguration getConfig();

    protected Configuration buildConfiguration() throws IOException {
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
            br = new BufferedReader(new FileReader(getConfig().getAuthConfigFile()));
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
