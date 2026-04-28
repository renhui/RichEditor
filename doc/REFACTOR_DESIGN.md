# RichNote 一步到位重构技术方案

> 适用版本：尚未发布的下一代 RichNote（Markdown First）
> 文档版本：v1.0 · 2026-04-27
> 范围：数据层、编辑器、渲染、UI/UX、工程结构、构建、测试

---

## 1. 重构背景与目标

### 1.1 现状问题

- **存储格式不可控**：内容以 `HTML` 片段存储，依赖 `Html.fromHtml` 与正则提取 `<img>`，跨设备/跨平台兼容差。
- **编辑器复杂且不稳**：自研 `RichEditor`（多 `EditText` + 图片容器）和 `RichEditView`（单 `EditText` + `ImageSpan`）双控件并行，回编、合并、光标、删除逻辑脆弱。
- **UI 体系陈旧**：使用 `ActionBar` + 老旧 `LinearLayout` 布局、自绘 FAB、自实现长微博，缺现代设计语言。
- **架构非分层**：Activity 直接耦合 SQLite、Glide、文件 IO、业务逻辑。
- **依赖老化**：含本地 `umeng` jar、`commons-lang3.jar`、`photo-view.jar` 等本地依赖。

### 1.2 重构目标

1. **Markdown First**：内容唯一真源是 Markdown 文本，所有渲染（列表摘要、详情预览、导出）共用一套引擎。
2. **现代 UI**：基于 Material 3（Material You）+ Jetpack Compose，提供动态主题、深色模式、可访问性、流畅动效。
3. **工程现代化**：Kotlin + 模块化 + MVVM/MVI + Hilt + Room + Coroutines/Flow。
4. **可扩展**：图片、附件、标签、搜索、导出能力按插件式接入。
5. **可演进**：为后续云同步、跨端、AI 辅助预留扩展点。

### 1.3 非目标（本期不做）

- 多端云同步、协同编辑
- 富文本（带样式数据）的二进制方案（Quill Delta、ProseMirror 等）
- 第三方账号体系

---

## 2. 总体设计原则

- **单一数据源（SSOT）**：UI 状态来自 ViewModel 暴露的 `StateFlow`，UI 不持有可变副本。
- **单向数据流（UDF）**：UI -> Intent -> ViewModel -> State -> UI。
- **关注点分离**：渲染、编辑、存储、媒体处理分别隔离，互不依赖具体实现。
- **可替换核心**：`MarkdownRenderer`、`MediaStore`、`NoteRepository` 通过接口暴露。
- **默认安全**：不持有外部 URI、不写公共目录、不使用 `WRITE_EXTERNAL_STORAGE`。
- **默认离线**：无需网络即可完整使用。

---

## 3. 技术栈

| 维度 | 选型 | 备注 |
|---|---|---|
| 语言 | **Kotlin 1.9.x** | 全量 Kotlin，移除 Java |
| UI | **Jetpack Compose + Material 3** | `material3` `material3-window-size-class` |
| 主题 | Material You 动态色 (Android 12+) + 自定义回退色 | 支持深色 / 浅色 / 跟随系统 |
| 架构 | **MVI（推荐）/ MVVM** | `ViewModel` + `StateFlow` + `Channel`(单次事件) |
| DI | **Hilt** | 简化 ViewModel/Repository/UseCase 注入 |
| 异步 | **Coroutines + Flow** | 替换 AsyncTask、Glide future.get |
| 数据库 | **Room 2.6+** | 内置 Flow，迁移可控 |
| 偏好 | **DataStore (Preferences)** | 替代 SharedPreferences |
| 图片加载 | **Coil 2.x（Compose 友好）** | 替换 Glide，体积更小 |
| Markdown | **Markwon 4.x（渲染）+ CommonMark 解析** | 通过适配层桥接到 Compose |
| 导航 | **Navigation Compose** | 单 Activity + Compose 路由 |
| 序列化 | **kotlinx.serialization** | 笔记导入/导出、备份 |
| 日志 | **Timber** | 统一日志门面 |
| 测试 | JUnit5 + Turbine + Compose UI Test + Robolectric | |
| 构建 | **AGP 8.x + Gradle 8.x + Version Catalog** | `libs.versions.toml` |
| 最低版本 | **minSdk 24 / targetSdk 34 / compileSdk 34** | 24 才能畅享 Compose、新 API、分区存储 |

