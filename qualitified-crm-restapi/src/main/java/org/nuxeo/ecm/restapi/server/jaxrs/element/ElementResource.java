package org.nuxeo.ecm.restapi.server.jaxrs.element;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import java.util.List;

@WebObject(type = "element")
@Produces(MediaType.TEXT_HTML)
public class ElementResource extends DefaultObject {
    private Log log = LogFactory.getLog(ElementResource.class);
    @GET
    @Path("{elementName}")
    public Response getElementByName(@PathParam("elementName") String elementName) throws JSONException {
        try {
            String adminPath = Framework.getProperty("qualitified.config.path", "/Admin");
            List<DocumentModel> elements = CoreInstance.doPrivileged(getContext().getCoreSession(), (session) -> {
                return session.query("SELECT * FROM ScriptNote WHERE ecm:path STARTSWITH '"+adminPath+"' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND (scriptnote:isDisabled is NULL OR scriptnote:isDisabled = 0) AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE '"+elementName+"' ORDER BY dc:created ASC");
            });

            if(elements.size()>0){
                String element = CoreInstance.doPrivileged(getContext().getCoreSession(), (session) -> {
                    return (String)session.getDocument(new IdRef(elements.get(0).getId())).getPropertyValue("note:note");
                });
                return Response.status(Response.Status.OK).entity(element).type(MediaType.TEXT_HTML).build();
            }else{
                return Response.status(Response.Status.NOT_FOUND).entity("").type(MediaType.TEXT_HTML).build();
            }
        } catch (Exception e) {
            log.error("Error while getting element: "+ elementName, e);
            return Response.status(Response.Status.BAD_REQUEST).entity("").type(MediaType.TEXT_HTML).build();
        }
    }
}
