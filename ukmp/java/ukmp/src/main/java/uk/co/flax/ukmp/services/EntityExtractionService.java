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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import uk.co.flax.ukmp.api.Entity;
import uk.co.flax.ukmp.config.StanfordConfiguration;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * Service class for handling entity extraction.
 */
public class EntityExtractionService {

	/**
	 * Regular expression for recognising entities. These are XML, of the form
	 * &lt;EntityType&gt;value&lt;/EntityType&gt;.
	 */
	private static final Pattern ENTITY_REGEX = Pattern.compile("<(\\w+)>([^<]+)</.*?>");

	/**
	 * The maximum number of words that should make an entity. Stanford sometimes matches
	 * large strings, and this should be restricted.
	 */
	private static final int MAX_ENTITY_WORDS = 4;

	private final CRFClassifier<CoreLabel> classifier;

	public EntityExtractionService(StanfordConfiguration config) {
		// Create a Stanford classifier
		this.classifier = new CRFClassifier<CoreLabel>(config.getJavaEntityProperties());

		String classifierPath = config.getEntityProperties().get("loadClassifier");
		// Load the classifier explicitly from the jar file... this is not done by default,
		// and the loadClassifier property above doesn't appear to do anything.
		classifier.loadJarClassifier(classifierPath, config.getJavaEntityProperties());
	}

	/**
	 * Extract a map of entities from an incoming text string.
	 * @param text the text to be analysed.
	 * @return a map of entity type -> the list of entities, extracted from the
	 * given text.
	 */
	public Map<String, List<String>> getEntities(String text) {
		Map<String, List<String>> retMap = new HashMap<String, List<String>>();
		Set<Entity> entities = extractEntities(text);

		for (Entity entity : entities) {
			String field = entity.getFieldName();
			if (!retMap.containsKey(field)) {
				retMap.put(field, new ArrayList<String>());
			}
			retMap.get(field).add(entity.getValue());
		}

		return retMap;
	}

	/**
	 * Extract the entities from a text block and return them.
	 * @param text the text to be processed.
	 * @return a set of entities in the text. Never <code>null</code>.
	 */
	private Set<Entity> extractEntities(String text) {
		Set<Entity> entityList = new HashSet<Entity>();

		if (StringUtils.isNotBlank(text)) {
			// Classify with inline XML - this will group multi-word entities, such
			// as people's names
			String classified = classifier.classifyWithInlineXML(text);

			// Use a regex to extract the entities from the classifed XML - nasty, but quicker
			// than transforming to an actual XML document.
			Matcher m = ENTITY_REGEX.matcher(classified);
			while (m.find()) {
				String type = m.group(1);
				String value = m.group(2);

				// Restrict the number of words that can make up an entity value
				if (value.split("\\s+").length <= MAX_ENTITY_WORDS) {
					Entity entity = new Entity(type, value);
					entityList.add(entity);
				}
			}
		}

		return entityList;
	}

}
