# IntelliJ Database Plugin Setup for H2

## Step-by-Step Guide to Access H2 via IntelliJ

### Prerequisites
- IntelliJ IDEA installed
- channel-configurations-service with H2ServerConfig enabled

### Step 1: Restart channel-configurations-service

The H2 TCP server is now enabled. Restart the service:

```bash
cd channel-configurations-service
mvn spring-boot:run
```

You should see in the logs:
```
TCP server running at tcp://localhost:9092 (only local connections)
```

### Step 2: Open Database Tool Window in IntelliJ

1. In IntelliJ, click **View** → **Tool Windows** → **Database**
2. Or press **Alt+1** (Windows/Linux) or **Cmd+1** (Mac)
3. Or click the **Database** tab on the right side panel

### Step 3: Add New H2 Data Source

1. In the Database tool window, click the **+** button
2. Select **Data Source** → **H2**

### Step 4: Configure Connection Settings

In the Data Source configuration dialog:

**Data Sources and Drivers Tab:**

1. **Name**: `channel-configurations-service`

2. **Connection Type**: Select **Remote**

3. **Host**: `localhost`

4. **Port**: `9092`

5. **Database**: `mem:channeldb`

6. **URL**: Should auto-populate to:
   ```
   jdbc:h2:tcp://localhost:9092/mem:channeldb
   ```

7. **Authentication**: 
   - **User**: `sa`
   - **Password**: (leave empty)

8. **Driver**: H2 (should be auto-selected)
   - If you see "Download missing driver files", click it

### Step 5: Test Connection

1. Click **Test Connection** button at the bottom
2. You should see: **Succeeded** with a green checkmark
3. If it fails, make sure:
   - channel-configurations-service is running
   - Port 9092 is not blocked by firewall
   - H2ServerConfig is loaded (check logs)

### Step 6: Apply and Connect

1. Click **OK** to save the configuration
2. The database should appear in the Database tool window
3. Click the database name to expand it
4. You should see:
   - **CHANNELDB** database
   - **PUBLIC** schema
   - **Tables**: MASTER, ACL_CONFIG

### Step 7: Browse and Query Data

**View Tables:**
1. Expand **PUBLIC** → **Tables**
2. Double-click **MASTER** or **ACL_CONFIG** to view data

**Run SQL Queries:**
1. Right-click the database → **New** → **Query Console**
2. Or press **Ctrl+Shift+F10**
3. Type your SQL query:
   ```sql
   SELECT * FROM MASTER;
   SELECT * FROM ACL_CONFIG;
   ```
4. Press **Ctrl+Enter** to execute

## Useful SQL Queries

### View all feature flags
```sql
SELECT * FROM MASTER;
```

### View all user ACL mappings
```sql
SELECT * FROM ACL_CONFIG;
```

### Check features for a specific user
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

### List all users with their features
```sql
SELECT 
    a.USER_ID,
    m.FEATURE_FLAG,
    m.ACL_ID
FROM ACL_CONFIG a
JOIN MASTER m ON a.ACL_ID = m.ACL_ID
ORDER BY a.USER_ID, m.FEATURE_FLAG;
```

### Add a new feature flag
```sql
INSERT INTO MASTER (ID, FEATURE_FLAG, IS_ACL_ENABLED, ACL_ID) 
VALUES (3, 'isPaymentEnabled', true, 1002);
```

### Grant feature access to a user
```sql
INSERT INTO ACL_CONFIG (USER_ID, ACL_ID) 
VALUES ('testuser', 1002);
```

## IntelliJ Database Features

### Auto-completion
- IntelliJ provides SQL auto-completion
- Press **Ctrl+Space** for suggestions

### Data Editor
- Double-click a table to open data editor
- Edit data directly in the grid
- Click **Submit** (Ctrl+Enter) to save changes

### Export Data
- Right-click table → **Export Data**
- Choose format: SQL, CSV, JSON, etc.

### Generate Diagrams
- Right-click database → **Diagrams** → **Show Visualization**
- See relationships between tables

### Compare Data
- Right-click table → **Compare With...**
- Compare data between different databases

## Troubleshooting

### "Cannot connect to database"

**Check if service is running:**
```bash
curl http://localhost:8082/actuator/health
```

**Check if TCP server started:**
Look for this in the service logs:
```
TCP server running at tcp://localhost:9092
```

**Check if port is available:**
```bash
netstat -ano | findstr :9092
```

### "Driver not found"

1. In Data Source settings, click **Driver** tab
2. Click **Download** to download H2 driver
3. Wait for download to complete
4. Try connecting again

### "Database not found"

Make sure the URL is exactly:
```
jdbc:h2:tcp://localhost:9092/mem:channeldb
```

Note: `mem:` prefix is important for in-memory databases

### "Connection refused"

1. Restart channel-configurations-service
2. Check firewall settings
3. Verify port 9092 is not blocked

### Tables are empty

The database is in-memory and populated from `data.sql` on startup:
1. Check if `data.sql` exists in `src/main/resources/`
2. Check service logs for SQL execution
3. Verify `spring.sql.init.mode: always` in application.yml

## Important Notes

### In-Memory Database Limitations

- **Data is lost on restart**: The database only exists while the service runs
- **Must reconnect after restart**: IntelliJ connection will break when service stops
- **Not for production**: Use PostgreSQL or MySQL for production

### Reconnecting After Service Restart

1. Stop the service (Ctrl+C)
2. IntelliJ will show connection error (red icon)
3. Restart the service
4. In IntelliJ, right-click database → **Refresh**
5. Or click the refresh icon in Database tool window

## Alternative: Use H2 Console in Browser

If you prefer a simpler approach:

1. Open: http://localhost:8082/h2-console
2. Enter:
   - JDBC URL: `jdbc:h2:mem:channeldb`
   - User: `sa`
   - Password: (empty)
3. Click Connect

Both methods work, choose what you prefer!

## Summary

You now have IntelliJ Database plugin connected to your H2 database. You can:
- ✅ Browse tables and data
- ✅ Run SQL queries with auto-completion
- ✅ Edit data directly
- ✅ Export data
- ✅ Generate diagrams
- ✅ Use all IntelliJ database features

The H2 TCP server runs on port 9092 and allows external connections while the service is running.
