@echo off

REM 设置中文字体支持
chcp 65001

ECHO 开始创建测试签名密钥库...

REM 检查Java环境
ECHO 检查Java环境...
java -version
if %ERRORLEVEL% neq 0 (
    ECHO 错误: 未找到Java运行环境，请确保已安装JDK并添加到PATH
    pause
    exit /b 1
)

REM 创建签名密钥目录
if not exist "signing_keys" mkdir signing_keys

REM 生成测试密钥库
ECHO 生成测试密钥库...
keytool -genkey -v ^
    -keystore signing_keys\upload-keystore ^
    -alias hydrotracker_key ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 10000 ^
    -storepass example_keystore_password_123 ^
    -keypass example_key_password_456 ^
    -dname "CN=HydroTracker, OU=Development, O=ExampleOrg, L=Unknown, ST=Unknown, C=CN"

if %ERRORLEVEL% equ 0 (
    ECHO ==================================================
    ECHO 测试签名密钥库创建成功！
    ECHO 密钥库位置: signing_keys\upload-keystore
    ECHO 密钥别名: hydrotracker_key
    ECHO 密钥库密码: example_keystore_password_123
    ECHO 密钥密码: example_key_password_456
    ECHO ==================================================
    ECHO 注意: 这是测试用密钥，仅供开发和测试使用
    ECHO 生产环境请使用安全的密钥生成方式
) else (
    ECHO ==================================================
    ECHO 错误: 密钥库创建失败
    ECHO 请检查错误信息并修复问题
    ECHO ==================================================
)

pause
