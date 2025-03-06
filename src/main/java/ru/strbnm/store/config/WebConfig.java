package ru.strbnm.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.RouterFunctions;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class WebConfig {

  @Value("${app.product.image.dir}")
  private String productImageDir;

  @Bean
  public RouterFunction<ServerResponse> staticResourceRouter() {
    return route(GET("/product_image_dir/{filename}"), request -> {
      String filename = request.pathVariable("filename");
      Path imagePath = Paths.get(productImageDir, filename);
      Resource imageResource = new PathResource(imagePath.toUri());

      return ok()
              .contentType(MediaType.IMAGE_PNG)
              .bodyValue(imageResource);
    })
            .and(RouterFunctions.resources("/css/**", new ClassPathResource("static/css/")))
            .and(RouterFunctions.resources("/imgs/**", new ClassPathResource("static/imgs/")));
  }
}


