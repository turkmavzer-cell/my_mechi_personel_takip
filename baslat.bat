@echo off
chcp 65001 > nul
echo ========================================================
echo   Mechi Personel Takip & Bordro Otomasyonu Baslatiliyor...
echo ========================================================

set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call gradlew.bat run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Bir hata olustu. Hata kodunu yukaridan inceleyebilirsiniz.
    pause
)
