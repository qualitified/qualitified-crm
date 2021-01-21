package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.operations.services.bulk.AutomationBulkAction;
import org.nuxeo.ecm.automation.core.util.PageProviderHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Operation(id = AddToCampaignBulk.ID, category = Constants.CAT_EXECUTION, label = "AddToCampaignBulk", description = "EmailingHistoryProcess...")
public class AddToCampaignBulk {

    public final static String ID = "Qualitified.AddToCampaignBulk";
    private Log logger = LogFactory.getLog(AddToCampaignBulk.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "query", required = false)
    protected String query;

    @Param(name = "providerName", required = false)
    protected String providerName;

    @Param(name = "queryParams", required = false)
    protected StringList queryParams;

    @Param(name = PageProviderService.NAMED_PARAMETERS, required = false, description = "Named parameters to pass to the page provider to "
            + "fill in query variables.")
    protected Properties namedParameters;
    @Param(name = "collection")
    protected DocumentModel collection;

    Map<String, Serializable> Params =new HashMap<>();
    @OperationMethod
    public void run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        Params.put("collection",collection);

        if (query == null && providerName == null) {
            throw new OperationException("Query and ProviderName cannot be both null");
        }
        PageProviderDefinition def = query != null ? PageProviderHelper.getQueryPageProviderDefinition(query)
                : PageProviderHelper.getPageProviderDefinition(providerName);

        if (def == null) {
            throw new OperationException("Could not get Provider Definition from either query or provider name");
        }

        PageProvider<?> provider = PageProviderHelper.getPageProvider(session, def, namedParameters,
                queryParams != null ? queryParams.toArray(new String[0]) : null);
        query = PageProviderHelper.buildQueryStringWithAggregates(provider);

        BulkCommand fetchMailHistoryCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME, query)
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID, "Qualitified.AddToCampaign")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) Params)
                .build();

        // run command
        BulkService BulkService = Framework.getService(BulkService.class);
        String CommandId = BulkService.submit(fetchMailHistoryCommand);

        // await end of computation
        //BulkService.await(CommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus BulkStatus = BulkService.getStatus(CommandId);

    }
}
