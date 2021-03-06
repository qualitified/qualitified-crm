package org.qualitified.crm.operation;
import freemarker.template.TemplateException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.PageProviderHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.PageProviderService;

import java.io.IOException;
import java.util.List;

@Operation(id = AddToCampaign.ID, category = Constants.CAT_EXECUTION, label = "Add To Campaign", description = "...")
public class AddToCampaign {
    public final static String ID = "Qualitified.AddToCampaign";
    @Context
    protected CoreSession session;
    @Context
    protected CollectionManager collectionManager;
    @Param(name = "collection")
    protected DocumentModel collection;
    @OperationMethod
   public DocumentModelList run(DocumentModelList docs) {
        for (DocumentModel doc : docs) {
              collectionManager.addToCollection(collection, doc, session);
        }

       return docs;
   }

   @OperationMethod()
   public DocumentModel run(DocumentModel doc) {
        collectionManager.addToCollection(collection, doc, session);
        return doc;
   }

}
