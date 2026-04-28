# D1 数据层变更记录

> 日期：2026-04-28  
> 状态：Room + FTS + Repository + Hilt 已接入；列表页已绑定真实 Flow 数据。

## 1) Room 数据模型（`core-database`）

新增：

- `NoteEntity`：`notes` 主表（`id/title/markdown/created_at/updated_at/pinned/deleted_at/cover_image_path/word_count`）
- `NoteAssetEntity`：`note_assets` 资产表（外键 `note_id -> notes.id`）
- `NoteFtsEntity`：`notes_fts`（`@Fts4(contentEntity = NoteEntity::class)`）

新增 DAO：

- `NoteDao`
  - 活跃列表订阅（更新时间 / 置顶优先）
  - FTS 搜索订阅（更新时间 / 置顶优先）
  - `observeById`、`upsert`、`softDelete`、`restore`、`updatePinned`、`countActiveNotes`
- `NoteAssetDao`
  - 按 note 订阅资产、`upsert`、删除

数据库入口：

- `RichNoteDatabase`（version = 1）

## 2) Repository（`data-note`）

新增：

- `NoteSort`
- `NoteSummary`
- `NoteRepository` 接口
- `RoomNoteRepository` 实现

实现细节：

- 使用 `combine(queryFlow, sortFlow)` + `flatMapLatest` 切换上游查询
- 搜索词通过 `buildFtsMatchQuery()` 转为 FTS MATCH 语法（token + `*`）
- 列表项通过 `toSummary()` 生成预览文本（简化 Markdown 符号）
- `ensureSeedData()` 首次启动自动写入 2 条演示数据

## 3) Hilt 注入（`data-note`）

新增 `NoteDataModule`：

- 提供 `RichNoteDatabase`（`Room.databaseBuilder(...).fallbackToDestructiveMigration()`）
- 提供 `NoteDao`、`NoteAssetDao`
- `@Binds` 绑定 `NoteRepository <- RoomNoteRepository`

## 4) 列表页接真实数据（`feature-note-list`）

新增：

- `NoteListUiState`
- `NoteListViewModel`（`@HiltViewModel`）
  - 管理 `queryFlow/sortFlow`
  - `uiState` 通过 Repository `Flow` 聚合
  - `init` 时调用 `ensureSeedData()`
- `NoteListRoute`
  - 搜索输入框（FTS）
  - 排序切换（置顶优先 / 更新时间）
  - 展示真实 note 列表卡片并可进入详情

## 5) 构建配置变更

- `gradle/libs.versions.toml`
  - 新增 `room` 版本及 `room-runtime/ktx/compiler/testing`
  - 新增 `lifecycle-runtime-compose`
  - 新增 `hilt-navigation-compose`
- `core/core-database/build.gradle.kts`
  - 新增 `kapt`、Room 依赖
- `data/data-note/build.gradle.kts`
  - 新增 `kapt`、Hilt 插件、Room/Hilt 依赖
- `feature/feature-note-list/build.gradle.kts`
  - 新增 `kapt`、Hilt 插件、`data-note` 依赖、`hilt-navigation-compose`
- `app/build.gradle.kts`
  - 版本提升到 `2.0.0-d1`
  - 添加对 `:data:data-note` / `:core:core-database` 的引用

## 6) 当前阻塞

无法在当前环境完成构建验证：`./gradlew :app:assembleDebug` 下载 `gradle-8.7-bin.zip` 超时。

请在本机联网后执行：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```
