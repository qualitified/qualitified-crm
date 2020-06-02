package org.qualitified.crm.functions;

import com.sun.jersey.api.core.ParentRef;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.operations.document.GetDocumentParent;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.template.api.context.DocumentWrapper;

public class ContextDocFunctions {
    protected final DocumentModel doc;
    protected AutomationService automationService;
    protected final DocumentWrapper nuxeoWrapper;


    public ContextDocFunctions(DocumentModel doc, DocumentWrapper nuxeoWrapper) {
        this.doc = doc;
        this.nuxeoWrapper = nuxeoWrapper;
    }
    public String getDocTitle(String id) {

        return doc.getCoreSession().getDocument(new IdRef(id)).getTitle();
    }
    public String getDocPath(String id) {

        return Framework.getProperty("nuxeo.url") + "/ui/#!/doc/" + id;
    }

    public String getEmail (String username) {
        UserManager um = Framework.getService(UserManager.class);
        return um.getPrincipal(username).getEmail();
    }
    public String getParentMetadata(String type,String metadata)
    {
        type = type.trim();
        if (type.length() != 0) {
            DocumentModel parentDoc = doc.getCoreSession().getDocument(doc.getParentRef());
            if (parentDoc != null && type.equals(parentDoc.getType())) {
                return (String) parentDoc.getPropertyValue(metadata);
            }
        }
        return "";
    }
    public String getDocumentMetadata(String documentId,String metadata)
    {
        if (documentId.length() != 0) {
            DocumentModel parentDoc = doc.getCoreSession().getDocument(doc.getParentRef());
            if (parentDoc != null && documentId.equals(parentDoc.getId())) {
                return (String) parentDoc.getPropertyValue(metadata);
            }
        }
        return "";

    }



}

