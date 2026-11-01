#!/bin/bash
set -e

echo "=== Codespaces Masaüstü GUI Ortamı Hazırlanıyor ==="

# Linux Compose Desktop bağımlılıkları (libxext, libxrender, libxtst, libxi)
sudo apt-get update
sudo apt-get install -y libxext6 libxrender1 libxtst6 libxi6 libfontconfig1 libasound2

# Masaüstü kısayolu oluştur
DESKTOP_DIR="/home/vscode/Desktop"
mkdir -p "$DESKTOP_DIR"

cat << 'EOF' > "$DESKTOP_DIR/Mechi-Puantaj.desktop"
[Desktop Entry]
Version=1.0
Type=Application
Name=Mechi Puantajı Başlat
Comment=Compose Desktop Uygulamasını Çalıştır
Exec=bash -c "cd /workspaces/my_mechi_personel_takip && ./gradlew run; read -p 'Pencere kapandı, çıkmak için Enter...'"
Icon=utilities-terminal
Terminal=true
StartupNotify=true
EOF

chmod +x "$DESKTOP_DIR/Mechi-Puantaj.desktop"
chown -R vscode:vscode "$DESKTOP_DIR"

echo "=== Kurulum Tamamlandı ==="
