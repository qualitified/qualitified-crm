package org.nuxeo.ecm.restapi.server.jaxrs.element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.List;

@WebObject(type = "link")
@Produces(MediaType.TEXT_HTML)
public class LinkResource extends DefaultObject {
    private Log log = LogFactory.getLog(LinkResource.class);
    @GET
    @Path("{linkCode}")
    public Response getLinkByCode(@PathParam("linkCode") String linkCode, @CookieParam("silhouette") Cookie cookie) {


        try {
            String silhouetteId = "";
            if(cookie != null && cookie.getValue() != null){
                silhouetteId = cookie.getValue();
            }
            DocumentModel silhouette = (DocumentModel) buildUnrestrictedRunner().getDocument(silhouetteId, linkCode);
            silhouetteId = silhouette.getId();

            DocumentModel link = (DocumentModel) buildUnrestrictedRunner().getLinkDocument(linkCode);

            Cookie responseCookie = new NewCookie("silhouette", silhouetteId,"/", null, null,1296000, false);

            if(link != null){
                String href = (String)link.getPropertyValue("link:href");
                return Response.status(Response.Status.OK).entity("<html><head></head><body onload=window.location='"+href+"'></body></html>").type(MediaType.TEXT_HTML).cookie((NewCookie) responseCookie).build();
            }else{
                return Response.status(Response.Status.NOT_FOUND).entity("").type(MediaType.TEXT_HTML).cookie((NewCookie) responseCookie).build();
            }
        } catch (Exception e) {
            log.error("Error while getting link: "+ linkCode, e);
            return Response.status(Response.Status.BAD_REQUEST).entity("").type(MediaType.TEXT_HTML).build();
        }
    }


    public UnrestrictedRunner buildUnrestrictedRunner() {

        return new UnrestrictedRunner() {
            @Override
            public Object getLinkDocument(CoreSession session, String linkCode) throws NuxeoException {
                List<DocumentModel> links = session.query("SELECT * FROM Link WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE '"+linkCode+"' ORDER BY dc:created ASC");
                if(links.size()>0){
                    return session.getDocument(new IdRef(links.get(0).getId()));
                }else{
                    return null;
                }
            }

            @Override
            public Object getDocument(CoreSession coreSession, String docId, String linkCode) {
                DocumentModel silhouette = null;
                if(docId == null || "".equals(docId) || !coreSession.exists(new IdRef(docId))){
                    DocumentModel silhouetteModel = coreSession.createDocumentModel("/Marketing", "silhouette", "Silhouette");
                    //set the last visit date
                    silhouetteModel.setPropertyValue("custom:dateField1", Calendar.getInstance());
                    silhouette = coreSession.createDocument(silhouetteModel);
                }else{
                    silhouette = coreSession.getDocument(new IdRef(docId));
                    //update the last visit date
                    silhouette.setPropertyValue("custom:dateField1", Calendar.getInstance());
                    coreSession.saveDocument(silhouette);
                }
                //TODO Log that the Silhouette clicked on the link
                DocumentModel interactionModel = coreSession.createDocumentModel("/Marketing", "interaction", "Interaction");
                interactionModel.setPropertyValue("dc:title", "a suivi le lien "+ linkCode);
                interactionModel.setPropertyValue("interaction:status", "DONE");
                interactionModel.setPropertyValue("interaction:activity", "Link");
                interactionModel.setPropertyValue("interaction:date", Calendar.getInstance());
                interactionModel.setPropertyValue("custom:systemField1", silhouette.getId());
                interactionModel.setPropertyValue("custom:systemField2", linkCode);

                DocumentModel link = (DocumentModel) getLinkDocument(coreSession, linkCode);
                interactionModel.setPropertyValue("custom:systemField3", link.getPropertyValue("link:href"));
                interactionModel.setPropertyValue("custom:systemField4", link.getPropertyValue("dc:description"));

                coreSession.createDocument(interactionModel);
                return silhouette;
            }
            @Override
            public Object createOrUpdateDocument(CoreSession coreSession, String input, String type, String name, String docId, DocumentModel inputDoc) {
                return null;
            }
        };
    }

}
