package org.nuxeo.ecm.restapi.server.jaxrs.element;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.Calendar;
import org.json.JSONException;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;

@WebObject(type = "notification")
@Produces(MediaType.TEXT_HTML)
public class WatchCalendarResource extends DefaultObject {
    private Log log = LogFactory.getLog(WatchCalendarResource.class);
    private String nuxeoUrl = Framework.getProperty("nuxeo.url");


    @GET
    //@Path("")
    public Response getUpdatedEvents() throws JSONException, OperationException {
        AutomationService automationService = Framework.getService(AutomationService.class);

        NuxeoPrincipal userPrincipal = CoreInstance.doPrivileged(getContext().getCoreSession(), (session) -> {
            return session.getPrincipal();});
        String currentUser = userPrincipal.getActingUser();
        JSONArray updatedInteractionsList = new JSONArray();

        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            log.warn("Synchronizing with google calendar...");
            //automationService.run(context, "Qualitified.FetchFromCalendar");
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
                DocumentModelList interactionDocs = CoreInstance.doPrivileged(getContext().getCoreSession(), (session) -> {
                return session.query("SELECT * FROM Interaction WHERE custom:stringField1 IS NOT NULL AND custom:booleanField1 = 1 AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'", "NXQL", null, 0, 0, false);});

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
                            CoreInstance.doPrivileged(getContext().getCoreSession(), (session) -> {
                                return session.saveDocument(interactionDoc);});
                            updatedInteractionsList.put(interactionDoc.getTitle());
                        }
                    }
                } else {
                    updatedInteractionsList.put("");
                }

            return Response.status(Response.Status.OK)
                        .entity("<html><head></head><body><h1>"+updatedInteractionsList.length()+" events has been changed!</h1></body></html>")
                        .type(MediaType.TEXT_HTML).build();
        } catch (Exception e) {
            log.error("Error while getting resource");
            return Response.status(Response.Status.BAD_REQUEST).entity("").type(MediaType.TEXT_HTML).build();
        }
    }



}