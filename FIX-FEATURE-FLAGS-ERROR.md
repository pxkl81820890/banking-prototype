# Fix "Error loading features: Failed to fetch"

## Issue

The dashboard shows "Error loading features: Failed to fetch" because:
1. The channel-configurations-service (port 8082) is not running, OR
2. CORS is not configured for the service

## Solution

I've added CORS configuration to channel-configurations-service. Now you need to restart it.

### Step 1: Check if Channel Configurations Service is Running

Open your browser and try to access:
- http://localhost:8082/actuator/health

If you get:
- **Connection refused or timeout**: Service is NOT running → Go to Step 2
- **JSON response with "UP" status**: Service IS running → Go to Step 3

### Step 2: Start Channel Configurations Service (If Not Running)

Open a new terminal:
```bash
cd channel-configurations-service
mvn spring-boot:run
```

Wait for: `Started ChannelConfigurationsServiceApplication`

Then go to Step 4.

### Step 3: Restart Channel Configurations Service (If Already Running)

If the service is already running, you need to restart it to apply the CORS configuration:

1. Go to the terminal running channel-configurations-service
2. Press `Ctrl+C` to stop it
3. Restart it:
   ```bash
   mvn spring-boot:run
   ```
4. Wait for: `Started ChannelConfigurationsServiceApplication`

### Step 4: Test the Feature Flags API

Open your browser and navigate to:
```
http://localhost:8082/api/v1/feature-flags
```

Add the header `USER_ID: testuser` (you can use a browser extension like ModHeader or test via curl):

```bash
curl -H "USER_ID: testuser" http://localhost:8082/api/v1/feature-flags
```

Expected response:
```json
{
  "userId": "testuser",
  "featureFlags": {
    "isArchiveEnquiryEnabled": true,
    "isReportsEnabled": false
  }
}
```

### Step 5: Refresh Dashboard

1. Go back to http://localhost:3000
2. If you're already logged in, click "Logout"
3. Login again with:
   - Bank Code: `101`
   - Branch Code: `1119`
   - Username: `testuser`
   - Password: `password123`
   - Currency: `SGD`

You should now see:
- ✅ No "Error loading features" message
- ✅ "View Archived Images" button (testuser has ACL 1000)
- ❌ No "View Reports" button (testuser doesn't have ACL 1001)

## What Was Added

Created `WebConfig.java` in channel-configurations-service with CORS configuration:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:3002"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("USER_ID")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

This allows the dashboard-mfe to make requests to the channel-configurations-service.

## Testing Different Users

Try logging in with different users to see different feature flags:

| Username   | Password    | Archive Enquiry | Reports |
|------------|-------------|-----------------|---------|
| testuser   | password123 | ✅ Yes          | ❌ No   |
| 1119test1  | password123 | ✅ Yes          | ❌ No   |
| 1119test2  | password123 | ❌ No           | ✅ Yes  |
| 1119test3  | password123 | ✅ Yes          | ✅ Yes  |

## Troubleshooting

### Still seeing "Failed to fetch"?

1. **Check if service is running**:
   ```bash
   curl http://localhost:8082/actuator/health
   ```

2. **Check browser console** (F12):
   - Look for CORS errors
   - Look for network errors
   - Check the Network tab for the failed request

3. **Verify the service logs**:
   - Look for any errors in the terminal running channel-configurations-service
   - Check if the request is reaching the service

4. **Test the API directly**:
   ```bash
   curl -H "USER_ID: testuser" http://localhost:8082/api/v1/feature-flags
   ```

### CORS Error in Browser Console?

If you see a CORS error, make sure:
1. You restarted channel-configurations-service after adding WebConfig.java
2. The service compiled successfully (no errors in terminal)
3. The WebConfig.java file is in the correct location

### Service Won't Start?

If channel-configurations-service won't start:
1. Check if port 8082 is already in use:
   ```bash
   netstat -ano | findstr :8082
   ```
2. Kill any process using port 8082:
   ```bash
   taskkill /PID <PID> /F
   ```
3. Try starting again

## Success Indicators

You'll know it's working when:
- ✅ No "Error loading features" message in dashboard
- ✅ Feature buttons appear based on user's ACL
- ✅ No CORS errors in browser console
- ✅ Network tab shows successful request to localhost:8082

## Complete System Check

All services should be running:

| Service                          | Port | Status Check                          |
|----------------------------------|------|---------------------------------------|
| Authentication Service           | 8081 | http://localhost:8081/actuator/health |
| Login Service                    | 8080 | http://localhost:8080/actuator/health |
| Channel Configurations Service   | 8082 | http://localhost:8082/actuator/health |
| Login MFE                        | 3001 | http://localhost:3001/remoteEntry.js  |
| Dashboard MFE                    | 3002 | http://localhost:3002/remoteEntry.js  |
| Host App                         | 3000 | http://localhost:3000                 |

All should return 200 OK (except actuator/health which returns JSON).
