/**
 * Copyright (c) 2013 Lemur Consulting Ltd.
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
package uk.co.flax.ukmp.config;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration details for the entity extractor.
 */
public class StanfordConfiguration {

	private static final String NER_CLASSIFIER_PROPERTY = "loadClassifier";

	private Map<String, String> entityProperties;

	private Map<String, String> sentimentProperties;

	/**
	 * @return the properties to set for NER.
	 */
	public Map<String, String> getEntityProperties() {
		return entityProperties;
	}

	/**
	 * @return the sentimentProperties
	 */
	public Map<String, String> getSentimentProperties() {
		return sentimentProperties;
	}

	/**
	 * Get the entity properties map as a Java Properties object.
	 * @return a Java Properties object containing the properties.
	 */
	public Properties getJavaEntityProperties() {
		Properties props = new Properties();

		for (String key : entityProperties.keySet()) {
			props.put(key, entityProperties.get(key));
		}

		return props;
	}

	/**
	 * @return the path to the classifier file.
	 */
	public String getClassifierFile() {
		return entityProperties.get(NER_CLASSIFIER_PROPERTY);
	}

	/**
	 * Get the sentiment properties map as a Java Properties object.
	 * @return the sentiment properties.
	 */
	public Properties getJavaSentimentProperties() {
		Properties props = new Properties();

		for (String key : sentimentProperties.keySet()) {
			props.put(key, sentimentProperties.get(key));
		}

		return props;
	}

}
