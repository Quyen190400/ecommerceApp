#!/bin/bash

echo "🚀 Installing ngrok..."

# Download ngrok
wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz

# Extract
tar xvzf ngrok-v3-stable-linux-amd64.tgz

# Move to /usr/local/bin
sudo mv ngrok /usr/local/bin

# Make executable
chmod +x /usr/local/bin/ngrok

# Clean up
rm ngrok-v3-stable-linux-amd64.tgz

echo "✅ ngrok installed successfully!"
echo "📋 Next steps:"
echo "1. Sign up at https://ngrok.com"
echo "2. Get your authtoken"
echo "3. Run: ngrok config add-authtoken YOUR_TOKEN"
echo "4. Test: ngrok tcp 1433" 