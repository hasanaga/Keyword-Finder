package com.keywordfinder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.keywordfinder.DatabaseHelper.Item;
import com.keywordfinder.DatabaseHelper.ItemList;
import com.keywordfinder.DatabaseHelper.onQueryListener;

public class GooglePlay {

	static String searchKeyword;
	static Connection connection;

	static String keyword;
	static boolean showSentencesSource;
	static boolean showAppSource;

	public static void main(String[] args) throws Exception {


//		String data = "asa asas asa.sas";
//		String[] arr1 = data.split("\\.");
//
//		System.out.println(arr1.length);
//		System.exit(23);


		keyword = "apk share";
		showAppSource = false;
		showSentencesSource = false;


		//Connection connection = DatabaseHelper.create();
		//DatabaseHelper.restoreDbFromFile(connection, "google.db");

		connection = DatabaseHelper.open("google.db");
		createTable();


		grabApps(keyword);


		final Set<String> uselessWordSet = new HashSet<>();

		DatabaseHelper.executeSelectQuery(connection, "select * from useless_words", new onQueryListener() {

			@Override
			public void onQuery(int counter, ResultSet resultSet) throws Exception {
				String word = resultSet.getString(2);
				uselessWordSet.add(word);
			}
		});


		System.out.println("Useless word set size: " + uselessWordSet.size());


		final List<Word> wordsList = new ArrayList<>();


		DatabaseHelper.executeSelectQuery(connection, "select * from data where search_category = '" + keyword + "'", new onQueryListener() {

			@Override
			public void onQuery(int counter, ResultSet resultSet) throws Exception {

				int id = resultSet.getInt(1);
				String title = resultSet.getString(2);
				String packageid = resultSet.getString(3);
				String shortDescription = resultSet.getString(4);
				String description = resultSet.getString(5);


				String data = title + ". " + shortDescription + ".  " + description;

				//data = shortDescription;

				data = data.replace(",", " ");
				//data = data.replace(".", " ");
				data = data.replace("\"", " ");
				data = data.replace("(", " ");
				data = data.replace(")", " ");
				data = data.replace("*", " ");
				data = data.replace("-", " ");
				data = data.replace("?", " ");
				data = data.replace("\\?", " ");
				data = data.replace("'", " ");
				data = data.replace(":", " ");
				data = data.replace("/", " ");
				data = data.replace("!", " ");
				data = data.replace(")", " ");
				data = data.replace("\t", " ");
				data = data.replace("–", " ");
				data = data.replace("-", " ");
				data = data.replace("\n\r", ".");
				data = data.replace("\n", ".");
				data = data.replace("&", " ");
				//data = data.replace("\t", " ");
				data = data.replace("  ", " ");
				data = data.replace(" ", " ");

				data = data.trim();


				String[] sentences = data.split("\\.");
				for (int j = 0; j < sentences.length; j++) {

					String sentence = sentences[j];
					sentence = sentence.replace("  ", " ");
					sentence = sentence.replace("  ", " ");


					sentence = sentence.trim();

					if (sentence.isEmpty()) continue;
					String[] words = sentence.split(" ");

					for (int i = 1; i < 5; i++) {


						//1 defe
						for (int k = 0; k < words.length; k++) {


							if (k + i > words.length) break;

							String word = "";
							for (int z = 0; z < i; z++) {
								word += words[k + z] + " ";
							}

							word = word.toLowerCase();

							Word wordObject = new Word(word);
							KeywordSource keywordSource = new KeywordSource(packageid, sentence);
							wordObject.addSource(keywordSource);

							if (wordsList.contains(wordObject)) {

								wordsList.get(wordsList.indexOf(wordObject)).count++;
								wordsList.get(wordsList.indexOf(wordObject)).addSource(keywordSource);

							} else {

								wordsList.add(wordObject);
							}




						}


					}


				}

			}
		});


		wordsList.sort((o1, o2) -> {
			return Integer.compare(o2.count, o1.count );
		});


		System.out.print("\n\nFounded keywords: \n");

		for (Word word : wordsList) {

			boolean f = true;

			//f &= word.count > 1;
			f &= word.wordCount() > 2;

			if (f) {
				System.out.println(word.count + ":" + word.word.trim() + ":");

				int i=0;
				for(KeywordSource keywordSource: word.keywordSources){

					i++;
					String text = "";
					if(showAppSource) text = keywordSource.packageId + ". \t";
					if(showSentencesSource) text += keywordSource.sentence;

					if(!text.isEmpty())
						System.out.println("\t\t" +i+". "+ text);
				}
			}
		}


	}


