# 🌐 Setup ngrok để kết nối SQL Server với Railway

## 📋 Prerequisites

- Docker đã cài đặt
- Tài khoản ngrok (free plan)
- Railway app đã deploy

## 🚀 Step-by-Step Setup

### Bước 1: Cài đặt ngrok

```bash
# Cấp quyền thực thi
chmod +x install-ngrok.sh

# Cài đặt ngrok
./install-ngrok.sh

# Hoặc cài thủ công
wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz
tar xvzf ngrok-v3-stable-linux-amd64.tgz
sudo mv ngrok /usr/local/bin
chmod +x /usr/local/bin/ngrok
```

### Bước 2: Cấu hình ngrok

```bash
# Đăng ký tại https://ngrok.com
# Lấy authtoken từ dashboard

# Cấu hình authtoken
ngrok config add-authtoken YOUR_AUTHTOKEN_HERE
```

### Bước 3: Khởi động SQL Server và ngrok

```bash
# Cấp quyền thực thi
chmod +x start-sql-ngrok.sh

# Khởi động tự động
./start-sql-ngrok.sh
```

### Bước 4: Lấy ngrok URL

```bash
# Cấp quyền thực thi
chmod +x get-ngrok-url.sh

# Lấy URL
./get-ngrok-url.sh
```

## 🔧 Cấu hình Railway

### Environment Variables cho Railway:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:sqlserver://YOUR_NGROK_HOST:YOUR_NGROK_PORT;databaseName=greenteashop;encrypt=true;trustServerCertificate=true
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=YourStrong@Password1

# Server Configuration
SERVER_PORT=8080

# File Upload Configuration
UPLOAD_DIR=/app/uploads/images

# Logging for debugging
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_JDBC=DEBUG
LOGGING_LEVEL_COM_MICROSOFT_SQLSERVER=DEBUG
```

## 📊 Monitoring và Debug

### Kiểm tra ngrok tunnel:

```bash
# Xem ngrok dashboard
curl http://localhost:4040/api/tunnels

# Kiểm tra logs
tail -f ngrok.log
```

### Kiểm tra SQL Server:

```bash
# Kiểm tra container
docker ps | grep sqlserver

# Xem logs SQL Server
docker logs beui-sqlserver-local
```

### Kiểm tra Railway logs:

```bash
# Xem logs Railway
railway logs --follow

# Kiểm tra health endpoint
curl https://your-app.railway.app/actuator/health
```

## ⚠️ Lưu ý quan trọng

### ngrok Free Plan Limitations:
- **Domain thay đổi** mỗi lần restart ngrok
- **Port thay đổi** mỗi lần restart
- **Session timeout** sau 2 giờ không hoạt động
- **Giới hạn 1 tunnel** cùng lúc

### Cảnh báo:
- **ngrok tắt = Railway app không kết nối được database**
- **Restart ngrok = phải update Railway environment variables**
- **Free plan có giới hạn bandwidth**

## 🔄 Workflow hàng ngày

### Khởi động:
```bash
# 1. Start SQL Server và ngrok
./start-sql-ngrok.sh

# 2. Lấy URL mới
./get-ngrok-url.sh

# 3. Update Railway environment variables
# 4. Restart Railway app
```

### Kiểm tra:
```bash
# Kiểm tra ngrok tunnel
curl http://localhost:4040/api/tunnels

# Kiểm tra Railway app
curl https://your-app.railway.app/actuator/health
```

## 🛠️ Troubleshooting

### Lỗi kết nối database:
```bash
# 1. Kiểm tra ngrok tunnel
./get-ngrok-url.sh

# 2. Kiểm tra SQL Server
docker logs beui-sqlserver-local

# 3. Kiểm tra Railway logs
railway logs --follow

# 4. Restart ngrok nếu cần
pkill ngrok
ngrok tcp 1433
```

### Lỗi ngrok:
```bash
# Kiểm tra authtoken
ngrok config check

# Restart ngrok
pkill ngrok
ngrok tcp 1433
```

## 📝 Best Practices

1. **Luôn backup data** trước khi restart ngrok
2. **Monitor ngrok logs** để phát hiện issues sớm
3. **Set up alerts** khi ngrok tunnel down
4. **Consider paid plan** cho production use
5. **Test connection** trước khi deploy

## 🔗 Links

- [ngrok Documentation](https://ngrok.com/docs)
- [Railway Documentation](https://docs.railway.app/)
- [SQL Server Docker](https://hub.docker.com/_/microsoft-mssql-server) 