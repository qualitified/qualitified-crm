package org.qualitified.crm.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.opensagres.xdocreport.core.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.features.HTTPHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Operation(id= GetImages.ID, category= Constants.CAT_EXECUTION, label="GetImages", description="Get Images as JSON based from Google")
public class GetImages {
    public static final String ID = "Qualitified.GetImages";
    private Log logger = LogFactory.getLog(GetImages.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "query")
    protected String query;

    @OperationMethod
    public Blob run() throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        String url = "https://www.googleapis.com/customsearch/v1?q="+ URLEncoder.encode(query, "UTF-8")+"&key="+Framework.getProperty("google.key")+"&cx="+Framework.getProperty("google.cx")+"&searchType=image&alt=json";
        HTTPHelper httpHelper = new HTTPHelper();
        Blob blob = httpHelper.call(null, null, "GET", url);
        StringWriter writer = new StringWriter();
        IOUtils.copy(blob.getStream(), writer, "UTF-8");
        String result = writer.toString();
        ctx.getLoginStack().pop();
        return new StringBlob(result, "application/json");

    }

}
