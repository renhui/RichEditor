# RichEditor · Markdown First Rich-Text Experience for Android

RichEditor 是一个面向 Android 的 **Markdown First 的富文本体验编辑器**（Rich-like UX, Markdown SSOT）。
它基于 **Kotlin + Jetpack Compose + 模块化架构** 构建，形成一套面向长期演进的现代工程基座。

> 口径说明：它提供\"富文本\"级别的阅读与排版效果，但**存储与编辑的真源始终是 Markdown 纯文本**；富效果来自渲染层对 Markdown 的解释，而不是传统富文本那种\"样式树/Span 结构\"作为主数据模型。

- 包名：`com.example.richtext`
- 当前版本：`2.0.0-d9`（versionCode `9`）
- 平台：Android（`minSdk 24` / `targetSdk 34` / `compileSdk 34`）

---

## 1. 设计目标

1. **Markdown 唯一真源（SSOT）**：内容只存 Markdown 文本，所有渲染（列表摘要、详情、编辑器预览、未来导出）共用一套引擎。
2. **现代 UI 体系**：基于 Material 3（Material You）+ Jetpack Compose，提供动态主题、深色模式、可访问性与流畅动效。
3. **工程现代化**：Kotlin 全量、模块化、MVI/MVVM、Hilt、Room、Coroutines/Flow、Version Catalog。
4. **可扩展、可替换**：渲染、媒体、仓储均通过接口暴露，便于替换实现或在未来接入云同步、跨端、AI 写作辅助。
5. **默认离线 + 默认安全**：无网络依赖，内容资产保存在应用私有目录，权限最小化。

---

## 2. 技术选型

| 维度 | 选型 | 说明 |
| --- | --- | --- |
| 语言 | **Kotlin 1.9.24** | 全量 Kotlin，已移除旧 Java 实现 |
| UI | **Jetpack Compose + Material 3** | `material3` + `material3-window-size-class` |
| 主题 | Material You 动态色（Android 12+）+ 自定义回退色板 | 支持浅色/深色/跟随系统 |
| 架构 | **MVI / MVVM** | `ViewModel` + `StateFlow`（状态）+ `Channel`（单次事件） |
| 依赖注入 | **Hilt 2.51.1** | 简化 ViewModel / Repository / 跨模块注入 |
| 异步 | **Kotlin Coroutines + Flow 1.8.1** | 取代 AsyncTask、回调式 IO |
| 数据库 | **Room 2.6.1 + FTS** | DAO 返回 `Flow`，FTS 支持标题/正文搜索 |
| 偏好存储 | **DataStore (Preferences) 1.1.1** | 取代 SharedPreferences |
| Markdown 渲染 | **Markwon 4.6.2** | 含 tables / tasklist / strikethrough / image |
| 图片加载 | **Glide 4.16.0** | 通过 `markwon-image-glide` 接入渲染管线 |
| 导航 | **Navigation Compose 2.7.7** | 单 Activity + Compose 路由 |
| 构建工具 | **AGP 8.5.2 + Gradle Version Catalog** | `gradle/libs.versions.toml` 集中管理 |
| Java 工具链 | **JDK 17** | `sourceCompatibility = 17`，`jvmTarget = 17` |

> 关键取舍说明：
>
> - 选 **Compose 而非 XML**：状态驱动写法天然契合"编辑/预览/分屏"的实时联动。
> - 选 **Markwon 而非自实现解析**：成熟、可插件化、渲染保真度高。
> - 选 **FTS 而非 LIKE 查询**：列表/搜索路径上能保持稳定的低延迟。
> - 选 **DataStore 而非 SharedPreferences**：协程友好，避免主线程阻塞写入。

---

## 3. 整体架构

### 3.1 分层结构

```text
RichEditor/
├── app/                                      # 装配层（单 Activity 入口）
│   ├── RichNoteApplication.kt                # @HiltAndroidApp
│   ├── MainActivity.kt                       # @AndroidEntryPoint，setContent { Theme { NavGraph() } }
│   └── navigation/                           # AppNavGraph（路由表）
├── core/
│   ├── core-common                           # 通用模型 / 工具 / 调度器
│   ├── core-design                           # 颜色、字号、形状等设计 token
│   ├── core-ui                               # 主题（RichNoteTheme）+ 复用 Composable
│   ├── core-database                         # Room（RichNoteDatabase + DAO + Entity）
│   ├── core-datastore                        # DataStore Preferences
│   ├── core-media                            # MediaImporter（选图/相机/压缩/EXIF 脱敏）
│   └── core-markdown                         # MarkdownRenderer + 摘要/首图提取
├── data/
│   └── data-note                             # NoteRepository 实现（Room + Markdown + Media 编排）
└── feature/
    ├── feature-note-list                     # 列表 + 搜索 + 排序
    ├── feature-note-detail                   # 只读详情/预览
    ├── feature-note-editor                   # 编辑/预览/分屏 三模式
    └── feature-settings                      # 设置 + 回收站
```

### 3.2 依赖方向

