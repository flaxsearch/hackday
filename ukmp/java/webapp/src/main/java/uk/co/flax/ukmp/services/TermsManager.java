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

import io.dropwizard.lifecycle.Managed;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.ukmp.api.Term;
import uk.co.flax.ukmp.config.TermsConfiguration;
import uk.co.flax.ukmp.search.SearchEngine;

/**
 * Manager class for handling the terms list.
 */
public class TermsManager implements Managed {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermsManager.class);

	private final TermsManagerThread termsThread;

	public TermsManager(SearchEngine engine, TermsConfiguration config) throws Exception {
		termsThread = new TermsManagerThread(engine, config);
	}

	@Override
	public void start() {
		LOGGER.info("Starting terms thread...");
		termsThread.start();
	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Shutting down terms thread...");
		termsThread.shutdown();
	}

	/**
	 * Get an unmodifiable list of the current terms.
	 * @return the current terms.
	 */
	public List<Term> getTerms() {
		return Collections.unmodifiableList(termsThread.getTerms());
	}

}