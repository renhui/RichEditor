# D7 动效与体验变更记录

> 日期：2026-04-28  
> 状态：关键路径已加入“删除可撤销 + 触觉反馈 + 保存状态可视化”。

## 1) 列表页交互增强（`feature-note-list`）

- `NoteListViewModel` 新增：
  - `deleteNote(id)`：软删除
  - `restoreNote(id)`：撤销删除
- `NoteListRoute` 新增：
  - 删除按钮（卡片内）
  - `SnackbarHost` + “撤销”动作
  - 触觉反馈：
    - 新建按钮：轻触反馈
    - 删除动作：长按反馈

## 2) 编辑器状态反馈（`feature-note-editor`）

- 顶栏保存状态旁新增 `Badge` 指示点
- 指示点颜色随 `SaveState` 动画过渡：
  - `SAVING`：蓝色
  - `SAVED`：绿色
  - `ERROR`：红色
  - `IDLE`：中性灰

## 3) 说明

- 当前 D7 优先落地高频反馈交互；列表到详情的复杂共享元素转场将在后续继续增强。
