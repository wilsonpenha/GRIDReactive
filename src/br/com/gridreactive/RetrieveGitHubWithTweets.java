package br.com.gridreactive;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.gridreactive.exception.GridReactiveException;

/**
 * This class is intent to search for repositories into github and just display a json results output at command line
 * 
 * all setting are placed at setting.properties file into this package
 * 
 * @author Wilson M da Penha Jr
 * @version 1
 * 
 * Date: December, 23 of 2016
 */
public class RetrieveGitHubWithTweets {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.0.14) Gecko/2009082707 Firefox/3.0.14 (.NET CLR 3.5.30729)";

    private static Properties retrieveProps = new Properties();

    private static final String GITHUB_URL = "https://api.github.com/search/repositories?q=Reactive";
	private static final String HITHUB_HOST = "api.github.com";
	private static final String TWITTER_HOST = "api.twitter.com";
	private static final String TWITTER_AUTH_URL = "https://api.twitter.com/oauth2/token";
	private static final String TWITTER_URL = "https://api.twitter.com/1.1/search/tweets.json?q=";

	/**
	 * this main method won't receive any argument from the caller
	 * 
	 */
	public static void main(String[] args) throws Exception {

		// loading setting properties, you can change values while test it
		InputStream input = RetrieveGitHubWithTweets.class.getResourceAsStream("settings.properties");
		retrieveProps = System.getProperties();
		retrieveProps.load(input);
		
		JSONArray githubResults = requestGitHub(GITHUB_URL);

		int projCounts = Integer.valueOf(retrieveProps.getProperty("numberOfProjectsToList","10"));
		
		JSONObject githubProjects = new JSONObject();
		
		JSONObject githubProjectWithTweets = new JSONObject();
		
		String[] githubRetrieveFields = retrieveProps.getProperty("githubRetrieveFields").split(",");
		
		boolean allFields = (githubRetrieveFields[0]=="*");

		for (int i = 0; (i < projCounts && i < githubResults.length()); i++) {
			JSONObject githubProject = githubResults.getJSONObject(i);
			String term = URLEncoder.encode(githubProject.getString(retrieveProps.getProperty("searchTweetsByGithubField")), "UTF-8");

			if (term!=null && !term.trim().isEmpty()){
				// searching for tweets
				JSONObject jsonTweets = fetchTimelineTweet(TWITTER_URL+term, requestBearerToken(TWITTER_AUTH_URL));
				
				// adding the tweets results to the github project
				githubProject.put("recent_tweets", jsonTweets);
			}

			// if all fields, add it all to the github project
			if (allFields){
				githubProjectWithTweets = (JSONObject)githubResults.get(i);
			}else{
				// feed the github project with selected fields
				for (String field : githubRetrieveFields) {
					githubProjectWithTweets.put(field, githubProject.get(field));
				}
			}
			
			githubProjects.append(retrieveProps.getProperty("githubProjectsList"), githubProjectWithTweets);
		}
		
		// print the output of the retrived github project within tweets 
		System.out.println(githubProjects);

	}

	/**
	 * Method requestGitHub, will perform a search into github repositories looking for Reactive term, no option to search others
	 * 
	 *  @param endPointUrl - url from github server with the proper term
	 *  @return JSONArray - object containing all projects searched by Reactive term
	 *    
	 */
	private static JSONArray requestGitHub(String endPointUrl) throws Exception {
		HttpsURLConnection connection = null;
		JSONObject obj = null;
		JSONArray arGithub = new JSONArray();

		try {
			URL url = new URL(endPointUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", HITHUB_HOST);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

			StringBuffer response = new StringBuffer(IOUtils.toString(connection.getInputStream()));

			obj = new JSONObject(response.toString());

			if (obj != null) {
				arGithub = obj.getJSONArray("items");
			}
			return arGithub;
		} catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		} catch (JSONException e) {
			throw new JSONException("Unable to parse the json response due : " + e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}


	/**
	 * Encodes the consumer key and secret to create the basic authorization key
	 * 
	 * @param consumerKey
	 * @param consumerSecret
	 * @return String - with full encoded key
	 * 
	 */
	private static String encodeKeys(String consumerKey, String consumerSecret) {
		try {
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");

			String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
			byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
			return new String(encodedBytes);
		} catch (UnsupportedEncodingException e) {
			return new String();
		}
	}

	/**
	 * Constructs the request for requesting a bearer token and returns that token as a string
	 * 
	 * @param endPointUrl
	 * @return
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws IOException
	 * @throws GridReactiveException
	 * 
	 */
	private static String requestBearerToken(String endPointUrl) throws MalformedURLException, JSONException, IOException, GridReactiveException {
		HttpsURLConnection connection = null;
		// set consumerKey and consumerSecret values from setting.properties file
		String encodedCredentials = encodeKeys(retrieveProps.getProperty("consumerKey").trim(), retrieveProps.getProperty("consumerSecret").trim());

		try {
			URL url = new URL(endPointUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Host", TWITTER_HOST);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			connection.setRequestProperty("Content-Length", String.valueOf(encodedCredentials.length()));
			connection.setUseCaches(false);

			// write the params to the request
			writeRequest(connection, "grant_type=client_credentials");

			int responseCode = connection.getResponseCode();
			
			if (responseCode==401){
				throw new GridReactiveException("Invalid or expired token to access Search Tweets: ",String.valueOf(responseCode));
			}else if (responseCode==403){
				throw new GridReactiveException("Invalid user credentials to access Search Tweets: ",String.valueOf(responseCode));
			}else if (responseCode==500){
				throw new GridReactiveException("Unexpected internal error at Search Tweets server: ",String.valueOf(responseCode));
			}
			
			// Parse the JSON response into a JSON mapped object to fetch fields
			// from.
			StringBuffer response = new StringBuffer(IOUtils.toString(connection.getInputStream()));

			JSONObject obj = new JSONObject(response.toString());

			String tokenType = (String) obj.get("token_type");
			String token = (String) obj.get("access_token");

			// return the bearer code with valid token
			return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
			
		} catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		} catch (JSONException e) {
			throw new JSONException("Unable to parse the json response due : " + e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Fetches tweets from a given bearer token and url within the search term
	 * 
	 * @param endPointUrl
	 * @param bearerToken
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws JSONException
	 * @throws GridReactiveException
	 */
	private static JSONObject fetchTimelineTweet(String endPointUrl, String bearerToken) throws MalformedURLException, IOException, JSONException, GridReactiveException {

		HttpsURLConnection connection = null;
		JSONObject tweets = new JSONObject();
		JSONArray tweetsResults = null;
		JSONObject obj = null;
		
		try {
			URL url = new URL(endPointUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", TWITTER_HOST);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setUseCaches(false);

			int responseCode = connection.getResponseCode();
			
			if (responseCode==403){
				throw new GridReactiveException("Invalid user credentials to access Search Tweets: ",String.valueOf(responseCode));
			}else if (responseCode==500){
				throw new GridReactiveException("Unexpected internal error at Search Tweets server: ",String.valueOf(responseCode));
			}else if (responseCode==400){
				throw new GridReactiveException("Invalid or bad params send to Search Tweets server: ",String.valueOf(responseCode));
			}
			
			// Parse the JSON response into a JSON mapped object to fetch fields
			StringBuffer response = new StringBuffer(IOUtils.toString(connection.getInputStream()));

			obj = new JSONObject(response.toString());

			// json array with tweets
			tweetsResults = obj.getJSONArray(retrieveProps.getProperty("tweetsResults"));
			
			// limited number of tweets to show at github projects
			int tweetsCounts = Integer.valueOf(retrieveProps.getProperty("numberOfTweetsToList","10"));
			
			// selected fields to display from each tweet
			String[] tweetsRetrieveFields = retrieveProps.getProperty("tweetsRetrieveFields").split(",");

			boolean allFields = (tweetsRetrieveFields[0]=="*");
			
			// get tweets limited to results set
			for (int i = 0; (i < tweetsCounts && i < tweetsResults.length()); i++) {
				// to feed the tweets 
				JSONObject tweet = new JSONObject();

				// if all fields, add to the tweet
				if (allFields){
					tweet = (JSONObject)tweetsResults.get(i);
				}else{
					// getting only specific fields to add to the tweet
					for (String field : tweetsRetrieveFields) {
						tweet.append(field, ((JSONObject)tweetsResults.get(i)).get(field));
					}
				}
				// feed the tweets with the tweet
				tweets.append(retrieveProps.getProperty("tweetsResults"), tweet);
			}

			// when there are no results 
			if (tweetsResults.length()==0){
				tweets.put(retrieveProps.getProperty("tweetsResults"), tweetsResults);
			}

			boolean getTweetsMetadata = retrieveProps.getProperty("getTweetsMetadata").equals("true");

			// I want to decide to bring or not the search_metadata
			if (getTweetsMetadata){
				tweets.put("search_metadata", obj.get("search_metadata"));
			}
			obj = tweets;
			
			// if no tweets is found, return the response to fill the github project json
			return obj;
		} catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		} catch (JSONException e) {
			throw new JSONException("Unable to parse the json response due : " + e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Writes textBody value to the request connection
	 * 
	 * @param connection
	 * @param textBody
	 * @return
	 */
	private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();

			return true;
		} catch (IOException e) {
			return false;
		}
	}
}