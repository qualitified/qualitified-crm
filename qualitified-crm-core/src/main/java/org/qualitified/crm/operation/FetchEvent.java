package org.qualitified.crm.operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.calendar.model.*;
import jdk.jfr.internal.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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
import org.nuxeo.ecm.platform.usermanager.UserManager;
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
import com.google.api.services.calendar.model.Events;



@Operation(id=FetchEvent.ID, category=Constants.CAT_NOTIFICATION, label="fetch event from Google Calendar", description=" ")
public class FetchEvent {

    public static final String ID = "Qualitified.FetchFromCalendar";
    private Log logger = LogFactory.getLog(FetchEvent.class);

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext context;

    @Param(name = "calendarId")
    protected String calendarId;

    @OperationMethod()
    public void run(DocumentModel syncInteraction) throws GeneralSecurityException, IOException {

        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport;
        EventServiceAdmin eventServiceAdmin = Framework.getService(EventServiceAdmin.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String[] responsible = (String[]) syncInteraction.getPropertyValue("interaction:responsible");
        String currentUser = responsible[0];
        String eventId = (String) syncInteraction.getPropertyValue("custom:stringField1");
        String eventTag = (String) syncInteraction.getPropertyValue("custom:stringField2");

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googleCalendar");
            Credential storedCredential = serviceProvider.loadCredential(currentUser);

            Credential credential = new GoogleCredential.Builder()
                    .setClientSecrets(serviceProvider.getClientId(), serviceProvider.getClientSecret())
                    .setJsonFactory(JSON_FACTORY).setTransport(httpTransport).build()
                    .setRefreshToken(storedCredential.getRefreshToken()).setAccessToken(storedCredential.getAccessToken());

            // Initialize Calendar service with valid OAuth credentials
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName("Qualitified-CCM").build();
            String calendarId = "primary";
            // disable publish event listener
            // syncInteraction.putContextData("custom:booleanField2", Boolean.TRUE);
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",false);

            // Retrieve an event
            Event event = service.events().get(calendarId, eventId).execute();
            if (!eventTag.equals(event.getEtag())) {
                long eventStart = event.getStart().getDateTime().getValue();
                long eventEnd = event.getEnd().getDateTime().getValue();
                long duration = (eventEnd - eventStart) / 60000;
                GregorianCalendar startDate = new GregorianCalendar();
                startDate.setTimeInMillis(eventStart);
                /*GregorianCalendar endDate = new GregorianCalendar();
                startDate.setTimeInMillis(eventEnd);*/
                syncInteraction.setPropertyValue("dc:title", event.getSummary());
                syncInteraction.setPropertyValue("interaction:date", startDate);
                syncInteraction.setPropertyValue("custom:stringField3", Long.toString(duration));
                syncInteraction.setPropertyValue("custom:stringField2", event.getEtag());

                session.saveDocument(syncInteraction);
                logger.warn("Event updated successfully!");

            } else {
                logger.warn("Event is already up to date!");
            }

        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 401) {
                logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        } finally {
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",true);
        }
    }
    @OperationMethod()
    public String run() {
        NuxeoPrincipal userPrincipal = session.getPrincipal();
        String currentUser = userPrincipal.getActingUser();
        ArrayList<String> updatedInteractionsList = new ArrayList<>();
        String eventsState = null;
        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport;
        EventServiceAdmin eventServiceAdmin = Framework.getService(EventServiceAdmin.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googleCalendar");
            Credential storedCredential = serviceProvider.loadCredential(currentUser);

            Credential credential = new GoogleCredential.Builder()
                    .setClientSecrets(serviceProvider.getClientId(), serviceProvider.getClientSecret())
                    .setJsonFactory(JSON_FACTORY).setTransport(httpTransport).build()
                    .setRefreshToken(storedCredential.getRefreshToken()).setAccessToken(storedCredential.getAccessToken());

            // Initialize Calendar service with valid OAuth credentials
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName("Qualitified-CCM").build();

            // disable publish event listener
            //interactionDoc.putContextData("custom:booleanField2", Boolean.TRUE);
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",false);
            DocumentModel interactionCalendar = session.getDocument(new IdRef(calendarId));
            String syncToken = (String) interactionCalendar.getPropertyValue("custom:stringField1");
            String oneDayAgo = LocalDateTime.now().minusDays(1).toString();
            Calendar.Events.List request = service.events().list("primary");
            if (syncToken == null) {
                logger.warn("Performing full sync.");
                // syncing events up to a day old.
                request.setTimeMin(new DateTime(oneDayAgo));
            } else {
                logger.warn("Performing incremental sync.");
                request.setSyncToken(syncToken);
            }
            // Retrieve the events, one page at a time.
            String pageToken = null;
            Events events = null;
            do {
                request.setPageToken(pageToken);
                try {
                    events = request.execute();
                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() == 410) {
                        // A 410 status code, "Gone", indicates that the sync token is invalid.
                        logger.warn("Invalid sync token, clearing event store and re-syncing.");
                    } else {
                        throw e;
                    }
                }

                List<Event> items = events.getItems();
                if (items.size() == 0) {
                    logger.warn("No new events to sync.");
                } else {
                    for (Event event : items) {
                        DocumentModelList interactionsDoc = session.query("SELECT * FROM Interaction WHERE custom:stringField1 = '"+ event.getId() +"' AND custom:booleanField1 = 1 AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'", "NXQL", null, 0, 0, false);
                        if (!interactionsDoc.isEmpty() && !("cancelled".equals(event.getStatus()))) {
                            DocumentModel interactionDoc = interactionsDoc.get(0);
                            String interactionTag = (String) interactionDoc.getPropertyValue("custom:stringField2");
                            if (!interactionTag.equals(event.getEtag())) {
                                long eventStart = event.getStart().getDateTime().getValue();
                                long eventEnd = event.getEnd().getDateTime().getValue();
                                long duration = (eventEnd - eventStart) / 60000;
                                GregorianCalendar startDate = new GregorianCalendar();
                                startDate.setTimeInMillis(eventStart);
                                /*GregorianCalendar endDate = new GregorianCalendar();
                                startDate.setTimeInMillis(eventEnd);*/
                                interactionDoc.setPropertyValue("dc:title", event.getSummary());
                                interactionDoc.setPropertyValue("interaction:date", startDate);
                                interactionDoc.setPropertyValue("custom:stringField3", Long.toString(duration));
                                interactionDoc.setPropertyValue("custom:stringField2", event.getEtag());
                                session.saveDocument(interactionDoc);

                                updatedInteractionsList.add(interactionDoc.getTitle());
                            }
                        }
                    }
                }
                pageToken = events.getNextPageToken();

            } while (pageToken != null);

            // store sync token in calendar.config doc
            interactionCalendar.setPropertyValue("custom:stringField1",events.getNextSyncToken());
            session.saveDocument(interactionCalendar);

            if (!updatedInteractionsList.isEmpty()) {
                // updated events count
                eventsState = String.valueOf(updatedInteractionsList.size());
                logger.warn(eventsState+" events has been changed.");
            } else {
                // else events never updated or may have been cancelled
                eventsState = "Nothing updated";
                logger.warn(eventsState);
            }

        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 401) {
                logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        } finally {
            eventServiceAdmin.setListenerEnabledFlag("publishEvent",true);
        }

        return eventsState;

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