import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * CalendarDemo
 * Demonstrates how to connect to the Google Calendar API and retrieve a list of
 * events from a Calendar.
 * Date Last Modified: 10/31/2017
 * @author Austin Walhof
 */
public class CalendarDemo {
	private static final String APPLICATION_NAME = "Google-API-Boilerplate";
	private static final String OAUTH_SECRETS = "client_secret.json"; // Added to .gitignore, so not public.
	private static final File OAUTH_DIRECTORY = new File(System.getProperty("user.home"),
			".credentials" + File.separator + APPLICATION_NAME); // Where credentials are cached
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY); // Permissions requested, full list can be found at https://developers.google.com/identity/protocols/googlescopes
	private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	private FileDataStoreFactory dataStoreFactory;
	private HttpTransport httpTransport;

	/**
	 * CalendarDemo
	 * Initializes an http connection and the cache directory.
	 */
	public CalendarDemo() {
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			dataStoreFactory = new FileDataStoreFactory(OAUTH_DIRECTORY);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ListDates
	 * Retrieves numEvents number of events from calendar with id calendarId. Prints
	 * the event title, description, and date to the console.
	 * @param String  calendarId to retrieve events from.
	 * @param Integer numEvents  to retrieve.
	 */
	public void listDates(String calendarId, int numEvents) {
		try {
			Calendar service = getCalendarService(); // Authorize with the Google API and get a handle to the Calendar service.
			DateTime now = new DateTime(System.currentTimeMillis()); // The current date and time, used to build the query.

			/*
			 * Request a list of events from the Calendar with id calendarId. Maximum of numEvents items, but it may be less if the Calendar is sparse. Only events that occur in the future. Ordered in ascending order by time. Ignore duplicate events. Execute the query.
			 */
			Events query = service.events().list(calendarId).setMaxResults(numEvents).setTimeMin(now).setOrderBy("startTime")
					.setSingleEvents(true).execute();

			List<Event> items = query.getItems(); // List of results from the executed query.
			if (items.size() == 0) {
				System.out.println("No events were found");
			} else {
				for (Event event : items) {
					DateTime start = event.getStart().getDateTime();
					if (start == null) {
						start = event.getStart().getDate(); // Ensure the start IS NOT null.
					}
					System.out.printf("%s %s (%s) %n", event.getSummary(), start, event.getDescription()); // Print title, start time, and description.
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Authorize
	 * Authenticates with the Google API using the client secrets file and 
	 * requested permissions.
	 * @return Credential that is preauthorized.
	 * @throws IOException if the client secrets file is not found.
	 */
	public Credential authorize() throws IOException {
		InputStreamReader in = new InputStreamReader(new FileInputStream(OAUTH_SECRETS)); // Used to parse the client secrets file.
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, in);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets,
				SCOPES).setDataStoreFactory(dataStoreFactory).setAccessType("offline").build(); // Request authorization and permissions.
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + OAUTH_DIRECTORY.getAbsolutePath());
		return credential;
	}

	/**
	 * GetCalendarService
	 * Authorize with the Google API and create a handle to the Calendar service.
	 * @return a handle to the Calendar service.
	 * @throws IOException if the client secrets file is not found.
	 */
	public Calendar getCalendarService() throws IOException {
		Credential credential = authorize();
		return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}

	public static void main(String[] args) throws IOException {
		CalendarDemo demo = new CalendarDemo();
		demo.listDates("mtu.edu_5fq1ohnvr8u8tu9p8rec395sg0@group.calendar.google.com", 10); // MTU Dining Calendar, 10 events.
	}
}
