package org.qualitified.crm.functions;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.template.api.context.ContextExtensionFactory;
import org.nuxeo.template.api.context.DocumentWrapper;

import java.util.Map;

public class DocFunctionsExtensionFactory implements ContextExtensionFactory {
    @Override
    public Object getExtension(DocumentModel currentDocument, DocumentWrapper wrapper, Map<String, Object> ctx) {
        return new ContextDocFunctions(currentDocument, wrapper);
    }
}
