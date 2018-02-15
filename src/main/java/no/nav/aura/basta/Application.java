package no.nav.aura.basta;

import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

@SpringBootApplication
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

    public static void main(String[] args) throws Exception {
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