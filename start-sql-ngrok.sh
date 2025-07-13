#!/bin/bash

echo "🚀 Starting SQL Server and ngrok tunnel..."

# Configuration
SQL_CONTAINER_NAME="beui-sqlserver-local"
NGROK_LOG_FILE="ngrok.log"
SQL_LOG_FILE="sqlserver.log"

# Function to cleanup on exit
cleanup() {
    echo "🛑 Stopping services..."
    docker stop $SQL_CONTAINER_NAME 2>/dev/null
    pkill ngrok 2>/dev/null
    exit 0
}

# Set trap for cleanup
trap cleanup SIGINT SIGTERM

# Start SQL Server
echo "📦 Starting SQL Server..."
docker run -d \
    --name $SQL_CONTAINER_NAME \
    -e ACCEPT_EULA=Y \
    -e SA_PASSWORD=YourStrong@Password1 \
    -p 1433:1433 \
    mcr.microsoft.com/mssql/server:2019-latest

if [ $? -eq 0 ]; then
    echo "✅ SQL Server started successfully!"
    
    # Wait for SQL Server to be ready
    echo "⏳ Waiting for SQL Server to be ready..."
    sleep 30
    
    # Start ngrok tunnel
    echo "🌐 Starting ngrok tunnel..."
    ngrok tcp 1433 > $NGROK_LOG_FILE 2>&1 &
    NGROK_PID=$!
    
    # Wait for ngrok to start
    sleep 5
    
    # Get ngrok URL
    NGROK_URL=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url' 2>/dev/null)
    
    if [ ! -z "$NGROK_URL" ] && [ "$NGROK_URL" != "null" ]; then
        echo "✅ ngrok tunnel started!"
        echo "🌐 Public URL: $NGROK_URL"
        echo ""
        echo "📋 Update your Railway environment variables:"
        echo "SPRING_DATASOURCE_URL=jdbc:sqlserver://$NGROK_URL;databaseName=greenteashop;encrypt=true;trustServerCertificate=true"
        echo ""
        echo "⚠️  IMPORTANT: Keep this script running!"
        echo "🛑 Press Ctrl+C to stop all services"
        
        # Keep script running
        while true; do
            sleep 10
            # Check if ngrok is still running
            if ! kill -0 $NGROK_PID 2>/dev/null; then
                echo "❌ ngrok stopped unexpectedly!"
                break
            fi
        done
    else
        echo "❌ Failed to get ngrok URL"
        cleanup
    fi
else
    echo "❌ Failed to start SQL Server"
    cleanup
fi 