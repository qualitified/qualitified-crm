package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;


@Operation(id=GetOptionValue.ID, category= Constants.CAT_EXECUTION, label="GetOptionValue", description="")
public class GetOptionValue {
    public static final String ID="Qualitified.GetOptionValue";
    private Log logger = LogFactory.getLog(GetOptionValue.class);

    @Param(name = "defaultValue",required = false)
    protected String defaultValue;
    @Param(name = "name",required = false)
    protected String name;

    @Context
    protected CoreSession session;
    @OperationMethod()
    public String run() throws  OperationException {

        DocumentModelList optionList = session.query("SELECT * FROM Option WHERE dc:title= '"+name+"' AND  ecm:isVersion = 0 AND ecm:isTrashed = 0");
        String option = null;
        if (optionList.size()>0) {
            option=(String)optionList.get(0).getPropertyValue("option:value");
            return option;
        }


        else {
            return defaultValue;
        }

    }

}








