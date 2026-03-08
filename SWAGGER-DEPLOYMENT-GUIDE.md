# Swagger/OpenAPI Deployment Guide

## Overview

This guide explains where and how Swagger/OpenAPI documentation is deployed in different environments (dev, staging, production).

---

## Current Local Setup

**Local Development:**
- Login Service: http://localhost:8080/swagger-ui.html
- Authentication Service: http://localhost:8081/swagger-ui.html
- Channel Config Service: http://localhost:8082/swagger-ui.html

---

## Deployment Options for Non-Local Environments

### Option 1: Embedded Swagger UI (Recommended for Dev/Staging)

**How it works:**
- Swagger UI is bundled with your Spring Boot application
- Deployed automatically when you deploy your service
- Accessible at `{service-url}/swagger-ui.html`

**Deployment URLs:**

| Environment | Service | Swagger URL |
|-------------|---------|-------------|
| **Development** | Login Service | https://dev-login.banking.example.com/swagger-ui.html |
| **Development** | Auth Service | https://dev-auth.banking.example.com/swagger-ui.html |
| **Staging** | Login Service | https://staging-login.banking.example.com/swagger-ui.html |
| **Staging** | Auth Service | https://staging-auth.banking.example.com/swagger-ui.html |
| **Production** | Login Service | https://login.banking.example.com/swagger-ui.html |
| **Production** | Auth Service | https://auth.banking.example.com/swagger-ui.html |

**Configuration:**

```yaml
# application-dev.yml
springdoc:
  swagger-ui:
    enabled: true  # Enable in dev
  api-docs:
    enabled: true

# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false  # Disable in production for security
  api-docs:
    enabled: true   # Keep API docs for internal tools
```

**Pros:**
- ✅ No separate deployment needed
- ✅ Always in sync with deployed code
- ✅ Easy to access for developers

**Cons:**
- ⚠️ Security risk if exposed in production
- ⚠️ Increases application size slightly
- ⚠️ Each service has separate Swagger UI

---

### Option 2: Centralized Swagger UI (Recommended for Production)

**How it works:**
- Deploy a single Swagger UI instance
- Point it to OpenAPI JSON endpoints from all services
- Provides unified API documentation portal

**Architecture:**

```
┌─────────────────────────────────────────────────────────┐
│  Centralized Swagger UI                                 │
│  https://api-docs.banking.example.com                   │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Service Selector:                             │    │
│  │  [Login Service ▼]                             │    │
│  │  [Auth Service]                                │    │
│  │  [Channel Config Service]                      │    │
│  └────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                        ↓
        ┌───────────────┼───────────────┐
        ↓               ↓               ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Login Service│ │ Auth Service │ │ Config Svc   │
│ /api-docs    │ │ /api-docs    │ │ /api-docs    │
└──────────────┘ └──────────────┘ └──────────────┘
```

**Setup:**

1. **Deploy Swagger UI as standalone app:**

```dockerfile
# Dockerfile for centralized Swagger UI
FROM swaggerapi/swagger-ui:latest

# Copy custom configuration
COPY swagger-config.json /usr/share/nginx/html/swagger-config.json

ENV URLS="[ \
  { url: 'https://login.banking.example.com/v3/api-docs', name: 'Login Service' }, \
  { url: 'https://auth.banking.example.com/v3/api-docs', name: 'Auth Service' }, \
  { url: 'https://config.banking.example.com/v3/api-docs', name: 'Config Service' } \
]"

EXPOSE 8080
```

2. **Deploy to your infrastructure:**

```bash
# Docker
docker build -t banking-swagger-ui .
docker run -p 8080:8080 banking-swagger-ui

# Kubernetes
kubectl apply -f swagger-ui-deployment.yaml

# AWS ECS
aws ecs create-service --service-name swagger-ui ...
```

3. **Access:**
- URL: https://api-docs.banking.example.com
- Select service from dropdown
- View unified documentation

**Pros:**
- ✅ Single portal for all services
- ✅ Can disable Swagger UI in individual services
- ✅ Better security control
- ✅ Easier for external consumers

**Cons:**
- ⚠️ Requires separate deployment
- ⚠️ Need to update URLs when adding services
- ⚠️ CORS configuration required

---

### Option 3: API Gateway with Swagger Aggregation

**How it works:**
- API Gateway aggregates OpenAPI specs from all services
- Provides single endpoint for documentation
- Automatically discovers services

**Architecture:**

```
┌─────────────────────────────────────────────────────────┐
│  API Gateway                                            │
│  https://api.banking.example.com                        │
│                                                          │
│  /swagger-ui.html  ← Aggregated Swagger UI             │
│  /v3/api-docs      ← Merged OpenAPI specs              │
└─────────────────────────────────────────────────────────┘
                        ↓
        ┌───────────────┼───────────────┐
        ↓               ↓               ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Login Service│ │ Auth Service │ │ Config Svc   │
│ /api-docs    │ │ /api-docs    │ │ /api-docs    │
└──────────────┘ └──────────────┘ └──────────────┘
```

