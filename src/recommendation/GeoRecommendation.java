package recommendation;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		
		DBConnection dbConnection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = dbConnection.getFavoriteItemIds(userId);
		
		Map<String, Integer> allCatagories = new HashMap<>();
		
		for (String itemId : favoritedItemIds) {
			Set<String> catagories = dbConnection.getCategories(itemId);
			for (String catagory : catagories) {
				allCatagories.put(catagory, allCatagories.getOrDefault(catagory, 0) + 1);
			}
		}
		
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCatagories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		
		Set<String> visitedItemIds = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = dbConnection.searchItems(lat, lon, category.getKey());
			
			for (Item item : items) {
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		
		dbConnection.close();
		return recommendedItems;
	}
}
