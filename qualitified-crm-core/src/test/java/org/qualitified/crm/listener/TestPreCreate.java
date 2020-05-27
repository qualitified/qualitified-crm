package org.qualitified.crm.listener;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, AutomationFeature.class})
@Deploy({"studio.extensions.crm"})
public class TestPreCreate {

    protected final List<String> events = Arrays.asList("emptyDocumentCreated");

    @Inject
    protected EventService s;

    @javax.inject.Inject
    CoreSession coreSession;

    @Test
    public void listenerRegistration() {
        EventListenerDescriptor listener = s.getEventListener("precreate");
        assertNotNull(listener);
        assertTrue(events.stream().allMatch(listener::acceptEvent));
    }

    @Test
    public void runPreCreate() throws OperationException{
        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreCreate","ScriptNote");
        script=coreSession.createDocument(script);
        coreSession.saveDocument(script);

        DocumentModel file = coreSession.createDocumentModel("/", "My File", "File");
        file = coreSession.createDocument(file);

        assertNotNull(script.getId());
        assertNotNull(file.getId());


    }

    @Test
    public void runMultiplePreCreate(){
        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreCreate","ScriptNote");
        script=coreSession.createDocument(script);
        coreSession.saveDocument(script);
        DocumentModel script2 = coreSession.createDocumentModel("/Admin/Scripts", "FilePreCreate","ScriptNote");
        script2=coreSession.createDocument(script2);
        coreSession.saveDocument(script2);
        assertNotNull(script2.getId());

        DocumentModel file = coreSession.createDocumentModel("/", "My File", "File");
        file = coreSession.createDocument(file);

        assertNotNull(file.getId());


    }


    @Test
    public void runPreCreateIsDisabled() throws OperationException{
        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreCreate","ScriptNote");
        script.setPropertyValue("dc:title", "FilePreCreate");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("scriptnote:isDisabled", true);
        script.setPropertyValue("note:note", "input['dc:description']='value set via script';\ninput['custom:booleanField1']=params.isCreation;");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel file = coreSession.createDocumentModel("/", "My File", "File");
        file = coreSession.createDocument(file);

        assertNotNull(file.getId());
        assertEquals(null, file.getPropertyValue("dc:description"));

    }
}
