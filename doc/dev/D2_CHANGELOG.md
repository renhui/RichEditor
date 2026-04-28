# D2 Markdown 渲染管线变更记录

> 日期：2026-04-28  
> 状态：`core-markdown` 已接入 Markwon，详情页可渲染 Markdown。

## 1) 依赖与模块

- `gradle/libs.versions.toml`
  - 新增 `markwon = 4.6.2`
  - 新增 `markwon-core/tasklist/tables/strikethrough` 依赖坐标
- `core/core-markdown/build.gradle.kts`
  - 接入 `kapt` + Hilt 插件
  - 新增 Markwon 依赖
- `app/build.gradle.kts`
  - 新增 `implementation(project(":core:core-markdown"))`
- `feature/feature-note-detail/build.gradle.kts`
  - 接入 `core-markdown`、`data-note`、Hilt、`lifecycle-runtime-compose`

## 2) Markdown 渲染核心（`core-markdown`）

新增：

- `MarkdownRenderer` 接口
  - `render(markdown, TextView)`
  - `extractPlainSummary(...)`
  - `extractFirstImagePath(...)`
- `MarkwonMarkdownRenderer` 实现
  - 基于 Markwon 渲染
  - 提供摘要提取与首图提取逻辑
- `MarkdownModule`
  - Hilt 提供 `Markwon`
  - 绑定 `MarkdownRenderer <- MarkwonMarkdownRenderer`
  - 启用 `tables/tasklist/strikethrough` 插件

> `note://asset/...` 的本地图片映射将在 D5 资产链路打通后补齐完整加载策略。

## 3) 详情页接入真实渲染（`feature-note-detail`）

新增：

- `NoteDetailUiState`
- `NoteDetailViewModel`
  - 从 `SavedStateHandle` 读取 `noteId`
  - 订阅 `NoteRepository.observeById(noteId)` 并映射 UI 状态

改造：

- `NoteDetailRoute`
  - 改为 `AndroidView(TextView)` + `MarkdownRenderer.render(...)`
  - 文案不再是 D0 占位字符串，改为数据库真实 markdown 内容

## 4) 导航与注入

- `MainActivity`
  - 注入 `MarkdownRenderer`
  - 传入 `RichNoteNavHost(markdownRenderer = ...)`
- `RichNoteNavHost`
  - 接受 `markdownRenderer` 参数
  - 详情页路由改为注入式渲染链路

## 5) 待验证项

当前环境仍受限于 Gradle 分发下载超时，未完成编译。请在本机联网后执行：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```
