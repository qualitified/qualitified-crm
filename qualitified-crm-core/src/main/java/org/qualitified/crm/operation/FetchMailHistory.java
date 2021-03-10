package org.qualitified.crm.operation;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import fr.opensagres.xdocreport.document.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Operation(id = FetchMailHistory.ID, category = Constants.CAT_EXECUTION, label = "FetchMailHistory", description = "FetchMailHistory...")
public class FetchMailHistory {

    public final static String ID = "Qualitified.FetchMailHistory";
    private Log logger = LogFactory.getLog(FetchMailHistory.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    @Param(name = "Status")
    protected String status;


    private String MessageID;
    int isDelivered = 1;
    int isSent = 0;
    int isOpened = 0;
    int isClicked = 0;
    String targetStatus = "subscribed";
    int isSpam = 0;
    int isBlocked = 0;
    String statusMail = "Not opened";
    JSONObject response = null;

    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, JSONException {
        EmailingService emailingService = Framework.getService(EmailingService.class);
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        MessageID = (String) interactionDoc.getPropertyValue("interaction:messageID");

        try {
            response = emailingService.fetchHistory(Long.parseLong(MessageID));
            // if no error appears during running mailjet service, the email considered as sent but
            // not necessarily delivered.
            List<Map<String, Object>> mailHistory = new ArrayList<>();

            for (int i = 0; i < response.getLong("count"); i++) {
                Map<String, Object> Details = new HashMap<String, Object>();
                JSONObject dataObject =response.getJSONArray("data").getJSONObject(i);
                //Details.put("comment",dataObject.getString("Comment"));
                //cal.setTimeInMillis(dataObject.getLong("EventAt"));
                // Details.put("eventAt",null);
                Details.put("eventTypes", dataObject.getString("EventType"));
                Details.put("state", dataObject.getString("State"));
                /*Details.put("userAgent", dataObject.getString("Useragent"));
                int userAgentID = Math.toIntExact(dataObject.getLong("UseragentID"));
                Details.put("userAgentID", userAgentID);*/
                mailHistory.add(Details);
            }

            // sometimes mailjet response can be empty array, should check the mailHistory if not empty
            if (mailHistory.size() > 0) {
                for (int i = 0; i < mailHistory.size(); i++) {
                    switch (mailHistory.get(i).get("eventTypes").toString()) {
                        case "sent":
                            isDelivered = 1;
                            break;
                        case "opened":
                            isOpened = 1;
                            statusMail = "Opened";
                            break;
                        case "clicked":
                            isClicked = 1;
                            break;
                        case "unsub":
                            targetStatus = "unsubscribed";
                            break;
                        case "spam":
                            isSpam = 1;
                            break;
                        case "blocked":
                            isBlocked = 1;
                            break;
                        case "hardbounced":
                            targetStatus = "hardbounced";
                            isDelivered = 0;
                            break;
                        default :
                            isDelivered = 0;
                    }
                }
                String[] person = (String[]) interactionDoc.getPropertyValue("interaction:contact");
                IdRef personId = new IdRef(person[0]);
                DocumentModel personDoc = documentManager.getDocument(personId);
                interactionDoc.setPropertyValue("interaction:data", (Serializable) mailHistory);
                interactionDoc.setPropertyValue("interaction:isDelivered", isDelivered);
                interactionDoc.setPropertyValue("interaction:isOpened", isOpened);
                interactionDoc.setPropertyValue("interaction:isClicked", isClicked);
                interactionDoc.setPropertyValue("interaction:statusMail", statusMail);
                personDoc.setPropertyValue("person:targetStatus",targetStatus);
                documentManager.saveDocuments(new DocumentModel[]{interactionDoc,personDoc});

            } else logger.warn("Something happened while email history fetching, MessageID: "+ MessageID);

        } catch (MailjetException m) {
            logger.error("Error while running mailjet service", m);
        } catch (MailjetSocketTimeoutException mst) {
            logger.error("Error while getting response, Mailjet SocketTimeout ", mst);
        }
    }

}
