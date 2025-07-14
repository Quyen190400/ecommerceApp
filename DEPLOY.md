# 🚀 Deploy Green Tea App lên Railway

Hướng dẫn đơn giản để deploy Spring Boot app lên Railway.

## 📋 Files cần thiết

- `Dockerfile` - Multi-stage build (backup)
- `Dockerfile.jar` - Sử dụng JAR đã build (recommended)
- `build-with-docker.sh` - Script build và push tự động
- `docker-compose.test.yml` - Test local
- `RAILWAY_DEPLOYMENT.md` - Hướng dẫn chi tiết Railway

## 🚀 Quick Deploy

### Bước 1: Build và Push Docker Image

```bash
# Cấp quyền thực thi
chmod +x build-with-docker.sh

# Build và push tự động
./build-with-docker.sh
```

**Hoặc sử dụng image đã có:**
```bash
# Image đã sẵn sàng: nthquyen1904/greentea-app:latest
docker pull nthquyen1904/greentea-app:latest
```

### Bước 2: Deploy lên Railway

1. Tạo tài khoản tại [railway.app](https://railway.app)
2. Tạo project mới
3. Deploy từ Docker Image: `nthquyen1904/greentea-app:latest`
4. Cấu hình environment variables

### Bước 3: Cấu hình Environment Variables

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:sqlserver://your-sql-server:1433;databaseName=greenteashop;encrypt=true;trustServerCertificate=true
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=YourStrong@Password1

# Server Configuration
SERVER_PORT=8080

# File Upload Configuration
UPLOAD_DIR=/app/uploads/images
```

## 🧪 Test Local

```bash
# Test với database
docker-compose -f docker-compose.test.yml up -d

# Kiểm tra logs
docker-compose -f docker-compose.test.yml logs -f greentea-app-test

# Test app tại: http://localhost:8080
```

## 🗄️ Database Options

### Option 1: SQL Server trên Railway (Recommended)
1. Tạo SQL Server service trên Railway
2. Sử dụng image: `mcr.microsoft.com/mssql/server:2019-latest`
3. Set environment variables: `ACCEPT_EULA=Y`, `SA_PASSWORD=YourStrong@Password1`

### Option 2: SQL Server Remote
- Azure SQL Database
- AWS RDS SQL Server
- Local SQL Server với public IP

### Option 3: Local SQL Server với ngrok
```bash
# Expose local SQL Server
ngrok tcp 1433

# Update SPRING_DATASOURCE_URL với ngrok URL
```

## 🔧 Troubleshooting

### Lỗi JAVA_HOME
```bash
# Sử dụng script Docker (không cần JAVA_HOME)
./build-with-docker.sh
```

### Lỗi Gradle
```bash
# Clean và rebuild
./gradlew clean
./gradlew build -x test --no-daemon
```

### Lỗi Docker
```bash
# Clean Docker
docker system prune -a

# Rebuild
./build-with-docker.sh
```

## 📊 Monitoring

### Health Check
- Endpoint: `/actuator/health`
- Railway tự động monitor

### Logs
```bash
# Railway CLI
railway logs

# Docker local
docker logs greentea-app-test -f
```

## 🔗 Links

- [Railway Documentation](https://docs.railway.app/)
- [Docker Hub](https://hub.docker.com/r/quyen1904/greentea-app)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra logs trong Railway dashboard
2. Verify environment variables
3. Test local trước khi deploy
4. Kiểm tra database connectivity 