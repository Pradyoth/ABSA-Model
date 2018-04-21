package com.sentiment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sentiment.dao.MySQLAccess;
import com.sentiment.modal.Hotel;
import com.sentiment.modal.HotelReview;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentApp {
	static StanfordCoreNLP pipeline;

	public static void init() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public static int findSentiment(String text) {

		int mainSentiment = 0;
		if (text != null && text.length() > 0) {
			int longest = 0;
			Annotation annotation = pipeline.process(text);
			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence.get(SentimentAnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
				String partText = sentence.toString();
				if (partText.length() > longest) {
					mainSentiment = sentiment;
					longest = partText.length();
				}
			}
		}
		return mainSentiment;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		init();
		MySQLAccess access = new MySQLAccess();
		Map<Integer, Hotel> hotelMap = access.getHotels();

		int hotelId;
		List<String> listOfHotelAttributes = access.getHotelAttributes();

		for(String hotelAttribute: listOfHotelAttributes) {
			System.out.println("Hotel Attribute Name--" + hotelAttribute);
			

			for(Hotel hotel: hotelMap.values()) {				
				hotelId = hotel.getId();
				/*
				 * Create a new classifier instance. The context features are Strings and the
				 * context will be classified with a String according to the featureset of the
				 * context.
				 */

				final Classifier<String, String> bayes = new BayesClassifier<String, String>();
				List<HotelReview> hotelReviews = access.getHotelReviews(hotelId);
//				System.out.println("Hotel Reviews: \n" + hotelReviews);
//				System.out.println("Hotel review for hotel category " + categoryName);
				for(HotelReview review: hotelReviews) {
					String reviewForHotelAttribute = review.reviewsForHotelAttribute(hotelAttribute);
//					System.out.println("Review:" + reviewForCategory);
					if(!"".equals(reviewForHotelAttribute)) {
						int sentiment = findSentiment(reviewForHotelAttribute);
//						System.out.println("Sentiment - " + sentiment);
						if (sentiment >= 2) {
							bayes.learn("positive", Arrays.asList(reviewForHotelAttribute));
						} else if (sentiment == 1) {
							bayes.learn("negative", Arrays.asList(reviewForHotelAttribute));
						}											
					}
				}
				String hotelAttributeSearch = null;
				if (hotelAttribute.trim().equalsIgnoreCase("Staff")) {
					hotelAttributeSearch = "Best Service";
				} else if (hotelAttribute.trim().equalsIgnoreCase("facility")) {
					hotelAttributeSearch = "Good Facilities";
				} else if (hotelAttribute.trim().equalsIgnoreCase("maintenance")) {
					hotelAttributeSearch = "Good maintenance";
				} else if (hotelAttribute.trim().equalsIgnoreCase("food")) {
					hotelAttributeSearch = "Good Food";
				} else if (hotelAttribute.trim().equalsIgnoreCase("room")) {
					hotelAttributeSearch = "Best Rooms Amenities";
				}

				final String[] hotelAttributeText = hotelAttributeSearch.split("\\s");

				Collection<Classification<String, String>> kt = ((BayesClassifier<String, String>) bayes)
						.classifyDetailed(Arrays.asList(hotelAttributeText));

				float postiveprob = 0.0f;
				float negativeprob = 0.0f;
				String feature = "";
				for(Classification<String, String> classification: kt) {
					List<String> lst = (List<String>) classification.getFeatureset();
					System.out.println("classification===" + classification.toString());
					for (int j = 0; j < lst.size(); j++) {
						String cat = (String) classification.getCategory();
						feature = lst.get(j);
						if (cat.trim() == "positive") {
							postiveprob = classification.getProbability();
						} else if (cat.trim() == "negative") {
							negativeprob = classification.getProbability();
						}
					}
					if (postiveprob > 0.0 && negativeprob > 0.0) {
						System.out.println("postiveprob==" + postiveprob);
						System.out.println("categoryName==" + hotelAttribute);
						System.out.println("hotelId==" + hotelId + " Hotel Name - " + hotel.getHotelName()); 
					}
				}			
				bayes.setMemoryCapacity(5000); // remember the last 5000 learned classifications
			}			
		}
	}

}