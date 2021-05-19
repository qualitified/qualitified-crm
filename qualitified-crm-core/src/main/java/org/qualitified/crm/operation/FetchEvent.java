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
import com.google.api.services.calendar.model.Events;



@Operation(id=FetchEvent.ID, category=Constants.CAT_NOTIFICATION, label="fetch event from Google Calendar", description=" ")
public class FetchEvent {

    public static final String ID = "Qualitified.FetchFromCalendar";
    private Log logger = LogFactory.getLog(Deploy.class);

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext context;

    @OperationMethod()
    public String run() {
        //NuxeoPrincipal userPrincipal = session.getPrincipal();
        String currentUser = context.getPrincipal().getActingUser();
        //session = getReconnectedCoreSession(repositoryName, originatingUsername);
        //String currentUser = userPrincipal.getActingUser();
        ArrayList<String> updatedInteractionsList = new ArrayList<>();
        String eventsState = null;
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


            DocumentModelList interactionsCalendar = session.query("SELECT * FROM Calendar WHERE calendar:docType='Interaction' AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'", "NXQL", null, 0, 0, false);
            DocumentModel interactionCalendar = interactionsCalendar.get(0);
            String syncToken = (String) interactionCalendar.getPropertyValue("custom:stringField1");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            //String next2weeks = LocalDateTime.now().plusWeeks(2).format(formatter);
            String oneDayAgo = LocalDateTime.now().minusDays(1).toString();
            Calendar.Events.List request = service.events().list("primary");
            if (syncToken == null) {
                logger.warn("Performing full sync.");
                // Set the filters you want to use during the full sync. Sync tokens aren't compatible with
                // most filters, but you may want to limit your full sync to only a certain date range.
                // In this example we are only syncing events up to a day old.
                //Date oneYearAgo = Utils.getRelativeDate(java.util.Calendar.YEAR, -1);
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
                        logger.error("Invalid sync token, clearing event store and re-syncing.");
                    } else {
                        throw e;
                    }
                }

                List<Event> items = events.getItems();
                if (items.size() == 0) {
                    logger.error("No new events to sync.");
                } else {
                    for (Event event : items) {
                        DocumentModelList interactionsDoc = session.query("SELECT * FROM Interaction WHERE custom:stringField1 = '"+ event.getId() +"' AND custom:booleanField1 = 1 AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'", "NXQL", null, 0, 0, false);
                        if (!interactionsDoc.isEmpty()) {
                            DocumentModel interactionDoc = interactionsDoc.get(0);
                            String interactionTag = (String) interactionDoc.getPropertyValue("custom:stringField2");
                            if (!interactionTag.equals(event.getEtag())) {
                                    GregorianCalendar startDate = new GregorianCalendar();
                                    startDate.setTimeInMillis(event.getStart().getDateTime().getValue());
                                    GregorianCalendar endDate = new GregorianCalendar();
                                    startDate.setTimeInMillis(event.getEnd().getDateTime().getValue());

                                    interactionDoc.setPropertyValue("dc:title", event.getSummary());
                                    interactionDoc.setPropertyValue("interaction:date", startDate);
                                    interactionDoc.setPropertyValue("custom:dateField1", endDate);
                                    interactionDoc.setPropertyValue("custom:stringField2", event.getEtag());
                                    session.saveDocument(interactionDoc);
                                    updatedInteractionsList.add(interactionDoc.getTitle());
                                }
                        }
                    }
                }
                pageToken = events.getNextPageToken();

            } while (pageToken != null);

            // store synch token in calendar.config doc
            interactionCalendar.setPropertyValue("custom:stringField1",events.getNextSyncToken());
            session.saveDocument(interactionCalendar);

            if (!updatedInteractionsList.isEmpty()) {
                eventsState = String.valueOf(updatedInteractionsList.size());
                logger.error(eventsState+" events has been changed.");
            } else {
                eventsState = "Nothing updated";
                logger.error(eventsState);
            }

        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 401) {
                logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
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