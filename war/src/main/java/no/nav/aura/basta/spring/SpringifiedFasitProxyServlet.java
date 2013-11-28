package no.nav.aura.basta.spring;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

@SuppressWarnings("serial")
public class SpringifiedFasitProxyServlet extends ProxyServlet {

    @Value("${fasit.rest.api.url}")
    private String fasitUrl;

    public void init(final ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        final Map<String, String> configMap = ImmutableMap.of("log", Boolean.FALSE.toString(), "targetUri", fasitUrl);
        super.init(new ServletConfig() {
            public String getServletName() {
                return config.getServletName();
            }

            public ServletContext getServletContext() {
                return config.getServletContext();
            }

            public String getInitParameter(String name) {
                return configMap.get(name);
            }

            @SuppressWarnings("rawtypes")
            public Enumeration getInitParameterNames() {
                return Iterators.asEnumeration(configMap.keySet().iterator());
            }

        });
    }

}
