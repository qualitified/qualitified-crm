package org.nuxeo.ecm.restapi.server.jaxrs.element;

import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;
import test.CCMFeature;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

@RunWith(FeaturesRunner.class)
@Features({ CCMFeature.class })

public class ElementResourceTest extends BaseTest {

    @Inject CoreSession session;

    @Before public void createElement() {

        DocumentModel element = session.createDocumentModel("/Admin/", "Element","ScriptNote");
        element.setPropertyValue("dc:title", "nuxeo-account-edit-layout");
        element.setPropertyValue("note:mime_type", "text/plain");
        element.setPropertyValue("note:note", "<dom-module id=\"nuxeo-account-edit-layout\">...</dom-module>");
        session.createDocument(element);
        session.save();

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
    }

    @Test public void getElementByName() throws Exception {

        String path = "/element/nuxeo-account-edit-layout";

        try (CloseableClientResponse response = getResponse(RequestType.GET, path)) {

            /*assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntityInputStream(), writer, "UTF-8");
            String element = writer.toString();
            assertEquals("<dom-module id=\"nuxeo-account-edit-layout\">...</dom-module>", element);*/
        }
    }

    @Test public void getElementByNameNotFound() throws Exception {

        /*String path = "/element/my-unexisting-element";

        try (CloseableClientResponse response = getResponse(RequestType.GET, path)) {

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntityInputStream(), writer, "UTF-8");
            String element = writer.toString();
            assertEquals("<html><head></head><body onload=window.location='/nuxeo/ui/document/"+element.split("-")[1]+"/"+element+".html'></body></html>", element);
        }*/
    }
}
