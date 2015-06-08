package no.nav.aura.basta.rest.vm;

import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.UnmarshalException;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.vmware.OrchestratorRequestFactory;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.backend.vmware.orchestrator.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.Vm;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXParseException;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

@Component
@Path("/vm/orders")
@Transactional
public class VmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(VmOrderRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private FasitRestClient fasitRestClient;

	@POST
	@Path("linux")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewPlainLinux(Map<String, String> map, @Context UriInfo uriInfo) {
		VMOrderInput input = new VMOrderInput(map);
		Guard.checkAccessToEnvironmentClass(input);
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new linux order {} with input {}", order.getId(), map);
		URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
		URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
		ProvisionRequest2 request = new ProvisionRequest2(OrchestratorEnvironmentClass.convert(input.getEnvironmentClass(), false), vmcreateCallbackUri,
				logCallabackUri);
		for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(Zone.fss, OSType.rhel60, MiddleWareType.linux, Classification.custom, input.getCpuCount(), input.getMemory());
            vm.setExtraDiskAsGig(input.getExtraDisk());
            vm.setDescription(input.getDescription());
			request.addVm(vm);
		}
		order = sendToOrchestrator(order, request);
		return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(createRichOrderDO(uriInfo, order)).build();
	}

	private Order sendToOrchestrator(Order order, OrchestatorRequest request) {

		WorkflowToken workflowToken;

		saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info);
		workflowToken = orchestratorService.provision(request);
		order.setExternalId(workflowToken.getId());
		order.setExternalRequest(convertXmlToString(request.censore()));

		order = orderRepository.save(order);
		return order;
	}
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response provisionNew(Map<String, String> map, @Context UriInfo uriInfo, @QueryParam("prepare") Boolean prepare) {

        VMOrderInput input = new VMOrderInput(map);
        input.addDefaultValueIfNotPresent(VMOrderInput.SERVER_COUNT, "1");
        input.addDefaultValueIfNotPresent(VMOrderInput.DISKS, "0");
        Guard.checkAccessToEnvironmentClass(input);
        if (input.getNodeType().equals(NodeType.PLAIN_LINUX)) {
            Guard.checkSuperUserAccess();
        }
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));

        URI vmInformationUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI resultUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new OrchestratorRequestFactory(order, User.getCurrentUser().getName(), vmInformationUri, resultUri, fasitRestClient).createProvisionOrder();
        WorkflowToken workflowToken;

        if (prepare == null || !prepare) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info);
            workflowToken = orchestratorService.send(request);
            order.setExternalId(workflowToken.getId());
            order.setExternalRequest(convertXmlToString(request.censore()));
        } else {
            order.setExternalRequest(convertXmlToString(request));
        }
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity(createRichOrderDO(uriInfo, order))
                .build();

    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public Response putXMLOrder(String string, @PathParam("orderId") Long orderId, @Context UriInfo uriInfo) {
        Guard.checkSuperUserAccess();
        ProvisionRequest request;
        try {
            request = XmlUtils.parseAndValidateXmlString(ProvisionRequest.class, string);
        } catch (UnmarshalException e) {
            SAXParseException spe = (SAXParseException) e.getLinkedException();
            return Response.status(400).entity(getValidationMessage(spe)).header("Content-type", "text/plain").build();
        }

        try {
            Guard.checkAccessToEnvironmentClass(ProvisionRequest.OrchestratorEnvClass.fromString(request.getEnvironmentClass()));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getLocalizedMessage()).header("Content-type", "text/plain").build();
        }

        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.findOne(orderId);
        if (order.getExternalId() == null) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info);
            order.setExternalRequest(convertXmlToString(request.censore()));
            order.setExternalId(workflowToken.getId());
            order.getInputAs(VMOrderInput.class).setXmlCustomized();
            order = orderRepository.save(order);
        }
        return Response.ok(new OrderDO(order, uriInfo)).build();
    }

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("{orderId}/statuslog")
    public void updateStatuslog(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO) {
		logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
		Order order = orderRepository.findOne(orderId);
		order.setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(orderStatusLogDO.getOption()));
		orderRepository.save(order);
		saveOrderStatusEntry(order, "Orchestrator", orderStatusLogDO.getText(), orderStatusLogDO.getType(), orderStatusLogDO.getOption());
	}

    private String getValidationMessage(SAXParseException spe) {
        String msg = spe.getLocalizedMessage();
        if (msg.contains(":")) {
            msg = msg.substring(msg.indexOf(":") + 1);
        }
        return "(" + spe.getLineNumber() + ":" + spe.getColumnNumber() + ")  - " + msg;
    }

    public static String convertXmlToString(OrchestatorRequest request) {
        return XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
    }


	public void createVmCallBack(Long orderId, List<OrchestratorNodeDO> vms) {
        logger.info("Received list of with {} vms as orderid {}", vms.size(), orderId);
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            Order order = orderRepository.findOne(orderId);
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatusAndNodeType(vm.getHostName(), ResultStatus.ACTIVE, order.getInputAs(VMOrderInput.class).getNodeType());
            fasitUpdateService.createFasitEntity(order, vm);
            orderRepository.save(order);
        }
    }


    private void saveOrderStatusEntry(Order order, String source, String text, String type, StatusLogLevel option) {
        order.addStatusLog(new OrderStatusLog(source, text, type, option));
        orderRepository.save(order);
    }

    protected OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        orderDO.setNextOrderId(orderRepository.findNextId(order.getId()));
        orderDO.setPreviousOrderId(orderRepository.findPreviousId(order.getId()));
        orderDO.setInput(order.getInputAs(MapOperations.class).copy());
        for (ResultDO result : order.getResult().asResultDO()) {
            result.setHistory(getHistory(uriInfo, result.getResultName()));
            orderDO.addResultHistory(result);
        }

        if (order.getExternalId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            orderDO.setExternalRequest(order.getExternalRequest());
        }

        return orderDO;
    }

    private List<OrderDO> getHistory(final UriInfo uriInfo, String result) {
        return FluentIterable.from(orderRepository.findRelatedOrders(result)).transform(new Function<Order, OrderDO>() {
            @Override
            public OrderDO apply(Order input) {
                return new OrderDO(input, uriInfo);
            }
        }).toList();
    }

    protected OrderDO enrichOrderDOStatus(OrderDO orderDO) {
        if (!orderDO.getStatus().isEndstate()) {
            String orchestratorOrderId = orderDO.getExternalId();
            if (orchestratorOrderId == null) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
            } else {
                Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                orderDO.setStatus(tuple.fst);
                orderDO.setErrorMessage(tuple.snd);
            }
            if (!orderDO.getStatus().isEndstate() && new DateTime(orderDO.getCreated()).isBefore(now().minus(standardHours(12)))) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Tidsavbrutt");
            }
        }
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
