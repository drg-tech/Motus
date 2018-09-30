import java.io.IOException;
import java.util.HashMap;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
/*
 * Project Motus - Crawler to grab initial event/things to do data around the city
 * Dustin Gunter - Christian Whiles 9/1/2018
 */
public class MotusCrawler {
	
	HashMap<String,String> crawlSchema = new HashMap<String,String>();
	//TODO: change this to support the PDXEvent object so it can handle the crawling much easier if the structure changes
	
	private class PDXEvent {
		/*
		 * Data is in strings since that is what is being pulled from the site. 
		 * Data can be transformed later to suit needs
		 */
		String date_data;
		String title_data;
		String price_data;
		String location_data;
		String relatedImageUrl_data;
		String description_data;
		String tag_data;
		String indoor_data;
		boolean reoccuring;
		
		public PDXEvent(){
			//initial all variables
			date_data = "";
			title_data = "";
			price_data = "";
			location_data = "";
			relatedImageUrl_data = "";
			description_data = "";
			tag_data = "";
			indoor_data = "";
			reoccuring = false;
		}
		public PDXEvent(String date, String title, String price, String location, String relatedImageUrl, String description, String tags, String indoor, boolean reocc){
			//overloaded to support multiple usecases
			date_data = date;
			title_data = title;
			price_data = price;
			location_data = location;
			relatedImageUrl_data = relatedImageUrl;
			description_data = description;
			tag_data = tags;
			indoor_data = indoor;
			reoccuring = reocc;
		}
		//Setter methods
		public void setReoccuring(boolean value){
			reoccuring = value;
		}
		public void setDate(String data){
			date_data = data;
		}
		public void setTitle(String data){
			title_data = data;
		}
		public void setPrice(String data){
			price_data = data;
		}
		public void setLocation(String data){
			location_data = data;
		}
		public void setRelatedImageUrl(String data){
			relatedImageUrl_data = data;
		}
		public void setDescription(String data){
			description_data = data;
		}
		public void setTags(String data){
			tag_data = data;
		}
		public void setIndoor(String data){
			indoor_data = data;
		}
		//Getter Methods
		public boolean getReoccuring(){
			return reoccuring;
		}
		public String getDate(){
			return date_data;
		}
		public String getTitle(){
			return title_data;
		}
		public String getPrice(){
			return price_data;
		}
		public String getLocation(){
			return location_data;
		}
		public String getRelatedImageUrl(){
			return relatedImageUrl_data;
		}
		public String getDescription(){
			return description_data;
		}
		public String getTags(){
			return tag_data;
		}
		public String getIndoor(){
			return indoor_data;
		}
		public String toString(){
			return( getDate() + "|"
					+ getTitle() + "|"
					+ getPrice() + "|"
					+ getLocation() + "|"
					+ getRelatedImageUrl() + "|"
					+ getDescription() + "|"
					//+ getTags() + "|"
					+ getIndoor() + "|");
		}
	}
	
