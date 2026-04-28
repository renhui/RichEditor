# D4 编辑器核心变更记录

> 日期：2026-04-28  
> 状态：编辑器已具备三模式、自动保存、防抖、基础撤销重做与工具栏动作。

## 1) 编辑器状态模型（`feature-note-editor/model`）

新增：

- `EditorMode`：`EDIT / PREVIEW / SPLIT`
- `SaveState`：`IDLE / SAVING / SAVED / ERROR`
- `EditorAction`：`HEADER_1 / BOLD / TODO / CODE_BLOCK`
- `EditorUiState`：包含 `title/markdown/mode/saveState/canUndo/canRedo/isLoading`

## 2) ViewModel（`NoteEditorViewModel`）

实现能力：

- 从 `SavedStateHandle.noteId` 读取当前便签 ID（`new` 时自动生成 UUID）
- 订阅 `NoteRepository.observeById` 回填历史内容
- `draftFlow.debounce(500)` 自动保存（保存状态回显）
- `undo/redo` 基础能力（历史栈上限 200）
- 工具栏动作插入 Markdown 片段（H1、粗体、待办、代码块）

## 3) UI（`NoteEditorRoute`）

- `TopAppBar` 展示实时保存状态文案
- 模式切换按钮（编辑 / 预览 / 分屏）
- `BottomBar` 工具栏（Markdown 片段 + 撤销 / 重做）
- `EditorPane`：标题 + Markdown 文本输入
- `PreviewPane`：通过 `MarkdownRenderer` 实时预览

## 4) 依赖与注入

- `feature-note-editor/build.gradle.kts`：
  - 新增对 `core-markdown`、`data-note` 的依赖
  - 新增 Hilt + lifecycle compose + hilt navigation compose
- `RichNoteNavHost`：
  - `NoteEditorRoute` 改为注入 `markdownRenderer`

## 5) 说明

- 当前 D4 先实现“可用核心闭环”，光标级精确包裹编辑、复杂撤销快照、滚动同步等细节会继续在后续迭代增强。
