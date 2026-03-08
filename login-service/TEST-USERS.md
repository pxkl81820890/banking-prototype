# Login Service - Test Users

## H2 Database Configuration

The login-service now uses H2 in-memory database for testing.

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:logindb`
- Username: `sa`
- Password: (leave empty)

## Test Users

All test users have the same password: `password123`

### User 1: testuser (Default Test User)
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "testuser",
  "password": "password123",
  "currency": "SGD"
}
```
- **User ID**: testuser
- **Bank**: 101
- **Branch**: 1119
- **Currency**: SGD

### User 2: 1119test1 (Matches Channel Config User)
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "1119test1",
  "password": "password123",
  "currency": "SGD"
}
```
- **User ID**: 1119test1
- **Bank**: 101
- **Branch**: 1119
- **Currency**: SGD
- **Feature Flags**: Has ACL 1000 (Archive Enquiry, Statement Download)

### User 3: 1119test2 (Matches Channel Config User)
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "1119test2",
  "password": "password123",
  "currency": "USD"
}
```
- **User ID**: 1119test2
- **Bank**: 101
- **Branch**: 1119
- **Currency**: USD
- **Feature Flags**: Has ACL 1001 (Transfer)

### User 4: 1119test3 (Matches Channel Config User)
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "1119test3",
  "password": "password123",
  "currency": "EUR"
}
```
- **User ID**: 1119test3
- **Bank**: 101
- **Branch**: 1119
- **Currency**: EUR
- **Feature Flags**: Has ACL 1000 and 1001 (All features)

### User 5: adminuser
```json
{
  "bankCode": "102",
  "branchCode": "2001",
  "username": "adminuser",
  "password": "password123",
  "currency": "SGD"
}
```
- **User ID**: adminuser
- **Bank**: 102
- **Branch**: 2001
- **Currency**: SGD

### User 6: demouser
```json
{
  "bankCode": "103",
  "branchCode": "3001",
  "username": "demouser",
  "password": "password123",
  "currency": "USD"
}
```
- **User ID**: demouser
- **Bank**: 103
- **Branch**: 3001
- **Currency**: USD

## Testing Login

### Via Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Find the `POST /api/v1/auth/login` endpoint
3. Click "Try it out"
4. Use any of the test user credentials above
5. Click "Execute"

### Via cURL
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "bankCode": "101",
    "branchCode": "1119",
    "username": "testuser",
    "password": "password123",
    "currency": "SGD"
  }'
```

### Via Login MFE
1. Open http://localhost:3001 (or http://localhost:3000 for Host App)
2. Enter credentials:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
3. Click Login

## Expected Response

```json
{
  "userId": "testuser",
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "username": "testuser",
  "bankCode": "101",
  "branchCode": "1119",
  "currency": "SGD"
}
```

## Integration with Channel Configurations Service

Users `1119test1`, `1119test2`, and `1119test3` are configured in the channel-configurations-service with different ACL permissions:

- **1119test1**: Can access Archive Enquiry and Statement Download
- **1119test2**: Can access Transfers
- **1119test3**: Can access all features (has both ACL 1000 and 1001)

After logging in with these users, you can test the feature flags by calling:
```bash
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: 1119test1"
```

## Verifying Data in H2 Console

1. Open http://localhost:8080/h2-console
2. Connect with JDBC URL: `jdbc:h2:mem:logindb`
3. Run query:
```sql
SELECT * FROM USERS;
```

You should see all 6 test users with their BCrypt hashed passwords.

## Password Hash Details

All users use the same BCrypt hash for password `password123`:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

This hash was generated using BCrypt with strength 10 (default).

## Troubleshooting

### Service won't start
- Check if port 8080 is already in use
- Verify Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`

### Tables not created
- Verify `spring.jpa.hibernate.ddl-auto: create-drop` in application.yml
- Check logs for Hibernate DDL statements

### Test data not loaded
- Verify `spring.jpa.defer-datasource-initialization: true` in application.yml
- Verify `spring.sql.init.mode: always` in application.yml
- Check logs for SQL execution statements

### Login fails with 401
- Verify the password is exactly `password123`
- Check the username matches one of the test users
- Verify bank code and branch code match the user's data
- Check authentication-service is running on port 8081
