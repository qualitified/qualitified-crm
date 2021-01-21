package org.qualitified.crm.operation;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.PageProviderHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.bulk.BulkAdminService;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.io.BulkParameters;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.bulk.message.BulkCommand.Builder;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;

@Operation(
        id = AddToCampaignBulk.ID,
        category = "Services",
        label = "Run a bulk command",
        addToStudio = true,
        description = "Run a bulk action on a set of documents expressed by a NXQL."
)
public class AddToCampaignBulk
 {
    public static final String ID = "Bulk.AddToCampaignBulk";
    @Context
    protected BulkService service;
    @Context
    protected BulkAdminService admin;
    @Context
    protected CoreSession session;
    @Param(
            name = "query",
            required = false
    )
    protected String query;
     @Param(name = "collection")
     protected DocumentModel collection;
    @Param(
            name = "providerName",
            required = false
    )
    protected String providerName;
    @Param(
            name = "queryParams",
            required = false
    )
    protected StringList queryParams;
    @Param(
            name = "namedParameters",
            required = false,
            description = "Named parameters to pass to the page provider to fill in query variables."
    )
    protected Properties namedParameters;
    @Param(
            name = "quickFilters",
            required = false,
            description = "Quick filter properties (separated by comma)"
    )
    protected StringList quickFilters;
    @Param(
            name = "action",
            required = true
    )
    protected String action;
    @Param(
            name = "repositoryName",
            required = false
    )
    protected String repositoryName;
    @Param(
            name = "bucketSize",
            required = false
    )
    protected int bucketSize;
    @Param(
            name = "batchSize",
            required = false
    )
    protected int batchSize;
    @Param(
            name = "parameters",
            required = false
    )
    protected String parametersAsJson;
     protected Map<String, Serializable> params = new HashMap();


     public AddToCampaignBulk() {
    }

    @OperationMethod(
            asyncService = BulkService.class
    )
    public BulkStatus run() throws IOException, OperationException {
        if (!this.admin.getActions().contains(this.action)) {
            throw new NuxeoException("Action '" + this.action + "' not found", 404);
        } else if (!this.admin.isHttpEnabled(this.action) && !this.session.getPrincipal().isAdministrator()) {
            throw new NuxeoException("Action '" + this.action + "' denied", 403);
        } else if (this.query == null && this.providerName == null) {
            throw new OperationException("Query and ProviderName cannot be both null");
        } else {
            String userName = this.session.getPrincipal().getName();
            PageProviderDefinition def = this.query != null ? PageProviderHelper.getQueryPageProviderDefinition(this.query) : PageProviderHelper.getPageProviderDefinition(this.providerName);
            if (def == null) {
                throw new OperationException("Could not get Provider Definition from either query or provider name");
            } else {
                PageProvider<?> provider = PageProviderHelper.getPageProvider(this.session, def, this.namedParameters, this.queryParams != null ? this.queryParams.toArray(new String[0]) : null);
                this.query = PageProviderHelper.buildQueryStringWithAggregates(provider);
                if (this.query.contains("?")) {
                    throw new OperationException("Query parameters could not be parsed");
                } else {
                    params.put("collection",collection);
                    Builder builder = (new Builder(this.action, this.query)).user(userName).params(this.params);

                    try {
                        builder.params(BulkParameters.paramsToMap(this.parametersAsJson));
                    } catch (IOException var6) {
                        throw new OperationException("Could not parse parameters, expecting valid json value", var6);
                    }

                    if (this.repositoryName != null) {
                        builder.repository(this.repositoryName);
                    } else {
                        builder.repository(this.session.getRepositoryName());
                    }

                    if (this.bucketSize > 0) {
                        builder.bucket(this.bucketSize);
                    }

                    if (this.batchSize > 0) {
                        builder.batch(this.batchSize);
                    }

                    String commandId = this.service.submit(builder.build());
                    return (BulkStatus)this.service.getStatus(commandId);
                }
            }
        }
    }
}