> 取舍说明：
> - 选 **Compose 而非 XML**：用户明确要求“市面最新的 UI 布局与体验”，Compose 是 Android 官方推荐的现代 UI 工具，状态驱动写法更适合编辑/预览实时联动。
> - 选 **Coil 而非 Glide**：Compose 原生集成，Kotlin first，包体更小，且不再需要本地 jar。
> - 选 **Markwon 而非纯自实现**：成熟、可插件化，渲染保真度高；编辑器侧通过 `Markwon Editor` 做语法高亮辅助。

---

## 4. 工程结构（多模块）

```
RichEditor/                       # 项目根
├── app/                          # 入口模块（仅装配）
│   └── RichNoteApplication.kt
├── core/
│   ├── core-ui/                  # 主题、设计 token、基础 Composable
│   ├── core-design/              # 颜色、字体、间距、Shape、Motion
│   ├── core-common/              # 通用工具、Result、Dispatcher
│   ├── core-database/            # Room 数据库、DAO
│   ├── core-datastore/           # DataStore Preferences
│   ├── core-media/               # 图片导入、压缩、清理
│   └── core-markdown/            # Markwon 配置、渲染、纯文本摘要
├── feature/
│   ├── feature-note-list/        # 列表 + 搜索
│   ├── feature-note-editor/      # 编辑/预览/分屏
│   ├── feature-note-detail/      # 只读详情（也可与 editor 复用）
│   └── feature-settings/         # 设置（主题、字号、备份）
├── data/
│   └── data-note/                # NoteRepository 实现，组合 DAO + Media + Markdown
└── build-logic/                  # Convention Plugins（统一编译参数）
```

> 单 Activity 架构：`app` 模块只有一个 `MainActivity`，通过 `Navigation Compose` 切换页面。

### 4.1 模块依赖方向

```
feature-* ──► data-* ──► core-database / core-media / core-markdown
   │
   └─► core-ui / core-design / core-common
```

不允许 feature 互相依赖，不允许 core 反向依赖 feature。

---

## 5. 数据层设计

### 5.1 实体

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,                 // UUID
    val title: String,
    val markdown: String,                       // 唯一真源
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean = false,
    val deletedAt: Long? = null,                // 软删除（回收站）
    val coverImagePath: String? = null,         // 列表卡片封面（首图缓存）
    val wordCount: Int = 0
)

