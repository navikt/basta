package no.nav.aura.basta.backend;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import no.nav.aura.basta.backend.vmware.OrchestratorServiceImpl;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutor;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

public class OrchestratorServiceImplTest {

    @Test
    public void getStatus_Ok() throws Exception {
        assertStatus(createAttrsFromFile("status_response_success.xml"), Tuple.of(OrderStatus.SUCCESS, (String) null));
    }

    @Test
    public void getStatus_Failure() throws Exception {
        assertStatus(createAttrsFromFile("status_response_failure.xml"), Tuple.of(OrderStatus.FAILURE, (String) null));
    }

    @Test
    public void getStatus_FailureWithMessage() throws Exception {
        Tuple<OrderStatus, String> expect = Tuple.of(OrderStatus.FAILURE, "Input was invalid: vAppApplication must be defined (Workflow:Provision vApp - new xml and cleanup / Handle XML (item113)#27)");
        assertStatus(createAttrsFromFile("status_response_failure_with_message.xml"), expect);
    }

    @Test
    public void getStatus_ProcessingWithEmptyXmlResponse() throws Exception {
        assertStatus(createAttrsFromXmlResponseValue(null), Tuple.of(OrderStatus.PROCESSING, (String) null));
    }

    @Test
    public void getStatus_ProcessingWithNoMessageResponse() throws Exception {
        assertStatus(null, Tuple.of(OrderStatus.ERROR, (String) null));
    }

    @Test
    public void getStatus_Error() throws Exception {
        assertStatus(Lists.newArrayList((WorkflowTokenAttribute) null), Tuple.of(OrderStatus.ERROR, "Empty response"));
    }

    @Test
    public void getDecommisioningStatus_Success() throws Exception {
        assertStatus(createAttrsFromFile("decommission_status_response_success.xml"), Tuple.of(OrderStatus.SUCCESS, (String) null));
    }

    @Test
    public void getDecommisioningStatus_Failure() throws Exception {
        assertStatus(createAttrsFromFile("decommission_status_response_failure.xml"), Tuple.of(OrderStatus.FAILURE, "Failure on e34jbsl01151: removed from Satellite [false]"));
    }

    private List<WorkflowTokenAttribute> createAttrsFromFile(String filename) throws IOException {
        return createAttrsFromXmlResponseValue(IOUtils.toString(getClass().getResourceAsStream(filename)));
    }

    private List<WorkflowTokenAttribute> createAttrsFromXmlResponseValue(String xmlResponseValue) throws IOException {
        WorkflowTokenAttribute attr = new WorkflowTokenAttribute();
        attr.setName("XmlResponse");
        attr.setType("string");
        attr.setValue(xmlResponseValue);
        return Lists.newArrayList(attr);
    }

    private void assertStatus(List<WorkflowTokenAttribute> attrs, Tuple<OrderStatus, String> expect) throws IOException {
        WorkflowExecutor workflowExecutor = mock(WorkflowExecutor.class);
        when(workflowExecutor.getStatus(null)).thenReturn(attrs);
        Tuple<OrderStatus, String> tuple = new OrchestratorServiceImpl(workflowExecutor).getOrderStatus(null);
        assertThat(tuple, equalTo(expect));
    }

}
