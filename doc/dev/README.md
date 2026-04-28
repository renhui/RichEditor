# `doc/dev` — AI 全栈实现说明

本目录描述 **D0–D9 各阶段均由 AI 生成/维护代码** 时的执行方式、交付物与验收标准。

| 文档 | 用途 |
|---|---|
| [`ROADMAP_D0_D9.md`](ROADMAP_D0_D9.md) | 按 D0–D9 的 **AI 实施路线、每步交付清单、验收点**（主文档） |
| [`AI_HUMAN_HANDOFF.md`](AI_HUMAN_HANDOFF.md) | **人做什么、不手写什么**；与 [`../DEV_PLAYBOOK.md`](../DEV_PLAYBOOK.md) 的对比 |
| [`PROMPTS_INDEX.md`](PROMPTS_INDEX.md) | 各阶段给 AI 的 **任务说明/提示词索引**（可整段复制到对话中） |
| [`D0_CHANGELOG.md`](D0_CHANGELOG.md) | **D0 已落地的仓库变更摘要**（模块、入口、Manifest） |
| [`D1_CHANGELOG.md`](D1_CHANGELOG.md) | **D1 数据层落地记录**（Room、FTS、Repository、Hilt） |
| [`D2_CHANGELOG.md`](D2_CHANGELOG.md) | **D2 渲染链路记录**（Markwon、Renderer、详情页接入） |
| [`D3_CHANGELOG.md`](D3_CHANGELOG.md) | **D3 页面体验记录**（列表升级、详情可读性优化） |
| [`D4_CHANGELOG.md`](D4_CHANGELOG.md) | **D4 编辑器核心记录**（三模式、自动保存、撤销重做） |
| [`D5_CHANGELOG.md`](D5_CHANGELOG.md) | **D5 媒体闭环记录**（选图、资产入库、`note://asset`） |
| [`D6_CHANGELOG.md`](D6_CHANGELOG.md) | **D6 设置记录**（DataStore、回收站、导入导出最小实现） |
| [`D7_CHANGELOG.md`](D7_CHANGELOG.md) | **D7 体验记录**（删除撤销、触觉反馈、保存状态指示） |
| [`D8_CHANGELOG.md`](D8_CHANGELOG.md) | **D8 测试记录**（单测、UI smoke、性能基线） |
| [`D9_CHANGELOG.md`](D9_CHANGELOG.md) | **D9 收尾记录**（删旧实现、清理依赖、Manifest 精简） |
| [`PERFORMANCE_BASELINE.md`](PERFORMANCE_BASELINE.md) | 性能测量步骤与基线建议 |
| [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md) | 发布前收口清单（权限、依赖、稳定性、文档一致性） |

## 与其它文档的关系

- 产品与技术总规：[`../REFACTOR_DESIGN.md`](../REFACTOR_DESIGN.md)（**以该文档为真**；本目录只写“怎么按阶段让 AI 落地”）
- 以学代练、自己写 C 档的版本：[`../DEV_PLAYBOOK.md`](../DEV_PLAYBOOK.md)（强调你手敲 MVI/Flow 等；**与 `doc/dev` 模式可二选一**）
- 本目录：当选择 **全部由 AI 写实现，人做审核/跑通/签入** 时使用

## 快速开始

1. 阅读 [`ROADMAP_D0_D9.md`](ROADMAP_D0_D9.md) 中的 **D0 节**。
2. 打开 [`PROMPTS_INDEX.md`](PROMPTS_INDEX.md) 中 **D0 Prompt**，粘贴到 AI 会话，附上 `REFACTOR_DESIGN.md` 路径或内容摘要。
3. 按 [`AI_HUMAN_HANDOFF.md`](AI_HUMAN_HANDOFF.md) 完成 **Sync → 编译 → 真机跑** 后，再进入 D1。
