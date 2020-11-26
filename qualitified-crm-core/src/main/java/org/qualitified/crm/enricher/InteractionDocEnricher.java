package org.qualitified.crm.enricher;
import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;


// The class is instantiated as a singleton
// Priority defines which marshaller is used in case of conflict. Priority is an integer.
// The higher the number, the higher the priority: 10 > 1 for instance.

@Setup(mode = SINGLETON, priority = REFERENCE)
public class InteractionDocEnricher extends AbstractJsonEnricher<DocumentModel> {

    // The enricher is called using enrichers.document: parentDoc
    public static final String NAME = "interactionDocEnricher";

    public InteractionDocEnricher() {
        super(NAME);
    }

    // Method that is called when the enricher is called
    @Override
    public void write(JsonGenerator jg, DocumentModel enriched) throws IOException {
        ObjectNode interactionDocJsonObject = addInteractionDocAsJson(enriched);
        jg.writeFieldName(NAME);
        jg.writeObject(interactionDocJsonObject);
    }


    private ObjectNode addInteractionDocAsJson(DocumentModel doc) {

        ObjectMapper o = new ObjectMapper();

        // First create the parent document's JSON object content
        CoreSession session = doc.getCoreSession();
        DocumentModel parentDoc = session.getDocument(doc.getParentRef());

        ObjectNode parentObject = o.createObjectNode();
        if(parentDoc.getType().equals("Account")){
            parentObject.put("accountTitle", parentDoc.getTitle());
            parentObject.put("OpportunityTitle", "");
        }else if(parentDoc.getType().equals("Opportunity")){
            parentObject.put("opportunityTitle", parentDoc.getTitle());
            parentObject.put("accountTitle", "");
        }
         else {
            parentObject.put("opportunityTitle", "");
            parentObject.put("accountTitle", "");
        }

        return parentObject;
    }

}

