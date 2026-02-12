# 纯净日历

面向 Android 的日历应用，支持公历与中国农历双历展示，并提供桌面小组件。

## 功能概览

- **日历主界面**：公历 + 农历（如正月初一、腊月廿三），左右滑动切换月份，今日与选中高亮区分，点击日期显示详情及相对今天的天数；顶部「今」回到今天，齿轮进入设置。
- **桌面小组件**：第一行公历月/日 + 星期，第二行农历月/日

## 技术说明

- 农历：1900–2100 年公历↔农历转换（`LunarCalendarHelper`）。
- 主界面：ViewPager2 月视图 + RecyclerView 7 列网格，ViewModel + LiveData。
- 小组件：App Widget，按系统日期自动更新。

## 环境与构建

- **最低 / 目标**：minSdk 34，targetSdk 35（对应需求 NF-001/NF-002：目标 Android 15，兼容 Android 14）。
- **构建**：Android Studio 打开工程，`./gradlew assembleDebug` 或 Run 安装到设备。

## 需求文档

详见 [doc/日历需求文档.md](doc/日历需求文档.md)。
