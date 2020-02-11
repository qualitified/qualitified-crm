package org.qualitified.crm.listener;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class, AutomationFeature.class})
@Deploy({"studio.extensions.crm"})
public class TestPostSaveAsync {

    protected final List<String> events = Arrays.asList("documentCreated", "documentModified");

    @Inject
    protected EventService s;

    @Inject
    protected EventService eventService;

    @Inject
    CoreSession coreSession;

    @Test
    public void listenerRegistration() {
        EventListenerDescriptor listener = s.getEventListener("postsaveasync");
        assertNotNull(listener);
        assertTrue(events.stream().allMatch(listener::acceptEvent));
    }

    @Test
    public void runPostSaveAsync() {

        DocumentModel folder = coreSession.createDocumentModel("/", "folder1", "Folder");
        folder.setPropertyValue("dc:title", "My Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel file = coreSession.createDocumentModel("/folder1", "file1", "File");
        file = coreSession.createDocument(file);
        coreSession.save();

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FolderPostSave", "ScriptNote");
        script.setPropertyValue("dc:title", "FolderPostSaveAsync");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("note:note", "var file = Document.Fetch(null, {'value':'/folder1/file1'});\nif(file != null){file['dc:description']=input.title;\nfile['custom:booleanField1']=params.isCreation;\nDocument.Save(file,{});}\n");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel scriptAsync = coreSession.createDocumentModel("/Admin/Scripts", "FolderPostSaveAsync", "ScriptNote");
        scriptAsync.setPropertyValue("dc:title", "FolderPostSaveAsync");
        scriptAsync.setPropertyValue("note:mime_type", "text/plain");
        scriptAsync.setPropertyValue("scriptnote:async", true);
        scriptAsync.setPropertyValue("note:note", "RunScript(null, {'script': 'java.lang.Thread.sleep(1500)'})\n;var file = Document.Fetch(null, {'value':'/folder1/file1'});\nif(file != null){file['dc:description']='AsyncDone';\nDocument.Save(file,{});}\n");
        coreSession.createDocument(scriptAsync);
        coreSession.save();

        folder.setPropertyValue("dc:title", "My New Folder Title");
        coreSession.saveDocument(folder);

        TransactionHelper.commitOrRollbackTransaction();
        eventService.waitForAsyncCompletion();
        TransactionHelper.startTransaction();

        assertNotNull(file.getId());
        assertEquals("AsyncDone", file.getPropertyValue("dc:description"));
        assertEquals(false, file.getPropertyValue("custom:booleanField1"));
    }
}
