# RichEditor（Markdown First）

Android 端 Markdown 便签重构项目，已完成 D0-D9 的主线改造（AI 协作模式）。

## 当前状态

- 架构：`app + core/* + data/* + feature/*`
- UI：Jetpack Compose + Material 3（单 Activity 导航）
- 数据：Room + FTS 搜索 + Flow 订阅
- 编辑器：Edit / Preview / Split 三模式，自动保存、防抖、撤销重做
- 渲染：Markwon Markdown 渲染管线
- 媒体：图片导入、资产入库、`note://asset/<id>` 解析
- 设置：DataStore（主题、字号、自动保存）
- 回收站：删除、还原、彻底删除

## 文档索引

- 设计总文档：`doc/REFACTOR_DESIGN.md`
- AI 路线图：`doc/dev/ROADMAP_D0_D9.md`
- 阶段变更记录：`doc/dev/D0_CHANGELOG.md` ... `doc/dev/D8_CHANGELOG.md`

## 本地运行

要求：
- JDK 17
- Android Studio 最新稳定版

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
```

## 测试命令

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## 说明

- 已删除旧富文本 Java 方案（`RichEditor` / `RichEditView` / `RichTextView` 等）及相关本地 jar 依赖。
- 若需查看每阶段具体改动，请参考 `doc/dev/README.md`。
