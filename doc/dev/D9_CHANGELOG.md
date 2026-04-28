# D9 收尾清理变更记录

> 日期：2026-04-28  
> 状态：旧富文本 Java 链路与历史依赖已清理，Manifest 与 README 已切到新架构口径。

## 1) 删除旧实现

已删除旧目录：

- `app/src/main/java/com/example/richtext/ui/**`
- `app/src/main/java/com/example/richtext/sqlite/**`
- `app/src/main/java/com/example/richtext/moudle/**`
- `app/src/main/java/com/example/richtext/utils/**`

说明：旧 `RichEditor/RichEditView/RichTextView`、旧 Activity、旧 SQLite 访问层等已彻底移除。

## 2) 清理历史依赖

已删除本地 jar：

- `app/libs/common-lang3.jar`
- `app/libs/photo-view.jar`
- `app/libs/umeng-analytics-v5.6.1.jar`
- `app/libs/universal-image-loader-1.9.5.jar`

并移除 `app/build.gradle.kts` 中的 `fileTree` 引用与不再需要的 legacy 依赖。

## 3) Manifest 精简

- 权限仅保留：`CAMERA`
- 删除旧 Activity 注册（`NoteActivity`、`NoteEditActivity`、`NoteDetailActivity`、`NoteReEditActivity`）
- 删除友盟 `meta-data`
- 删除 FileProvider 与 `xml/file_paths.xml`（当前链路未使用相机输出到文件）
- 启动入口仅保留 `MainActivity`

## 4) 文档同步

- 根 `README.md` 改为新架构与新能力说明
- 更新 `doc/dev/README.md` 索引并新增本文件

## 5) 注意事项

- 本次为结构性清理，建议在联网环境执行完整构建与回归：
  - `./gradlew :app:assembleDebug`
  - `./gradlew testDebugUnitTest`
  - `./gradlew connectedDebugAndroidTest`
