package org.qualitified.crm.operation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.ChainException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;

import javax.persistence.RollbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

@Operation(id = SendToDocusign.ID, category = Constants.CAT_EXECUTION, label = "Send to Docusign", description = "...")
public class SendToDocusign {
    public final static String ID = "Qualitified.SendToDocusign";
    private Log logger = LogFactory.getLog(Deploy.class);

    @Context
    protected CoreSession session;
    @Context
    protected OperationContext ctx;

    @Param(
            name = "signerEmails",
            description= "A StringList of email addresses",
            required = true)
    protected StringList signerEmails;

    @Param(
            name = "subject",
            description= "The subject of the email sent to the signers",
            required = true)
    protected String subject;

    @Param(
            name = "contextVariable",
            description= "The name of the context variable where the envelope ID will be stored",
            required = true)
    protected String contextVariable;

    @Param(
            name = "customFields",
            description= "Custom Fields to be added to the envelope",
            required = false)
    protected Properties customFields = new Properties();

    @Param(
            name = "callbackUrl",
            description= "The callback URL DocuSign will use for this envelope",
            required = false)
    protected String callbackUrl;

   @OperationMethod
   public DocumentModelList run(DocumentModelList docs) {
        for (DocumentModel doc : docs) {
        }

       return docs;
   }

   @OperationMethod()
   public void run(DocumentModel doc) throws LoginException, OperationException,RollbackException {
       LoginContext lc = Framework.loginAsUser("Administrator");
       ctx.getLoginStack().push(lc);
       AutomationService automationService = Framework.getService(AutomationService.class);
       Map<String, Object> params = new HashMap();
       params.put("signerEmails",signerEmails);
       params.put("subject",subject);
       params.put("contextVariable",contextVariable);
       params.put("customFields",customFields);
       params.put("callbackUrl",callbackUrl);
       try {
           automationService.run(ctx, "SendToDocuSign", params);
           logger.warn("Sending to DocuSign...");
       } catch (OperationException e){
           logger.error("One or both of Username and Password are invalid. Invalid access token");
       } catch (RollbackException r) {
           logger.error("Transaction is marked for rollback");
       }
   }

}
