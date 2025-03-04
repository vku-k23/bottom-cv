package com.cnpm.bottomcv;

import com.cnpm.bottomcv.dto.AppContactDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


/**
 * The main class of the application
 * test jira integration in scrum-1
 */

@SpringBootApplication
@EnableConfigurationProperties(AppContactDto.class)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@OpenAPIDefinition(
        info = @Info(
                title = "Bottom CV REST API Documentation",
                description = "Bottom CV REST API Documentation, the API is used to manage the bottom cv of the system",
                version = "v1",
                contact = @Contact(
                        name = "vku-k23",
                        email = "vietnq23ceb@vku.udn.vn, \n" +
                                "khapd.23it@vku.udn.vn\n" +
                                "trieunv.23it@vku.udn.vn\n" +
                                "tuantq.23it@vku.udn.vn",
                        url = "https://github.com/vku-k23/bottom-cv"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "github.com/vku-k23"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Bottom CV REST API Documentation",
                url = "https://localhost:8080/swagger-ui.html"
        )
)
public class BottomCvApplication {

    public static void main(String[] args) {
        SpringApplication.run(BottomCvApplication.class, args);
    }

}
