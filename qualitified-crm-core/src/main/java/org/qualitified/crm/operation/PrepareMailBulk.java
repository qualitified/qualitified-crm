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
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Operation(id = PrepareMailBulk.ID, category = Constants.CAT_EXECUTION, label = "PrepareMailBulk", description = "PrepareMailBulk...")

public class PrepareMailBulk {
    public final static String ID = "Qualitified.PrepareMailBulk";
    private Log logger = LogFactory.getLog(PrepareMailBulk.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    @Param(name = "campaignId")
    protected String campaignId;



    @OperationMethod
    public BulkStatus run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        Map<String, Serializable> campaignIdParam = new HashMap<>();
        campaignIdParam.put("campaignId", campaignId);

        BulkCommand prepareMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Document " +
                        "WHERE ecm:primaryType IN ('Contact','Silhouette')" +
                        "AND ecm:mixinType != 'HiddenInNavigation'" +
                        "AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState !='deleted'" +
                        "AND ecm:isTrashed = 0 " +
                        "AND collectionMember:collectionIds/* = '"+campaignId+"' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID,"Qualitified.CreateInteractions")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) campaignIdParam)
                .build();

        // run command
        BulkService prepareMailBulkService = Framework.getService(BulkService.class);
        String prepareMailCommandId = prepareMailBulkService.submit(prepareMailCommand);

        // await end of computation
        // prepareMailBulkService.await(prepareMailCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus prepareMailBulkStatus = prepareMailBulkService.getStatus(prepareMailCommandId);
        return prepareMailBulkStatus;
    }
}