**Implementation (Spring Cloud Gateway):**

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("all-services")
                .pathsToMatch("/**")
                .build();
    }
    
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>();
            
            resources.add(swaggerResource("Login Service", 
                "/login-service/v3/api-docs"));
            resources.add(swaggerResource("Auth Service", 
                "/auth-service/v3/api-docs"));
            resources.add(swaggerResource("Config Service", 
                "/config-service/v3/api-docs"));
            
            return resources;
        };
    }
    
    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource resource = new SwaggerResource();
        resource.setName(name);
        resource.setLocation(location);
        resource.setSwaggerVersion("3.0");
        return resource;
    }
}
```

**Pros:**
- ✅ Single entry point
- ✅ Automatic service discovery (if configured)
- ✅ Consistent with API routing
- ✅ Better security (gateway handles auth)

**Cons:**
- ⚠️ Requires API Gateway setup
- ⚠️ More complex configuration
- ⚠️ Gateway becomes single point of failure

---

### Option 4: Static Documentation Site (Production-Safe)

**How it works:**
- Generate static OpenAPI YAML/JSON files
- Deploy to static hosting (S3, Netlify, GitHub Pages)
- Use Swagger UI or Redoc for rendering

**Setup:**

1. **Generate OpenAPI specs during build:**

```bash
# Maven plugin to generate OpenAPI spec
mvn springdoc-openapi:generate

# Output: target/openapi.json
```

2. **Deploy to static hosting:**

```bash
# AWS S3
aws s3 cp target/openapi.json s3://api-docs.banking.example.com/login-service/openapi.json
aws s3 cp swagger-ui/ s3://api-docs.banking.example.com/ --recursive

# GitHub Pages
cp target/openapi.json docs/
git add docs/
git commit -m "Update API docs"
git push
```

3. **Access:**
- URL: https://api-docs.banking.example.com
- Static HTML with Swagger UI
- No backend required

**Pros:**
- ✅ No runtime overhead
- ✅ Can be versioned (v1, v2, etc.)
- ✅ Very secure (read-only)
- ✅ Fast loading (CDN)

**Cons:**
- ⚠️ Manual update process
- ⚠️ Can get out of sync with code
- ⚠️ No "Try it out" functionality

---

## Recommended Approach by Environment

### Development Environment
**Use:** Embedded Swagger UI (Option 1)
- Enable Swagger UI in each service
- Developers can test directly
- URL: https://dev-{service}.banking.example.com/swagger-ui.html

### Staging Environment
**Use:** Centralized Swagger UI (Option 2)
- Single portal for QA team
- Aggregates all services
- URL: https://api-docs-staging.banking.example.com

### Production Environment
**Use:** Static Documentation Site (Option 4) OR Centralized Swagger UI with Auth (Option 2)
- Disable Swagger UI in services
- Provide read-only documentation
- Require authentication for access
- URL: https://api-docs.banking.example.com

---

## Security Considerations

### 1. Disable Swagger UI in Production Services

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false  # Disable interactive UI
  api-docs:
    enabled: true   # Keep JSON endpoint for internal tools
    path: /internal/api-docs  # Move to internal path
```

### 2. Add Authentication to Documentation Portal

