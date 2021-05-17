package org.qualitified.crm.operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.calendar.model.*;
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


@Operation(id=FetchEvent.ID, category=Constants.CAT_NOTIFICATION, label="fetch event from Google Calendar", description=" ")
public class FetchEvent {

    public static final String ID = "Qualitified.FetchFromCalendar";
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

            String calendarId = "primary";
            String eventId = (String) interactionDoc.getPropertyValue("custom:stringField1");
            if (eventId != null) {
                String interactionTag = (String) interactionDoc.getPropertyValue("custom:stringField2");

                // Retrieve an event
                Event event = service.events().get(calendarId, eventId).execute();
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
                    logger.warn("Event updated successfully!");

                } else {
                    logger.warn("Event is already up to date!");
                }
            } else {
                logger.warn("this event is not published in google calendar!");
            }


        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 401) {
                logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        }
        return interactionDoc;
    }

    @OperationMethod()
    public Object run() {
        NuxeoPrincipal userPrincipal = session.getPrincipal();
        String currentUser = userPrincipal.getActingUser();
        JSONArray updatedInteractionsList = new JSONArray();

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
            DocumentModelList interactionDocs = session.query("SELECT * FROM Interaction WHERE custom:stringField1 IS NOT NULL AND custom:booleanField1 = 1 AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'", "NXQL", null, 0, 0, false);

            String calendarId = "primary";
            if (!interactionDocs.isEmpty()) {
                for (DocumentModel interactionDoc : interactionDocs) {
                    String eventId = (String) interactionDoc.getPropertyValue("custom:stringField1");
                    String interactionTag = (String) interactionDoc.getPropertyValue("custom:stringField2");
                    Event event = service.events().get(calendarId, eventId).execute();
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
                        updatedInteractionsList.put(interactionDoc.getTitle());
                    }
                }
            } else {
                updatedInteractionsList.put("");
            }

        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 401) {
                logger.error("Refresh token has been expired.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new NuxeoException(e);
        }
        return updatedInteractionsList.toString();
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