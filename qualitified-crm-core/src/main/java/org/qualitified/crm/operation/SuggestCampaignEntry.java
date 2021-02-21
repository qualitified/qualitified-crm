package org.qualitified.crm.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.operations.services.DocumentPageProviderOperation;
import org.nuxeo.ecm.automation.core.util.PageProviderHelper;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.automation.features.SuggestConstants;
import org.nuxeo.ecm.collections.api.CollectionConstants;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

@Operation(id = SuggestCampaignEntry.ID, category = Constants.CAT_SERVICES, label = "Get campaign suggestion", description = "Get the campaign list accessible by the current user. This is returning a blob containing a serialized JSON array..")
public class SuggestCampaignEntry {

    public static final String ID = "Qualitified.SuggestCampaignEntry";

    private static final String PATH = "path";
    public static final String CAMPAIGN_PAGE_PROVIDER = "default_campaign";

    @Param(name = "currentPageIndex", required = false)
    protected Integer currentPageIndex = 0;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize = 20;

    @Context
    protected AutomationService service;

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Context
    protected CollectionManager collectionManager;

    @Param(name = "lang", required = false)
    protected String lang;

    @Param(name = "searchTerm", required = false)
    protected String searchTerm;


    @OperationMethod
    public Blob run() throws OperationException, IOException {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> vars = new HashMap<>();
        {
            StringList sl = new StringList();
            sl.add(searchTerm + (searchTerm.endsWith("%") ? "" : "%"));
            vars.put("queryParams", sl);
            vars.put("providerName", CAMPAIGN_PAGE_PROVIDER);
        }
        OperationChain chain = new OperationChain("operation");
        OperationParameters oparams = new OperationParameters(DocumentPageProviderOperation.ID, vars);
        chain.add(oparams);
        @SuppressWarnings("unchecked")
        List<DocumentModel> docs = (List<DocumentModel>) service.run(ctx, chain);

        boolean found = false;
        for (DocumentModel doc : docs) {
            Map<String, Object> obj = new LinkedHashMap<>();
            if (collectionManager.canAddToCollection(doc, session)) {
                obj.put(SuggestConstants.ID, doc.getId());
            }
            if (doc.getTitle().equals(searchTerm)) {
                found = true;
            }
            obj.put(SuggestConstants.LABEL, doc.getTitle());
            if (StringUtils.isNotBlank((String) doc.getProperty("common", "icon"))) {
                obj.put(SuggestConstants.ICON, doc.getProperty("common", "icon"));
            }
            obj.put(PATH, doc.getPath().toString());
            result.add(obj);
        }

        if (!found && StringUtils.isNotBlank(searchTerm)) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put(SuggestConstants.LABEL, searchTerm);
            obj.put(SuggestConstants.ID, CollectionConstants.MAGIC_PREFIX_ID + searchTerm);
            result.add(0, obj);
        }

        return Blobs.createJSONBlobFromValue(result);
    }

    protected Locale getLocale() {
        return new Locale(getLang());
    }

    protected String getLang() {
        if (lang == null) {
            lang = (String) ctx.get("lang");
            if (lang == null) {
                lang = SuggestConstants.DEFAULT_LANG;
            }
        }
        return lang;
    }

}



