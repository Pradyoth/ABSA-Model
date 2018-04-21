package com.sentiment.modal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelReview {

	int id;
	int hotelId;
	String name;
	String reviewTitle;
	String reviewText;
	
	private static Map<String, List<String>> hotelCategoryMappingWords() {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		
		List<String> staffList = new ArrayList<String>();
		staffList.add("Staff");
		staffList.add("service");
		staffList.add("Everyone");
		List<String> facilityList = new ArrayList<String>();
		facilityList.add("facility");
		facilityList.add("fitness");
		facilityList.add("gym");
		facilityList.add("pool");
		facilityList.add("swim");
		List<String> maintenanceList = new ArrayList<String>();
		maintenanceList.add("maintenance");
		List<String> foodList = new ArrayList<String>();
		foodList.add("food");
		List<String> roomList = new ArrayList<String>();
		roomList.add("room");
		roomList.add("bed");
		map.put("Staff", staffList);
		map.put("facility", facilityList);
		map.put("maintenance", maintenanceList);
		map.put("food", foodList);
		map.put("room", roomList);		
		return map;
	}

	public HotelReview(int id, int hotelId, String name, String reviewTitle, String reviewText) {
		super();
		this.id = id;
		this.hotelId = hotelId;
		this.name = name;
		this.reviewTitle = reviewTitle;
		this.reviewText = reviewText;
		
	}

	public int getId() {
		return id;
	}

	public int getHotelId() {
		return hotelId;
	}

	public String getName() {
		return name;
	}

	public String getReviewTitle() {
		return reviewTitle;
	}

	public String getReviewText() {
		return reviewText;
	}


	public String reviewsForHotelAttribute(String hotelAttribute) {
		String[] reviewSentences = reviewText.split("(\\r\\n|\\r|\\n|\\.)");
		StringBuilder sb = new StringBuilder();
		List<String> mappingWords = HotelReview.hotelCategoryMappingWords().get(hotelAttribute);
		for(String sentence: reviewSentences) {
//			System.out.println(sentence);
			for(String mappingWord: mappingWords) {
				if(sentence.toUpperCase().contains(mappingWord.toUpperCase())) {
					sb.append(sentence).append(". ");
					break;
				}
			}
		}
		return sb.toString();
	}
	@Override
	public String toString() {
		return "Hotel [id=" + id + ", hotelId=" + hotelId + ", name=" + name + ", reviewTitle=" + reviewTitle
				+ ", reviewText=" + reviewText + ", sentiment=" + "]";
	}
}
