package no.nav.aura.basta.rest.vm;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import no.nav.aura.basta.domain.input.vm.ServerSize;
import no.nav.aura.basta.util.SerializableFunction;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
@Path("/vm/choices")
public class ChoicesRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ChoiceInfo get() {
        return new ChoiceInfo();
    }

    @SuppressWarnings("unused")
    private static class ChoiceInfo {
        @SuppressWarnings("serial")
        public Map<ServerSize, ServerSizeInfo> getServerSizes() {
            return Maps.asMap(Sets.newHashSet(ServerSize.values()), new SerializableFunction<ServerSize, ServerSizeInfo>() {
                public ServerSizeInfo process(ServerSize input) {
                    return new ServerSizeInfo(input);
                }
            });
        }
    }

    @SuppressWarnings("unused")
    private static class ServerSizeInfo {
        private ServerSize serverSize;

        public ServerSizeInfo(ServerSize serverSize) {
            this.serverSize = serverSize;
        }

        public int getCpuCount() {
            return serverSize.cpuCount;
        }

        public int getRamMB() {
            return serverSize.ramMB;
        }

        public int getExternDiskMB() {
            return serverSize.externDiskMB;
        }
    }
}
