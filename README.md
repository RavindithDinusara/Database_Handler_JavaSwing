# Database Handler – Java Swing MySQL Backup & Restore Tool

This application is a Java Swing-based tool for configuring MySQL database connections, performing backups using mysqldump.exe, and restoring SQL files using mysql.exe. It automatically saves all configuration settings to the user’s AppData directory so the user does not need to re-enter them each time. It provides a clean interface for database configuration, backup creation, and restore operations.

## UI Preview
## UI Preview
![Database Config Window](https://github.com/RavindithDinusara/Database_Handler_JavaSwing/blob/main/Screenshot%202025-11-26%20115805.png?raw=true)
![Backup Screen](https://github.com/RavindithDinusara/Database_Handler_JavaSwing/blob/main/Screenshot%202025-11-26%20115816.png?raw=true)
![Restore Screen](https://github.com/RavindithDinusara/Database_Handler_JavaSwing/blob/main/Screenshot%202025-11-26%20115825.png?raw=true)


## Features
- Configure MySQL host, port, database name, username, and password
- Test database connectivity using JDBC
- Auto-save all settings when fields lose focus
- Backup databases using mysqldump.exe
- Restore SQL files using mysql.exe
- Automatically create new database during restore
- Timestamped backup file names
- Settings stored in C:/Users/<User>/DatabaseHandler/


## Technologies Used
Java 8+  
Swing UI  
MySQL Connector/J  
mysqldump.exe and mysql.exe  
Properties-based configuration storage




## Backup Process
The application creates a file such as:
mydb_backup_2025-01-01_12-30-55.sql  
Internally it runs:
mysqldump -h host -P port -u user -ppassword dbName > backup.sql

## Restore Process
The tool creates a new database and imports the selected SQL file:
mysql -h host -P port -u user -ppassword newDbName < file.sql

## Troubleshooting
If the connection fails, verify MySQL is running, host/port are correct, and the credentials are valid.  
If backup fails, ensure a valid mysqldump.exe path is selected. Common locations include:
C:/xampp/mysql/bin/  
C:/Program Files/MySQL/MySQL Server/bin/  
If restore fails, ensure the SQL file is valid and the MySQL user has permissions to create databases.
