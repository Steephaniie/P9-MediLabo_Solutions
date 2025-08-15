package fr.medilabo.solutions.front.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UrlConfiguration {
    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;
    @Value("${spring.application.name}")
    private String appName;

    public String getUrlSitePublic() {
        return gatewayUrl + "/" + appName;
    }
}