```text
feature-*  ──►  data-*  ──►  core-database / core-media / core-markdown / core-datastore
   │
   └────────►  core-ui / core-design / core-common
```

**铁律：**

- `feature` 之间**不允许横向依赖**。
- `core` 模块**不允许反向依赖** `feature` / `data`。
- `app` 模块仅做装配（Hilt + Navigation + Theme），不写业务逻辑。

### 3.3 单向数据流（UDF）

```text
   ┌────────────┐    Intent    ┌────────────┐    Flow     ┌────────────┐
   │  Composable │ ───────────►│  ViewModel │ ───────────►│ Repository │
   │   (UI 层)   │             │  (状态机)  │             │ (data 层)  │
   └────────────┘◄──────────── └────────────┘◄─────────── └────────────┘
                  StateFlow                    Room/Flow
                  (UiState)                    (Entity → Domain)
```

- UI 不持有可变副本，仅消费 `StateFlow<UiState>`。
- 一次性事件（导航、Snackbar、唤起选图）通过 `Channel<Effect>` 传递，避免被 `StateFlow` 重放。
- 所有 IO 调度在 `Dispatchers.IO`，UI 线程只做渲染。

---

## 4. 模块职责一览

| 模块 | 关键产物 | 职责 |
| --- | --- | --- |
| `:core-common` | `Result`、`DispatcherProvider` | 通用工具与协程调度抽象 |
| `:core-design` | 颜色 / 间距 / 形状 / 字号 token | 设计系统的最底层常量 |
| `:core-ui` | `RichNoteTheme`、基础 Composable | M3 主题封装与可复用组件 |
| `:core-database` | `RichNoteDatabase`、`NoteDao`、`NoteAssetDao` | Room 数据库与 DAO（FTS 搜索内置） |
| `:core-datastore` | `SettingsDataStore` | 主题、字号、自动保存等偏好持久化 |
| `:core-media` | `MediaImporter` / `AndroidMediaImporter` | 选图、压缩、EXIF 脱敏、写入私有目录 |
| `:core-markdown` | `MarkdownRenderer`、`MarkdownTextParser` | Markdown 渲染、摘要/首图/标题提取 |
| `:data-note` | `NoteRepository` / `RoomNoteRepository`、`NoteSearchQueryFormatter` | Repository 实现 + Flow 组合 + 搜索查询构造 |
| `:feature-note-list` | `NoteListRoute`、`NoteListViewModel` | 列表 / 搜索 / 排序 / 卡片摘要 |
| `:feature-note-detail` | `NoteDetailRoute`、`NoteDetailViewModel` | 只读详情，复用 Markdown 渲染 |
| `:feature-note-editor` | `NoteEditorRoute`、`NoteEditorViewModel`、`EditorUiState/Action/Mode/SaveState` | 编辑器三模式 + 防抖保存 + 工具栏 |
| `:feature-settings` | `SettingsRoute`、`RecycleBinRoute` | 设置项与回收站（恢复 / 彻底删除） |

---

## 5. 关键设计决策

### 5.1 内容真源：Markdown 文本

`NoteEntity.markdown` 是唯一可信内容源。
不再保留 HTML 字段，所有渲染路径（列表摘要、详情、编辑器预览、未来导出）共用 `MarkdownRenderer` 与 `MarkdownTextParser`。

### 5.2 资源协议：`note://asset/<id>`

图片不写绝对路径，而是用自定义 scheme：

```markdown
![sunset](note://asset/9e2c1c50-6f6b-4a5e-b4ad-8b7e1c8e2d4f)
```

- 渲染层通过 `SchemeHandler` 解析为本地文件，`NoteAssetEntity` 维护资产元数据（路径、尺寸、MIME）。
- 文本本身**与设备无关**，备份/导入/跨端复制都不会失效。
- 删除笔记时按外键级联清理，避免孤儿资产。

### 5.3 编辑器状态机

- `EditorUiState`：纯数据类，包含文本、选区、模式（Edit / Preview / Split）、保存态（Idle / Saving / Saved / Error）、撤销重做栈。
- `EditorAction`：所有用户意图（输入、格式化、插图、模式切换、撤销/重做）都通过 `Action` 进入 `ViewModel`，避免 UI 直接操作底层文本。
- 工具栏所有按钮 → `EditorAction` → 纯函数 reducer 修改 `TextFieldValue`，不直接触碰 `TextField`。

### 5.4 防抖自动保存

```text
TextChanged ──► snapshotFlow { state.draft }
            ──► debounce(500ms)
            ──► Repository.update(...)  // Dispatchers.IO
            ──► SaveState.Saved
```

- 离开页面或切后台时**强制 flush**，杜绝丢稿。
- 顶栏小指示器实时反馈"保存中 / 已保存"。

### 5.5 搜索：Room FTS

- 通过 FTS 虚拟表索引标题与正文。
- ViewModel 层对查询关键词 `debounce(300ms)`，再交给 Repository 用 `combine(query, sort) { ... flatMapLatest { dao.observe(...) } }` 触发 Room 重新发送数据。
- `NoteSearchQueryFormatter` 负责把用户输入转义为 FTS MATCH 表达式，避免 SQL 注入与畸形查询崩溃。

