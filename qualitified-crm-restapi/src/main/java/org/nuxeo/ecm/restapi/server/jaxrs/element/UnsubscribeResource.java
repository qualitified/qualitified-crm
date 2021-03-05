package org.nuxeo.ecm.restapi.server.jaxrs.element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

@WebObject(type = "unsub")
@Produces(MediaType.TEXT_HTML)
public class UnsubscribeResource extends DefaultObject {
    private Log log = LogFactory.getLog(UnsubscribeResource.class);
    private String nuxeoUrl = Framework.getProperty("nuxeo.url");

    @GET
    @Path("{personId}")
    public Response getUnsubPageById(@PathParam("personId") String personId) throws JSONException {

        try {
            DocumentModel personDoc = (DocumentModel) buildUnrestrictedRunner().getDocument(personId, null);
            if (personDoc != null) {
                buildUnrestrictedRunner().createOrUpdateDocument(null,null,null,null,personDoc);
                String encodedTitle = Base64.getEncoder().encodeToString(personDoc.getTitle().getBytes());

                return Response.status(Response.Status.OK)
                        .entity("<html><head></head><body onload=window.location='"+nuxeoUrl+"/unsubscribe.html?unsub="+encodedTitle+"'></body></html>")
                        .type(MediaType.TEXT_HTML).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("").type(MediaType.TEXT_HTML).build();
            }
        } catch (Exception e) {
            log.error("Error while getting unsub page for : "+ personId, e);
            return Response.status(Response.Status.BAD_REQUEST).entity("").type(MediaType.TEXT_HTML).build();
        }
    }
    public UnrestrictedRunner buildUnrestrictedRunner() {

        return new UnrestrictedRunner() {
            @Override
            public Object getLinkDocument(CoreSession coreSession, String code) {
                return null;
            }

            @Override
            public Object getDocument(CoreSession coreSession, String docId, String code) throws NuxeoException{
                DocumentModel documentModel= coreSession.getDocument(new IdRef(docId));
                return documentModel;
            }

            @Override
            public Object createOrUpdateDocument(CoreSession coreSession, String input, String type, String name, String docId, DocumentModel inputDoc) throws IOException {
                inputDoc.setPropertyValue("person:targetStatus","unsubscribed");
                coreSession.saveDocument(inputDoc);
                return inputDoc;
            }
        };

    }
}