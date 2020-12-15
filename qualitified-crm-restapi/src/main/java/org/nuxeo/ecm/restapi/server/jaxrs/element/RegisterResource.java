package org.nuxeo.ecm.restapi.server.jaxrs.element;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.core.impl.provider.entity.StringProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonReader;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

@WebObject(type = "register")
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource extends DefaultObject {
    private Log log = LogFactory.getLog(RegisterResource.class);

    @GET
    public Response getRegister(@CookieParam("silhouette") Cookie cookie) {
        return null;
    }

    @POST
    public Response register(DocumentModel inputDoc, @CookieParam("silhouette") Cookie cookie) {
        //We just assume that there is a cookie
        try {
            String silhouetteId = "";
            if(cookie != null && cookie.getValue() != null){
                silhouetteId = cookie.getValue();
            }

            DocumentModel silhouette = (DocumentModel) buildUnrestrictedRunner().createOrUpdateDocument("/Marketing", "Silhouette", "silhouette", silhouetteId, inputDoc);
            silhouetteId = silhouette.getId();

            Cookie responseCookie = new NewCookie("silhouette", silhouetteId,"/", null, null,1296000, false);

            return Response.status(Response.Status.OK).entity("{\"Status\":200}").type(MediaType.APPLICATION_JSON).cookie((NewCookie) responseCookie).build();

        } catch (Exception e) {
            log.error("Error while registering", e);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"Status\":400}").type(MediaType.APPLICATION_JSON).build();
        }
    }


    public UnrestrictedRunner buildUnrestrictedRunner() {

        return new UnrestrictedRunner() {
            @Override
            public Object getLinkDocument(CoreSession session, String linkCode) throws NuxeoException {
                return null;
            }

            @Override
            public Object getDocument(CoreSession coreSession, String docId, String linkCode) {
                return null;
            }

            @Override
            public Object createOrUpdateDocument(CoreSession coreSession, String input, String type, String name, String docId, DocumentModel inputDoc) throws IOException {

                DocumentModel silhouette = null;
                if(docId == null || "".equals(docId) || !coreSession.exists(new IdRef(docId))){
                    DocumentModel silhouetteModel = coreSession.createDocumentModel(input, name, type);
                    //set the last visit date
                    silhouetteModel.setPropertyValue("custom:dateField1", Calendar.getInstance());

                    DocumentModelJsonReader.applyPropertyValues(inputDoc, silhouetteModel);
                    silhouette = coreSession.createDocument(silhouetteModel);
                }else{
                    silhouette = coreSession.getDocument(new IdRef(docId));
                    //update the last visit date
                    silhouette.setPropertyValue("custom:dateField1", Calendar.getInstance());
                    DocumentModelJsonReader.applyPropertyValues(inputDoc, silhouette);

                    coreSession.saveDocument(silhouette);
                }
                //if(properties.containsKey("custom:booleanField1")){
                    DocumentModel interactionModel = coreSession.createDocumentModel(input, "interaction", "Interaction");
                    interactionModel.setPropertyValue("dc:title", silhouette.getPropertyValue("custom:systemField1"));
                    interactionModel.setPropertyValue("interaction:status", "DONE");
                    interactionModel.setPropertyValue("interaction:activity", "Inscription");
                    interactionModel.setPropertyValue("interaction:date", Calendar.getInstance());
                    interactionModel.setPropertyValue("custom:systemField1", silhouette.getId());
                    coreSession.createDocument(interactionModel);
                /*}else{
                    //Filled form to be a customer
                }*/
                return silhouette;
            }
        };
    }

}
