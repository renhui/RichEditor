# RichNote 重构开发 Playbook

> 子标题：AI 协同开发执行手册 + 个人学习路线
> 关联文档：[`../REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md)
> 版本：v1.0 · 2026-04-27

若你选择 **D0–D9 全部由 AI 实现代码、人只做验收与合入**，请改用 [`dev/ROADMAP_D0_D9.md`](dev/ROADMAP_D0_D9.md) 与 [`dev/AI_HUMAN_HANDOFF.md`](dev/AI_HUMAN_HANDOFF.md)，与本 Playbook（强调你手敲 C 档）**二选一**即可。

---

## 0. 协作总原则

### 0.1 三档分工

| 档位 | 内容范围 | 由谁写 | 你需要做什么 |
|---|---|---|---|
| 🟦 A 档（AI 全包） | 脚手架、样板代码、重复模板、Gradle 配置、数据建表、基础 UI、纯展示 Composable | **AI 直接生成** | 看懂 + 跑通 + 复述结构 |
| 🟨 B 档（AI 草稿 + 你改造） | 业务流程（图片导入、搜索、自动保存）、复杂 Composable、复杂 UseCase | **AI 出初稿，你改边界 / 异常 / 逻辑** | 改 ≥ 30% 代码，写 changelog 注释 |
| 🟥 C 档（你必须亲手） | MVI 状态机、编辑器核心、Flow 组合、撤销重做、滚动同步、性能调优 | **手敲 / 仿写**（可看 AI 思路，但代码自己写） | 完整能讲清楚 + 不看就能写 |

**铁律**：
- 🟥 C 档代码若是 AI 直接生成的，**不算完成**，必须重写一次。
- 🟨 B 档若你没改任何东西，回去重做（说明你没真懂）。
- 🟦 A 档生成完要能口头讲清"为什么这样写"，否则也不算过。

### 0.2 学习闭环

每个阶段（D0-D9）都按这 4 步推进：

```
学（看官方文档/视频 30-90min） → 让 AI 生成 A/B 档代码 →
手写 C 档代码 → 复盘（写学习笔记 + 提交 PR）
```

### 0.3 推荐节奏

- 单阶段时长：**0.5-1.5 个工作日**（含学习时间）。
- 全程：**4-6 周**（按每周 15-20 小时投入估算，含学习）。
- 每阶段必须产出：1 个可运行的 commit + 1 篇学习笔记（≥ 200 字）。

### 0.4 文件夹建议

```
doc/
├── DEV_PLAYBOOK.md           # 本文件
├── notes/                    # 你的学习笔记
│   ├── D0-scaffolding.md
│   ├── D1-room-flow.md
│   └── ...
└── prompts/                  # 复用过的 AI Prompt 沉淀
    └── *.md
```

---

## 1. 学习路线总图

```
D0  脚手架       ─►  Kotlin / Gradle / Hilt 入门
D1  数据层       ─►  Room + Flow（必杀技）
D2  Markdown    ─►  自定义协议 + 渲染管线
D3  列表+详情    ─►  Compose 基础 + Material 3
D4  编辑器核心   ─►  ★MVI + 状态机 + Compose 进阶（最重要）
D5  媒体         ─►  ActivityResult API + 图片压缩
D6  设置+备份    ─►  DataStore + SAF
D7  动效         ─►  Compose 动画 + 共享元素
D8  测试+性能    ─►  Profiler + Baseline Profile
D9  收尾清理     ─►  代码 Review + 文档同步
```

🟥 **C 档密度最高的阶段**：D1（Flow 组合）、D4（编辑器/MVI）、D8（性能）。
🟦 **A 档可放心交付的阶段**：D0、D6、D9。
🟨 **B 档为主**：D2、D3、D5、D7。

---

## 2. 各阶段详细拆解

> 每阶段格式：`目标` → `学习清单` → `任务表` → `AI Prompt 模板` → `自学产出`

---

### D0 · 脚手架（约 0.5 - 1 天）

#### 2.0.1 阶段目标
拉起一个空项目骨架：单 Activity、Compose、Material 3 主题、Hilt、Version Catalog、模块化空目录。

#### 2.0.2 学习清单
- Kotlin 基础（如果不熟）：高阶函数、扩展函数、`data class`、`sealed`、委托。
- Gradle Version Catalog 是什么、为什么要用。
- Hilt 注解 4 件套：`@HiltAndroidApp`、`@AndroidEntryPoint`、`@Inject`、`@HiltViewModel`。

#### 2.0.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| 创建多模块目录结构（`core-*` `feature-*` `data-note`） | 🟦 A | AI | `settings.gradle.kts` 列出全部模块，能 sync |
| `libs.versions.toml` 全量依赖版本 | 🟦 A | AI | 至少包含 Compose BOM、Room、Hilt、Coil、Markwon |
| `build-logic` Convention Plugin（统一 Kotlin/Compose 配置） | 🟦 A | AI | 各模块 `build.gradle.kts` 不超过 30 行 |
| `MainActivity` + `RichNoteApplication`（Hilt 入口） | 🟦 A | AI | 启动空白 Compose 屏幕成功 |
| Material 3 主题骨架（Light/Dark + 动态色） | 🟦 A | AI | Android 12+ 自动跟动态色 |
| Navigation Compose 路由占位（5 个空屏） | 🟦 A | AI | 能跳转 |
| **手动跑通整个工程，写 D0 学习笔记** | 🟥 C | **你** | 笔记里能解释每个模块为什么存在 |

#### 2.0.4 AI Prompt 模板

> 给 AI 用：
> "用 AGP 8.5 + Kotlin 1.9 + Compose BOM 2024.09 + Hilt 2.51，按以下模块结构生成空骨架：[贴 REFACTOR_DESIGN.md 第 4 节]。要求：使用 Version Catalog（`libs.versions.toml`）；提取 `build-logic` Convention Plugin；`MainActivity` 用 `setContent { RichNoteTheme { AppNavGraph() } }`；给我每个文件的完整内容。"

#### 2.0.5 自学产出（你必须能回答）
- 为什么要拆 `core` / `feature` / `data` 三层？依赖方向是怎样的？
- `build-logic` 解决什么问题？没有它会怎样？
- Hilt 的依赖图什么时候构建？为什么要 `@HiltAndroidApp`？

---

### D1 · 数据层（约 1 - 1.5 天）🟥 重点

#### 2.1.1 阶段目标
跑通 `notes` + `note_assets` 两张表 + FTS 搜索；`NoteRepository` 暴露 `Flow<List<NoteSummary>>`。

#### 2.1.2 学习清单（**必学**）
- Room：`@Entity` `@Dao` `@Database` `@Query` `@Transaction`、Flow 返回值。
- **Kotlin Flow（重点）**：`map` / `filter` / `combine` / `flatMapLatest` / `distinctUntilChanged` / `flowOn`。
- 协程作用域：`viewModelScope`、`Dispatchers.IO/Default`。
- 单测：`runTest`、`Turbine`。

#### 2.1.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| `NoteEntity` / `NoteAssetEntity` 定义 | 🟦 A | AI | 字段对齐 REFACTOR_DESIGN §5.1 |
| `NoteDao` / `NoteAssetDao`（基础 CRUD + Flow） | 🟦 A | AI | 至少 8 个方法，全 Flow 化 |
| FTS4 虚拟表 + 触发器同步 | 🟨 B | AI 出稿，你改 | 中文分词降级方案；空查询返回全部 |
| `NoteRepository` 接口 | 🟦 A | AI | 接口形如 §5.2 |
| **`NoteRepository` 实现（含 Flow 组合：列表 + 搜索 + 排序）** | 🟥 C | **你手写** | 用 `combine(query, sort) { ... flatMapLatest { dao.observe(...) } }` |
| Room 单元测试（内存库） | 🟨 B | AI 出稿，你补边界用例 | 至少 5 个用例，覆盖空字符串、SQL 注入、长文本 |
| **手写一篇 Flow 操作符笔记**（map/combine/flatMapLatest 各一个例子） | 🟥 C | **你** | 笔记 ≥ 300 字 |

#### 2.1.4 AI Prompt 模板

> "基于 Room 2.6 + Kotlin Flow，生成 `NoteEntity` 和 `NoteDao`，要求 [贴实体定义]。`observeAll(query, sort)` 不要直接实现搜索逻辑，留给 Repository 用 Flow 组合。"

> ⚠️ **不要让 AI 写 Repository 实现**。让它给你伪代码或注释提示即可，代码你自己敲。

#### 2.1.5 自学产出（你必须能讲清）
- `Flow.map` 和 `Flow.flatMapLatest` 在数据库订阅场景下的区别，错用会怎样？
- 为什么 `combine` 比 `zip` 更适合"搜索关键词 + 排序方式"组合？
- Room 的 `@Query` 返回 `Flow<List<T>>` 时，谁负责重发数据？

---

### D2 · Markdown 引擎（约 1 天）

#### 2.2.1 阶段目标
封装 `MarkdownRenderer`：能把 Markdown 渲染到 Compose、提取摘要、识别 `note://asset/<id>` 协议。

#### 2.2.2 学习清单
- Markwon 的核心 API：`Markwon.builder()`、`AbstractMarkwonPlugin`、`SchemeHandler`。
- CommonMark + GFM 子集语法。
- `AndroidView` 在 Compose 里嵌入传统 View。

#### 2.2.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| Markwon 初始化 + 表格/任务列表/删除线插件 | 🟦 A | AI | 能跑 demo |
| `note://asset/<id>` `SchemeHandler`，映射到本地文件 | 🟨 B | AI 出稿，你改 | 处理找不到资产、磁盘失效情况 |
| `extractPlainSummary(maxChars)`：去 Markdown 语法 | 🟨 B | AI 出稿 | 表格、代码块、图片正确处理；写 5 个单测 |
| Compose `MarkdownText(markdown)` 包装（用 `AndroidView`） | 🟨 B | AI 出稿 | 主题色跟随 M3、字号可配置 |
| **增量预览：按段落 diff 重渲染** | 🟥 C | **你手写** | 文档 50KB 输入延迟 < 16ms |
| 单元测试：摘要提取、首图提取 | 🟨 B | AI 出稿 | ≥ 8 个用例 |

#### 2.2.4 AI Prompt 模板

> "用 Markwon 4.6 写一个 `note://asset/<id>` 的 SchemeHandler，根据 ID 查询 `NoteAssetDao` 拿到本地路径，用 Coil 加载到 `ImageView`。返回错误时用占位图。"

#### 2.2.5 自学产出
- 为什么不直接用 `Compose Material Markdown`（如果存在）？Markwon 的扩展点优势在哪？
- 50KB Markdown 文档每次全量渲染为什么会卡？解决思路是什么？

---

### D3 · 列表 + 详情（约 1.5 天）

#### 2.3.1 阶段目标
笔记列表（瀑布流 + 搜索 + FAB）和详情阅读页全部跑通。

#### 2.3.2 学习清单
- Compose 基础：`Modifier`、`remember` / `rememberSaveable`、`LazyColumn` / `LazyVerticalStaggeredGrid`。
- Material 3 组件：`Scaffold`、`LargeTopAppBar`、`SearchBar`、`Card`、`Chip`、`ExtendedFloatingActionButton`。
- `collectAsStateWithLifecycle`、`viewModel()` 在 Compose 中的用法。

#### 2.3.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| `NoteListScreen` 静态布局（卡片 + 顶栏 + FAB） | 🟦 A | AI | 视觉对齐 §7.3 |
| `NoteListViewModel` 接 Repository | 🟨 B | AI 出稿，你接 Flow | 用 `stateIn(SharingStarted.WhileSubscribed(5000))` |
| `SearchBar` + 搜索状态 | 🟨 B | AI 出稿，你改防抖 | 输入 300ms 防抖再发起查询 |
| 卡片摘要 / 首图 / 时间格式化 | 🟦 A | AI | — |
| **手势：左滑置顶、右滑删除（含 Snackbar 撤销）** | 🟥 C | **你手写** | `SwipeToDismissBox` + 撤销 5s 计时 |
| `NoteDetailScreen`（只读、目录抽屉） | 🟨 B | AI 出稿 | 滚动隐藏顶栏 |
| 空态 / 错误态 | 🟦 A | AI | 有插画占位符 |
| Compose UI 测试（点击 FAB → 跳新建） | 🟨 B | AI 出稿 | 至少 3 个用例 |

#### 2.3.4 AI Prompt 模板

> "用 Compose Material 3 + Navigation Compose 实现 `NoteListScreen`：状态来自 `NoteListUiState`，包含 `LargeTopAppBar`（标题"便签"，右侧搜索/排序图标）、`LazyVerticalStaggeredGrid` 双列卡片、`ExtendedFloatingActionButton`。卡片内容：标题 2 行、摘要 4 行、时间、首图缩略图（可空）。提供深色 Preview。"

#### 2.3.5 自学产出
- `collectAsStateWithLifecycle` 比 `collectAsState` 多了什么？为什么推荐前者？
- `LazyColumn` 滑动卡顿，可能的 5 个原因和定位方法？

---

### D4 · 编辑器核心（约 3 - 5 天）🟥🟥🟥 全程重点

#### 2.4.1 阶段目标
完整跑通：三模式切换、防抖自动保存、撤销重做、工具栏、滚动同步。**这是整个项目的核心，AI 只能给思路，代码必须你自己写。**

#### 2.4.2 学习清单（务必扎实）
- **MVI 模型**：`State / Intent / Effect / Reducer`。
- **Compose 状态进阶**：`derivedStateOf`、`snapshotFlow`、`LaunchedEffect`、`DisposableEffect`、`SideEffect`。
- `TextFieldValue` vs `String` 的区别（光标、选区、组合输入）。
- `WindowInsets.ime` + `imePadding()` 软键盘适配。
- 防抖：`Flow.debounce` / 自定义 `MutableSharedFlow`。

#### 2.4.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| `EditorUiState` / `EditorIntent` / `EditorEffect` 定义 | 🟥 C | **你手写** | 不参考 AI；交付后再让 AI Review |
| **`EditorViewModel` 状态归约（reducer）** | 🟥 C | **你手写** | 每个 Intent 单独函数；不可有 if 嵌套 > 2 层 |
| **防抖自动保存（debounce 500ms）** | 🟥 C | **你手写** | 用 `snapshotFlow { state.draftMarkdown.text }.debounce(500).onEach { save() }` |
| `EditorScreen` 三模式骨架 | 🟨 B | AI 出稿 | 你接状态 |
| `EditorPane`（含工具栏挂载点） | 🟨 B | AI 出稿，你接 IME | 软键盘弹出工具栏不被遮挡 |
| `PreviewPane` | 🟦 A | AI | 直接挂 D2 的渲染器 |
| **撤销/重做栈（200 步）** | 🟥 C | **你手写** | 数据结构选定后写测试先行 |
| **滚动同步（Edit ↔ Preview，按行号映射）** | 🟥 C | **你手写** | 容差 ±2 行；分屏模式默认开启 |
| 工具栏按钮：H1/H2/B/I/列表/任务/链接/图片 | 🟨 B | AI 出稿格式逻辑，你接 Intent | 选区有选中时包裹，光标位置时插入模板 |
| 智能换行续 `- ` `1. ` `> ` `- [ ] ` | 🟨 B | AI 出稿 | 写 6 个边界用例：行尾、行中、空行、嵌套 |
| **大文档（50KB+）增量预览接入** | 🟥 C | **你** | 编辑帧率 ≥ 60fps |
| Compose UI 测试：插入图片、撤销、模式切换 | 🟨 B | AI 出稿 | ≥ 5 个用例 |
| **D4 学习笔记：手画一张 MVI 时序图** | 🟥 C | **你** | 必须包含 IME → Intent → State → UI 全链路 |

#### 2.4.4 AI Prompt 模板（仅用于 🟨/🟦 部分）

> "我已经定义了 `EditorIntent.FormatApplied(action)`，请帮我生成一个纯函数 `applyFormat(text: TextFieldValue, action: EditorAction): TextFieldValue`，处理这些 action：[列出 8 个]。要求纯函数、可单测，不要碰 ViewModel。"

> ⚠️ 让 AI 写工具函数，**不要让它直接给 ViewModel 全部代码**。

#### 2.4.5 自学产出（必须能讲）
- 为什么用 `TextFieldValue` 而不是 `String`？光标怎么传递？
- `snapshotFlow` 是什么？它和 `Flow` 的本质区别？
- 你的撤销重做用什么数据结构？时间复杂度？大文档时怎么省内存？
- 软键盘挡住工具栏的根本原因是什么？`WindowInsets` 在 Compose 中怎么消费？

---

### D5 · 媒体管理（约 1.5 天）

#### 2.5.1 阶段目标
图片插入全链路：选图 → 复制到沙盒 → 压缩 → 写资产表 → 插入 Markdown → 渲染。

#### 2.5.2 学习清单
- `ActivityResultContracts.PickVisualMedia`（无需权限）。
- `MediaStore` 与分区存储变化。
- Bitmap 压缩：`BitmapFactory.Options`、采样、WebP 编码。
- EXIF 处理：`ExifInterface`。

#### 2.5.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| `MediaImporter`：URI → File copy + 压缩 | 🟨 B | AI 出稿，你改异常 | OOM 防护 + 大图采样 |
| `PickVisualMedia` 接入 + 相机 `TakePicture` | 🟦 A | AI | 无 `WRITE_EXTERNAL_STORAGE` |
| EXIF 隐私剥离 | 🟦 A | AI | 至少剥地理位置 |
| 资产 GC（孤儿清理） | 🟨 B | AI 出稿，你改策略 | 启动后台扫描、删除前归档 `.trash` |
| **编辑器 → 媒体 → ViewModel 闭环（Effect 触发选图）** | 🟥 C | **你** | 用 `Channel<EditorEffect>` 单向 |
| 单测 + 集成测试 | 🟨 B | AI 出稿 | 覆盖压缩失败、URI 失效 |

#### 2.5.4 自学产出
- `PickVisualMedia` 为什么不需要 `READ_MEDIA_IMAGES` 权限？
- 沙盒 vs 公共目录在分区存储下的区别？

---

### D6 · 设置 + 备份（约 1 天）

#### 2.6.1 阶段目标
DataStore 偏好持久化、SAF 导入导出、回收站。

#### 2.6.2 学习清单
- DataStore Preferences API。
- Storage Access Framework：`OpenDocument`、`CreateDocument`、`OpenDocumentTree`。
- 文件 zip 打包：`ZipOutputStream`。

#### 2.6.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| `SettingsRepository`（DataStore） | 🟦 A | AI | 主题/字号/自动保存 三个键 |
| `SettingsScreen` UI | 🟦 A | AI | 对齐 §7.6 |
| 导出单文件 `.md` | 🟦 A | AI | 协议重写：`note://asset/...` → `./img/...` |
| 导出 zip 包 | 🟨 B | AI 出稿 | 包含图片 |
| 导入 zip 包 | 🟨 B | AI 出稿 | 路径校验、id 冲突处理 |
| 回收站列表 + 还原 + 彻底删除 | 🟦 A | AI | — |

#### 2.6.4 自学产出
- DataStore 比 SharedPreferences 好在哪？什么时候不要用 DataStore？
- SAF 在不同 Android 版本上的行为差异？

---

### D7 · 动效抛光（约 1 - 1.5 天）

#### 2.7.1 阶段目标
列表 → 详情容器变换、保存状态指示、空态、触觉反馈。

#### 2.7.2 学习清单
- Compose Animation：`animate*AsState`、`AnimatedVisibility`、`AnimatedContent`。
- `SharedTransitionLayout`（Compose 1.7+）。
- `HapticFeedback`、`Snackbar`。

#### 2.7.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| 列表卡片 → 详情 容器变换 | 🟨 B | AI 出稿，你调时长曲线 | 无闪烁，转场流畅 |
| 顶栏保存状态指示器（圆点动画） | 🟦 A | AI | — |
| 空态插画 + 入场动画 | 🟦 A | AI | — |
| 删除 Snackbar 撤销 | 🟦 A | AI | — |
| 触觉反馈接入（长按、置顶、删除） | 🟦 A | AI | — |

#### 2.7.4 自学产出
- 容器变换的核心是什么？没有它怎么用 `LookaheadLayout` 手写？

---

### D8 · 测试 + 性能（约 1.5 天）🟥 重点

#### 2.8.1 阶段目标
关键链路有自动化测试；冷启动 < 800ms；编辑帧率 60fps。

#### 2.8.2 学习清单
- Compose UI Test：`createComposeRule`、`onNodeWithText`、`assertIsDisplayed`。
- **Recomposition 优化**：`@Stable` `@Immutable`、`derivedStateOf`、`key()`。
- Baseline Profiles。
- Profiler：CPU、Memory、Frame Profiler。

#### 2.8.3 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| 列表 / 详情 / 编辑器 UI 测试 | 🟨 B | AI 出稿 | 关键路径覆盖 |
| Markdown 渲染快照测试（Paparazzi） | 🟨 B | AI 出稿 | 8 个常见语法 |
| Baseline Profile 生成 | 🟦 A | AI | 集成到 release |
| **Recomposition 排查（Layout Inspector）** | 🟥 C | **你** | 找出 ≥ 3 处不必要重组并修复 |
| **冷启动测量与优化** | 🟥 C | **你** | 写 D8 笔记记录优化前后数据 |

#### 2.8.4 自学产出
- 一个 Composable 重组次数过多，排查 5 步法？
- `@Stable` 和 `@Immutable` 的区别？乱标会怎样？
- Baseline Profile 工作原理？

---

### D9 · 收尾清理（约 0.5 天）

#### 2.9.1 阶段目标
删除旧代码、补 README、CI 配置。

#### 2.9.2 任务表

| 任务 | 档位 | 谁来写 | 验收点 |
|---|---|---|---|
| 删除 `RichEditor.java` / `RichEditView.java` / `RichTextView.java` 等旧文件 | 🟦 A | AI | 见 §16 清单 |
| 移除本地 jar（umeng/photo-view/commons-lang3） | 🟦 A | AI | — |
| 清理 `AndroidManifest.xml` 多余权限 | 🟦 A | AI | 只保留 `CAMERA` |
| 更新 `README.md` | 🟦 A | AI | 同步新功能列表 |
| GitHub Actions CI（Lint + Unit Test） | 🟦 A | AI | PR 触发 |
| **写一篇总复盘**（这次重构学到什么） | 🟥 C | **你** | ≥ 800 字，沉淀到 `doc/notes/retrospective.md` |

---

## 3. 跨阶段的"你必须亲手做"清单（汇总）

按优先级排列，这些是 🟥 C 档的精华，**全部要自己写**：

1. `NoteRepository` 实现（D1）—— 练 Flow 组合
2. Flow 操作符笔记（D1）
3. `EditorUiState` / Reducer / ViewModel（D4）
4. 防抖自动保存（D4）
5. 撤销/重做栈（D4）
6. 滚动同步算法（D4）
7. 增量预览渲染（D4）
8. 编辑器 ↔ 媒体 Effect 闭环（D5）
9. Recomposition 优化（D8）
10. 冷启动测量与优化（D8）
11. 每阶段一篇学习笔记（D0-D9）
12. 总复盘笔记（D9）

> 这 12 项做完，你对 Compose / Flow / MVI / 性能优化 4 个核心维度都能讲到面试官点头。

---

## 4. AI 协作的避坑清单

| 坑 | 现象 | 应对 |
|---|---|---|
| AI 自作主张换技术栈 | 让你用 LiveData 而不是 Flow | 在 Prompt 里硬性要求 "禁止使用 LiveData / RxJava / AsyncTask" |
| AI 给的 Compose 代码用 Material 2 | `androidx.compose.material.*` | Prompt 加 "只用 androidx.compose.material3" |
| AI 把状态写成 `mutableStateOf` 直接放 ViewModel | 不可测试 | Prompt 要求 "ViewModel 只暴露 `StateFlow<UiState>`" |
| AI 不写主题色，硬编码颜色 | 视觉割裂 | Prompt 加 "所有颜色来自 MaterialTheme.colorScheme" |
| AI 在 Composable 里写 IO | 卡顿 | Prompt 加 "Composable 中禁止任何 IO，所有副作用走 LaunchedEffect" |
| AI 给的代码缺导入 | 编译失败 | 让 AI "列出 imports" 部分 |
| AI 编造不存在的 API | 找不到方法 | 让 AI 给版本号 + 官方文档链接交叉验证 |

---

## 5. 学习笔记模板

每阶段笔记写在 `doc/notes/D{N}-{topic}.md`，建议按这个结构：

```markdown
# D{N} · {主题}

## 1. 这一阶段我学到了什么
- 概念 1：...
- 概念 2：...

## 2. 哪些代码 AI 写的，哪些我写的
- 🟦 A：...
- 🟨 B（我改了什么）：...
- 🟥 C（我自己写的）：...

## 3. 踩坑记录
- 坑 1 + 解决方案

## 4. 我还没搞懂的问题
- 问题 1（下次去查/问）

## 5. 一句话总结
（用一句话告诉自己这阶段最值钱的收获）
```

---

## 6. Definition of Done（每阶段交付前自检）

- [ ] 代码 commit 信息清晰，遵循 `feat:` / `fix:` / `refactor:` 前缀
- [ ] 🟥 C 档代码不依赖 AI 直接生成（提交记录里能体现是手写）
- [ ] 🟨 B 档代码至少改过 30%，关键改动有注释
- [ ] 🟦 A 档代码看完能 5 分钟内复述结构
- [ ] 写了 D{N} 学习笔记
- [ ] 工程能编译、关键路径能跑通

---

## 7. 推荐学习资源（按需精读，别囫囵）

- **Kotlin 协程 + Flow**：官方 [`kotlinx.coroutines/docs`](https://kotlinlang.org/docs/coroutines-overview.html)
- **Compose 官方 Codelab**：`Compose Basics` → `State` → `Side-effects` → `Performance`
- **Compose 性能**：Google I/O `Compose Performance` 演讲，Recomposition 篇
- **Material 3**：`m3.material.io` 设计 + Compose 实现样例
- **Room + Flow**：Android Developers 官方 `Room with a view` Codelab
- **Markwon**：GitHub `noties/Markwon` README 与示例工程
- **MVI**：尝试读 `cashapp/molecule` 与官方 `Architecture Samples` 中 `mvi` 分支

---

## 8. 备注

- 这份 Playbook 与 [`REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md) 是兄弟文档：前者偏"怎么做"和"谁来做"，后者偏"做成什么样"。
- 任何阶段你发现 AI 出的代码和 `REFACTOR_DESIGN.md` 冲突，**以 REFACTOR_DESIGN.md 为准**。
- 你随时可以根据进度更新本文件（增加坑、修正分工档位）。

---

> 文档结束。下一步：进入 **D0 阶段**，开始让 AI 生成脚手架，但 **D0 笔记必须你自己写**。
