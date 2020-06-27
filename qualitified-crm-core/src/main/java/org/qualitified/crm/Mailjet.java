package org.qualitified.crm;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.sun.jersey.core.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Mailjet {
    public static void main(String[] args) throws MailjetException, MailjetSocketTimeoutException, JSONException, IOException {

        String json = "{"+
                "\"Messages\":["+
        "{"+
            "\"From\": {"+
            "\"Email\": \"xxx\","+
                    "\"Name\": \"Me\""+
        "},"+
            "\"To\": ["+
            "{"+
                "\"Email\": \"xxx\","+
                    "\"Name\": \"You\""+
            "}"+
					"],"+
            "\"Subject\": \"My first Mailjet Email!\","+
                "\"TextPart\": \"Greetings from Mailjet!\","+
                "\"HTMLPart\": \"<h3>Dear passenger 1, welcome to <a href='https://www.mailjet.com/'>Mailjet</a>!</h3><br />May the delivery force be with you!\""+
        "}"+
	"]"+
"}";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.mailjet.com/v3.1/send");
        StringEntity jsonEntity = new StringEntity(json.toString());
        jsonEntity.setContentType(String.valueOf(ContentType.APPLICATION_JSON));
        post.setEntity(jsonEntity);
        post.setHeader("Authorization", "Basic " + new String(Base64.encode("xxx:xxx"), "ASCII"));
        post.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(post);
        HttpEntity entity = response.getEntity();

        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(EntityUtils.toString(entity));



    }
}