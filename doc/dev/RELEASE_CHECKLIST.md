# 发布前收口清单（Release Checklist）

> 适用：D0-D9 完成后的发布前收口。  
> 目标：在发版前一次性检查权限、依赖、稳定性、包体与文档一致性。

---

## 1. 构建与测试（必须全绿）

- [ ] `./gradlew :app:assembleDebug`
- [ ] `./gradlew :app:assembleRelease`
- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew connectedDebugAndroidTest`（至少跑 smoke）

建议：
- [ ] 如果连接设备不稳定，先重启 adb：`adb kill-server && adb start-server`
- [ ] 记录本次构建机环境（JDK、AGP、Gradle 版本）

---

## 2. 权限与 Manifest

当前目标权限最小化（仅保留拍照能力）：

- [ ] `AndroidManifest.xml` 仅包含必要权限（当前应为 `CAMERA`）
- [ ] 无历史权限残留：
  - `READ_PHONE_STATE`
  - `ACCESS_WIFI_STATE`
  - `WRITE_EXTERNAL_STORAGE`
  - `ACCESS_NETWORK_STATE`（如无业务必需）
- [ ] 无历史 Activity 注册残留（旧 Java 页面）
- [ ] 无第三方统计 meta-data 残留（如 umeng key）

---

## 3. 依赖与包体

- [ ] `app/libs` 无历史 jar（`photo-view`、`umeng`、`universal-image-loader`、`common-lang3`）
- [ ] `app/build.gradle.kts` 不再使用 `fileTree(libs/*.jar)`
- [ ] 依赖只来自 `libs.versions.toml` 或模块依赖
- [ ] `./gradlew :app:dependencies` 抽查无明显重复大依赖
- [ ] 记录 debug/release APK 体积（用于后续回归对比）

---

## 4. 核心功能回归（人工点通）

### 4.1 列表
- [ ] 打开首页，能显示已有便签
- [ ] 搜索可用（标题/正文关键字）
- [ ] 删除后出现 Snackbar，可“撤销”

### 4.2 编辑器
- [ ] Edit / Preview / Split 三模式切换正常
- [ ] 自动保存状态会变化（保存中→已保存）
- [ ] 撤销/重做可用
- [ ] 插图后可看到 `note://asset/...` 被正确渲染预览

### 4.3 详情页
- [ ] Markdown 正常渲染（标题、列表、代码块）
- [ ] 图片可显示（本地资产）

### 4.4 设置/回收站
- [ ] 主题、字号、自动保存设置可持久化
- [ ] 回收站可看到删除项、可还原、可彻底删除
- [ ] 导出预览与导入示例流程可跑通

---

## 5. 数据与兼容性

- [ ] 新建便签后重启 App，数据仍在
- [ ] 删除后重启 App，回收站状态正确
- [ ] 图片资产路径在重启后仍可解析
- [ ] 不依赖外部存储历史路径（仅沙盒）

---

## 6. 稳定性与性能抽查

参考文档：`doc/dev/PERFORMANCE_BASELINE.md`

- [ ] 冷启动抽样 3-5 次，无明显异常峰值
- [ ] 编辑器输入 30s，不出现明显卡顿/ANR
- [ ] 列表滚动无严重掉帧
- [ ] 日志中无持续异常刷屏（尤其 Room/Hilt/Markwon）

---

## 7. 文档一致性

- [ ] `README.md` 与当前实现一致（Markdown First，不再描述旧 HTML 链路）
- [ ] `doc/dev/README.md` 索引完整（D0~D9、性能文档）
- [ ] 各阶段 changelog 与代码实际一致
- [ ] 若有偏离 `REFACTOR_DESIGN.md` 的实现，已在文档中注明

---

## 8. 发布前最后动作

- [ ] 更新版本号（如从 `2.0.0-d9` 到正式版本）
- [ ] 打标签前保存构建产物 hash（可追溯）
- [ ] 保留一份“已知问题清单”（若有）
- [ ] 产出发布说明（功能/风险/回滚点）

---

## 9. 快速命令清单

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```
