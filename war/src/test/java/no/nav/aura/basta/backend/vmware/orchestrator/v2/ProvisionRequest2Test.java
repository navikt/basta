package no.nav.aura.basta.backend.vmware.orchestrator.v2;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

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

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;

import org.junit.Test;

public class ProvisionRequest2Test {

	@Test
	public void test() {
		ProvisionRequest2 request = new ProvisionRequest2(OrchestratorEnvironmentClass.utv, URI.create("http://holmenkollstafetten.no/resultater/"),
				URI.create("http://holmenkollstafetten.no/status"));
		request.setApplications("app1", "app2", "app3");
		request.setEnvironmentId("t8");
        request.setOrderedBy("username");
        Vm vm1 = new Vm(Zone.fss, OSType.rhel60, MiddleWareType.jboss, Classification.custom, 1, 1600);
		vm1.setDescription("Dette er en vm");
		vm1.addPuppetFact("puppetfact1", "myfact");
		vm1.addPuppetFact("puppetfact2", "myfact2");
		
		request.addVm(vm1);

		printXml(request);
	}

	/**
	 * Useful utility method for printing the XML when debugging unmarshalling
	 * app-config.xml
	 * */
	public void printXml(Object jaxbObject) {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(jaxbObject.getClass());
			final Marshaller marshaller = context.createMarshaller();
			StringWriter xml = new StringWriter();
			marshaller.marshal(jaxbObject, xml);
			System.out.println(prettyFormat(xml.toString(), 4));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private String prettyFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter writer = new StringWriter();
			StreamResult xmlOutput = new StreamResult(writer);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);

			return xmlOutput.getWriter().toString();

		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new RuntimeException("Error when formatting XML", e);
		}
	}

}
