# Swagger UI Setup Guide

## What I've Added

I've configured both services with Springdoc OpenAPI to provide interactive Swagger UI documentation.

## Setup Steps

### 1. Install Dependencies

Run Maven install for both services:

```bash
# Login Service
cd login-service
mvn clean install

# Authentication Service
cd ../authentication-service
mvn clean install
```

### 2. Start the Services

**Terminal 1 - Authentication Service:**
```bash
cd authentication-service
mvn spring-boot:run
```

**Terminal 2 - Login Service:**
```bash
cd login-service
mvn spring-boot:run
```

### 3. Access Swagger UI

Once both services are running:

**Login Service Swagger UI:**
- URL: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

**Authentication Service Swagger UI:**
- URL: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api-docs

## Using Swagger UI

1. Open the Swagger UI URL in your browser
2. You'll see all available endpoints with their schemas
3. Click on any endpoint to expand it
4. Click the "Try it out" button (top right of the endpoint section)
5. Fill in the request body/parameters
6. Click "Execute" to test the endpoint
7. View the response directly in the browser

**Note:** If you don't see the "Try it out" button:
- Make sure you've clicked on an endpoint to expand it first
- Check that `tryItOutEnabled: true` is set in `application.yml`
- Try refreshing the page or clearing browser cache
- Verify the service restarted after configuration changes

## Features

- **Interactive Testing**: Test all endpoints without curl or Postman
- **Schema Validation**: See request/response schemas with examples
- **Auto-Generated**: Documentation updates automatically when you add new endpoints
- **Try It Out**: Execute real API calls from the browser
- **Response Examples**: View success and error response examples

## What Was Added

### Dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

**Note:** Version 2.7.0 is required for compatibility with Spring Boot 3.4.1. Earlier versions (like 2.3.0) will cause `NoSuchMethodError` with `ControllerAdviceBean`.

### Configuration Files
- `src/main/resources/application-swagger.yml` - Springdoc configuration
- `src/main/java/.../infrastructure/config/OpenApiConfig.java` - OpenAPI bean configuration

### Static OpenAPI Specs
- `docs/api-spec.yaml` - Hand-crafted OpenAPI 3.0 specification (for reference)

## Automatic Updates

The doc-hook at `common-config/.kiro/hooks/doc-hook.md` will automatically:
- Update `docs/api-spec.yaml` when controllers change
- Springdoc will auto-generate documentation from your controller annotations
- Both stay in sync!

## Troubleshooting

**Swagger UI shows "Failed to load API definition" with 500 error?**
- **Version incompatibility**: If you see `NoSuchMethodError: ControllerAdviceBean.<init>`, upgrade Springdoc to 2.7.0 or later (required for Spring Boot 3.4.x)
- Check that all DTOs have `@Schema` annotations (including ErrorResponse)
- Verify `springdoc` configuration exists in `application.yml`:
  ```yaml
  springdoc:
    api-docs:
      path: /v3/api-docs
      enabled: true
    swagger-ui:
      path: /swagger-ui.html
      enabled: true
  ```
- Check application logs for Springdoc initialization errors
- Restart the service: `mvn clean install && mvn spring-boot:run`
- Verify Security config allows: `/swagger-ui/**`, `/v3/api-docs/**`

**Swagger UI not loading?**
- Ensure the service is running: `curl http://localhost:8080/actuator/health`
- Check for port conflicts
- Verify Maven dependencies installed: `mvn dependency:tree | grep springdoc`

**Endpoints not showing?**
- Springdoc auto-discovers `@RestController` classes
- Ensure controllers are in the component scan path
- Check application logs for errors

## Alternative: View Static YAML

If you prefer to view the static OpenAPI spec:
1. Go to https://editor.swagger.io/
2. File → Import File
3. Select `docs/api-spec.yaml` from either service
4. View and test from Swagger Editor
