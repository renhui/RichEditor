# D0–D9 全 AI 实施路线

> **目标**：与 [`../REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md) 一致，将项目改造为 **Markdown 便签的编辑 + 预览** 工具；**实现工作默认全部由 AI 完成**（含原 `DEV_PLAYBOOK` 中标记为“人写”的 C 档）。  
> **人** 的参与见 [`AI_HUMAN_HANDOFF.md`](AI_HUMAN_HANDOFF.md)。  
> **提示词** 见 [`PROMPTS_INDEX.md`](PROMPTS_INDEX.md)。

---

## 总览表

| 阶段 | 主题 | AI 交付物（摘要） | 人验收（必须） |
|:---:|---|---|---|
| D0 | 脚手架 | 多模块、Version Catalog、Hilt、Compose 主题、Nav 占位 | Sync 成功、能进空壳主屏 |
| D1 | 数据层 | Room 双表、FTS、Repository+Flow、Hilt 提供 | 单测通过、插查改查流 |
| D2 | Markdown | Markwon、图片协议、摘要、Compose 预览 | 长文+图片能渲 |
| D3 | 列表+详情 | 列表/搜索/详情/导航 | 真机滑动、搜索、进入编辑 |
| D4 | 编辑器 | MVI、三模式、防抖保存、撤销、工具栏 | 编辑预览闭环、不退稿 |
| D5 | 媒体 | 选图/相机、压缩、资产表、`note://` | 插图可回显、重开可编 |
| D6 | 设置+备份 | DataStore、导入导出、回收站 | 导出的 zip 能再导入 |
| D7 | 动效 | 转场、保存态、Snack 撤销 | 无闪退、动效可关（如有） |
| D8 | 测试+性能 | 单测/UI 测、Baseline、文档化性能 | CI/本地 green；冷启有数 |
| D9 | 收尾 | 删旧富文本、清依赖、Manifest、README | release 可编、体积合规 |

---

## D0 · 脚手架

### AI 产出

- `settings.gradle.kts`：`include` 全部模块  
- `gradle/libs.versions.toml`：`compose`、`hilt`、`room`、`coil`、`navigation`、`serialization`（若设计文档含）  
- `build-logic/`：Convention Plugin，`kotlin`、`android`、`compose`、`hilt`  
- `:app`：`RichNoteApplication` `@HiltAndroidApp`，`MainActivity` `@AndroidEntryPoint`，`setContent { Theme { NavHost(...) } }`  
- `:core-*` `:feature-*` `:data-note`：**最小空模块**（含 `build.gradle.kts`，避免循环依赖）  
- **Navigation**：占位路由：`note_list`、`note_detail`、`note_editor`、`settings`、`recycle_bin`  

### AI 自检

- `./gradlew :app:assembleDebug`（路径以最终实现为准）
- App 启动无崩溃；能点进占位页再返回  

### 人验收

- Android Studio Gradle Sync  
- Debug 安装跑一次  

---

## D1 · 数据层

### AI 产出（对齐 REFACTOR_DESIGN §5、§10）

- `@Entity`：`Note`、`NoteAsset`  
- `@Database`、`DAO`：**Flow** 返回 observable 查询  
- FTS：`notes_fts`（或等价方案）与同步策略（触发器/`@RawQuery`/`UPDATE` hook）  
- **`NoteRepository` + 实现**：含搜索关键词、`sort`、`observeAll`、`observeById`、`insert`、`update`、`softDelete`  
- Repository：Flow **`combine`** / **`flatMapLatest`** 切换查询条件  
- **单元测试**：内存数据库 + Turbine/`runTest`  

### 人验收

- Data 模块测试任务全部 green  
- 如无 CI，本地至少跑一次 `./gradlew testDebugUnitTest`  

---

## D2 · Markdown 渲染管线

### AI 产出（对齐 REFACTOR_DESIGN §6）

- `MarkdownRenderer` 接口与 Markwon 实现  
- GFM 子集：**表格、任务列表、删除线** 等按需插件  
- **`note://asset/<id>`**：解析到本地路径，失败占位图  
- `extractPlainSummary`、**首图** 用于列表卡片  
- Compose 包装：只读预览可复用（`AndroidView` 等）  
- 单测：摘要/空/仅图/表格  

### 人验收

- 粘贴 README 级别 Markdown + 插图路径（mock），预览正确  

---

## D3 · 列表与详情 UI

### AI 产出（对齐 REFACTOR_DESIGN §7）

- `NoteListScreen`：**LazyVerticalStaggeredGrid**（或单列降级）、Large Top Bar、搜索、FAB  
- `NoteListViewModel`：**`SharedWhileSubscribed`**/`stateIn`，接 Repository  
- **防抖搜索**（如 300ms）：可在 VM 层 `debounce`  
- `NoteDetailScreen`：**只读** Markdown，`RichNoteTheme`  
- Navigation：`list → detail`、`FAB → editor(new)`、`detail → edit`  

### 人验收

- 滚动流畅；夜间模式；搜标题/正文  

---

## D4 · 编辑器核心（全文由 AI 实现）

### AI 产出（对齐 REFACTOR_DESIGN §8 + §7.4）

以下内容 **全部由 AI 编写**，不再区分“手写 C 档”：

| 类别 | 内容 |
|---|---|
| 状态模型 | `EditorUiState`、`EditorIntent`、`EditorEffect`、`EditorMode`、`SaveState` |
| ViewModel | reducer / `when(intent)`，`StateFlow`，**防抖保存**、`snapshotFlow`/`debounce` |
| UI | Edit / Preview / Split；`TextFieldValue` |
| 交互 | **撤销/重做**（如 200 步，可配置） |
| 工具栏 | 标题/粗体/列表/任务/链接/图片入口（调 D5） |
| 性能 | 大文档**增量预览**或分块渲染、**避免**在 UI 线程阻塞 Markwon |

### 可选 AI 附带

- `EditorEffectChannel`：**单次事件** → Activity 发起 `PickVisualMedia`

### AI 自检

- `EditorReducerTest`：**纯函数**路径覆盖  
- Compose UI Test：**模式切换**、**模拟保存**

### 人验收

- 输入法中文、光标、后退栈  
- 杀进程（若启用自动保存）后不丢主要内容（取决于是否落地持久草稿）  

---

## D5 · 媒体与资产闭环

### AI 产出（对齐 REFACTOR_DESIGN §9）

- **`ActivityResultContracts`**：`PickVisualMedia`、`TakePicture`  
- **`MediaImporter`**：`Uri → File`（`filesDir/notes/<noteId>/...`）、**WebP/压缩**、EXIF 脱敏（若可行）  
- **`NoteAsset` 表** 增删改、**引用** 与 Markdown 一致性（或定期 GC）  
- 在光标处插入：`![...](note://asset/...)`  

### 人验收

- 相册/相机各走一遍；卸载重装前**勿**依赖 `content://` 永存  

---

## D6 · 设置与导入导出

### AI 产出

- **DataStore**：主题/字号/自动保存等  
- **SettingsScreen**  
- **导出**：`.md`、`.zip`（`note://` → 相对路径）  
- **导入**：zip 解压、冲突策略（覆盖/新 id）  
- **回收站**：列表、恢复、彻底删除  

### 人验收

- 导出 zip 拷到另一模拟器/路径可导入  

---

## D7 · 动效与体验

### AI 产出

- 列表 → 详情 **共享元素/容器过渡**（按 Compose 版本选用稳定 API）  
- 顶栏 **保存中/已保存** 指示  
- 删除 **Snackbar 撤销**  

### 人验收

- 低端机是否掉帧；若有开关则关后无黑屏  

---

## D8 · 测试与性能

### AI 产出

- 覆盖 D1 Repository、D2 摘要、D4 reducer、D5 importer 的**单元测试**  
- **关键路径** Compose UI Test（列表→编辑→保存）  
- **Baseline Profile**（`profileInstaller` + 生成说明）  
- `doc` 或 `README` 片段：**如何** 量冷启动、输入延迟（Profiler 步骤）  

### 人验收

- 跑全量测试；Profiler 抽样一次，数值记入 README（可选）  

---

## D9 · 收尾与删除旧实现

### AI 产出（对齐 REFACTOR_DESIGN §16）

- 删除：`RichEditor`、`RichEditView`、`RichTextView`、`RichImageView`、手写板等旧链路  
- 移除：`libs` 下不再需要 jar、`Manifest` **精简权限**  
- **README**：新项目启动步骤、功能列表、与设计文档一致  

### 人验收

- `./gradlew assembleRelease`（若启用 shrink）  
- 安装包体积与权限列表符合预期  

---

## 依赖顺序（勿并行打乱）

```
D0 → D1 → D2 → D3 → D4 ⇄ D5（D5 可与 D4 后半并行，但须在编辑器接插图前完成 Importer）
→ D6 → D7 → D8 → D9
```

---

## 与设计文档冲突时

一律以 **[`REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md)** 为准；本路线只做「谁实现、什么时候交付」的补充。
