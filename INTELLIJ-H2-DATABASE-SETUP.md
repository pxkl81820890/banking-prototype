# IntelliJ H2 Database Setup Guide

## Accessing H2 Database via IntelliJ Database Plugin

### Prerequisites
- IntelliJ IDEA (Ultimate Edition recommended, but Community Edition works too)
- channel-configurations-service running on port 8082

### Important Note About In-Memory Databases

The channel-configurations-service uses an **in-memory H2 database** (`jdbc:h2:mem:channeldb`). This means:
- The database only exists while the service is running
- Data is lost when the service stops
- You must connect while the service is running

## Method 1: Using IntelliJ Database Plugin (Recommended)

### Step 1: Open Database Tool Window

1. In IntelliJ, go to **View** → **Tool Windows** → **Database**
2. Or press `Alt+1` (Windows) or click the **Database** tab on the right side

### Step 2: Add New Data Source

1. Click the **+** button in the Database tool window
2. Select **Data Source** → **H2**

### Step 3: Configure Connection

Enter the following details:

**General Tab:**
- **Name**: `channel-configurations-service (H2)`
- **Host**: `localhost`
- **Port**: `8082` (leave empty, not needed for in-memory)
- **Database**: Leave empty
- **URL**: `jdbc:h2:tcp://localhost:9092/mem:channeldb`
- **User**: `sa`
- **Password**: (leave empty)

**WAIT!** The above won't work for in-memory databases. Use this instead:

### Step 3 (Corrected): Configure for In-Memory Database

Since it's an in-memory database, you need to enable TCP server in the application.

**Option A: Use H2 Console (Easiest)**

Just use the web-based H2 console:
1. Make sure channel-configurations-service is running
2. Open browser: http://localhost:8082/h2-console
3. Enter connection details:
   - **JDBC URL**: `jdbc:h2:mem:channeldb`
   - **User Name**: `sa`
   - **Password**: (leave empty)
4. Click **Connect**

**Option B: Enable H2 TCP Server for IntelliJ Access**

To access the in-memory database from IntelliJ, you need to enable H2's TCP server.

## Method 2: Enable H2 TCP Server (For IntelliJ Access)

### Step 1: Update application.yml

Add TCP server configuration to `channel-configurations-service/src/main/resources/application.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
```

### Step 2: Add H2 TCP Server Bean

Create a configuration class to start H2 TCP server:

**File**: `channel-configurations-service/src/main/java/com/banking/channelconfig/infrastructure/config/H2ServerConfig.java`

```java
package com.banking.channelconfig.infrastructure.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

@Configuration
@Profile("dev") // Only enable in dev profile
public class H2ServerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer(
            "-tcp",
            "-tcpAllowOthers",
            "-tcpPort", "9092"
        );
    }
}
```

### Step 3: Run with Dev Profile

Start the service with dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or add to application.yml:
```yaml
spring:
  profiles:
    active: dev
```

### Step 4: Connect from IntelliJ

1. Open **Database** tool window
2. Click **+** → **Data Source** → **H2**
3. Configure:
   - **URL**: `jdbc:h2:tcp://localhost:9092/mem:channeldb`
   - **User**: `sa`
   - **Password**: (empty)
4. Click **Test Connection**
5. Click **OK**

## Method 3: Use H2 Console in Browser (Simplest)

This is the easiest method and doesn't require any changes:

### Step 1: Start the Service
```bash
cd channel-configurations-service
mvn spring-boot:run
```

### Step 2: Open H2 Console
Open browser: http://localhost:8082/h2-console

### Step 3: Connect
- **JDBC URL**: `jdbc:h2:mem:channeldb`
- **User Name**: `sa`
- **Password**: (leave empty)
- Click **Connect**

### Step 4: Query the Database

You can now run SQL queries:

