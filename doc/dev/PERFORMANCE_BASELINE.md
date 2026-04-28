# D8 性能与测试基线

## 1) 本阶段目标

- 单测：覆盖 Markdown 摘要与 FTS 查询格式化关键逻辑
- UI 测试：主流程 smoke（启动后列表标题可见）
- 性能测量：提供冷启动、输入延迟、重组排查步骤

## 2) 推荐执行命令

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## 3) 冷启动测量（手工）

1. 在 Android Studio 打开 Profiler
2. 选择 App 进程并执行 Cold Start（Force Stop 后启动）
3. 记录 `Time To Full Display`，目标 `< 800ms`（参考机）
4. 连续测 5 次取中位数，写入发布记录

## 4) 编辑输入延迟测量

1. 进入编辑器，准备约 3KB / 20KB / 50KB 三组文本
2. 打开 System Trace 或 CPU Profiler
3. 连续输入 30 秒，观察主线程是否出现明显长帧（>16ms）
4. 若长帧明显，优先检查：
   - 自动保存触发频率
   - Markdown 预览是否在 UI 线程执行
   - 大量状态更新导致的重组

## 5) Compose 重组排查

1. 打开 Layout Inspector 的 Recomposition Counts
2. 在列表和编辑器页面执行核心交互
3. 重点观察：
   - 搜索输入时是否整页重组
   - 编辑器输入时 toolbar 是否频繁重组
4. 优化方向：
   - 拆分大 Composable
   - `derivedStateOf` 缓解重复计算
   - 减少不必要的 lambda/对象新建

## 6) Baseline Profile（当前阶段说明）

当前仓库未接入 baseline profile 模块，D8 先完成测量说明与测试基线。  
后续可新增 `:benchmark` 模块并接入 `profileinstaller` / Macrobenchmark 生成规则。
