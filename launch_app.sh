#!/usr/bin/env bash
# ==============================================================================
# Script de compilación, instalación y lanzamiento para IotasLife / MyNotes
# Diseñado para ejecutar la aplicación directamente en cualquier dispositivo o
# emulador Android conectado por ADB.
# ==============================================================================

set -e

# Configuración de colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== [IotasLife] Lanzador de aplicación Android ===${NC}"

# 1. Configurar JAVA_HOME si es necesario
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    if [ -d "/home/ismael/.jdks/jdk-21" ]; then
        export JAVA_HOME="/home/ismael/.jdks/jdk-21"
    elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
    elif [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    echo -e "${GREEN}✓ Usando JAVA_HOME:${NC} $JAVA_HOME"
fi

# 2. Localizar comando adb
ADB_CMD="adb"
if ! command -v adb &> /dev/null; then
    if [ -x "$HOME/Android/Sdk/platform-tools/adb" ]; then
        ADB_CMD="$HOME/Android/Sdk/platform-tools/adb"
    elif [ -n "$ANDROID_HOME" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
        ADB_CMD="$ANDROID_HOME/platform-tools/adb"
    else
        echo -e "${RED}Error: 'adb' no se encuentra en el PATH ni en Android SDK.${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}✓ Usando ADB:${NC} $($ADB_CMD version | head -n 1)"

# 3. Detectar dispositivo conectado
DEVICES=$($ADB_CMD devices | grep -v "List of devices" | grep "device$" | awk '{print $1}')
DEVICE_COUNT=$(echo "$DEVICES" | grep -v '^$' | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}Error: No hay ningún dispositivo o emulador conectado vía ADB.${NC}"
    echo -e "${YELLOW}Por favor inicia un emulador o conecta un dispositivo con depuración USB habilitada.${NC}"
    exit 1
fi

TARGET_DEVICE=$(echo "$DEVICES" | head -n 1)
echo -e "${GREEN}✓ Dispositivo objetivo detectado:${NC} $TARGET_DEVICE"

# 4. Compilar APK Debug
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo -e "${BLUE}🔨 Compilando aplicación con Gradle...${NC}"
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Error: No se encontró el APK generado en $APK_PATH${NC}"
    exit 1
fi

# 5. Instalar en el dispositivo
echo -e "${BLUE}📲 Instalando APK en $TARGET_DEVICE...${NC}"
$ADB_CMD -s "$TARGET_DEVICE" install -r "$APK_PATH"

# 6. Iniciar la actividad principal
PACKAGE_NAME="com.iotaslife.app"
MAIN_ACTIVITY="com.iotaslife.app.presentation.main.MainActivity"

echo -e "${BLUE}🚀 Lanzando actividad principal ($PACKAGE_NAME)...${NC}"
$ADB_CMD -s "$TARGET_DEVICE" shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"

echo -e "${GREEN}======================================================${NC}"
echo -e "${GREEN}✓ ¡Aplicación desplegada y ejecutándose con éxito!${NC}"
echo -e "${GREEN}======================================================${NC}"
