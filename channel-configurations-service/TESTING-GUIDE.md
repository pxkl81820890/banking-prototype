# Channel Configurations Service - Testing Guide

## Overview
This guide helps you test the channel-configurations-service to verify feature flag functionality with ACL support.

## Prerequisites
- Java 17 or higher
- Maven installed
- Service running on port 8082

## Starting the Service

```bash
cd channel-configurations-service
mvn spring-boot:run
```

Wait for the service to start. You should see logs indicating:
- H2 database initialized
- Tables created (MASTER, ACL_CONFIG)
- Test data loaded from data.sql

## Test Data Overview

### MASTER Table (Feature Flags)
| ID | FEATURE_FLAG | IS_ACL_ENABLED | ACL_ID |
|----|--------------|----------------|--------|
| 1 | isArchiveEnquiryEnabled | true | 1000 |
| 2 | isTransferEnabled | true | 1001 |
| 3 | isPaymentEnabled | false | null |
| 4 | isStatementDownloadEnabled | true | 1000 |
| 5 | isChequeBookRequestEnabled | false | null |

### ACL_CONFIG Table (User Permissions)
| USER_ID | ACL_ID |
|---------|--------|
| 1119test1 | 1000 |
| 1119test2 | 1001 |
| 1119test3 | 1000 |
| 1119test3 | 1001 |

## Expected Results by User

### User: 1119test1 (ACL_ID: 1000)
**Expected feature flags:**
- ✅ isArchiveEnquiryEnabled: true (ACL 1000)
- ✅ isStatementDownloadEnabled: true (ACL 1000)
- ✅ isPaymentEnabled: true (public - no ACL required)
- ✅ isChequeBookRequestEnabled: true (public - no ACL required)
- ❌ isTransferEnabled: false (requires ACL 1001 - user doesn't have it)

### User: 1119test2 (ACL_ID: 1001)
**Expected feature flags:**
- ✅ isTransferEnabled: true (ACL 1001)
- ✅ isPaymentEnabled: true (public - no ACL required)
- ✅ isChequeBookRequestEnabled: true (public - no ACL required)
- ❌ isArchiveEnquiryEnabled: false (requires ACL 1000 - user doesn't have it)
- ❌ isStatementDownloadEnabled: false (requires ACL 1000 - user doesn't have it)

### User: 1119test3 (ACL_ID: 1000, 1001)
**Expected feature flags:**
- ✅ isArchiveEnquiryEnabled: true (ACL 1000)
- ✅ isTransferEnabled: true (ACL 1001)
- ✅ isStatementDownloadEnabled: true (ACL 1000)
- ✅ isPaymentEnabled: true (public - no ACL required)
- ✅ isChequeBookRequestEnabled: true (public - no ACL required)

### User: unknownUser (No ACL entries)
**Expected feature flags:**
- ✅ isPaymentEnabled: true (public - no ACL required)
- ✅ isChequeBookRequestEnabled: true (public - no ACL required)
- ❌ isArchiveEnquiryEnabled: false (requires ACL 1000 - user doesn't have it)
- ❌ isTransferEnabled: false (requires ACL 1001 - user doesn't have it)
- ❌ isStatementDownloadEnabled: false (requires ACL 1000 - user doesn't have it)

## Testing with cURL

### Test 1: User 1119test1
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: 1119test1"
```

**Expected Response:**
```json
{
  "isArchiveEnquiryEnabled": true,
  "isTransferEnabled": false,
  "isStatementDownloadEnabled": true,
  "isPaymentEnabled": true,
  "isChequeBookRequestEnabled": true
}
```

### Test 2: User 1119test2
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: 1119test2"
```

**Expected Response:**
```json
{
  "isArchiveEnquiryEnabled": false,
  "isTransferEnabled": true,
  "isStatementDownloadEnabled": false,
  "isPaymentEnabled": true,
  "isChequeBookRequestEnabled": true
}
```

### Test 3: User 1119test3 (Multiple ACLs)
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: 1119test3"
```

**Expected Response:**
```json
{
  "isArchiveEnquiryEnabled": true,
  "isTransferEnabled": true,
  "isStatementDownloadEnabled": true,
  "isPaymentEnabled": true,
  "isChequeBookRequestEnabled": true
}
```

### Test 4: Unknown User
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: unknownUser"
```

**Expected Response:**
```json
{
  "isArchiveEnquiryEnabled": false,
  "isTransferEnabled": false,
  "isStatementDownloadEnabled": false,
  "isPaymentEnabled": true,
  "isChequeBookRequestEnabled": true
}
```

### Test 5: Missing USER_ID Header
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags
```

**Expected Response:**
```json
{
  "timestamp": "2024-XX-XXTXX:XX:XX.XXX+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Required header 'USER_ID' is not present",
  "path": "/api/v1/feature-flags"
}
```

## Testing with Swagger UI

1. Open browser and navigate to: http://localhost:8082/swagger-ui.html
2. Find the `GET /api/v1/feature-flags` endpoint
3. Click "Try it out"
4. Enter a USER_ID in the header field (e.g., "1119test1")
5. Click "Execute"
6. Verify the response matches expected results

## Testing with H2 Console

1. Open browser and navigate to: http://localhost:8082/h2-console
2. Use the following connection details:
   - JDBC URL: `jdbc:h2:mem:channeldb`
   - Username: `sa`
   - Password: (leave empty)
3. Click "Connect"
4. Run queries to verify data:

```sql
-- View all feature flags
SELECT * FROM MASTER;

-- View all ACL configurations
SELECT * FROM ACL_CONFIG;

-- View feature flags for a specific user
SELECT m.* 
FROM MASTER m
WHERE m.IS_ACL_ENABLED = false
   OR m.ACL_ID IN (
       SELECT a.ACL_ID 
       FROM ACL_CONFIG a 
       WHERE a.USER_ID = '1119test1'
   );
```

## Verifying the Logic

The service implements the following logic:

1. **Public Features (IS_ACL_ENABLED = false)**
   - Available to ALL users regardless of ACL configuration
   - Examples: isPaymentEnabled, isChequeBookRequestEnabled

2. **ACL-Protected Features (IS_ACL_ENABLED = true)**
   - Only available to users with matching ACL_ID in ACL_CONFIG table
   - Examples: isArchiveEnquiryEnabled (ACL 1000), isTransferEnabled (ACL 1001)

3. **Users with Multiple ACLs**
   - Get access to all features from all their ACL_IDs
   - Example: 1119test3 has both ACL 1000 and 1001

4. **Users with No ACLs**
   - Only get access to public features (IS_ACL_ENABLED = false)

## Troubleshooting

### Service won't start
- Check if port 8082 is already in use
- Verify Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`

### Tables not created
- Verify `spring.jpa.hibernate.ddl-auto: create-drop` in application.yml
- Check logs for Hibernate DDL statements

### Test data not loaded
- Verify `spring.jpa.defer-datasource-initialization: true` in application.yml
- Verify `spring.sql.init.mode: always` in application.yml
- Check logs for SQL execution statements

### Wrong feature flags returned
- Verify test data in data.sql matches expected values
- Check H2 console to confirm data is loaded correctly
- Review service logs for debug information

## Next Steps

After verifying the service works correctly:
1. Integrate with Dashboard MFE to show/hide features based on user permissions
2. Add more feature flags as needed
3. Consider adding feature flag management endpoints (POST, PUT, DELETE)
4. Add integration tests
5. Consider migrating from H2 to a persistent database (PostgreSQL, MySQL, etc.)
