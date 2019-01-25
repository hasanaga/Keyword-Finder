package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.SQLiteConfig;

import database.DatabaseHelper.Item;
import database.DatabaseHelper.Item.TYPE;

public class DatabaseHelper {


	
	public static Connection open(String db){
		

		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + db);
			System.out.println("Opened database successfully");

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(107);
		}
		
		return c;
	}
	
	public static void restoreDbFromFile(Connection c, String file){
		
		try {
			
			File tmpFile = new File(file); 
			//if(tmpFile.exists()){
				System.out.println("Path:" + tmpFile.getAbsolutePath());
				Statement stmt = c.createStatement();
		        stmt.executeUpdate("restore from " + tmpFile.getAbsolutePath());
		        stmt.close();			
			//}
		
		} catch (SQLException e) {

			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(106);
		}
		
	}
	
	public static void backup(Connection c, String file){
		try{
		
			Statement stmt = c.createStatement();	
			File tmpFile = new File(file); 
	        System.out.println("backup start: " + tmpFile.getAbsolutePath());
	        stmt.executeUpdate("backup to " + tmpFile.getAbsolutePath());
	        stmt.close();
		} catch (SQLException e) {

			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(105);
		}
	}
	
	public static void close(Connection c){
		
		try {
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(104);
		}
	}
	
	
	
	public static Connection create(){
		
		Connection c = null;
		Statement stmt = null;
		try {
			
			SQLiteConfig config = new SQLiteConfig();
			config.setSharedCache(false);
			config.enableRecursiveTriggers(false);
		
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite::memory:");//" + db + ".db");
				
			System.out.println("Created database successfully");
			stmt = c.createStatement();
			stmt.execute("PRAGMA encoding=\"UTF-8\"");
			stmt.close();

		} catch (Exception e) {
			
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(103);
		}
		
		return c;
	}
	
	
	public static void executeQuery(Connection c, String query){
		
		Statement stmt = null;
		try {
		
			stmt = c.createStatement();
			stmt.executeUpdate(query);
			stmt.close();

			System.out.println(query);

		} catch (Exception e) {
			
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(102);
		}
		
	}
	
	
	
	public static void update(Connection c, String query, List<Item> items){
		
		PreparedStatement stmt;
		try {
			
			stmt = c.prepareStatement(query);

			
			for (Item item : items) {
				
				if(item.type == TYPE.INTEGER){
					
					stmt.setInt(item.columnIndex, item.dataInt);
				
				}else if(item.type == TYPE.FLOAT){
					
					stmt.setFloat(item.columnIndex, item.dataFloat);

				}else if(item.type == TYPE.TEXT){
					
					stmt.setString(item.columnIndex, item.dataString);
				
				}else if(item.type == TYPE.BLOB){
					
					stmt.setBytes(item.columnIndex, item.dataBytes);
				
				}
			}
			
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {

			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(102);
		}
		
	}
	
	public static void insertBatch(Connection c, String query, List<ItemList> itemLists){
	
		
		for(ItemList itemList : itemLists) {
			
			insert(c, query, itemList.build());
		}
		
	}
	
	public static long insert(Connection c, String query, List<Item> items){
		
		
		PreparedStatement stmt;
		ResultSet generatedKeys = null;
		long generatedKey = -1;
		
		try {
			
			stmt = c.prepareStatement(query);

			
			for (Item item : items) {
				
				if(item.type == TYPE.INTEGER){
					
					stmt.setInt(item.columnIndex, item.dataInt);
				
				}else if(item.type == TYPE.FLOAT){
					
					stmt.setFloat(item.columnIndex, item.dataFloat);

				}else if(item.type == TYPE.TEXT){
					
					stmt.setString(item.columnIndex, item.dataString);
				
				}else if(item.type == TYPE.BLOB){
					
					stmt.setBytes(item.columnIndex, item.dataBytes);
				
				}
			}
			
						
			stmt.execute();
			
		    try {
		    	generatedKeys = stmt.getGeneratedKeys();
		    	   if (generatedKeys.next()) {		    		 
		    		   generatedKey = generatedKeys.getLong(1);
		            }
		    	
		    } catch (Exception e) {
		        generatedKey =  -1;
		    }
		    
			stmt.close();

		} catch (SQLException e) {

			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		
		return generatedKey;
		
	}
	
	
	public static class Item{
		
		public enum TYPE{
			INTEGER, TEXT, BLOB, FLOAT
		}
		
		public int columnIndex;
		public TYPE type;
		
		public int dataInt;
		public float dataFloat;
		public String dataString;
		public byte[] dataBytes;
		
		public Item(int columnIndex, TYPE type, float data) {
		
			this.columnIndex = columnIndex;
			this.type = type;
			this.dataFloat = data;
		}
		
		public Item(int columnIndex, TYPE type, int data) {
			
			this.columnIndex = columnIndex;
			this.type = type;
			this.dataInt = data;
		}
		
		public Item(int columnIndex, TYPE type, String data) {
			
			this.columnIndex = columnIndex;
			this.type = type;
			this.dataString = data;
		}		
		
		public Item(int columnIndex, TYPE type, byte[] data) {
			
			this.columnIndex = columnIndex;
			this.type = type;
			this.dataBytes = data;
		}
		
		public static List<Item> createList() {
			List<Item> list = new ArrayList<>();
			
			return list;
		}
		
	}
	
	
	public static class ItemList{
		private  List<Item> list;
		
		public ItemList(){
			list = new ArrayList<>();
		}
		
		public static ItemList create() {
			
			return new ItemList();
		}
		
		public ItemList add(Item item) {
			
			list.add(item);
			return this;
		}
		
		public List<Item> build(){
			return list;
		}
		
	}
	
	
	public static void executeSelectQuery(Connection c, String query, onQueryListener onQueryListener){
		
		try{
		
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int counter = 0;
			while (rs.next()) {
				counter++;	
				onQueryListener.onQuery(counter, rs);
			}
		
			rs.close();
			stmt.close();
			
		}catch (Exception e) {

			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(101);
		}
	}
	
	public interface onQueryListener{
		void onQuery(int counter, ResultSet resultSet) throws Exception;
	}
	
	
}
