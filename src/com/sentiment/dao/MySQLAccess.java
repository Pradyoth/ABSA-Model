package com.sentiment.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sentiment.modal.Hotel;
import com.sentiment.modal.HotelReview;

public class MySQLAccess {
	private static Connection connect = null;
	private static Connection connect1 = null;
	private static Statement statement = null;
	private static PreparedStatement preparedStatement = null;
	private static PreparedStatement preparedStatement1 = null;
	private static ResultSet resultSet = null;


	public List<HotelReview> getHotelReviews(int hotelId) {
		List<HotelReview> hotelReviews = new ArrayList<HotelReview>();
		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement("SELECT * FROM hotel_reviews where hotel_id=" + hotelId);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				HotelReview review = new HotelReview(resultSet.getInt("review_id"), resultSet.getInt("hotel_id"), resultSet.getString("Hotel_Name"), resultSet.getString("Review_Title"),
						resultSet.getString("Review_Text"));
				hotelReviews.add(review);
			}
			resultSet.close();
			preparedStatement.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotelReviews;
	}

	public List<String> getHotelAttributes() {
		List<String> list = new ArrayList<String>();
		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement("SELECT * FROM categories");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				list.add(resultSet.getString("categoryName"));
			}
			resultSet.close();
			preparedStatement.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void insertHotel() {
		List<String> lst = new ArrayList<String>();

		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement("SELECT DISTINCT(NAME) FROM hotel_reviews");
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				lst.add(resultSet.getString("Name").replaceAll("'", ""));
			}
			resultSet.close();
			preparedStatement.close();
			connect.close();

			for (int i = 0; i < lst.size(); i++) {
				connect = connectJDBC();
				preparedStatement = connect.prepareStatement("insert into  Hotel(hotelName) values (?)");

				preparedStatement.setString(1, lst.get(i).trim());
				preparedStatement.execute();
			}
			preparedStatement.close();
			connect.close();
			// SELECT * FROM hotel_reviews WHERE NAME LIKE '%Accord%Metropolitan%'

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Map<Integer, Hotel> getHotels() {
		Map<Integer, Hotel> hotelsMap = new HashMap<Integer, Hotel>();
		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement("SELECT * FROM hotel where hotel_id = 1");
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Hotel hotel = new Hotel(resultSet.getInt("hotel_id"), resultSet.getString("hotel_Name"));

				hotelsMap.put(hotel.getId(), hotel);
			}
			resultSet.close();
			preparedStatement.close();
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotelsMap;
	}

	public void updateHotelReviewsSentiment(Map<Integer, HotelReview> hashMap) {

		try {
			connect = connectJDBC();
			statement = connect.createStatement();

			Set entrySet = hashMap.entrySet();

			Iterator it = entrySet.iterator();

			int i = 0;
			while (it.hasNext()) {
				i++;
				Map.Entry me = (Map.Entry) it.next();
				HotelReview hotel = (HotelReview) me.getValue();
				String query = "update hotel_reviews set Sentiment = " + " where id ="
						+ hotel.getHotelId() + "";
				statement.addBatch(query);

				// execute and commit batch of 1000 queries
				if (i % 1000 == 0)
					statement.executeBatch();
			}

			statement.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connect.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean insertProductsPositiveProbabilty(String prob, String categoryName, int hotelId) {

		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement(
					"insert into  positivecategories(probability, categoryName, hotelId) values (?,?,?)");
			boolean row;

			preparedStatement.setString(1, prob);
			preparedStatement.setString(2, categoryName);
			preparedStatement.setInt(3, hotelId);
			row = preparedStatement.execute();

			// resultSet.close();
			preparedStatement.close();
			connect.close();

			return row;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean insertProductsNegativeProbabilty(String prob, String categoryName, int hotelId) {

		try {
			connect = connectJDBC();
			preparedStatement = connect.prepareStatement(
					"insert into  negativecategories(probability, categoryName, hotelId) values (?,?,?)");
			boolean row;

			preparedStatement.setString(1, prob);
			preparedStatement.setString(2, categoryName);
			preparedStatement.setInt(3, hotelId);
			row = preparedStatement.execute();

			preparedStatement.close();
			connect.close();

			return row;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Connection connectJDBC() {

		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return connection;
		}

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + "localhost" + ":" + 3306 + "/" + "nychotels", "root",
					"");

		} catch (SQLException e) {
			System.out.println("Connection Failed!:\n" + e.getMessage());
		}

		if (connection != null) {
			// System.out.println("SUCCESS!!!! You made it, take control your database
			// now!");
		} else {
			System.out.println("FAILURE! Failed to make connection!");
		}
		return connection;

	}

}
