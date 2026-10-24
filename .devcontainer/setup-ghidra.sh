#!/usr/bin/env bash
# Codespaces 启动后自动装 Ghidra（只跑一次，缓存进容器）
set -euo pipefail
if [ -d "$HOME/ghidra" ]; then echo "Ghidra 已存在"; exit 0; fi
URL=$(curl -s https://api.github.com/repos/NationalSecurityAgency/ghidra/releases/latest \
      | grep -o '"browser_download_url": *"[^"]*\.zip"' | head -1 | cut -d'"' -f4)
echo "下载 $URL"
curl -sL "$URL" -o /tmp/g.zip
mkdir -p "$HOME/ghidra" && unzip -q /tmp/g.zip -d "$HOME/ghidra"
GH=$(ls -d "$HOME"/ghidra/ghidra_*_PUBLIC | head -1)
ln -sfn "$GH" "$HOME/ghidra/current"
echo "export GHIDRA_HOME=$HOME/ghidra/current" >> "$HOME/.bashrc"
echo "export PATH=\$PATH:\$GHIDRA_HOME/support" >> "$HOME/.bashrc"
echo "装好了: $GH"
