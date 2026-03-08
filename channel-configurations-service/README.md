# Channel Configurations Service

Feature flag service with ACL (Access Control List) support for banking platform.

## Overview

This service manages feature flags and user access permissions. It determines which features are available to each user based on:
1. **Public features**: Available to all users (IS_ACL_ENABLED = false)
2. **ACL-protected features**: Available only to users with matching ACL IDs

## Database Schema

### MASTER Table
Stores feature flag definitions with ACL configuration.

| Column | Type | Description |
|--------|------|-------------|
| ID | BIGINT | Primary key |
| FEATURE_FLAG | VARCHAR | Feature flag name (unique) |
| IS_ACL_ENABLED | BOOLEAN | Whether ACL check is required |
| ACL_ID | BIGINT | ACL identifier (null if IS_ACL_ENABLED = false) |

### ACL_CONFIG Table
Maps users to ACL IDs for access control.

| Column | Type | Description |
|--------|------|-------------|
| ID | BIGINT | Primary key |
| USER_ID | VARCHAR | User identifier |
| ACL_ID | BIGINT | ACL identifier |

## How It Works

### Example Scenario

**Database State:**

MASTER table:
```
ID | FEATURE_FLAG              | IS_ACL_ENABLED | ACL_ID
1  | isArchiveEnquiryEnabled   | true           | 1000
2  | isTransferEnabled         | true           | 1001
3  | isPaymentEnabled          | false          | null
```

ACL_CONFIG table:
```
USER_ID    | ACL_ID
1119test1  | 1000
```

**Request:**
```http
GET /api/v1/feature-flags
Header: USER_ID: 1119test1
```

**Response:**
```json
{
  "userId": "1119test1",
  "featureFlags": {
    "isArchiveEnquiryEnabled": true,
    "isPaymentEnabled": true
  }
}
```

**Explanation:**
- `isArchiveEnquiryEnabled` = true (user has ACL_ID 1000)
- `isPaymentEnabled` = true (public feature, no ACL required)
- `isTransferEnabled` = NOT included (requires ACL_ID 1001, user doesn't have it)

## API Endpoints

### Get Feature Flags
```http
GET /api/v1/feature-flags
Header: USER_ID: {userId}
```

Returns all feature flags available to the specified user.

## Running the Service

### Prerequisites
- Java 21
- Maven 3.8+

### Start the Service
```bash
cd channel-configurations-service
mvn spring-boot:run
```

The service will start on port **8082**.

### Access Points
- **API**: http://localhost:8082/api/v1/feature-flags
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **H2 Console**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:channeldb`
  - Username: `sa`
  - Password: (leave empty)

## Test Data

The service comes with pre-populated test data:

### Feature Flags
1. **isArchiveEnquiryEnabled** - ACL protected (ACL_ID: 1000)
2. **isTransferEnabled** - ACL protected (ACL_ID: 1001)
3. **isPaymentEnabled** - Public (no ACL)
4. **isStatementDownloadEnabled** - ACL protected (ACL_ID: 1000)
5. **isChequeBookRequestEnabled** - Public (no ACL)

### Test Users
- **1119test1**: Has ACL_ID 1000
- **1119test2**: Has ACL_ID 1001
- **1119test3**: Has both ACL_ID 1000 and 1001

## Testing

### Test with curl

**User with ACL_ID 1000:**
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags \
  -H "USER_ID: 1119test1"
```

Expected response:
```json
{
  "userId": "1119test1",
  "featureFlags": {
    "isArchiveEnquiryEnabled": true,
    "isStatementDownloadEnabled": true,
    "isPaymentEnabled": true,
    "isChequeBookRequestEnabled": true
  }
}
```

**User with ACL_ID 1001:**
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags \
  -H "USER_ID: 1119test2"
```

Expected response:
```json
{
  "userId": "1119test2",
  "featureFlags": {
    "isTransferEnabled": true,
    "isPaymentEnabled": true,
    "isChequeBookRequestEnabled": true
  }
}
```

**User with both ACL IDs:**
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags \
  -H "USER_ID: 1119test3"
```

Expected response:
```json
{
  "userId": "1119test3",
  "featureFlags": {
    "isArchiveEnquiryEnabled": true,
    "isTransferEnabled": true,
    "isStatementDownloadEnabled": true,
    "isPaymentEnabled": true,
    "isChequeBookRequestEnabled": true
  }
}
```

### Test with Swagger UI

1. Open http://localhost:8082/swagger-ui.html
2. Click on "GET /api/v1/feature-flags"
3. Click "Try it out"
4. Enter USER_ID in the header field (e.g., "1119test1")
5. Click "Execute"
6. View the response

## Architecture

The service follows hexagonal architecture:

```
┌─────────────────────────────────────────┐
│         Infrastructure Layer            │
│  ┌───────────────────────────────────┐  │
│  │   FeatureFlagController           │  │
│  │   (REST API)                      │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  ┌───────────────────────────────────┐  │
│  │   FeatureFlagService              │  │
│  │   (Business Logic)                │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         Repository Layer                │
│  ┌──────────────┐  ┌─────────────────┐ │
│  │FeatureFlagMaster│  │  AclConfig    │ │
│  │Repository    │  │  Repository     │ │
│  └──────────────┘  └─────────────────┘ │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│          H2 Database                    │
│  ┌──────────────┐  ┌─────────────────┐ │
│  │   MASTER     │  │   ACL_CONFIG    │ │
│  └──────────────┘  └─────────────────┘ │
└─────────────────────────────────────────┘
```

## Integration with Other Services

This service can be called by:
- **Dashboard MFE**: To show/hide features based on user permissions
- **Login Service**: To include feature flags in login response
- **Other services**: To check if a feature is enabled for a user

### Example Integration

```javascript
// In Dashboard MFE
const fetchFeatureFlags = async (userId) => {
  const response = await fetch('http://localhost:8082/api/v1/feature-flags', {
    headers: {
      'USER_ID': userId
    }
  });
  const data = await response.json();
  return data.featureFlags;
};

// Use feature flags
const featureFlags = await fetchFeatureFlags('1119test1');
if (featureFlags.isArchiveEnquiryEnabled) {
  // Show "View Archived Cheques" button
}
```

## Configuration

### Change Port
Edit `application.yml`:
```yaml
server:
  port: 8082  # Change to desired port
```

### Use PostgreSQL Instead of H2
1. Add PostgreSQL dependency to `pom.xml`
2. Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/channeldb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Future Enhancements

1. **Caching**: Add Redis cache for feature flags
2. **Admin API**: CRUD operations for feature flags
3. **Audit Log**: Track feature flag changes
4. **Real-time Updates**: WebSocket for flag changes
5. **A/B Testing**: Percentage-based rollouts
6. **Feature Flag History**: Track when flags were enabled/disabled
7. **User Groups**: Support for group-based ACLs
8. **API Key Authentication**: Secure the API endpoints
