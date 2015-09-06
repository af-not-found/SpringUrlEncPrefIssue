package net.afnf.springUrlEncPrefIssue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    private static final Log logger = LogFactory.getLog(WebConfig.class);

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {

        // my implementation
        String fast = System.getProperty("fast");
        if (fast != null && fast.equals("true")) {
            logger.info("using CachingResourceUrlEncodingFilter");
            return new CachingResourceUrlEncodingFilter("/static/");
        }
        // original implementation
        else {
            logger.info("using ResourceUrlEncodingFilter");
            return new ResourceUrlEncodingFilter();
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/public/static/").setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addFixedVersionStrategy("" + System.currentTimeMillis(), "/**"));
    }

}