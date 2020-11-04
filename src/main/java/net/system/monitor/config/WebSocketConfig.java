package net.system.monitor.config;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
//@EnableWebSocket
@ConditionalOnWebApplication
public class WebSocketConfig implements WebMvcConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public MySpringConfigurator springConfigurator() {
        return new MySpringConfigurator();
    }

    public static class MySpringConfigurator extends Configurator implements ApplicationContextAware {

        private static volatile BeanFactory context;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            MySpringConfigurator.context = applicationContext; // NOSONAR
        }

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return context.getBean(endpointClass);
        }
    }

}
