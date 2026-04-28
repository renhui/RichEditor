# D0–D9 提示词索引（喂给 AI 用）

> 使用方式：新开对话，先贴 **约束块**（见下），再贴对应 **D{n} 任务段**，最后附上仓库内 [`REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md) 的路径或核心章节（§3 技术栈、§4 工程结构、§5 数据、§6 Markdown、§7 UI）。

## 通用约束块（建议每次放在最前）

```
你是 Android 开发助手。请严格按项目文档 REFACTOR_DESIGN.md 实现，使用 Kotlin、Jetpack Compose、Material 3、Hilt、Room、Navigation Compose、Coroutines/Flow、Coil、Markwon。
禁止：引入未在 REFACTOR_DESIGN 中声明的无关 SDK；在 Composable 里做阻塞 IO；使用已废弃的 AsyncTask。
交付：给出需要新增/修改的文件路径列表 + 完整可编译的代码块；说明如何本地验证（gradle 任务与点击路径）。
```

---

## D0 — 脚手架

```
任务：D0 脚手架。按 REFACTOR_DESIGN §4 创建多模块结构（app、core-*、feature-*、data-note、build-logic）、
Version Catalog libs.versions.toml、Convention Plugin、单 Activity + Compose 入口、Material3 主题、
Hilt Application、Navigation 占位路由（列表/详情/编辑/设置/回收站）。minSdk 按设计文档。
升级根 build.gradle / settings.gradle.kts 以支持新结构。给出完整 diff 级说明。
```

---

## D1 — 数据层

```
任务：D1 数据层。实现 NoteEntity、NoteAssetEntity、Room Database、DAO（含 Flow）、
FTS 搜索（与 REFACTOR_DESIGN §5、§10 一致）、NoteRepository 接口与实现（含 query+sort 的 Flow 组合）、
Hilt 绑定。提供单元测试（内存 Room）。不动 UI 或仅接最小占位。
```

---

## D2 — Markdown

```
任务：D2 Markdown。实现 MarkdownRenderer 封装 Markwon、表格/任务列表/删除线等插件、
note://asset/<id> 图片协议与本地文件映射、Compose 侧预览封装（AndroidView 或推荐方案）、
纯文本摘要 extractPlainSummary。单测覆盖摘要与首图提取。
```

---

## D3 — 列表与详情

```
任务：D3 列表+详情。NoteListScreen（LazyVerticalStaggeredGrid、SearchBar、ExtendedFAB）、
NoteListViewModel + 接 Repository；NoteDetailScreen 只读渲染；导航从列表到详情/新建；空态与加载态。
```

---

## D4 — 编辑器（核心）

```
任务：D4 编辑器。实现三模式 Edit/Preview/Split、EditorUiState/EditorIntent/EditorEffect、
MVI 或 UDF ViewModel、TextFieldValue、防抖自动保存（如 500ms）、撤销/重做、
工具栏插入 Markdown 片段、与 D2 渲染联动。大文档时做增量预览或防抖渲染避免卡顿。
单测关键 reduce 与纯函数；Compose UI Test 覆盖模式切换与一次保存。
```

---

## D5 — 媒体

```
任务：D5 媒体。PickVisualMedia、相机 TakePicture、复制到 app 沙盒、WebP 压缩、
NoteAsset 写入、编辑器插入 ![](note://asset/...)、资产 GC 或引用清理策略。处理常见失败（URI 失效、OOM）。
```

---

## D6 — 设置与备份

```
任务：D6。DataStore 设置项、SettingsScreen、导出 md/zip、导入 zip、回收站界面与还原/永久删除。
```

---

## D7 — 动效

```
任务：D7。列表到详情的过渡动效（按当前 Compose 版本 API）、保存状态指示、Snackbar 撤销、触觉反馈（可选）。
```

---

## D8 — 测试与性能

```
任务：D8。补充关键路径测试、Baseline Profile 配置、用 Layout Inspector/文档说明可做的重组优化点、
README 中写冷启动与编辑性能目标及测量方法。
```

---

## D9 — 收尾

```
任务：D9。删除旧 RichEditor/RichEditView/RichTextView 等相关 Java/XML、移除不需要的 jar 与权限、
更新 AndroidManifest、同步 README 与 REFACTOR_DESIGN 中「已删除」清单。确保工程可 release assemble。
```

---

## 整仓一次性（慎用）

仅当你希望 AI 分多轮仍保持上下文时使用；否则 **按 D0→D9 分阶段** 更稳。

```
任务：在单仓库内按 doc/dev/ROADMAP_D0_D9.md 顺序实现 D0 到 D9 全部内容，每阶段可独立编译通过。
若一次无法完成，请先完成 D0-D1 并列出后续待办。
```