@Entity(
    tableName = "note_assets",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class NoteAssetEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val relativePath: String,                   // notes/<noteId>/img/xxx.webp
    val mimeType: String,
    val sizeBytes: Long,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Long
)
```

> 不再保留 HTML 字段；图片通过资产表显式管理，便于清理孤儿资源。

### 5.2 仓库接口

```kotlin
interface NoteRepository {
    fun observeAll(query: String? = null, sort: NoteSort = NoteSort.UpdatedDesc): Flow<List<NoteSummary>>
    fun observeById(id: String): Flow<Note?>
    suspend fun create(initial: NoteDraft): String
    suspend fun update(note: Note)
    suspend fun togglePin(id: String, pinned: Boolean)
    suspend fun softDelete(id: String)
    suspend fun restore(id: String)
    suspend fun purge(id: String)                 // 物理删除 + 资产清理
    suspend fun export(id: String, format: ExportFormat): Uri
    suspend fun import(uri: Uri): String
}
```

### 5.3 备份/导入导出

- **单文件导出**：`.md`（纯 Markdown，图片用 base64 内联或外部 zip 包）。
- **批量导出**：`.zip`，目录结构为 `notes/<id>/index.md` + `notes/<id>/img/*`。
- **JSON 备份**：`kotlinx.serialization` 序列化 `NoteEntity` + 资产清单，便于跨设备恢复。

---

## 6. Markdown 引擎设计

### 6.1 渲染契约

```kotlin
interface MarkdownRenderer {
    fun renderToAnnotated(markdown: String): AnnotatedString          // Compose 文本
    fun renderToView(markdown: String, container: ComposeView)        // 复杂内容（图片、代码块）
    fun extractPlainSummary(markdown: String, maxChars: Int = 120): String
    fun extractFirstImagePath(markdown: String): String?
    fun extractTitle(markdown: String, fallback: String): String      // 第一行 # 或首行非空
}
```

### 6.2 支持的语法

- 必备：标题、粗体/斜体/删除线、行内代码、代码块（含语言标记）、引用、有序/无序列表、任务列表、链接、图片、分隔线、表格。
- 进阶：脚注、Mermaid（可选插件，渲染为图片）、KaTeX（可选）。
- 不支持：自定义 HTML（禁用以防 XSS 与渲染歧义）。

### 6.3 图片协议

统一使用 `note://asset/<assetId>` 作为图片源，渲染层映射到本地私有目录，Markdown 文本保持设备无关：

```
![sunset](note://asset/9e2c1c50-...)
```

> 导出 `.zip` 时再把 `note://asset/...` 重写为相对路径 `./img/xxx.webp`。

---

## 7. UI / UX 设计

### 7.1 设计语言

- **Material 3 (Material You)**：动态色优先，回退色板基于品牌主色（建议靛蓝 #4F6BED 或自定）。
- **字体系统**：Display / Headline / Title / Body / Label 五级，编辑器内容使用 `JetBrains Mono` 或系统等宽字体的可选项。
- **形状**：默认 12dp 圆角，卡片 16dp，按钮 20dp。
- **动效**：使用 `MotionScheme` Standard，列表入场 staggered，详情进入用容器变换 (Container Transform)。
- **深色模式**：跟随系统，关键阅读色对比度 ≥ 4.5。
- **可访问性**：所有可点击元素 ≥ 48dp，提供 `contentDescription`，TalkBack 友好。
- **国际化**：默认中文，预留 `values-en` 字符串占位。

### 7.2 屏幕清单

| 屏幕 | 角色 | 关键控件 |
|---|---|---|
| 启动闪屏 | 系统 SplashScreen API | 品牌图标 + 动态主色 |
| 笔记列表 | 主页 | 顶部 LargeTopAppBar + 搜索 + 排序、卡片瀑布流 / 双列、FAB.Extended |
| 编辑器 | 创建/再编辑 | 三模式（编辑/预览/分屏）、底部 BottomAppBar 工具栏、IME 适配 |
| 详情预览 | 只读 | 沉浸式阅读、滚动隐藏顶栏、目录抽屉 |
| 设置 | 个性化 | 主题、字号、自动保存、备份恢复 |
| 回收站 | 软删除恢复 | 列表 + 还原 + 彻底删除 |
| 标签管理（可选） | 分类 | 标签 Chip、过滤 |

### 7.3 笔记列表

- **布局**：`LazyVerticalStaggeredGrid`，紧凑型 1 列、平板/横屏 2-3 列。
- **卡片信息**：标题（最多 2 行）+ 摘要（4 行，Markdown 转纯文本）+ 时间 + 标签 Chip + 首图缩略图（若有，圆角覆盖右上角）。
- **顶栏**：`LargeTopAppBar`，滚动折叠；右侧按钮为搜索、排序、视图切换（瀑布流/列表）。
- **搜索**：`SearchBar`（M3），支持标题、正文、标签全文匹配（FTS4 / FTS5）。
- **空态**：插画 + 一句鼓励语 + “开始第一条便签”按钮。
- **手势**：长按多选；左滑置顶/取消，右滑删除（带 SnackBar 撤销）。
- **FAB**：`ExtendedFloatingActionButton`，滚动时收缩成图标。

### 7.4 编辑器

#### 7.4.1 模式切换

- **编辑模式**：纯文本输入，行号可选，光标定位精确，软键盘上方悬浮工具栏。
- **预览模式**：仅渲染结果，可滚动；提供“返回编辑”浮动按钮。
- **分屏模式**：横屏/平板默认开启；纵向手机仅在长按模式按钮时启用，左右两栏滚动联动（基于段落行号）。

#### 7.4.2 工具栏（BottomAppBar）

按钮分组（左滑/右滑切换组）：
1. **格式组**：H1/H2/H3、加粗、斜体、删除线、行内代码。
2. **块组**：引用、代码块、分隔线、任务列表、有序/无序列表。
3. **插入组**：链接、图片、表格、日期、Checkbox。
4. **视图组**：撤销 / 重做 / 模式切换 / 大纲。

> 工具栏跟随 IME 浮起；点击任何按钮通过 `EditorAction` 改变文本而不是直接操作 `TextField`。

#### 7.4.3 输入体验

- **自动补全**：列表换行自动续 `- `、`1. `、`> `；任务项续 `- [ ] `。
- **智能粘贴**：粘贴 URL 时若处于选中文本上则自动转 `[text](url)`；粘贴图片直接调用图片导入流程。
- **撤销/重做**：基于 `TextFieldValue` 历史栈，最多 200 步。
- **自动保存**：500ms 防抖写库；离开页面强制 flush；保存状态在顶栏小字提示（"已保存 / 保存中"）。
- **大文档**：> 50KB 时启用增量预览（按段落 diff，仅重渲染受影响节点）。
- **滚动同步**：分屏下编辑器滚动到第 N 行 -> 预览滚动到对应段落 anchor。

#### 7.4.4 工具栏插入图片流程

```
点击插图 → 选择来源（相册 / 相机 / 粘贴板）
       → ActivityResultContracts.PickVisualMedia / TakePicture
       → MediaImporter 复制到 filesDir/notes/<noteId>/img/<uuid>.webp（压缩到 1080p / 85 质量）
       → 写 NoteAssetEntity
       → 在光标处插入 ![alt](note://asset/<id>)
       → Compose 通过 MarkdownRenderer 解析并用 Coil 显示
```

### 7.5 详情/预览

- 顶部 `MediumTopAppBar`，滚动时折叠；右上角“编辑”、“分享”、“导出”、“更多”。
- 内容区使用 `SelectionContainer` 支持选中复制。
- 长按图片 → 弹出大图查看（`Modifier.pointerInput` 实现 pinch-to-zoom 与 swipe to dismiss，替代 `photo-view.jar`）。
- 阅读进度条贴顶；目录抽屉从右侧滑出，列出所有 H1-H3。

### 7.6 设置

分组：
- 外观：主题（跟随系统/浅色/深色）、动态色开关、字号、字体。
- 编辑器：自动保存、智能列表、行号、Tab 大小、显示不可见字符。
- 数据：导出全部、导入、备份位置、清理孤儿图片。
- 关于：版本、开源协议、反馈。

### 7.7 动效与微交互

- **页面过渡**：列表卡片 → 详情，使用 `SharedElement / LookaheadLayout`（Compose 1.7+）的容器变换。
- **保存提示**：顶栏小圆点（保存中蓝色闪烁，已保存灰色对勾）。
- **删除撤销**：`Snackbar` + Material Action，5 秒内可撤销。
- **空态/错误**：图文结合，非空白屏。
- **触觉反馈**：长按、置顶、删除等关键操作给 `HapticFeedback`。

---

## 8. 状态管理（编辑器示例）

```kotlin
data class EditorUiState(
    val noteId: String,
    val title: String,
    val draftMarkdown: TextFieldValue,
    val mode: EditorMode,                        // Edit / Preview / Split
    val saveState: SaveState,                    // Idle / Saving / Saved(time) / Error
    val outline: List<HeadingItem>,
    val isInsertingImage: Boolean = false,
    val toolbarGroup: ToolbarGroup = ToolbarGroup.Format,
    val undoRedo: UndoRedoState
)

sealed interface EditorIntent {
    data class TextChanged(val value: TextFieldValue) : EditorIntent
    data class TitleChanged(val title: String) : EditorIntent
    data class FormatApplied(val action: EditorAction) : EditorIntent
    data class InsertImage(val source: ImageSource) : EditorIntent
    data class ModeSwitched(val mode: EditorMode) : EditorIntent
    object Undo : EditorIntent
    object Redo : EditorIntent
    object Save : EditorIntent
}

sealed interface EditorEffect {
    data class ShowSnackbar(val msg: String, val undo: (() -> Unit)? = null) : EditorEffect
    data object NavigateBack : EditorEffect
}
```

`ViewModel` 通过 `StateFlow<EditorUiState>` 暴露状态，单次事件用 `Channel<EditorEffect>`。

---

## 9. 媒体管理

- **存储位置**：`context.filesDir/notes/<noteId>/img/<uuid>.<ext>`。
- **压缩策略**：长边 ≤ 1920px，WebP 85% 优先；动图保留为 GIF/WebP 动图。
- **EXIF**：默认剥离地理位置等隐私信息。
- **GC**：保存笔记时与 `NoteAssetEntity` 比对，未引用的资产移入 `.trash` 目录，定时清理。
- **来源**：`ActivityResultContracts.PickVisualMedia`（无需存储权限）+ `TakePicture` + 剪贴板 `ClipData`。
- **权限**：仅在使用相机时请求 `CAMERA`，不再请求 `READ_PHONE_STATE`、`WRITE_EXTERNAL_STORAGE`、`ACCESS_WIFI_STATE` 等历史多余权限。

---

## 10. 搜索

- **方案**：Room FTS4 虚拟表 `notes_fts(title, markdown)`。
- **建索**：插入/更新触发同步；后台 IO 调度。
- **能力**：标题前缀加权、正文 BM25 排序、关键词高亮（搜索结果摘要）。
- **未来**：可扩展为标签搜索、相似笔记推荐。

---

## 11. 性能预算

| 指标 | 预算 |
|---|---|
| 冷启动 | < 800ms（Pixel 6 / Android 13） |
| 列表 60fps | 卡片首图懒加载，缓存摘要 |
| 编辑输入延迟 | < 16ms（不在主线程做 Markdown 渲染） |
| 预览首屏 | < 200ms，超过 50KB 文档启用增量渲染 |
| APK 体积 | 增量后 < 8MB（移除 umeng 等 jar） |
| 内存峰值 | 编辑器场景 < 150MB |

---

## 12. 安全与隐私

- 移除友盟（umeng）等三方 SDK 与对应权限。
- 不写公共目录，所有用户内容位于应用沙盒；备份导出由用户主动选择目录（SAF）。
- Markdown 渲染禁用原始 HTML 注入。
- 链接点击二次确认（非 https 警告）。
- 备份文件可选 AES-256 加密（口令派生用 PBKDF2/Argon2）。

---

## 13. 测试策略

- **单元测试**：`MarkdownRenderer`、`NoteRepository`、`MediaImporter`、状态归约函数。
- **DAO 测试**：`Room` 内存数据库 + 迁移测试。
- **UI 测试**：Compose UI Test 覆盖编辑器关键路径（输入、插图、模式切换、保存）。
- **快照测试**：列表/详情/编辑器渲染快照（Paparazzi）。
- **目标覆盖率**：核心模块（`core-markdown`、`data-note`）≥ 80%。

---

## 14. 构建与发布

- 使用 `libs.versions.toml` 统一版本；`build-logic` 抽取 Convention Plugin。
- 打开 `R8` minify、resource shrink；`baselineProfiles` 提升启动速度。
- 不再使用本地 jar，全部走 Maven Central / Google Maven。
- CI（GitHub Actions）：Lint + Unit Test + Assemble Debug；Release 走 PR + Tag 触发。

---

## 15. 重构落地步骤

> 估算总工时 4 周（1 人全职），可并行可分组。

| 阶段 | 工时 | 产出 |
|---|---|---|
| **D0：脚手架** | 2d | 模块化目录、Version Catalog、build-logic、Hilt、单 Activity、主题骨架 |
| **D1：数据层** | 3d | Room 实体/DAO、Repository、FTS 搜索、迁移占位（首版无迁移） |
| **D2：Markdown 引擎** | 3d | Markwon 接入、自定义图片协议、纯文本摘要、分段增量渲染 |
| **D3：列表 + 详情** | 3d | LazyStaggeredGrid、SearchBar、卡片样式、详情阅读体验 |
| **D4：编辑器核心** | 5d | 三模式切换、工具栏、自动保存、撤销重做、滚动同步 |
| **D5：媒体** | 3d | PickVisualMedia / 相机、压缩、资产表、孤儿清理 |
| **D6：设置 + 备份** | 2d | DataStore、导入导出、回收站 |
| **D7：动效与抛光** | 3d | 容器变换、共享元素、空态、Snackbar、触觉反馈 |
| **D8：测试 + 性能** | 3d | 单测、UI 测、Baseline Profile、内存/帧率优化 |
| **D9：清理旧代码** | 1d | 删除 `RichEditor`、`RichEditView`、`RichTextView`、`HTML` 相关代码 |

---

## 16. 需删除/替换的旧资产

| 旧文件 | 处理方式 |
|---|---|
| `app/src/main/java/com/example/richtext/ui/widget/RichEditor.java` | 删除 |
| `app/src/main/java/com/example/richtext/ui/widget/RichEditView.java` | 删除 |
| `app/src/main/java/com/example/richtext/ui/widget/RichTextView.java` | 删除 |
| `app/src/main/java/com/example/richtext/ui/widget/RichImageView.java` | 删除（用 Coil + Compose Image 替代） |
| `app/src/main/java/com/example/richtext/ui/widget/handwriting/*` | 暂时下线（手写本期不做） |
| `app/src/main/java/com/example/richtext/utils/LongBlogContent.java` | 删除（导出能力重做为图片导出） |
| `app/src/main/res/layout/*.xml` | 全部删除 |
| `app/libs/*.jar`（commons-lang3 / photo-view / umeng） | 全部移除 |
| `AndroidManifest.xml` 中 `READ_PHONE_STATE` `ACCESS_WIFI_STATE` `WRITE_EXTERNAL_STORAGE` | 移除 |

---

## 17. 验收标准（DoD）

- [ ] 单 Activity + Compose + M3 主题，跟随系统深色 / 动态色。
- [ ] 笔记列表瀑布流，支持搜索、排序、置顶、删除（带撤销）。
- [ ] 编辑器三模式切换流畅；工具栏覆盖文档第 7.4.2 节所有功能。
- [ ] 自动保存防抖 500ms，断电/切后台不丢稿。
- [ ] 图片插入/删除自动管理资产，导出 zip 还原一致。
- [ ] FTS 搜索命中标题与正文，关键词高亮。
- [ ] 关键路径 UI 自动化测试通过。
- [ ] 移除全部旧 HTML 相关代码与本地 jar。
- [ ] 冷启动 < 800ms，编辑输入帧率 60fps。

---

## 18. 风险与应对

| 风险 | 影响 | 应对 |
|---|---|---|
| Compose 学习曲线 | 进度延期 | 前期投入 D0 阶段做 PoC；保持 Composable 简单纯净 |
| 大文档预览卡顿 | 体验差 | 增量渲染 + 节点缓存 + 后台预渲染 |
| 软键盘与底部工具栏冲突 | 输入区域被遮挡 | 使用 `WindowInsets.ime` + `imePadding` + `Scaffold` 适配 |
| 图片资产孤儿 | 占用空间 | 保存时同步引用清单，定时 GC |
| Markdown 方言差异 | 与桌面端不一致 | 固定 CommonMark + GFM 子集，文档化 |

---

## 19. 后续可选演进

- 云同步（WebDAV / 自托管 / Drive）
- 跨平台（KMP）共享 `core-markdown` 与 `data-note`
- AI 写作辅助（摘要、续写、润色），全部在端侧或可控后端
- 标签系统、双向链接（[[wiki link]]）
- 桌面端（Compose Multiplatform）

---

## 附录 A：依赖建议（`libs.versions.toml` 摘要）

```toml
[versions]
agp = "8.5.2"
kotlin = "1.9.24"
compose-bom = "2024.09.02"
hilt = "2.51.1"
room = "2.6.1"
coroutines = "1.8.1"
coil = "2.7.0"
markwon = "4.6.2"
datastore = "1.1.1"
nav-compose = "2.8.1"
serialization = "1.7.2"
timber = "5.0.1"

[libraries]
androidx-core-ktx = "androidx.core:core-ktx:1.13.1"
androidx-activity-compose = "androidx.activity:activity-compose:1.9.2"
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material3-wsc = { module = "androidx.compose.material3:material3-window-size-class" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
nav-compose = { module = "androidx.navigation:navigation-compose", version.ref = "nav-compose" }
hilt = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
markwon-core = { module = "io.noties.markwon:core", version.ref = "markwon" }
markwon-image-coil = { module = "io.noties.markwon:image-coil", version.ref = "markwon" }
markwon-tables = { module = "io.noties.markwon:ext-tables", version.ref = "markwon" }
markwon-tasklist = { module = "io.noties.markwon:ext-tasklist", version.ref = "markwon" }
markwon-strikethrough = { module = "io.noties.markwon:ext-strikethrough", version.ref = "markwon" }
markwon-editor = { module = "io.noties.markwon:editor", version.ref = "markwon" }
kotlinx-serialization = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }
```

---

## 附录 B：示例 - 编辑器 Composable 骨架

```kotlin
@Composable
fun NoteEditorScreen(
    state: EditorUiState,
    onIntent: (EditorIntent) -> Unit,
) {
    Scaffold(
        topBar = { EditorTopBar(state, onIntent) },
        bottomBar = {
            if (state.mode != EditorMode.Preview) {
                EditorToolbar(state.toolbarGroup, onIntent = onIntent)
            }
        },
        floatingActionButton = {
            if (state.mode == EditorMode.Preview) {
                FloatingActionButton(onClick = { onIntent(EditorIntent.ModeSwitched(EditorMode.Edit)) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                }
            }
        }
    ) { padding ->
        when (state.mode) {
            EditorMode.Edit -> EditorPane(state, onIntent, Modifier.padding(padding))
            EditorMode.Preview -> PreviewPane(state.draftMarkdown.text, Modifier.padding(padding))
            EditorMode.Split -> Row(Modifier.padding(padding)) {
                EditorPane(state, onIntent, Modifier.weight(1f))
                VerticalDivider()
                PreviewPane(state.draftMarkdown.text, Modifier.weight(1f))
            }
        }
    }
}
```

---

> 文档结束。任何技术决策若与本文档冲突，以本文档为准；如需调整，请通过 PR 修改本文件并附决策记录。
