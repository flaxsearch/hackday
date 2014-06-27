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
package uk.co.flax.ukmp.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a sentiment analysis output.
 */
public class Sentiment {

	public static final int SENTIMENT_POSITIVE = 3;
	public static final int SENTIMENT_NEUTRAL = 2;
	public static final int SENTIMENT_NEGATIVE = 1;

	@JsonProperty("class")
	private final String sentimentClass;
	@JsonProperty("value")
	private final int sentimentValue;

	public Sentiment(String classText, int value) {
		this.sentimentClass = classText;
		this.sentimentValue = value;
	}

	/**
	 * @return the sentimentClass
	 */
	public String getSentimentClass() {
		return sentimentClass;
	}

	/**
	 * @return the sentimentValue
	 */
	public int getSentimentValue() {
		return sentimentValue;
	}

}
