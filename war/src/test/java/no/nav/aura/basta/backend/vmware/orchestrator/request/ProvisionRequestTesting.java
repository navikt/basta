package no.nav.aura.basta.backend.vmware.orchestrator.request;

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

import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest.OrchestratorEnvClass;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.OSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class ProvisionRequestTesting {

    private static final Logger logger = LoggerFactory.getLogger(ProvisionRequestTesting.class);

	// @Test
    public void print() {
        ProvisionRequest req = new ProvisionRequest();
        req.setApplication("app");
        req.setChangeDeployerPassword(false);
        req.setEnvironmentClass(OrchestratorEnvClass.utv.getName());
        req.setEnvironmentId("u11");
        req.setOrderedBy("v137023");
        req.setOwner("v137023");
        req.setResultCallbackUrl(URI.create("http://thisserver.devillo.no/vmware/result/123123123"));
        req.setStatusCallbackUrl(URI.create("http://thisserver.devillo.no/vmware/status/123123123"));
        req.setRole(Role.was);
        req.setZone(Zone.fss);

        VApp vapp = new VApp(Site.so8, "description");
        Vm vm = new Vm(OSType.rhel60, MiddleWareType.jb, 3, 4, new Disk(5), new Disk(12));
        vm.setCustomFacts(ImmutableList.of(new Fact(FactType.cloud_app_was_mgr, "hurra")));
        vapp.addVm(vm);
        req.getvApps().add(vapp);

        printXml(req);
    }

    /**
     * Useful utility method for printing the XML when debugging unmarshalling app-config.xml
     * */
    public void printXml(Object jaxbObject) {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(jaxbObject.getClass());
            final Marshaller marshaller = context.createMarshaller();
            StringWriter xml = new StringWriter();
            marshaller.marshal(jaxbObject, xml);
            logger.info(prettyFormat(xml.toString(), 4));
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