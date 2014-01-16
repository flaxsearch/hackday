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
package uk.co.flax.ukmp.services;

import uk.co.flax.ukmp.api.Sentiment;
import uk.co.flax.ukmp.config.StanfordConfiguration;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * A service class to parse and return sentiment values for strings.
 */
public class SentimentAnalysisService {

	private final StanfordCoreNLP pipeline;

	/**
	 * Create a new SentimentAnalyzer using details from the Stanford
	 * configuration properties.
	 *
	 * @param config
	 *            the configuration.
	 */
	public SentimentAnalysisService(StanfordConfiguration config) {
		pipeline = new StanfordCoreNLP(config.getJavaSentimentProperties());
	}

	public Sentiment analyze(String text) {
		Sentiment mainSentiment = null;

		if (text != null && text.length() > 0) {
			int longest = 0;
			Annotation annotation = pipeline.process(text);
			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				String partText = sentence.toString();
				if (partText.length() > longest) {
					Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
					int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
					mainSentiment = new Sentiment(sentence.get(SentimentCoreAnnotations.ClassName.class), sentiment);
					longest = partText.length();
				}

			}
		}

		return mainSentiment;
	}

}
