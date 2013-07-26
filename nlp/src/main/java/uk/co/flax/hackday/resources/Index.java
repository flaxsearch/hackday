package uk.co.flax.hackday.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

@Path("/")
public class Index {
	
	private final AbstractSequenceClassifier<CoreLabel> classifier;
	
	public Index(String classifierFile) {
		this.classifier = CRFClassifier.getClassifierNoExceptions(classifierFile);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String handleGet() {
		return "POST text to this page!";
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String handlePost(String text) {
		String retVal = "{}";
		
		Map<String, List<String>> retMap = new HashMap<String, List<String>>(); 
		
		List<List<CoreLabel>> sentences = classifier.classify(text);
		for (List<CoreLabel> sentence : sentences) {
			// Current object and object type
			String currObj = "";
			String currType = "";
			
			for (CoreLabel word : sentence) {
				String type = word.get(AnswerAnnotation.class);
				String obj = word.word();
				
				// Ignore "O" annotations - these are general
				if (type.equals(currType) && !type.equals("O")) {
					// Append the current word to the previous one - join multi-word names
					currObj = currObj + " " + obj;
				} else {
					// New type - if we have a current object, store it
					if (currObj.length() > 0 && !currType.equals("O")) {
						if (!retMap.containsKey(currType)) {
							// New annotation type - add to the map
							retMap.put(currType, new ArrayList<String>());
						}

						retMap.get(currType).add(currObj);
					}
					
					// Hang on to the current type and object, use in the next iteration
					currType = type;
					currObj = obj;
				}
			}
		}

		// Convert the map of classifiers to JSON
		try {
			ObjectMapper mapper = new ObjectMapper();
			retVal = mapper.writeValueAsString(retMap);
		} catch (JsonProcessingException e) {
			System.err.println("Cannot convert words to JSON: " + e.getMessage());
		}
		
		return retVal;
	}

}