	public static void createTable(){


		DatabaseHelper.executeQuery(connection, "CREATE TABLE IF NOT EXISTS `data` (" +
				"`id` INTEGER, " +
				"`title` TEXT, " +
				"`package_id` TEXT, " +
				"`short_description` TEXT, " +
				"`description` TEXT, " +
				"`search_category` TEXT, " +
				"UNIQUE (package_id, search_category)" +
				"PRIMARY KEY(`id`)" +
				");");


		//DatabaseHelper.executeQuery(connection, "delete from data");
		DatabaseHelper.executeQuery(connection,"CREATE TABLE IF NOT EXISTS `useless_words` (" +
				"`id` INTEGER," +
				"`text` TEXT UNIQUE ON CONFLICT IGNORE," +
				"PRIMARY KEY(`id`)" +
				");");

		DatabaseHelper.executeQuery(connection,"INSERT INTO `useless_words`(`text`) VALUES " +
				"('')," +
				" ('and')," +
				" ('you')," +
				" ('your')," +
				" ('the')," +
				" ('can')," +
				" ('with')," +
				" ('or')," +
				" ('a')," +
				" ('an')," +
				" ('of')," +
				" ('in')," +
				" ('is')," +
				" ('on')," +
				" ('to')," +
				" ('this')," +
				" ('by')," +
				" ('at')," +
				" ('one')," +
				//" ('.')," +
				" ('it')," +
				" ('are')," +
				" ('as')," +
				" ('etc');");

	}
	
	public static void grabApps(String searchKeyword)  throws Exception{
		

		 
		String url = "https://play.google.com/store/search?q=%s&c=apps&hl=en";
		
		Document document = Jsoup.connect(String.format(url, searchKeyword.replace(" ", "%20"))).userAgent("Mozilla 1.1")
				.ignoreContentType(true)
				.maxBodySize(0)
				.referrer("https://play.google.com")
				.timeout(0)
				.get();
		
		
		Elements apps = document.select("div.apps");


		for(int i=0; i<apps.size(); i++) {

			Element app = apps.get(i);

			if(app.dataset().get("docid") == null) continue;

			String packageId = app.dataset().get("docid");
			String title = app.select("a.title").text();
			String shortDescription = app.select("div.description").text();


			final int[] count = new int[1];
			count[0] = 0;
			DatabaseHelper.executeSelectQuery(connection, "select * from data where package_id = \"" + packageId + "\" and search_category = \"" + searchKeyword + "\"", new onQueryListener() {
				@Override
				public void onQuery(int counter, ResultSet resultSet) throws Exception {

					count[0] = counter;
				}
			});



			System.out.println(i + ". \t" + title);

			if(count[0] > 0) continue;

			Document document2 = Jsoup.connect(String.format("https://play.google.com/store/apps/details?id=%s&hl=en", packageId)).userAgent("Mozilla 1.1")
					.ignoreContentType(true)
					.maxBodySize(0)
					.referrer("https://play.google.com")
					.timeout(0)
					.get();



			String description = document2.select("div.text-body").text();

			//System.out.println(document2.select("meta"));

			Elements metaTags = document2.getElementsByTag("meta");

			for (Element metaTag : metaTags) {
				String content = metaTag.attr("content");
				String name = metaTag.attr("name");

				if(name.equals("description")){
					description = content;
				}

			}


			DatabaseHelper.insert(connection, "insert into data(title, package_id, short_description, description, search_category) values(?,?,?,?,?)",
					ItemList.create()
						.add(new Item(1, Item.TYPE.TEXT, title))
						.add(new Item(2, Item.TYPE.TEXT, packageId))
						.add(new Item(3, Item.TYPE.TEXT, shortDescription))
						.add(new Item(4, Item.TYPE.TEXT, description))
						.add(new Item(5, Item.TYPE.TEXT, searchKeyword)).build());


			//System.out.println(description);


		}
		
		
		System.out.println(apps.size());
		
	}
	
	
	
	public static class Word{
		String word;
		int count;
		List<KeywordSource> keywordSources;

		public Word(String word) {
			this.word = word;
			this.count = 1;
			keywordSources = new ArrayList<>();
		}

		public void addSource(KeywordSource keywordSource){
			keywordSources.add(keywordSource);
		}


		public int wordCount(){
			return  word.trim().split(" ").length;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((word == null) ? 0 : word.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Word other = (Word) obj;
			if (word == null) {
				if (other.word != null)
					return false;
			} else if (!word.equals(other.word))
				return false;
			return true;
		}
		
		
	}

	public static class KeywordSource{
		public String packageId;
		public String sentence;

		public KeywordSource(String packageId, String sentence){
			this.packageId = packageId;
			this.sentence = sentence;
		}
	}
	
}
