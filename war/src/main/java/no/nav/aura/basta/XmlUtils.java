package no.nav.aura.basta;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XmlUtils {
    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter writer = new StringWriter();
            StreamResult xmlOutput = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();

        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Error when formatting XML", e);
        }
    }

    public static String generateXml(Object o) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(o.getClass());
        final Marshaller marshaller = context.createMarshaller();
        StringWriter request = new StringWriter();
        marshaller.marshal(o, request);
        return request.toString();
    }


    @SuppressWarnings("unchecked")
    public static <T> T parseXmlString(Class<T> tClass, String xmlString) {
        try {
            JAXBContext context = JAXBContext.newInstance(tClass);
            return (T) context.createUnmarshaller().unmarshal(new StringReader(xmlString));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseAndValidateXmlString(Class<T> tClass, String xmlString) throws UnmarshalException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(tClass).createUnmarshaller();
            unmarshaller.setSchema(generateSchemaFor(tClass));
            return (T) unmarshaller.unmarshal(new StringReader(xmlString));
        } catch (UnmarshalException e) {
            throw e;
        } catch (JAXBException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static <T> Schema generateSchemaFor(Class<T> tClass) throws JAXBException, IOException, SAXException {

        JAXBContext jc = JAXBContext.newInstance(tClass);
        final List<ByteArrayOutputStream> outs = new ArrayList<>();

        jc.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                outs.add(out);
                StreamResult streamResult = new StreamResult(out);
                streamResult.setSystemId("");
                return streamResult;
            }
        });
        StreamSource[] sources = new StreamSource[outs.size()];
        for (int i = 0; i < outs.size(); i++) {
            ByteArrayOutputStream out = outs.get(i);
            sources[i] = new StreamSource(new ByteArrayInputStream(out.toByteArray()), "");
        }

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(sources);
        return schema;
    }

}
