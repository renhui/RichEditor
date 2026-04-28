# D0 脚手架变更记录

> 日期：2026-04-28  
> 状态：结构已落地；请在本地联网执行 `./gradlew :app:assembleDebug` 完成首次依赖与 Gradle 分发下载。

## 构建系统

- 删除根目录 `build.gradle`（Groovy）、`settings.gradle`，改为 **`build.gradle.kts`**、**`settings.gradle.kts`**
- **`gradle/libs.versions.toml`**：集中管理 AGP、Kotlin、Compose BOM、Hilt、Navigation 等版本
- **Gradle Wrapper**：**8.7**（需首次联网下载）
- **`app/build.gradle`** → **`app/build.gradle.kts`**：`minSdk 24`、`compileSdk 34`、Compose、Hilt、`fileTree(libs/*.jar)` 保留以兼容旧 Java 代码与友盟等 jar

## 模块（空壳 + 占位类）

见 `settings.gradle.kts` 中 `include`：`core-*`、`feature-*`、`data:data-note`。各模块含最小 `build.gradle.kts` 与 `*Placeholder.kt`（或 D0 UI）。

## 应用入口

- **`RichNoteApplication`**：`@HiltAndroidApp`，`getInstance()` 供 `ToastUtils` 等旧代码使用；已删除 **`MyApplication.java`**
- **`MainActivity`**：`@AndroidEntryPoint`，`RichNoteTheme` + `RichNoteNavHost`
- **`AndroidManifest.xml`**：`MainActivity` 为 **LAUNCHER**；`NoteActivity` 改为 `exported="false"` 且无 MAIN 过滤器

## UI（Compose 占位）

- **`core-ui`**：`RichNoteTheme`（Material 3 + Android 12+ 动态色）
- **`feature-note-list`**：`NoteListRoute`
- **`feature-note-detail`**：`NoteDetailRoute`
- **`feature-note-editor`**：`NoteEditorRoute`（多行 Markdown 占位输入框）
- **`feature-settings`**：`SettingsRoute`、`RecycleBinRoute`

## 下一步（D1）

接入 Room、`NoteRepository`、FTS，并将列表/详情绑定真实数据（见 `doc/REFACTOR_DESIGN.md` §5、§10）。
