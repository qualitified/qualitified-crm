package org.qualitified.crm.operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import com.google.api.services.calendar.model.EventReminder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.runtime.api.Framework;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;


@Operation(id=EventPublisher.ID, category=Constants.CAT_NOTIFICATION, label="Publish event on Google Calendar", description=" ")
public class EventPublisher {

    public static final String ID = "Qualitified.SynchronizeWithCalendar";
    private Log logger = LogFactory.getLog(Deploy.class);

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext context;

   /* @Param(name = "userEmailAddress", required = true)
    String userEmailAddress = "";

    @Param(name = "summary", required = true)
    String summary = "";

    @Param(name = "location", required = false)
    String location = "";

    @Param(name = "description", required = false)
    String description = "";

    @Param(name = "startDate", required = true)
    GregorianCalendar startDate = new GregorianCalendar();

    @Param(name = "endDate", required = true)
    GregorianCalendar endDate = new GregorianCalendar();

    @Param(name = "attendeeEmailAddress", required = false)
    String attendeeEmailAddress = "";*/

    @OperationMethod()
    public DocumentModel run(DocumentModel interactionDoc) {
        NuxeoPrincipal userPrincipal = session.getPrincipal();
        String currentUser = userPrincipal.getActingUser();

        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            //Credential credential = new GoogleCredential().setAccessToken(getAccessToken(userEmailAddress));

            OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googleCalendar");
            Credential storedCredential = serviceProvider.loadCredential(currentUser);

            Credential credential = new GoogleCredential.Builder()
                    .setClientSecrets(serviceProvider.getClientId(), serviceProvider.getClientSecret())
                    .setJsonFactory(JSON_FACTORY).setTransport(httpTransport).build()
                    .setRefreshToken(storedCredential.getRefreshToken()).setAccessToken(storedCredential.getAccessToken());

            // Initialize Calendar service with valid OAuth credentials
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential).build();

            StringBuilder bld = new StringBuilder();

            String responsibleDescription = "- Responsible: <br>"+"  • "+userPrincipal.getFirstName()+" "+userPrincipal.getLastName()+", "+userPrincipal.getEmail()+".<br>";
            bld.append(responsibleDescription);

            String[] persons = (String[]) interactionDoc.getPropertyValue("interaction:contact");

            if (persons != null) {
                String personsDescription = "- Participants: <br>";
                bld.append(personsDescription);

                for (String personId : persons) {
                    DocumentModel personDoc = session.getDocument(new IdRef(personId));
                    String personTitle = personDoc.getTitle();
                    String personEmail = (personDoc.getPropertyValue("person:email")!= null)
                            ? ", "+personDoc.getPropertyValue("person:email")
                            : "";
                    String personNumber = (personDoc.getPropertyValue("person:phoneNumber")!= null)
                            ? ", "+personDoc.getPropertyValue("person:phoneNumber")+". <br>"
                            : ". <br>";
                    String personsDetail = "  • "+personTitle+personEmail+personNumber;
                    bld.append(personsDetail);
                }
            }
            String desc = (String) interactionDoc.getPropertyValue("dc:description");
            if (desc != null) {
                String report = "- Report: "+ desc+".<br>";
                bld.append(report);
            }

            String eventDescription = bld.toString();

            GregorianCalendar interStartDateTime = (GregorianCalendar) interactionDoc.getPropertyValue("interaction:date");
            GregorianCalendar interEndDateTime = (GregorianCalendar) interactionDoc.getPropertyValue("custom:dateField1");

            Event event = new Event()
                    .setSummary(interactionDoc.getTitle())
                    .setDescription(eventDescription);

            DateTime startDateTime = new DateTime(interStartDateTime.getTime());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);
            event.setStart(start);

            DateTime endDateTime = new DateTime(interEndDateTime.getTime());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);
            event.setEnd(end);

            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).setSendUpdates("all").execute();
            logger.warn("Event created: "+ event.getHtmlLink());

        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        }
        return interactionDoc;
    }

    protected String getAccessToken(String user) {
        OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googleCalendar");
        Credential credential = serviceProvider.loadCredential(user);
        if (credential != null) {
            String accessToken = credential.getAccessToken();
            if (accessToken != null) {
                return accessToken;
            }
        }
        return null;
    }

}