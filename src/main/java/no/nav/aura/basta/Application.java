package no.nav.aura.basta;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import no.nav.aura.basta.repository.OrderRepository;

@SpringBootApplication
@ComponentScan(basePackages = "no.nav.aura.basta")
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
        if (appContext == null) {
            System.out.println("No context loader listener available");
        }
//        if (appContext != null) {
//            servletContext.setInitParameter("resteasy.scan", "true");
//            servletContext.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
//            servletContext.addListener(new ResteasyBootstrap());
//        } else {
//            System.out.println("No context loader listener available");
//        }
    }
}