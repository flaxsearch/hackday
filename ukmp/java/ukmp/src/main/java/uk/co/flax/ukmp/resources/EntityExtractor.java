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
package uk.co.flax.ukmp.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.co.flax.ukmp.api.StanfordData;
import uk.co.flax.ukmp.services.EntityExtractionService;

/**
 * Resource handling entity extraction - pass in text, returns a list of
 * entities.
 */
@Path("/entityExtractor")
public class EntityExtractor {

	private final EntityExtractionService entityService;

	public EntityExtractor(EntityExtractionService entityService) {
		this.entityService = entityService;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public StanfordData handlePost(String text) {
		Map<String, List<String>> entityMap = entityService.getEntities(text);
		return new StanfordData(entityMap, null);
	}

}
