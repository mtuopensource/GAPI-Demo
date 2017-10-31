import java.io.*;
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

public class Demo {
	private static final String APPLICATION_NAME = "Open Source Club Calendar Demo";
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);
	private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/open-source-calendar-demo");
	private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	private FileDataStoreFactory dataStoreFactory;
	private HttpTransport httpTransport;

	public Demo() {
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	public void listDates(String calendarId, int numEvents) {
		try {
			Calendar service = getCalendarService();
			DateTime now = new DateTime(System.currentTimeMillis());
			Events eventsList = service.events().list(calendarId).setMaxResults(numEvents).setTimeMin(now)
					.setOrderBy("startTime").setSingleEvents(true).execute();
			List<Event> items = eventsList.getItems();
			if(items.size() == 0) {
				System.out.println("No events were found");
			} else {
				for(Event event : items) {
					DateTime start = event.getStart().getDateTime();
					if(start == null) {
						start = event.getStart().getDate();
					}
					System.out.printf("%s (%s) : %s %n", event.getSummary(), event.getDescription(), start);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public Credential authorize() throws IOException {
		InputStream in = new FileInputStream("client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES).setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	public Calendar getCalendarService() throws IOException {
		Credential credential = authorize();
		return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}

	public static void main(String[] args) throws IOException {
		Demo demo = new Demo();
		demo.listDates("mtu.edu_5fq1ohnvr8u8tu9p8rec395sg0@group.calendar.google.com", 10);
	}
}
