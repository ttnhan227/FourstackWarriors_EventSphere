package fpt.aptech.eventsphere.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/css/**",
                "/js/**",
                "/images/**",
                "/sass/**",
                "/webfonts/**")
            .addResourceLocations(
                    "classpath:/static/css/",
                    "classpath:/static/js/",
                    "classpath:/static/images/",
                    "classpath:/static/sass/",
                    "classpath:/static/webfonts/");
    }
}
