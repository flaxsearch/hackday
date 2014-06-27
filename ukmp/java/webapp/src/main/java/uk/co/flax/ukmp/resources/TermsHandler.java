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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.co.flax.ukmp.api.Term;
import uk.co.flax.ukmp.api.TermsResponse;
import uk.co.flax.ukmp.services.TermsManager;

/**
 * Resource handler dealing with the search compopnent.
 */
@Path("/terms")
public class TermsHandler {

	private final TermsManager termsManager;

	public TermsHandler(TermsManager termsManager) {
		this.termsManager = termsManager;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TermsResponse handleGet() {
		TermsResponse ret;

		List<Term> terms = termsManager.getTerms();
		ret = new TermsResponse(terms);

		return ret;
	}

}
