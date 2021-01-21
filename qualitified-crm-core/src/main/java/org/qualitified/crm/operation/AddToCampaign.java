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
    /*@Param(name = "providerName", required = false)
    protected String providerName;

    @Param(name = PageProviderService.NAMED_PARAMETERS, required = false, description = "Named parameters to pass to the page provider to "
            + "fill in query variables.")
    protected Properties namedParameters;
*/
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
   /* @OperationMethod
    public void run() throws TemplateException, IOException, OperationException {

        PageProviderDefinition def = PageProviderHelper.getPageProviderDefinition(providerName);

        PageProvider<DocumentModel> pp = (PageProvider<DocumentModel>) PageProviderHelper.getPageProvider(session, def,
                namedParameters, null, null, 1000L, null, null, null, null);
        List<DocumentModel> docs = new DocumentModelListImpl();

        while (pp.isNextEntryAvailable()) {
            DocumentModel doc = pp.getCurrentEntry();
            docs.add(doc);
            pp.nextEntry();
        }
        docs.add(pp.getCurrentEntry());
        collectionManager.addToCollection(collection, docs, session);
    }*/

}
