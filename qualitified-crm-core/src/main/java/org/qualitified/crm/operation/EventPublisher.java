package org.qualitified.crm.operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.calendar.model.*;
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
import org.nuxeo.ecm.core.event.EventServiceAdmin;
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


@Operation(id=EventPublisher.ID, category=Constants.CAT_NOTIFICATION, label="Publish event on Google Calendar", description=" ")
public class EventPublisher {

    public static final String ID = "Qualitified.SynchronizeWithCalendar";
    private Log logger = LogFactory.getLog(Deploy.class);

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext context;

    @OperationMethod()
    public DocumentModel run(DocumentModel interactionDoc) {
        NuxeoPrincipal userPrincipal = session.getPrincipal();
        String currentUser = userPrincipal.getActingUser();
        EventServiceAdmin eventServiceAdmin = Framework.getService(EventServiceAdmin.class);

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
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName("Qualitified-CCM").build();

            StringBuilder bld = new StringBuilder();

            String responsibleDescription = "- Responsible: <br>" + "  • " + userPrincipal.getFirstName() + " " + userPrincipal.getLastName() + ", " + userPrincipal.getEmail() + ".<br>";
            bld.append(responsibleDescription);

            String[] persons = (String[]) interactionDoc.getPropertyValue("interaction:contact");

            if (persons != null) {
                String personsDescription = "- Participants: <br>";
                bld.append(personsDescription);

                for (String personId : persons) {
                    DocumentModel personDoc = session.getDocument(new IdRef(personId));
                    String personTitle = personDoc.getTitle();
                    String personEmail = (personDoc.getPropertyValue("person:email") != null)
                            ? ", " + personDoc.getPropertyValue("person:email")
                            : "";
                    String personNumber = (personDoc.getPropertyValue("person:phoneNumber") != null)
                            ? ", " + personDoc.getPropertyValue("person:phoneNumber") + ". <br>"
                            : ". <br>";
                    String personsDetail = "  • " + personTitle + personEmail + personNumber;
                    bld.append(personsDetail);
                }
            }
            String desc = (String) interactionDoc.getPropertyValue("dc:description");
            if (desc != null) {
                String report = "- Report: " + desc + ".<br>";
                bld.append(report);
            }

            String eventDescription = bld.toString();

            GregorianCalendar interStartDateTime = (GregorianCalendar) interactionDoc.getPropertyValue("interaction:date");
            GregorianCalendar interEndDateTime = (GregorianCalendar) interStartDateTime.clone();
            String eventDuration = (String) interactionDoc.getPropertyValue("custom:stringField3");

            interEndDateTime.add(GregorianCalendar.MINUTE, Integer.parseInt(eventDuration));
            String interEventId = (String) interactionDoc.getPropertyValue("custom:stringField1");

            DateTime startDateTime = new DateTime(interStartDateTime.getTime());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);

            DateTime endDateTime = new DateTime(interEndDateTime.getTime());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);
            String calendarId = "primary";

            if (interEventId == null) {
                // do insert event
                Event event = new Event()
                        .setSummary(interactionDoc.getTitle())
                        .setDescription(eventDescription)
                        .setStart(start)
                        .setEnd(end);

                EventReminder[] reminderOverrides = new EventReminder[]{
                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                        new EventReminder().setMethod("popup").setMinutes(10),
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);

                event = service.events().insert(calendarId, event).setSendUpdates("all").execute();
                interactionDoc.setPropertyValue("custom:stringField1", event.getId());
                interactionDoc.setPropertyValue("custom:stringField2", event.getEtag());
                // disable publish event listener
                //interactionDoc.putContextData("custom:booleanField2", Boolean.TRUE);
                logger.warn("Event created: " + event.getHtmlLink());

            } else {
                // do update event
                // Retrieve the event from the API
                Event event = service.events().get(calendarId, interEventId).execute();
                // Make changes
                event.setSummary(interactionDoc.getTitle())
                        .setDescription(eventDescription)
                        .setStart(start)
                        .setEnd(end);
                // Update the event
                Event updatedEvent = service.events().update(calendarId, event.getId(), event).execute();
                interactionDoc.setPropertyValue("custom:stringField1", updatedEvent.getId());
                interactionDoc.setPropertyValue("custom:stringField2", updatedEvent.getEtag());
                // disable publish event listener
                //interactionDoc.putContextData("custom:booleanField2", Boolean.TRUE);
                logger.warn("Event updated: " + event.getHtmlLink());
            }
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",false);
            session.saveDocument(interactionDoc);

        } catch (GoogleJsonResponseException ge){
            if (ge.getStatusCode() == 401) {
               logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        } finally {
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",true);
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