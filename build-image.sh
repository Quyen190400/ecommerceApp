#!/bin/bash

# Script build và push Docker image SQL Server với database sẵn có
# Tác giả: AI Assistant

# Cấu hình
IMAGE_NAME="nthquyen1904/sqlserver-greenteashop"  # Registry: nthquyen1904
TAG="latest"
SA_PASSWORD="YourStrong@Password1"

echo "🚀 Bắt đầu build Docker image SQL Server với database sẵn có..."

# Kiểm tra file create_tables.sql tồn tại
if [ ! -f "./src/main/resources/create_tables.sql" ]; then
    echo "❌ Không tìm thấy file ./src/main/resources/create_tables.sql"
    exit 1
fi

# Kiểm tra Docker đang chạy
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker không đang chạy. Vui lòng khởi động Docker trước."
    exit 1
fi

# Build image
echo "🔨 Building Docker image..."
docker build -f Dockerfile.sqlserver -t ${IMAGE_NAME}:${TAG} .

if [ $? -ne 0 ]; then
    echo "❌ Lỗi khi build image"
    exit 1
fi

echo "✅ Build image thành công!"

# Test image locally (tùy chọn)
echo "🧪 Test image locally..."
docker run -d --name test-sqlserver \
    -e ACCEPT_EULA=Y \
    -e SA_PASSWORD=${SA_PASSWORD} \
    -p 1433:1433 \
    ${IMAGE_NAME}:${TAG}

# Đợi container khởi động
echo "⏳ Đợi container khởi động..."
sleep 45

# Kiểm tra database đã được tạo
echo "🔍 Kiểm tra database..."
docker exec test-sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P ${SA_PASSWORD} -C \
  -Q "SELECT name FROM sys.databases WHERE name = 'greenteashop'" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Database greenteashop đã được tạo thành công!"
else
    echo "❌ Có lỗi khi tạo database"
fi

# Dừng container test
echo "🛑 Dừng container test..."
docker stop test-sqlserver
docker rm test-sqlserver

# Push lên registry (tùy chọn)
read -p "🤔 Bạn có muốn push image lên registry không? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📤 Pushing image lên registry..."
    docker push ${IMAGE_NAME}:${TAG}
    
    if [ $? -eq 0 ]; then
        echo "✅ Push image thành công!"
    else
        echo "❌ Lỗi khi push image"
    fi
fi

echo ""
echo "🎉 Hoàn thành!"
echo ""
echo "📋 Thông tin image:"
echo "   - Image: ${IMAGE_NAME}:${TAG}"
echo "   - Database: greenteashop (với tables từ create_tables.sql)"
echo "   - Username: sa"
echo "   - Password: ${SA_PASSWORD}"
echo ""
echo "🔧 Cách sử dụng:"
echo "   docker run -d --name sqlserver \\"
echo "     -e ACCEPT_EULA=Y \\"
echo "     -e SA_PASSWORD=${SA_PASSWORD} \\"
echo "     -p 1433:1433 \\"
echo "     ${IMAGE_NAME}:${TAG}"
echo ""
echo "📝 Lưu ý:"
echo "   - Registry: nthquyen1904"
echo "   - Đảm bảo đã login vào Docker Hub trước khi push"
echo "   - Image sẽ tự động tạo database và tables từ create_tables.sql" 