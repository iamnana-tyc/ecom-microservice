# E-Commerce Microservices Backend

## Overview

This project is a **production-style microservices backend** for an e-commerce system built with the Spring ecosystem. It demonstrates **event-driven communication, service-to-service interaction, resilience patterns, and security integration**.

**This project simulates production-like concerns such as eventual consistency, fault tolerance, and secure inter-service communication.**

The focus is on **how distributed services coordinate reliably**, not just CRUD operations.


## Architecture

### Services

* **API Gateway** – Entry point, routing + security
* **Eureka Server** – Service discovery
* **Config Server** – Centralized configuration
* **Order Service** – Core orchestration (orders + event publishing)
* **Product Service** – Product data (PostgreSQL)
* **User Service** – User data (MongoDB + Keycloak integration)
* **Notification Service** – Event consumer (async processing)


## Core Patterns Implemented

* API Gateway Pattern
* Service Discovery (Eureka)
* Event-Driven Architecture (Kafka via Spring Cloud Stream)
* Circuit Breaker, Retry, Rate Limiting (Resilience4j)
* Polyglot Persistence
* OAuth2 / OpenID Connect (Keycloak)


## Order Processing Flow (End-to-End)

1. Client sends request → **API Gateway**
2. Gateway authenticates request (JWT via Keycloak)
3. Request routed → **Order Service**
4. Order Service:

    * Fetches cart
    * Calls Product & User services (via service discovery)
    * Persists order (PostgreSQL)
    * Clears cart
5. Publishes event:

   ```java
   streamBridge.send("createOrder-out-0", orderCreatedEvent);
   ```
6. **Notification Service** consumes event:

    * Logs order + user info
    * Simulates async notification processing

  System is **eventually consistent** (order creation does not depend on notification success)


## Event Design

### Event: `OrderCreatedEvent`

```json
{
  "orderId": 1,
  "userId": "user-123",
  "status": "CONFIRMED",
  "items": [...],
  "totalAmount": 150.00,
  "createdAt": "2026-01-01T10:00:00"
}
```

### Producer

* Uses **Spring Cloud Stream + StreamBridge**
* Dynamic binding: `createOrder-out-0`

### Consumer

* Functional style (`Consumer<T>` bean)
* Decoupled from producer implementation

---

## Inter-Service Communication

* Implemented using **Spring RestClient + Service Discovery**
* Uses `@LoadBalanced` for resolving service names via Eureka

Example:

```java
baseUrl("http://product-service")
```

* HTTP interfaces generated via `HttpServiceProxyFactory`
* Clean separation between client interface and implementation


## Resilience (Resilience4j)

### Circuit Breaker + Retry Example

```java
@CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallback")
@Retry(name = "retryBreaker", fallbackMethod = "addToCartFallback")
public boolean addToCart(...)
```

### Behavior

* Retries transient failures automatically
* Circuit opens after failure threshold
* Fallback prevents system crash

### Fallback Strategy

```java
public boolean addToCartFallback(..., Throwable ex) {
    return false;
}
```
  Ensures **graceful degradation** when downstream services fail


## Messaging Infrastructure

### Kafka (Primary Choice)

Kafka was chosen as the primary message broker due to its strengths in **event-driven systems and high-throughput streaming**:

* Designed for **event streaming**, making it ideal for propagating domain events (e.g., `OrderCreatedEvent`)
* Supports **high throughput and horizontal scalability** via partitions
* Provides **durability and fault tolerance** through replication
* Enables **decoupled communication** between services (producer does not depend on consumer availability)
* Works seamlessly with **Spring Cloud Stream**, reducing boilerplate configuration

 In this project, Kafka allows the system to evolve toward **event-driven architecture patterns** commonly used in real-world e-commerce platforms.


### RabbitMQ (Alternative Implementation)

* Implemented manually using:

    * Queue
    * Topic Exchange
    * Binding
    * RabbitTemplate
    * JSON Message Converter

* Demonstrates understanding of:

    * Exchange routing
    * Message serialization
    * Infrastructure provisioning via `AmqpAdmin`

### Why Kafka over RabbitMQ in this project?

* Kafka is better suited for **event streaming pipelines**, while RabbitMQ is optimized for **message queuing and task distribution**
* Kafka enables **event replay and log-based architecture**, which is valuable for scaling systems later
* Integration with Spring Cloud Stream made Kafka **simpler to manage in a microservices context**

RabbitMQ was explored to understand traditional messaging, but Kafka was selected for **scalability and event-driven design alignment**



### RabbitMQ (Alternative Implementation)

* Implemented manually using:

    * Queue
    * Topic Exchange
    * Binding
    * RabbitTemplate
    * JSON Message Converter

* Demonstrates understanding of:

    * Exchange routing
    * Message serialization
    * Infrastructure provisioning via `AmqpAdmin`

 Kafka was ultimately chosen for event streaming simplicity with Spring Cloud Stream


## Security (Keycloak Integration)

* OAuth2 Resource Server configured in Gateway
* JWT validation + role extraction

### Role Extraction

```java
jwt.getClaimAsMap("resource_access")
```

* Converts roles into Spring Security authorities

### Admin Operations

* Programmatic user creation via Keycloak Admin API
* Token-based authentication
* Role assignment to users


## Observability

* Distributed tracing via Zipkin
* Spring Boot Actuator endpoints:

    * health
    * metrics
    * circuit breakers

## Database Design

* **PostgreSQL** → Orders & Products (transactional consistency)
* **MongoDB** → Users (flexible schema)


## Key Engineering Decisions

### 1. StreamBridge over Direct Bindings

Used for **dynamic event publishing**, avoiding tight coupling to function definitions.

### 2. Event-Driven Notifications

Decouples order processing from side effects (notifications)

### 3. Resilience at Service Boundary

Circuit breaker applied at **client level**, not controller level

### 4. Polyglot Persistence

Different databases per service based on use-case


## Challenges & Solutions

### Kafka Deserialization Issues

**Problem:**

* Consumer failed to deserialize event payload
* Caused by inconsistent message structure/config

**Solution:**

* Standardized event DTO across services
* Used Spring Cloud Stream with JSON serialization
* Switched to StreamBridge for controlled publishing


## What This Project Demonstrates

* Real microservices communication (sync + async)
* Fault tolerance using Resilience4j
* Secure API Gateway with JWT
* Event-driven architecture with Kafka
* Service discovery and load balancing


## Future Improvements

* Dead Letter Queue (DLQ)
* Real notification system (email/SMS)
* Kubernetes deployment
* Centralized logging (ELK)


## Author

Nana Owusu Appiah

## Key notes

This project demonstrates **practical backend engineering skills**, including distributed systems design, asynchronous messaging, resilience handling, and secure service communication.

It reflects real-world patterns used in scalable systems rather than basic CRUD applications.
