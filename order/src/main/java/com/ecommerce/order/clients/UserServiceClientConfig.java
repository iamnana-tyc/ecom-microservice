package com.ecommerce.order.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class UserServiceClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder userServiceRestBuilder(){
        return RestClient.builder();
    }

    @Bean
    public UserServiceClient userServiceClientInterface(
            @Qualifier("userServiceRestBuilder") RestClient.Builder userServiceBuilder){
        RestClient restClient = userServiceBuilder
                .baseUrl("http://user-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        ((request, response) -> Optional.empty()))
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factor = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        UserServiceClient service = factor.createClient(UserServiceClient.class);

        return service;
    }
}
