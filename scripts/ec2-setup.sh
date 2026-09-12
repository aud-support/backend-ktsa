#!/bin/bash
# Run this ONCE on your EC2 instance after launching it
# ssh -i ktsa-key.pem ubuntu@YOUR_EC2_IP
# then: bash ec2-setup.sh

set -e

echo "=== Updating system ==="
sudo apt update && sudo apt upgrade -y

echo "=== Installing Java 21 ==="
sudo apt install -y openjdk-21-jdk

echo "=== Verifying Java ==="
java -version

echo "=== Creating app directory ==="
mkdir -p /home/ubuntu/app

echo "=== Creating placeholder JAR (GitHub Actions will replace this) ==="
touch /home/ubuntu/app/foosball.jar

echo "=== Creating systemd service ==="
sudo tee /etc/systemd/system/ktsa-backend.service > /dev/null <<'SERVICE'
[Unit]
Description=KTSA Foosball Backend
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app
ExecStart=/usr/bin/java -jar /home/ubuntu/app/foosball.jar
EnvironmentFile=/etc/environment
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
SERVICE

echo "=== Enabling service ==="
sudo systemctl daemon-reload
sudo systemctl enable ktsa-backend

echo ""
echo "=== DONE ==="
echo "Now add your environment variables to /etc/environment"
echo "Then push to main branch to trigger your first deployment!"
