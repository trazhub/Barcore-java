# Deployment Guide (Ubuntu)

This guide explains how to host your Discord bot on an Ubuntu server and set up the web dashboard.

## Prerequisites

- An Ubuntu Server (VPS or local machine).
- Java 17 or higher installed.
- Access to the terminal (SSH).

## 1. Install Java 17

Update your package list and install OpenJDK 17:

```bash
sudo apt update
sudo apt install openjdk-17-jre-headless -y
```

Verify the installation:

```bash
java -version
```

## 2. Upload Your Bot

1. Build the shadow JAR on your local machine:
   ```bash
   ./gradlew.bat shadowJar
   ```
   This creates a file in `build/libs/BardCore-1.0-all.jar`.

2. Upload this JAR file and your `.env` file to your server (e.g., using SCP or FileZilla) to a folder like `/home/yourusername/bardcore`.

## 3. Configure Systemd Service

1. Create a service file:
   ```bash
   sudo nano /etc/systemd/system/bardcore.service
   ```

2. Paste the contents of the `bardcore.service` file (ensure you update the paths and username):

   ```ini
   [Unit]
   Description=BardCore Discord Bot
   After=network.target

   [Service]
   User=yourusername
   WorkingDirectory=/home/yourusername/bardcore
   ExecStart=/usr/bin/java -jar BardCore-1.0-all.jar
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

3. Reload systemd and start the bot:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl start bardcore
   sudo systemctl enable bardcore
   ```

4. Check the status:
   ```bash
   sudo systemctl status bardcore
   ```

## 4. Access Web Dashboard

The dashboard runs on port `8080` (or `DASHBOARD_PORT` in your `.env`).

1. **Firewall**: Ensure port 8080 is open.
   ```bash
   sudo ufw allow 8080
   ```

2. **Access**: Open your browser and go to:
   ```
   http://<your-server-ip>:8080
   ```

You should see the bot status, uptime, and memory usage!
