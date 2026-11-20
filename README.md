# Chantel SIM (Changan 4G Cellular & APN Healer)

Бесплатная автомобильная утилита для головных устройств Changan (Android Automotive OS / платформа MediaTek T-Box SPM8675), решающая проблему слетающего APN и отсутствия мобильного интернета при установке местных SIM-карт.

---

## 🚀 Возможности
1. **Быстрая настройка в 1 клик:**
   - Предустановленные профили операторов Кыргызстана (MEGA, O!, Beeline), России (МТС, МегаФон, Билайн, Tele2, Т-Мобайл, СберМобайл, Yota), Казахстана (Beeline KZ, Kcell, Tele2, Altel), Узбекистана (Ucell, Beeline, Mobiuz) и Беларуси.
   - Возможность ручного ввода любого кастомного APN, логина и пароля.
2. **Аппаратное переключение через MediaTek T-Box HAL:**
   - Прямая запись в `vendor.mediatek.hardware.tbox.V1_0.ITeleService` (Profile 1).
   - Перезапуск сессии данных (`deactiveDataCall` + `setupDataCall`).
   - Принудительная маршрутизация сотового трафика на головное устройство через `INetwork` (`networkSwitchToHU(true)`, `setRoute`, `setDnsServer("8.8.8.8")`).
3. **Авто-удержание интернета (Watchdog-демон):**
   - Фоновая служба `CellularWatchdogService` с автоматическим запуском при старте авто (`BOOT_COMPLETED`, `ACTION_BOOT_HU`).
   - При выходе машины из глубокого сна (`PM_SLEEP`, STR) или отвале модема служба за 10–15 секунд автоматически восстанавливает соединение.
4. **Встроенный диагностический монитор:**
   - Отображение текущего IP интерфейса `ccmni0`.
   - Замер реальной задержки (Ping к Google DNS 8.8.8.8).
   - Проверка DNS-резолвинга доменов.
5. **Промо-воронка Chantel PRO:**
   - Встроенный интерактивный промо-блок флагманской телематики **Chantel PRO** (автозапуск двигателя, климат-контроль, GPS-трекинг, управление замками с телефона) со сканируемым QR-кодом на сайт `https://chantel.online`.

---

## 🛠 Сборка APK
```bash
python3 /Users/talantdzanbekov/development/chantel_sim/build_and_deploy.py
```
Собранный релизный APK (подписанный платформенным ключом AOSP):
`/Users/talantdzanbekov/development/chantel_sim/ChantelSIM_signed.apk` (размер ~612 КБ).

---

## 📲 Установка на ГУ Changan
Через ADB:
```bash
adb install -r -d -g /Users/talantdzanbekov/development/chantel_sim/ChantelSIM_signed.apk
```
Или скопировать APK на USB-флешку и установить через штатный «Центр приложений» / файловый менеджер машины.
