package org.qualitified.crm.operation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.runtime.api.Framework;

/**
 *
 */
@Operation(id=GetSchemaFromPrefix.ID, category=Constants.CAT_DOCUMENT, label="Get Schema From Prefix", description="Describe here what your operation does.")
public class GetSchemaFromPrefix {

    public static final String ID = "Document.GetSchemaFromPrefix";

    @Param(name = "xpath", required = true)
    protected String xpath;

    @OperationMethod
    public String run() {

        int p = xpath.indexOf(':');
        if (p == -1) {
            throw new PropertyNotFoundException(xpath, "Schema not specified");
        }
        String prefix = xpath.substring(0, p);
        SchemaManager schemaManager = Framework.getService(SchemaManager.class);
        Schema schema = schemaManager.getSchemaFromPrefix(prefix);
        if (schema == null) {
            schema = schemaManager.getSchema(prefix);
            if (schema == null) {
                throw new PropertyNotFoundException(xpath, "No schema for prefix");
            }
        }
        return schema.getName();

    }
}
