# D6 设置与导入导出变更记录

> 日期：2026-04-28  
> 状态：设置页已接入 DataStore，回收站已接入真实删除数据流，导入导出有可用最小实现。

## 1) DataStore（`core-datastore`）

新增：

- `UserSettings` / `ThemeMode`
- `SettingsRepository` 接口
- `DataStoreSettingsRepository` 实现
  - 存储项：`theme_mode`、`font_scale`、`auto_save_enabled`
- Hilt 模块：`DataStoreProviderModule` + `DataStoreBindModule`

并更新：

- `core-datastore/build.gradle.kts`：接入 `datastore-preferences`、Hilt、协程
- `libs.versions.toml`：新增 `datastore` 版本与依赖别名

## 2) 数据层补充（`data-note`）

- `NoteDao` 新增 `observeDeleted()`
- `NoteRepository` 新增：
  - `observeDeleted()`
  - `exportAllAsMarkdown()`
  - `importFromMarkdown(...)`
  - `purge(id)`
- `RoomNoteRepository` 已实现上述能力（最小可用）

## 3) 设置页（`feature-settings`）

新增：

- `SettingsUiState`、`SettingsViewModel`
- `SettingsRoute`（真实设置）
  - 主题切换（跟随系统/浅色/深色）
  - 字号滑杆
  - 自动保存开关
  - “预览导出 Markdown”按钮（文本预览）
  - “导入示例”按钮（导入示例便签）

## 4) 回收站页（`feature-settings`）

新增：

- `RecycleBinUiState`、`RecycleBinViewModel`
- `RecycleBinRoute` 接入 `observeDeleted()`
  - 列表展示删除便签
  - 支持“还原”
  - 支持“彻底删除”

## 5) 说明

- 当前导出/导入为最小实现（字符串级）；完整 zip + SAF 工作流将在后续阶段增强。
