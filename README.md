# Guileless Bopomofo 樸實注音鍵盤

![Screenshot of Guileless Bopomofo](./media/Screenshot_20230727_140124.png)
![Screenshot of Guileless Bopomofo](./media/Screenshot_20230727_140620.png)

## About 這是啥

A [Bopomofo](https://en.wikipedia.org/wiki/Bopomofo) software keyboard (aka input method editor) on Android, which is utilizing [libchewing](http://chewing.im/) for intelligent phonetic processing.

樸實注音鍵盤是 Android 平台上的[注音符號](https://zh.wikipedia.org/wiki/%E6%B3%A8%E9%9F%B3%E7%AC%A6%E8%99%9F)軟體鍵盤（輸入法），藉由[新酷音輸入法](http://chewing.im/)程式庫的輔助，提供自動選字處理機能。

<a href='https://play.google.com/store/apps/details?id=org.ghostsinthelab.apps.guilelessbopomofo&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Google Play立即下載' src='https://play.google.com/intl/en_us/badges/static/images/badges/zh-tw_badge_web_generic.png'/></a>
<a href='https://f-droid.org/zh_Hant/packages/org.ghostsinthelab.apps.guilelessbopomofo/'><img alt="F-Droid立即下載" src="./media/badge_get-it-on-zh-tw.png"/></a>

## TL;DR 廢話不多說

1. Get source code:

        git clone --recursive https://github.com/hiroshiyui/GuilelessBopomofo.git

1. Build it:
    * Import this project into Android Studio, then run **"Buile -> Make Project"**, or...
    * execute `./gradlew :app:assembleDebug` or `./gradlew :app:assembleRelease` from shell command line

1. Locate the generated APK files from `./app/build/outputs/apk/`

1. Enjoy!

## About libchewing_android_app_module

Considering the convenience of modularization for development and project management, this project has established the Android App Module of libchewing in a separated location at '[libchewingAndroidAppModule](https://github.com/hiroshiyui/libchewingAndroidAppModule)'.

The embedded _app/libs/libchewing_android_app_module\_\*-release.aar_ is built by the 'libchewingAndroidAppModule' project.

考慮到模組化能夠更方便地進行開發與專案管理，本專案將 libchewing 的 Android App Module 另外開設於 "[libchewingAndroidAppModule](https://github.com/hiroshiyui/libchewingAndroidAppModule)"。

內嵌的 _app/libs/libchewing_android_app_module\_\*-release.aar_ 即是由 "libchewingAndroidAppModule" 專案建置產生。

## Acknowledgements 感謝有您

* [Chewing contributors](http://chewing.im/about.html)
* [Bobby Tung](https://bobtung.medium.com/)
* [Wen-Chun Lin](https://github.com/cataska)
* [Jim Huang (jserv)](https://github.com/jserv)
* [StarForcefield](https://starforcefield.wordpress.com/)
* [Weizhong Yang (zonble)](https://github.com/zonble)
* My dear friends, my family, and my wife.
