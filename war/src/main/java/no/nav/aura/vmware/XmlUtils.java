package no.nav.aura.vmware;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.nav.aura.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.vmware.orchestrator.request.ProvisionRequest;

public class XmlUtils {
    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter writer = new StringWriter();
            StreamResult xmlOutput = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();

        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Error when formatting XML", e);
        }
    }

    public static String generateXml(OrchestatorRequest orcRequest) throws JAXBException {
        final JAXBContext context;
        if( orcRequest instanceof DecomissionRequest ) {
            context = JAXBContext.newInstance(DecomissionRequest.class);
        }
        else if(orcRequest instanceof ProvisionRequest) {
            context = JAXBContext.newInstance(ProvisionRequest.class);
        }
        else {
            throw new IllegalArgumentException("Unable to determine instance of class for performing Marshalling" );
        }
        
        final Marshaller marshaller = context.createMarshaller();
        StringWriter request = new StringWriter();
        marshaller.marshal(orcRequest, request);
        return request.toString();
    }

}
