package no.nav.aura.basta;

import no.nav.aura.basta.repository.OrderRepository;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.ServletContext;

@SpringBootApplication
@EnableCaching
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

    @Inject
    OrderRepository orderRepository;

    public static void main(String[] args) {
        SpringApplication springApp = new SpringApplication(Application.class);
        springApp.setBannerMode(Banner.Mode.OFF);
        springApp.run(args);
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        WebApplicationContext appContext = createRootApplicationContext(servletContext);
        if (appContext != null) {
            servletContext.setInitParameter("resteasy.scan", "true");
            servletContext.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
            servletContext.addListener(new ResteasyBootstrap());
        } else {
            System.out.println("No context loader listener available");
        }
    }
}