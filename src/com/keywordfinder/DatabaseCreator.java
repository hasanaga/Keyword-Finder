package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHelper.Item;
import database.DatabaseHelper.Item.TYPE;
import database.DatabaseHelper.onQueryListener;
import dictionary.DatabaseConvertor;

public class DatabaseCreator {

	public static String sql_items = "INSERT INTO " + DatabaseConvertor.TABLE_ITEM_DATA + " (" 
			+ DatabaseConvertor.KEY_ID_ID + ", "
			+ DatabaseConvertor.KEY_ID_BODY + ") VALUES (?,?)";


public static String sql_search_items = "INSERT INTO " + DatabaseConvertor.TABLE_ITEM + " (" 
			//+ DatabaseConvertor.KEY_I_ID + "," 
			+ DatabaseConvertor.KEY_I_TITLE  + ", "
			+ "is_title"
			+ ") VALUES (?,?)";

public static String sql_search_items_info = "INSERT INTO " + DatabaseConvertor.TABLE_ITEM_INFO + " ("
			+ DatabaseConvertor.KEY_II_ITEM_ID + ", "
			+ DatabaseConvertor.KEY_II_TITLE + ", "
			+ DatabaseConvertor.KEY_II_CATEGORY
			+ ") VALUES (?, ?, ?); ";
	
	
	public static void main(String[] args) {

		final Connection writeConnection = DatabaseHelper.create();
		DatabaseHelper.restoreDbFromFile(writeConnection, "D:/Projects/translator/Dictamp/apps/src/Russianpoets/RussianVladimirVysotsky/assets/database");
		
		
		Connection openConnection = DatabaseHelper.open("D:/Projects/translator/Dictamp/apps/src/Russianpoets/RussianVladimirVysotsky/assets/new2.db");
		
		DatabaseHelper.executeSelectQuery(openConnection, "select * from new order by title2", new onQueryListener() {
			
			int id = 539;
			@Override
			public void onQuery(int counter, ResultSet resultSet) throws SQLException  {
				
				id++;	
				String title = resultSet.getString(3);
				String body = resultSet.getString(4);
					
				System.out.println(counter + " : " + title);
	
				List<Item> items = new ArrayList<>();
					
				items.add(new Item(1, TYPE.INTEGER, id));
				items.add(new Item(2, TYPE.BLOB, Helper.compress(body)));
				
				DatabaseHelper.insert(writeConnection, sql_items , items);
				
				items.clear();
				items.add(new Item(2, TYPE.TEXT, title));
				items.add(new Item(1, TYPE.TEXT, Helper.generateSearchString(title)));
				
				DatabaseHelper.insert(writeConnection, sql_search_items , items);
				
			}
		});
		
		

		DatabaseHelper.backup(writeConnection, "D:/Projects/translator/Dictamp/apps/src/Russianpoets/RussianVladimirVysotsky/assets/result.db");
		DatabaseHelper.close(writeConnection);
		
		
		
	}

	
}