### 5.6 媒体导入

- 使用 `ActivityResultContracts.PickVisualMedia` / `TakePicture`，**完全绕开存储权限**。
- 导入后复制到 `context.filesDir/notes/<noteId>/img/<uuid>.webp`，长边压到 1920px、WebP 85%。
- 默认剥离 EXIF（含地理位置）。
- 仅在使用相机时声明 `CAMERA` 权限，不再请求 `READ_PHONE_STATE` / `WRITE_EXTERNAL_STORAGE` / `ACCESS_WIFI_STATE` 等历史多余权限。

### 5.7 软删除与回收站

- 删除走 `deletedAt` 字段软删除 + Snackbar 5s 撤销。
- 回收站列表支持还原 / 彻底删除；彻底删除时同步清理资产文件。

---

## 6. 已交付能力（v2.0.0-d9）

- 笔记列表：搜索、排序、卡片摘要、首图缩略、空态
- 编辑器：Edit / Preview / Split 三模式，防抖自动保存，撤销重做，格式化工具栏
- Markdown：标题、粗体/斜体/删除线、行内代码、代码块、引用、有序/无序列表、任务列表、链接、图片、分隔线、表格
- 媒体：相册 / 相机选图 → 压缩 → 资产入库 → 插入 `note://asset/<id>`
- 详情：只读阅读、`SelectionContainer` 选中复制
- 设置：主题、字号、自动保存等偏好持久化（DataStore）
- 回收站：软删除 → 还原 → 彻底删除（含资产清理）

---

## 7. 工具链与构建

### 7.1 环境要求

- **JDK 17**
- **Android Studio** 最新稳定版
- Android SDK：`compileSdk 34` / `targetSdk 34` / `minSdk 24`

### 7.2 常用命令

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Debug 包
./gradlew :app:assembleDebug

# Release 包（当前未启用 minify）
./gradlew :app:assembleRelease

# 单元测试（推荐 PR 前跑）
./gradlew testDebugUnitTest

# 仪器化测试（需要连接设备/模拟器）
./gradlew connectedDebugAndroidTest

# 清理
./gradlew clean
```

### 7.3 依赖管理

所有依赖与版本统一收敛在 `gradle/libs.versions.toml`，子模块通过 `libs.xxx` 引用，禁止在子模块直接写硬编码版本。

---

## 8. 测试策略

| 层 | 范围 | 工具 |
| --- | --- | --- |
| 单元测试 | `MarkdownRenderer`、`MarkdownTextParser`、`NoteSearchQueryFormatter`、Repository 状态归约 | JUnit + `kotlinx-coroutines-test` |
| DAO 测试 | Room 内存数据库下的 CRUD / FTS | `room-testing` |
| UI 测试 | 列表、详情、编辑器关键路径（输入、保存、模式切换） | `compose-ui-test-junit4` + Espresso |

代码贡献时优先用单元测试覆盖 reducer / 纯函数路径，UI 测试只覆盖最关键的端到端流程。

---

## 9. 工程约定（贡献者必读）

- 不引入 **LiveData / RxJava / AsyncTask**；状态用 `StateFlow`，事件用 `Channel`。
- Composable **禁止任何 IO**；副作用必须走 `LaunchedEffect` / `DisposableEffect`。
- ViewModel **只暴露 `StateFlow<UiState>`**，不暴露可变 state。
- 颜色、字号、间距**只允许从 `MaterialTheme` / `core-design` 读取**，禁止硬编码。
- 仅修改 Markdown 文本即可改变内容，**不写 HTML、不存绝对路径**。
- 新增依赖必须先在 `libs.versions.toml` 登记，再在子模块引用。
- Commit 前缀：`feat:` / `fix:` / `refactor:` / `test:` / `docs:` / `chore:`。

---

## 10. 路线图

### 10.1 已交付

- ✅ 模块化骨架（`app` + `core/*` + `data/*` + `feature/*`）
- ✅ Room + FTS 数据层 + Flow 订阅
- ✅ Markdown 渲染管线（Markwon + 自定义资源协议）
- ✅ 编辑器三模式 + 防抖保存 + 撤销重做
- ✅ 媒体导入闭环（PickVisualMedia / 相机 / 压缩 / 资产表）
- ✅ 设置 + 回收站

### 10.2 计划中

- 笔记导入导出（单文件 `.md` / `.zip` 含图片）
- Baseline Profile 与冷启动优化
- 列表 → 详情 共享元素 / 容器变换
- 标签系统与双向链接（`[[wiki link]]`）
- 云同步（WebDAV / Drive / 自托管）
- Compose Multiplatform 跨端复用 `core-markdown` 与 `data-note`
- 端侧 AI 写作辅助（摘要、续写、润色）

---

## 11. 许可

本仓库当前用作个人重构与学习项目。如需再发布，请在引入前补充 `LICENSE` 文件。
