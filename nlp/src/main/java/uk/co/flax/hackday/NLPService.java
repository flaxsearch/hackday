package uk.co.flax.hackday;

import uk.co.flax.hackday.resources.Index;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class NLPService extends Service<NLPConfiguration> {
	
	@Override
	public void initialize(Bootstrap<NLPConfiguration> arg0) {
	}

	@Override
	public void run(NLPConfiguration config, Environment env) throws Exception {
		env.addResource(new Index(config.getClassifier()));
	}

	public static void main(String[] args) throws Exception {
		new NLPService().run(args);
	}

}
