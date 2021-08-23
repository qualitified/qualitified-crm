package org.qualitified.crm.operation;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Operation(id = CopyBlob.ID, category = Constants.CAT_EXECUTION, label = "Copy blob", description = "...")

public class CopyBlob {


    public final static String ID = "Qualitified.CopyBlob";
    String folderPath = Framework.getProperty("nuxeo.war.dir");
    private Log logger = LogFactory.getLog(org.qualitified.crm.operation.CopyBlob.class);

    @Context
    protected CoreSession session;
    @Context
    protected CoreSession documentManager;

    @OperationMethod()
    public void run(DocumentModel image) throws IOException, OperationException, JSONException {

        Blob backgroundImage = (Blob) image.getPropertyValue("branding:backgroundImage");
        Blob loginLogo = (Blob) image.getPropertyValue("branding:loginLogo");
        Blob dashboardLogo = (Blob) image.getPropertyValue("branding:dashboardLogo");
        Blob faviconPng = (Blob) image.getPropertyValue("branding:faviconPng");
        Blob faviconIco = (Blob) image.getPropertyValue("branding:faviconIco");

        backgroundImage.setFilename("background.png");
        loginLogo.setFilename("logo.png");
        dashboardLogo.setFilename("logo.png");
        faviconPng.setFilename("favicon-32x32.png");
        faviconIco.setFilename("favicon.ico");


        File dest = new File(folderPath + "/img/" + backgroundImage.getFilename());
        File logoDest = new File(folderPath + "/img/" + loginLogo.getFilename());
        File dashboardDest = new File(folderPath + "/ui/themes/default/" + dashboardLogo.getFilename());
        File faviconPngDest = new File(folderPath + "/ui/images/touch/" + faviconPng.getFilename());
        File faviconIcoDest = new File(folderPath + "/icons/" + faviconIco.getFilename());
        backgroundImage.transferTo(dest);
        loginLogo.transferTo(logoDest);
        dashboardLogo.transferTo(dashboardDest);
        faviconPng.transferTo(faviconPngDest);
        faviconIco.transferTo(faviconIcoDest);
        String loginFilePath=folderPath+"/login.jsp";
        String filePath = folderPath+"/ui/themes/default/theme.html";
        StringBuilder stringBuilder = new StringBuilder();
        FileReader fr = new FileReader(filePath);  //Creation of File Reader object
        //BufferedReader br = new BufferedReader(fr); //Creation of BufferedReader object
        FileReader login = new FileReader(loginFilePath);
        //BufferedReader bufferedReader = new BufferedReader(login);
        List<Map<String, String>> complexValuesList = new ArrayList<Map<String, String>>();
        List<Map<String,String>> styles  = (List<Map<String,String>>)image.getPropertyValue("branding:styles");
        Path path1=Paths.get(loginFilePath);
        Path path = Paths.get(filePath);
        Charset charset = StandardCharsets.UTF_8;
        String content = null;
        String loginContent=null;
        loginContent = new String(Files.readAllBytes(path1), charset);
        loginContent = loginContent.replaceAll(".*loginButtonBackgroundColor.*","String loginButtonBackgroundColor = LoginScreenHelper.getValueWithDefault(screenConfig.getLoginButtonBackgroundColor(),\""+ image.getPropertyValue("branding:loginBackgroundColor") +"\");");
        Files.write(path1, loginContent.getBytes(charset));
        content = new String(Files.readAllBytes(path), charset);
        for (int i = 0; i < styles.size(); i++) {
            content = content.replaceAll(styles.get(i).get("name")+": .*;" , styles.get(i).get("name")+": "+styles.get(i).get("value")+";");
            Files.write(path, content.getBytes(charset));

        }
    }
}
















