package org.qualitified.crm.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.mailjet.client.resource.Messagehistory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.List;

public class MailjetEmailingService extends DefaultComponent implements EmailingService {
    MailjetClient client;
    MailjetRequest request;
    MailjetResponse response;
    String apiKey= Framework.getProperty("mailjetservice.apikey");
    String secretKey=Framework.getProperty("mailjetservice.secretkey");



    @Override
    public JSONArray send(List<String> mailDetails) throws JSONException, MailjetSocketTimeoutException, MailjetException {

        client = new MailjetClient(apiKey, secretKey, new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM,
                                        new JSONObject()
                                                .put("Email", mailDetails.get(0))
                                                .put("Name", mailDetails.get(1)))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", mailDetails.get(2))
                                                .put("Name", mailDetails.get(3))))
                                .put(Emailv31.Message.SUBJECT, mailDetails.get(4))
                                .put(Emailv31.Message.TEXTPART, mailDetails.get(5))
                                .put(Emailv31.Message.HTMLPART, mailDetails.get(6))
                                .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
        response = client.post(request);

        return response.getJSONArray("Messages");
    }

    @Override
    public JSONObject fetchHistory(Long MessageID) throws MailjetSocketTimeoutException, MailjetException {
        // return "Hello";
        client = new MailjetClient(apiKey, secretKey);
        request = new MailjetRequest(Messagehistory.resource, MessageID);
        response = client.get(request);
        JSONObject mailHistory = new JSONObject(response);
         return mailHistory;


    }
}
