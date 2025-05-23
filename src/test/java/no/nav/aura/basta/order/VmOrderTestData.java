package no.nav.aura.basta.order;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

public class VmOrderTestData {

    public static Order newProvisionOrderWithDefaults(NodeType nodeType) {
        VMOrderInput input = new VMOrderInput();
        input.setNodeType(nodeType);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setEnvironmentName("u1");
        input.setZone(Zone.fss);
        input.addDefaultValueIfNotPresent(VMOrderInput.SERVER_COUNT, "1");
        return new Order(OrderType.VM, OrderOperation.CREATE, input);
    }

    private static Order newOrderOfType(OrderOperation orderOperation, String... hostnames) {
        return new Order(OrderType.VM, orderOperation, new HostnamesInput(hostnames));
    }

    public static Order newDecommissionOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.DELETE, hostnames);
    }

    public static Order newStopOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.STOP, hostnames);
    }

    public static Order newStartOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.START, hostnames);
    }

}
