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
package uk.co.flax.ukmp;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.co.flax.ukmp.health.SolrHealthCheck;
import uk.co.flax.ukmp.resources.BrowseResource;
import uk.co.flax.ukmp.resources.EntityExtractor;
import uk.co.flax.ukmp.resources.Ping;
import uk.co.flax.ukmp.resources.SentimentAnalyzer;
import uk.co.flax.ukmp.resources.StanfordNLP;
import uk.co.flax.ukmp.resources.TermsHandler;
import uk.co.flax.ukmp.search.SearchEngine;
import uk.co.flax.ukmp.search.solr.SolrSearchEngine;
import uk.co.flax.ukmp.services.EntityExtractionService;
import uk.co.flax.ukmp.services.SentimentAnalysisService;
import uk.co.flax.ukmp.services.TermsManager;


/**
 * Main service class for the UKMP Tweet indexer/web application.
 */
public class UKMPService extends Application<UKMPConfiguration> {

	@Override
	public void initialize(Bootstrap<UKMPConfiguration> bootstrap) {
		// Add bundle for static asset directories
		bootstrap.addBundle(new AssetsBundle("/static", "/", "index.html"));
		// Add webjars AssetsBundle, to include bootstrap, etc.
	    bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars"));
	}

	@Override
	public void run(UKMPConfiguration configuration, Environment environment) throws Exception {
		// Create the search engine
		SearchEngine engine = new SolrSearchEngine(configuration.getSolrConfiguration());
		// Create the Stanford services
		EntityExtractionService entityService = new EntityExtractionService(configuration.getStanfordConfiguration());
		SentimentAnalysisService sentimentService = new SentimentAnalysisService(configuration.getStanfordConfiguration());

		// Create the terms manager
		// Uses the environment's lifecycle management to start/shutdown the threads.
		TermsManager termsManager = new TermsManager(engine, configuration.getSolrConfiguration().getTermsConfiguration());
		environment.lifecycle().manage(termsManager);

		environment.jersey().register(new Ping());
		environment.jersey().register(new EntityExtractor(entityService));
		environment.jersey().register(new SentimentAnalyzer(sentimentService));
		environment.jersey().register(new StanfordNLP(entityService, sentimentService));

		environment.jersey().register(new BrowseResource(engine));
		environment.jersey().register(new TermsHandler(termsManager));

		// Add health checks
		environment.healthChecks().register("solr", new SolrHealthCheck(engine));
	}

	public static void main(String[] args) throws Exception {
		new UKMPService().run(args);
	}

}
