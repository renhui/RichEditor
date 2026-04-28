# D8 测试与性能变更记录

> 日期：2026-04-28  
> 状态：补齐关键单测、主流程 smoke UI 测试，并新增性能测量基线文档。

## 1) 单元测试

### `core-markdown`

- 新增 `MarkdownTextParser`（纯函数解析）
- 新增测试 `MarkdownTextParserTest`
  - 验证摘要提取去除 Markdown 噪音
  - 验证首图提取逻辑

### `data-note`

- 新增 `NoteSearchQueryFormatter`（FTS 查询格式化）
- 新增测试 `NoteSearchQueryFormatterTest`
  - 单关键词
  - 多关键词 AND 拼接
  - 空输入处理

## 2) UI 测试（smoke）

- `app` 新增 `MainActivitySmokeTest`
  - 启动后断言列表标题“便签”可见

## 3) 构建依赖补充

- `libs.versions.toml`
  - 新增 `coroutines-test`
  - 新增 `compose-ui-test-junit4`
- `core-markdown/build.gradle.kts`
  - 新增 `testImplementation(libs.junit)`
- `data-note/build.gradle.kts`
  - 新增 `junit/coroutines-test/room-testing`
- `app/build.gradle.kts`
  - 新增 `androidTestImplementation(libs.compose.ui.test.junit4)`

## 4) 性能文档

- 新增 `doc/dev/PERFORMANCE_BASELINE.md`
  - 冷启动测量步骤
  - 编辑输入延迟测量步骤
  - Compose 重组排查方法
  - Baseline Profile 后续接入建议
