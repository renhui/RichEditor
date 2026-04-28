# D3 列表与详情 UI 变更记录

> 日期：2026-04-28  
> 状态：列表与详情在 D1/D2 基础上升级为可用的阅读流程 UI。

## 1) 列表页（`feature-note-list`）

- `NoteListViewModel`
  - 搜索流增加 `debounce(300)`，减少频繁触发 FTS 查询。
- `NoteListRoute`
  - 改为 `Scaffold` 结构（TopAppBar + FAB）
  - 增加 `ExtendedFloatingActionButton`（新建便签）
  - 保留设置/回收站入口
  - 列表卡片展示：标题、摘要、置顶标记、更新时间
  - 增加空态和加载态
  - 列表底部增加 `contentPadding`，避免 FAB 遮挡

## 2) 详情页（`feature-note-detail`）

- `NoteDetailRoute`
  - 继续使用 D2 的 `AndroidView + MarkdownRenderer`
  - 增加 `TextView` 行距提升可读性
  - 保持返回/编辑操作入口

## 3) 说明

- 当前 D3 重点是“可用 UI 流程”，完整的瀑布流布局、动画、目录抽屉等将继续在后续阶段增强。
