#!/bin/bash

echo "🔍 Getting ngrok tunnel URL..."

# Wait for ngrok to start
sleep 3

# Get ngrok URL
NGROK_URL=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url' 2>/dev/null)

if [ ! -z "$NGROK_URL" ] && [ "$NGROK_URL" != "null" ]; then
    echo "✅ ngrok tunnel URL: $NGROK_URL"
    
    # Extract host and port
    HOST=$(echo $NGROK_URL | cut -d':' -f1)
    PORT=$(echo $NGROK_URL | cut -d':' -f2)
    
    echo ""
    echo "📋 Environment variables for Railway:"
    echo "SPRING_DATASOURCE_URL=jdbc:sqlserver://$HOST:$PORT;databaseName=greenteashop;encrypt=true;trustServerCertificate=true"
    echo "SPRING_DATASOURCE_USERNAME=sa"
    echo "SPRING_DATASOURCE_PASSWORD=YourStrong@Password1"
    echo ""
    echo "📋 For application.properties:"
    echo "spring.datasource.url=jdbc:sqlserver://$HOST:$PORT;databaseName=greenteashop;encrypt=true;trustServerCertificate=true"
    echo ""
    echo "📋 For application.yml:"
    echo "spring:"
    echo "  datasource:"
    echo "    url: jdbc:sqlserver://$HOST:$PORT;databaseName=greenteashop;encrypt=true;trustServerCertificate=true"
else
    echo "❌ Failed to get ngrok URL"
    echo "💡 Make sure ngrok is running: ngrok tcp 1433"
fi 