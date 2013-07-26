package uk.co.flax.hackday;

import com.yammer.dropwizard.config.Configuration;

public class NLPConfiguration extends Configuration {
	
	private String classifier;
	
	public String getClassifier() {
		return classifier;
	}

}
