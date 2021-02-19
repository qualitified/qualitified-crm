package org.qualitified.crm.enricher;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;


// The class is instantiated as a singleton
// Priority defines which marshaller is used in case of conflict. Priority is an integer.
// The higher the number, the higher the priority: 10 > 1 for instance.

@Setup(mode = SINGLETON, priority = REFERENCE)
public class PersonDocEnricher extends AbstractJsonEnricher<DocumentModel> {

    // The enricher is called using enrichers.document: parentDoc
    public static final String NAME = "personDocEnricher";

    public PersonDocEnricher() {
        super(NAME);
    }

    // Method that is called when the enricher is called
    @Override
    public void write(JsonGenerator jg, DocumentModel enriched) throws IOException {
        ObjectNode personDocJsonObject = addPersonDocAsJson(enriched);
        jg.writeFieldName(NAME);
        jg.writeObject(personDocJsonObject);
    }


    private ObjectNode addPersonDocAsJson(DocumentModel doc) {

        ObjectMapper o = new ObjectMapper();

        // First create the parent document's JSON object content
        CoreSession session = doc.getCoreSession();
        ObjectNode personObject = o.createObjectNode();

        //get contact info
        String[] persons = (String[]) doc.getPropertyValue("interaction:contact");
        if (persons.length > 0) {
            DocumentModel personDoc = session.getDocument(new IdRef(persons[0]));
            personObject.put("personTitle", personDoc.getTitle());
            personObject.put("personEmail", (String) personDoc.getPropertyValue("person:email"));
            personObject.put("personSubscription", (String) personDoc.getPropertyValue("custom:integerField1"));

        } else {
            personObject.put("personTitle", "");
            personObject.put("personEmail", "");
            personObject.put("personSubscription", "");

        }

        return personObject;
    }
}

