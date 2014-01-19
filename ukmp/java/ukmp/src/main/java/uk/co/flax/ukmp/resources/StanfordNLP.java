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
package uk.co.flax.ukmp.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.co.flax.ukmp.api.Sentiment;
import uk.co.flax.ukmp.api.StanfordData;
import uk.co.flax.ukmp.services.EntityExtractionService;
import uk.co.flax.ukmp.services.SentimentAnalysisService;

/**
 * Resource handler for extracting entities and sentiment analysis from tweet
 * text.
 */
@Path("/stanford")
public class StanfordNLP {

	private final EntityExtractionService entityService;
	private final SentimentAnalysisService sentimentService;

	public StanfordNLP(EntityExtractionService entSvc, SentimentAnalysisService saSvc) {
		this.entityService = entSvc;
		this.sentimentService = saSvc;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public StanfordData handlePost(String text) {
		Map<String, List<String>> entities = entityService.getEntities(text);
		Sentiment sentiment = sentimentService.analyze(text);

		return new StanfordData(entities, sentiment);
	}

}