```java
@Configuration
@EnableWebSecurity
public class SwaggerSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                .authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

### 3. Use API Gateway for Access Control

```yaml
# API Gateway routes
spring:
  cloud:
    gateway:
      routes:
        - id: swagger-docs
          uri: http://swagger-ui-service
          predicates:
            - Path=/api-docs/**
          filters:
            - name: AuthenticationFilter  # Custom auth filter
```

### 4. IP Whitelisting

```nginx
# Nginx configuration
location /swagger-ui {
    allow 10.0.0.0/8;      # Internal network
    allow 203.0.113.0/24;  # Office IP range
    deny all;
    
    proxy_pass http://swagger-ui-backend;
}
```

---

## AWS Deployment Examples

### Deploy to AWS ECS (Fargate)

```yaml
# swagger-ui-task-definition.json
{
  "family": "banking-swagger-ui",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "swagger-ui",
      "image": "swaggerapi/swagger-ui:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "URLS",
          "value": "[{url:'https://login.banking.example.com/v3/api-docs',name:'Login'},{url:'https://auth.banking.example.com/v3/api-docs',name:'Auth'}]"
        }
      ]
    }
  ]
}
```

```bash
# Deploy to ECS
aws ecs register-task-definition --cli-input-json file://swagger-ui-task-definition.json
aws ecs create-service \
  --cluster banking-cluster \
  --service-name swagger-ui \
  --task-definition banking-swagger-ui \
  --desired-count 1 \
  --launch-type FARGATE
```

### Deploy to AWS S3 + CloudFront (Static)

```bash
# Build static site
npm run build

# Upload to S3
aws s3 sync ./dist s3://api-docs.banking.example.com --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id E1234567890ABC \
  --paths "/*"
```

### Deploy to AWS API Gateway

```yaml
# serverless.yml
service: banking-api-docs

provider:
  name: aws
  runtime: nodejs18.x

functions:
  swaggerUI:
    handler: handler.swaggerUI
    events:
      - http:
          path: /
          method: get
      - http:
          path: /{proxy+}
          method: get

resources:
  Resources:
    SwaggerUIBucket:
      Type: AWS::S3::Bucket
      Properties:
        BucketName: banking-swagger-ui-assets
```

---

## Kubernetes Deployment

### Deployment YAML

```yaml
# swagger-ui-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: swagger-ui
  namespace: banking
spec:
  replicas: 2
  selector:
    matchLabels:
      app: swagger-ui
  template:
    metadata:
      labels:
        app: swagger-ui
    spec:
      containers:
      - name: swagger-ui
        image: swaggerapi/swagger-ui:latest
        ports:
        - containerPort: 8080
        env:
        - name: URLS
          value: |
            [
              {url: 'http://login-service/v3/api-docs', name: 'Login Service'},
              {url: 'http://auth-service/v3/api-docs', name: 'Auth Service'}
            ]
---
apiVersion: v1
kind: Service
metadata:
  name: swagger-ui
  namespace: banking
spec:
  selector:
    app: swagger-ui
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: swagger-ui-ingress
  namespace: banking
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api-docs.banking.example.com
    secretName: swagger-ui-tls
  rules:
  - host: api-docs.banking.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: swagger-ui
            port:
              number: 80
```

```bash
# Deploy to Kubernetes
kubectl apply -f swagger-ui-deployment.yaml

# Check status
kubectl get pods -n banking
kubectl get svc -n banking
kubectl get ingress -n banking
```

---

## Docker Compose (Multi-Service Local Testing)

```yaml
# docker-compose.yml
version: '3.8'

services:
  login-service:
    build: ./login-service
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  auth-service:
    build: ./authentication-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  swagger-ui:
    image: swaggerapi/swagger-ui:latest
    ports:
      - "8090:8080"
    environment:
      URLS: |
        [
          {url: 'http://login-service:8080/v3/api-docs', name: 'Login Service'},
          {url: 'http://auth-service:8081/v3/api-docs', name: 'Auth Service'}
        ]
    depends_on:
      - login-service
      - auth-service
```

```bash
# Start all services
docker-compose up -d

# Access Swagger UI
open http://localhost:8090
```

---

## Monitoring & Maintenance

### Health Checks

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  show-actuator: true  # Include actuator endpoints in Swagger
```

### Versioning

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API")
                        .version("v1.0.0")  // Version your API
                        .description("Multi-entity banking platform API"))
                .servers(List.of(
                        new Server()
                                .url("https://api.banking.example.com/v1")
                                .description("Production v1"),
                        new Server()
                                .url("https://api.banking.example.com/v2")
                                .description("Production v2")
                ));
    }
}
```

### Automated Updates

```yaml
# .github/workflows/update-api-docs.yml
name: Update API Documentation

on:
  push:
    branches: [main]
    paths:
      - 'src/**'
      - 'pom.xml'

jobs:
  update-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Generate OpenAPI spec
        run: mvn springdoc-openapi:generate
      
      - name: Deploy to S3
        run: |
          aws s3 cp target/openapi.json \
            s3://api-docs.banking.example.com/login-service/openapi.json
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
```

---

## Summary

### Quick Reference

| Environment | Recommended Approach | URL Pattern | Security |
|-------------|---------------------|-------------|----------|
| **Local** | Embedded Swagger UI | localhost:8080/swagger-ui.html | None |
| **Dev** | Embedded Swagger UI | dev-{service}.example.com/swagger-ui.html | Basic Auth |
| **Staging** | Centralized Swagger UI | api-docs-staging.example.com | OAuth2 |
| **Production** | Static Docs OR Centralized with Auth | api-docs.example.com | OAuth2 + IP Whitelist |

### Best Practices

1. ✅ **Disable Swagger UI in production services** (security)
2. ✅ **Use centralized portal for staging/production** (better UX)
3. ✅ **Add authentication to documentation** (access control)
4. ✅ **Version your APIs** (backward compatibility)
5. ✅ **Automate documentation updates** (CI/CD)
6. ✅ **Use HTTPS everywhere** (security)
7. ✅ **Monitor documentation access** (analytics)

### Next Steps

1. Choose deployment approach based on your environment
2. Configure security (authentication, IP whitelisting)
3. Set up CI/CD for automated updates
4. Test access from different environments
5. Document the URLs for your team