	public void crawlUrl(String url) throws IOException{
		/*
		 * Uses url to grab event data and outputs a Pojo for easy manipulation
		 */
		try{
			Document doc = Jsoup.connect(url).referrer("http://www.google.com").userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US;   rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(0).get();
			String selectors[] = crawlSchema.get(getRoot(url)).split("\\|");
			//date|title|price|location|relatedImageUrl|description|tags|indoor
			Element date = doc.select(selectors[0]).first();
			Element title = doc.select(selectors[1]).first();	
			Element price = doc.select(selectors[2]).first();
			Element location = doc.select(selectors[3]).first(); 
			Element relatedImageUrl = doc.select(selectors[4]).first();
			Element description = doc.select(selectors[5]).first();		
			Elements tags = doc.select(selectors[6]); 
			Element indoor;
			/*if (selectors[7] != ""){
				indoor = doc.select(selectors[7]).first();
			}
			else{
				indoor = null;
				//will require manual update or some sort of text parser to determine if its indoors or not.
			}*/
			PrintWriter writer = null;
			try{
				PDXEvent event = new PDXEvent(
						date.text(),
						title.text(),
						price.text(),
						location.text(),
						relatedImageUrl.attr("src"),
						description.text().replaceAll("<span>", "").replaceAll("</span>", "").trim(),
						tags.text().replaceAll("<span>", "").replaceAll("</span>", "").trim(),
						"",
						false);
				writer = new PrintWriter(new FileOutputStream( new File("event_data.csv"), true));
				
				System.out.println(event.toString());
				writer.println(event.toString());
				writer.close();
			}
			catch(NullPointerException e){
				System.out.println("Issue with data on this page");
			}
			
			/*TODO: add data to database
			boolean upload = uploadToDatabase(event);
			
			if(upload){
				System.out.println("The upload should have been successful");
			}
			else{
				System.out.println("Upload unsuccessful");
			} */
		}
		catch(HttpStatusException e){
			System.out.println("This page returned a 404 error");
		}
		
				
		
	}
	public void buildSchema(){
		//second string is pipe delimited collection of selectors that follows this order:
		//date|title|price|location|relatedImageUrl|description|tags|indoor
		
		crawlSchema.put("eventbrite", "time.clrfix|"
				+ "h1.listing-hero-title|"
				+ ".js-display-price|"
				+ "#event-page > main > div.js-hidden-when-expired.event-listing.event-listing--has-image > div.g-grid.g-grid--page-margin-manual > div > section.listing-info.clrfix > div.listing-info__body.l-sm-pad-vert-0.l-sm-pad-vert-6.clrfix.g-group.g-group--page-margin-reset > div > div > div.g-cell.g-cell-12-12.g-cell-md-4-12.g-offset-md-1-12.g-cell--no-gutters.l-lg-pad-left-6 > div.event-details.hide-small > div:nth-child(4)|"
				+ "#event-page > main > div.js-hidden-when-expired.event-listing.event-listing--has-image > div.g-grid.g-grid--page-margin-manual > div > div.listing-hero-details__main-container.fx--fade-in.fx--delay-4 > div > div.g-cell.g-cell-1-1.g-cell-lg-8-12.g-cell--no-gutters.listing-hero--image-container > div.listing-hero.listing-hero--bkg.clrfix.fx--delay-6.fx--fade-in > picture > source|"
				+ "div.js-xd-read-more-contents|"
				+ "a.badge > span|"
				+ "null");
		crawlSchema.put("portlandmercury", "#event-more-info > div > div:nth-child(2) > div.event-times > ul > li > span|"
				+ "#event-header > h1|"
				+ "#event-sub-head > div > div|"
				+ "#event-venue-info > p|"
				+ "#event-slideshow > div > img|"
				+ "#event-description > p|"
				+ "null");
		crawlSchema.put("wweek", "#sidepanelEvent > div > div > div.single-listing-details-box.row > div.row > div > div.single-date|"
				+ "#sidepanelEvent > div > div > h1|"
				+ "#sidepanelEvent > div > div > div.single-restrictions|"
				+ "#sidepanelEvent > div > div > div.venue-details.row > div.venue-content > div:nth-child(2)|"
				+ "#EventSidepanel__tns-item0|"
				+ "#sidepanelEvent > div > div > div.single-summary|"
				+ "null");
		
		
	}
	public boolean uploadToDatabase(PDXEvent event){
		String url = "jdbc:mysql://11.70.0.51:3306/Events";
        String user = "dustingunter"; 
        String password = "Pangia88!";
 
 
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
 
            String sql = "INSERT INTO Events (event_title,event_date,event_description,indoor_outdoor,reoccuring,events_created_by) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, event.getTitle());
            statement.setString(2, event.getDate());
            //statement.setString(3, "TODO"); //TODO: Update with event data, might have to remove this if most time data is included in the date
            //statement.setString(3, event.getLocation());
            statement.setString(3, event.getDescription());
            //statement.setString(6, "PLACEHOLDER FOR IMAGE URL"); // TODO: update with a call to upload the images first and then update the ids here
            statement.setString(4, event.getIndoor());
            //statement.setString(8, "PLACEHOLDER FOR CATEGORY IDS"); // TODO: Update category table so the references make sense, and then update these.
            statement.setBoolean(5, event.getReoccuring());
            //statement.setString(10, "TODO");
            //statement.setString(11, "PLACEHODLER FOR PARTICIPANTS"); //TODO: replace with call to another table to get participants? or just upload as blank
            statement.setString(6, "A Happening Admin");
            
            int row = statement.executeUpdate();
            System.out.println(row);
            if (row > 0) {
                System.out.println("Event uploaded....");
                conn.close();
                return true;
            }
            else{
            	return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

	public static String getRoot(String url){
		String tokens[] = url.replace("www.","").replace(".com","").split("/");
		
		return tokens[2];
	}
	public static void main(String[] args){
		MotusCrawler crawler = new MotusCrawler();
		crawler.buildSchema();
		try{
			BufferedReader reader = new BufferedReader(new FileReader("motus_urls.txt"));
			String line = "";
			while((line = reader.readLine()) != null){
				crawler.crawlUrl(line);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
}
