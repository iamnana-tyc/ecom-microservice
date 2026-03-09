package com.ecommerce.order.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class ProductServiceClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilderLoadBalance(){
        return RestClient.builder();
    }

    @Bean
    public ProductServiceClient productServiceInterface(
             @Qualifier("restClientBuilderLoadBalance") RestClient.Builder productServicebuilder ){
        RestClient restClient = productServicebuilder
                .baseUrl("http://product-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        ((request, response) -> Optional.empty()))
                .build();

        RestClientAdapter clientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(clientAdapter)
                .build();

        ProductServiceClient serviceClient = factory.createClient(ProductServiceClient.class);

        return serviceClient;
    }
}
