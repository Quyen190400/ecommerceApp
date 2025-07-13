# 🐬 Migration từ SQL Server sang MySQL

## 📋 Tổng quan

Đã chuyển đổi từ SQL Server sang MySQL với các thay đổi sau:

### ✅ Đã thực hiện:
- ✅ Thay thế `beui-sqlserver` bằng `beui-mysql` (MySQL 8.0)
- ✅ Cập nhật port từ `1433` sang `3306`
- ✅ Cấu hình MySQL environment variables
- ✅ Bỏ service `sql-init` (MySQL tự động tạo database)
- ✅ Cập nhật Spring Boot configuration cho MySQL
- ✅ Thêm MySQL dependency trong build.gradle
- ✅ Tạo file SQL mới cho MySQL syntax

## 🚀 Files đã tạo/cập nhật:

### 1. Docker Compose Files:
- `docker-compose-mysql.yml` - Production với MySQL
- `docker-compose-mysql-test.yml` - Test local với MySQL

### 2. Configuration Files:
- `src/main/resources/application-mysql.yml` - Spring Boot config cho MySQL
- `src/main/resources/create_tables_mysql.sql` - SQL schema cho MySQL

### 3. Build Configuration:
- `build.gradle` - Đã thêm MySQL dependency

## 🔧 Cách sử dụng:

### Production (MySQL):
```bash
# Chạy với MySQL
docker-compose -f docker-compose-mysql.yml up -d

# Kiểm tra logs
docker-compose -f docker-compose-mysql.yml logs -f greentea-app
```

### Test Local (MySQL):
```bash
# Test với MySQL
docker-compose -f docker-compose-mysql-test.yml up -d

# Kiểm tra logs
docker-compose -f docker-compose-mysql-test.yml logs -f greentea-app-test

# Test app tại: http://localhost:8080
```

## 🔄 Migration Steps:

### 1. Backup SQL Server Data (nếu cần):
```bash
# Backup từ SQL Server
docker exec beui-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P YourStrong@Password1 -Q "BACKUP DATABASE greenteashop TO DISK = '/var/opt/mssql/backup.bak'"
```

### 2. Stop SQL Server Services:
```bash
# Stop SQL Server
docker-compose down

# Hoặc stop từng service
docker stop beui-sqlserver
docker stop greentea-app
```

### 3. Start MySQL Services:
```bash
# Start MySQL
docker-compose -f docker-compose-mysql.yml up -d

# Kiểm tra MySQL
docker exec beui-mysql mysql -u greentea_user -p greentea_pass -e "SHOW DATABASES;"
```

### 4. Build và Deploy App:
```bash
# Build với MySQL config
./build-with-docker.sh

# Deploy lên Railway với MySQL environment variables
```

## 🔧 Environment Variables cho Railway:

```env
# MySQL Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://host:port/greenteashop?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=greentea_user
SPRING_DATASOURCE_PASSWORD=greentea_pass
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect

# Server Configuration
SERVER_PORT=8080

# File Upload Configuration
UPLOAD_DIR=/app/uploads/images
```

## 🎯 Key Differences:

| Feature | SQL Server | MySQL |
|---------|------------|-------|
| **Port** | 1433 | 3306 |
| **Driver** | SQLServerDriver | MySQLDriver |
| **Dialect** | SQLServerDialect | MySQLDialect |
| **URL Format** | jdbc:sqlserver:// | jdbc:mysql:// |
| **Authentication** | Windows Auth/SA | Native Password |
| **Database Creation** | Manual/Init Script | Auto via ENV |

## ✅ Advantages của MySQL:

- ✅ **Faster startup** - MySQL khởi động nhanh hơn
- ✅ **Smaller footprint** - Image nhỏ hơn
- ✅ **Better performance** cho web apps
- ✅ **Widely supported** - Nhiều hosting providers
- ✅ **Free trên Railway** - 500MB storage
- ✅ **Auto database creation** - Không cần init script

## 🔧 Troubleshooting:

### Lỗi MySQL connection:
```bash
# Kiểm tra MySQL container
docker logs beui-mysql

# Test connection
docker exec beui-mysql mysql -u greentea_user -p greentea_pass -e "SELECT 1;"
```

### Lỗi Spring Boot:
```bash
# Kiểm tra logs
docker logs greentea-app

# Verify environment variables
docker exec greentea-app env | grep SPRING
```

### Lỗi JPA/Hibernate:
```bash
# Set ddl-auto=create-drop lần đầu
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop

# Sau đó đổi thành update
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## 🎉 Migration Complete!

Bây giờ bạn có thể sử dụng MySQL thay vì SQL Server với:
- ✅ Stable connection
- ✅ Better performance
- ✅ Free hosting trên Railway
- ✅ Auto-scaling
- ✅ Backup tự động 