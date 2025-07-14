# Hướng dẫn Deploy lên Railway

## 📋 Thông tin Docker Hub
- **Repository**: `nthquyen1904/greentea-app`
- **Image**: `nthquyen1904/greentea-app:latest`
- **Size**: ~301MB
- **Status**: ✅ Đã build và push thành công

## 1. Build và Push Docker Image

### Cách 1: Sử dụng script tự động (Recommended)
```bash
# Cấp quyền thực thi cho script
chmod +x build-with-docker.sh

# Chạy script build và push
./build-with-docker.sh
```

### Cách 2: Thực hiện từng bước
```bash
# Build image
docker build -f Dockerfile.jar -t nthquyen1904/greentea-app:latest .

# Test local (tùy chọn)
docker run --rm -d --name test-app -p 8080:8080 nthquyen1904/greentea-app:latest

# Push lên Docker Hub
docker push nthquyen1904/greentea-app:latest
```

### Cách 3: Kiểm tra image đã có
```bash
# Kiểm tra images local
docker images | grep nthquyen1904/greentea-app

# Pull image từ Docker Hub
docker pull nthquyen1904/greentea-app:latest
```

## 2. Deploy lên Railway

### Bước 1: Tạo tài khoản Railway
1. Truy cập [railway.app](https://railway.app)
2. Đăng ký tài khoản (có thể dùng GitHub)
3. Tạo project mới

### Bước 2: Deploy từ Docker Image
1. Trong Railway dashboard, click "New Service"
2. Chọn "Deploy from Docker Image"
3. Nhập image name: `nthquyen1904/greentea-app:latest`
4. Click "Deploy"
5. Đợi Railway pull và deploy image (có thể mất 2-3 phút)

### Bước 3: Cấu hình Environment Variables
Trong Railway dashboard, vào tab "Variables" và thêm các biến môi trường:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:sqlserver://your-sql-server:1433;databaseName=greenteashop;encrypt=true;trustServerCertificate=true
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=YourStrong@Password1

# JWT Configuration (nếu cần)
JWT_SECRET=your-jwt-secret-key-here

# File Upload Configuration
UPLOAD_DIR=/app/uploads/images

# Server Configuration
SERVER_PORT=8080
```

### Bước 4: Cấu hình Port
1. Vào tab "Settings"
2. Đảm bảo port được set là `8080`
3. Railway sẽ tự động expose port này

## 3. Kết nối Database

### Option 1: SQL Server trên Railway
1. Tạo service SQL Server mới trên Railway
2. Sử dụng image: `mcr.microsoft.com/mssql/server:2019-latest`
3. Set environment variables:
   - `ACCEPT_EULA=Y`
   - `SA_PASSWORD=YourStrong@Password1`
4. Update `SPRING_DATASOURCE_URL` để trỏ tới SQL Server service

### Option 2: SQL Server Remote
Nếu bạn có SQL Server ở nơi khác (Azure, AWS, local với public IP):
1. Update `SPRING_DATASOURCE_URL` với IP/domain của SQL Server
2. Đảm bảo SQL Server cho phép kết nối từ Railway IPs
3. Mở port 1433 trên firewall

### Option 3: SQL Server Local với ngrok
```bash
# Cài đặt ngrok
# Expose local SQL Server
ngrok tcp 1433

# Update SPRING_DATASOURCE_URL với ngrok URL
SPRING_DATASOURCE_URL=jdbc:sqlserver://your-ngrok-url:1433;databaseName=greenteashop;encrypt=true;trustServerCertificate=true
```

## 4. Test và Debug

### Kiểm tra logs
```bash
# Xem logs trong Railway dashboard
# Hoặc sử dụng Railway CLI
railway logs

# Xem logs real-time
railway logs --follow
```

### Health Check
Railway sẽ tự động kiểm tra health endpoint:
- `https://your-app.railway.app/actuator/health`
- `https://your-app.railway.app/` (homepage)

### Kiểm tra deployment
```bash
# Kiểm tra status deployment
railway status

# Xem thông tin service
railway service
```

### Troubleshooting
1. **App không start**: Kiểm tra logs và environment variables
2. **Database connection failed**: Kiểm tra SQL Server connectivity
3. **File upload không work**: Kiểm tra UPLOAD_DIR permission

## 5. Custom Domain (Tùy chọn)
1. Trong Railway dashboard, vào tab "Settings"
2. Click "Custom Domain"
3. Thêm domain của bạn
4. Cấu hình DNS records theo hướng dẫn

## 6. Monitoring và Scaling
- Railway tự động scale dựa trên traffic
- Có thể set manual scaling trong dashboard
- Monitor performance qua Railway metrics

## 7. Backup và Recovery
- Database: Sử dụng SQL Server backup features
- Application: Railway tự động backup deployments
- Environment variables: Export từ Railway dashboard

## 8. Update và Redeploy

### Update code mới
```bash
# Build và push image mới
./build-with-docker.sh

# Railway sẽ tự động pull image mới khi có update
```

### Rollback
1. Vào Railway dashboard
2. Tab "Deployments"
3. Click "Redeploy" trên version cũ

## 9. Monitoring và Metrics

### Railway Dashboard
- CPU usage
- Memory usage
- Network traffic
- Response time

### Custom Monitoring
```bash
# Health check endpoint
curl https://your-app.railway.app/actuator/health

# Application metrics
curl https://your-app.railway.app/actuator/metrics
``` 