```sql
-- View all feature flags
SELECT * FROM MASTER;

-- View all ACL configurations
SELECT * FROM ACL_CONFIG;

-- View feature flags for a specific user
SELECT m.FEATURE_FLAG, m.IS_ACL_ENABLED, a.USER_ID
FROM MASTER m
LEFT JOIN ACL_CONFIG a ON m.ACL_ID = a.ACL_ID
WHERE a.USER_ID = 'testuser';
```

## Recommended Approach

**For Development**: Use **Method 3** (H2 Console in Browser)
- No configuration needed
- Works immediately
- Easy to use
- Already enabled in your application

**For Production**: Use a persistent database like PostgreSQL or MySQL
- In-memory databases lose data on restart
- Not suitable for production use

## Current Database Schema

### MASTER Table
| Column           | Type    | Description                    |
|------------------|---------|--------------------------------|
| ID               | BIGINT  | Primary key                    |
| FEATURE_FLAG     | VARCHAR | Feature flag name              |
| IS_ACL_ENABLED   | BOOLEAN | Whether ACL check is required  |
| ACL_ID           | BIGINT  | ACL ID for access control      |

### ACL_CONFIG Table
| Column   | Type    | Description                    |
|----------|---------|--------------------------------|
| ID       | BIGINT  | Primary key                    |
| USER_ID  | VARCHAR | User identifier                |
| ACL_ID   | BIGINT  | ACL ID assigned to user        |

## Current Test Data

```sql
-- Feature Flags
INSERT INTO MASTER (ID, FEATURE_FLAG, IS_ACL_ENABLED, ACL_ID) VALUES
(1, 'isArchiveEnquiryEnabled', true, 1000),
(2, 'isReportsEnabled', true, 1001);

-- User ACL Mappings
INSERT INTO ACL_CONFIG (USER_ID, ACL_ID) VALUES
('testuser', 1000),
('1119test1', 1000),
('1119test2', 1001),
('1119test3', 1000),
('1119test3', 1001);
```

## Useful SQL Queries

### Check which features a user has access to
```sql
SELECT 
    m.FEATURE_FLAG,
    m.IS_ACL_ENABLED,
    CASE 
        WHEN m.IS_ACL_ENABLED = FALSE THEN 'Public Feature'
        WHEN a.USER_ID IS NOT NULL THEN 'Access Granted'
        ELSE 'Access Denied'
    END AS ACCESS_STATUS
FROM MASTER m
LEFT JOIN ACL_CONFIG a ON m.ACL_ID = a.ACL_ID AND a.USER_ID = 'testuser';
```

### List all users and their ACLs
```sql
SELECT 
    a.USER_ID,
    a.ACL_ID,
    m.FEATURE_FLAG
FROM ACL_CONFIG a
JOIN MASTER m ON a.ACL_ID = m.ACL_ID
ORDER BY a.USER_ID, m.FEATURE_FLAG;
```

### Add a new feature flag
```sql
INSERT INTO MASTER (ID, FEATURE_FLAG, IS_ACL_ENABLED, ACL_ID) 
VALUES (3, 'isPaymentEnabled', true, 1002);
```

### Grant ACL to a user
```sql
INSERT INTO ACL_CONFIG (USER_ID, ACL_ID) 
VALUES ('testuser', 1002);
```

## Troubleshooting

### Can't connect to H2 Console
- Make sure channel-configurations-service is running
- Check http://localhost:8082/actuator/health
- Verify the service started without errors

### Database is empty
- Check if data.sql is being executed
- Look for SQL logs in the console
- Verify `spring.sql.init.mode: always` is set

### IntelliJ can't connect
- In-memory databases are tricky to access externally
- Use H2 Console in browser instead (Method 3)
- Or enable TCP server (Method 2)

## Summary

**Easiest Method**: Use H2 Console at http://localhost:8082/h2-console
- No setup required
- Works immediately
- Perfect for development

**For IntelliJ Access**: Enable H2 TCP server (requires code changes)
- More complex setup
- Better IDE integration
- Useful for advanced queries
