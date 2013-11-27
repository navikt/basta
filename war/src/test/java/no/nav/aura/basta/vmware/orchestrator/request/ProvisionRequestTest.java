package no.nav.aura.basta.vmware.orchestrator.request;

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

import no.nav.aura.basta.vmware.orchestrator.request.Disk;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;

import org.junit.Test;

public class ProvisionRequestTest {

    @Test
    public void print() {
        ProvisionRequest req = new ProvisionRequest();
        req.setApplication("app");
        req.setChangeDeployerPassword(false);
        req.setEnvironmentClass(envClass.utv);
        req.setEnvironmentId("u11");
        req.setOrderedBy("v137023");
        req.setOwner("v137023");
        req.setResultCallbackUrl(URI.create("http://thisserver.devillo.no/vmware/result/123123123"));
        req.setStatusCallbackUrl(URI.create("http://thisserver.devillo.no/vmware/status/123123123"));
        req.setRole(Role.was);
        req.setZone(Zone.fss);

        VApp vapp = new VApp(Site.so8, "description");
        Vm vm = new Vm(OSType.rhel60, MiddleWareType.jb, 3, 15, 4, new Disk(5), new Disk(12));
        vm.getCustomFacts().add(new Fact("myfact", "hurra"));
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
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();

        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Error when formatting XML", e);
        }
    }

}
