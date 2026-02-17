#!/bin/bash

echo "🚀 Everypaisa - Build & Install Script"
echo "======================================"
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

echo "📦 Cleaning previous builds..."
./gradlew clean

echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK Location:"
    echo "   $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Check if device is connected
    if adb devices | grep -q "device$"; then
        echo "📲 Device detected. Installing APK..."
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ Installation successful!"
            echo "🎉 You can now open Everypaisa on your device"
        else
            echo ""
            echo "⚠️  Installation failed. Please install manually from:"
            echo "   app/build/outputs/apk/debug/app-debug.apk"
        fi
    else
        echo "⚠️  No device connected via ADB"
        echo ""
        echo "To install manually:"
        echo "1. Transfer app-debug.apk to your device"
        echo "2. Open the APK file on your device"
        echo "3. Allow installation from unknown sources if prompted"
    fi
else
    echo ""
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
