# PathoDex — Android App

[![License: MIT](https://img.shields.io/badge/License-MIT-6C5CE7.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-10B981.svg)](https://developer.android.com)

اپلیکیشن اندروید سامانه‌ی **PathoDex** — مرجع تخصصی عوامل بیماری‌زا با پرونده‌ی بالینی
کامل، فلش‌کارت، خودآزمایی و مقایسه‌ی تطبیقی.

An Android client for **PathoDex**, a Persian medical reference for pathogens: full
clinical dossiers, flashcards, self-assessment and side-by-side comparison.

The app is a client of the PathoDex service at [pathodex.ir](https://pathodex.ir) —
it fetches its content from the public API and caches it for offline reading. The
medical content itself is **not** part of this repository; see [NOTICE](NOTICE).

---

## Features

- **Full clinical dossier** for every pathogen — epidemiology, signs, pathogenesis,
  diagnosis, treatment, prevention and prognosis, with a fast section jump bar.
- **Taxonomy explorer** — Category › Class › Family › Pathogen as a collapsible tree,
  with search and per-level counts.
- **Flashcards and self-assessment** with a learning state and progress tracking.
- **Side-by-side comparison** of any number of pathogens.
- **Typo-tolerant search** for both Persian and English — normalises Arabic/Persian
  letter variants, missing ZWNJ, and matches phonetic transliteration variants
  (`سفلیس` / `سیفلیس` → `سیفیلیس`), plus edit-distance and Jaro-Winkler matching.
- **Google sign-in** (Credential Manager) and username/password sign-in.
- **Deep links** to pathodex.ir dossiers (off by default; enable in Settings).
- **Offline reading** — the pathogen list and dossiers are cached on device.
- Fully **right-to-left**, built with Jetpack Compose.

## Building

Requirements:

- JDK 21
- Android SDK (compileSdk 36)
- Gradle 9.x

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

gradle assembleDebug        # app/build/outputs/apk/debug/
gradle assembleRelease      # requires a release keystore, see below
gradle testDebugUnitTest    # unit tests
```

### Signing

The app uses application id `pathodex.ir`. Debug builds are signed with the standard
Android `debug.keystore`. Release builds expect a keystore at `${rootDir}/my-upload-key.jks`
with alias `upload`, supplied through the `KEYSTORE_PATH`, `STORE_PASSWORD` and
`KEY_PASSWORD` environment variables — the password is never committed.

To enable Google sign-in on a given build, that build's signing certificate must be
registered in the Google Cloud project as an **Android** OAuth client for package
`pathodex.ir`. The app itself always uses the web client id, which is a public value.

## Contributing

Contributions are welcome — see [CONTRIBUTING](CONTRIBUTING.md). Please open an issue
first for anything non-trivial.

## Licence

The **code** in this repository is released under the [MIT Licence](LICENSE).

The **medical content** displayed by the app is a separate rights grant: it is
© PathoDex and is served from [pathodex.ir](https://pathodex.ir). It is **not**
covered by the MIT Licence. See [NOTICE](NOTICE) for details and for the medical
disclaimer.

---

## ویژگی‌ها

- **پرونده‌ی بالینی کامل** هر عامل بیماری‌زا با نوار «دسترسی سریع به بخش‌ها».
- **درخت تاکسونومی** دسته › رده › خانواده › پاتوژن با جستجو و شمارش در هر سطح.
- **فلش‌کارت و خودآزمایی** با شرح یادگیری و پیشرفت.
- **مقایسه‌ی تطبیقی** چند پاتوژن کنار هم.
- **جستجوی خطاپذیر** فارسی و انگلیسی: یکدست‌سازی حروف عربی/فارسی، نیم‌فاصله‌ی جاافتاده
  و تطبیق واریانت‌های ترجمه‌ی صوتی (`سفلیس` / `سیفلیس` → `سیفیلیس`).
- **ورود با گوگل** (Credential Manager) و ورود با نام‌کاربری و رمز عبور.
- **پیوندهای عمیق** pathodex.ir — به‌صورت پیش‌فرض خاموش؛ در تنظیمات فعال می‌شود.
- **مطالعه‌ی آفلاین** — فهرست پاتوژن‌ها و پرونده‌ها روی دستگاه ذخیره می‌شوند.
- کاملاً **راست‌به‌چپ** و ساخته‌شده با Jetpack Compose.

## ساخت

پیش‌نیازها: JDK 21، Android SDK (compileSdk 36) و Gradle 9.x.

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

gradle assembleDebug          # خروجی: app/build/outputs/apk/debug/
gradle testDebugUnitTest      # آزمون‌های واحد
```

## امضا

اپلیکیشن با نام بسته‌ی `pathodex.ir` ساخته می‌شود. نسخه‌ی دیباگ با `debug.keystore`
امضا می‌شود و نسخه‌ی رلیز با کلید رلیز (alias `upload`) که از متغیرهای محیطی
`KEYSTORE_PATH`، `STORE_PASSWORD` و `KEY_PASSWORD` خوانده می‌شود. گذرواژه هرگز در
مخزن ثبت نمی‌شود.

## مجوز

**کد** این مخزن تحت [مجوز MIT](LICENSE) منتشر شده است.

**محتوای پزشکی** که اپلیکیشن نمایش می‌دهد حق تألیف جداگانه‌ای است: متعلق به
پاتودکس بوده و از [pathodex.ir](https://pathodex.ir) ارائه می‌شود و شامل مجوز MIT
نمی‌شود. برای جزئیات و سلب مسئولیت پزشکی [NOTICE](NOTICE) را ببینید.
