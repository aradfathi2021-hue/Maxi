# Maxi VPN + Xray Core

نسخه توسعه‌یافته Maxi VPN با اتصال به **XTLS/libXray** (wrapper رسمی Xray-core).

ویژگی‌ها:
- آیکن و برند Maxi VPN
- افزودن دستی لینک‌های VLESS / VMess / Trojan / Shadowsocks
- افزودن Subscription URL
- تبدیل Share Link به Xray JSON با libXray
- ذخیره پروفایل و Xray JSON
- انتخاب پروفایل
- Android VpnService
- اجرای/توقف managed Xray instance
- GitHub Actions که AAR رسمی libXray را از آخرین release رسمی دریافت و APK را Build می‌کند

منبع رسمی:
- XTLS/libXray: https://github.com/XTLS/libXray
- XTLS/Xray-core: https://github.com/XTLS/Xray-core

نکته فنی:
این پروژه هسته Xray را به‌عنوان موتور پردازش کانفیگ/پروکسی اضافه می‌کند. اتصال کامل TUN-to-core در یک کلاینت تولیدی معمولاً به لایه‌ی routing/TUN integration مناسب نیاز دارد؛ بنابراین قبل از انتشار عمومی، روی دستگاه واقعی تست و مجوزها/لایسنس‌های وابستگی‌ها را بررسی کنید.
