# D5 媒体与资产闭环变更记录

> 日期：2026-04-28  
> 状态：编辑器已打通“选图 -> 沙盒落盘 -> 资产入库 -> 插入 `note://asset`”链路。

## 1) `core-media` 模块

新增：

- `MediaImporter` 接口与 `ImportedMedia` 模型
- `AndroidMediaImporter` 实现
  - 使用 `ContentResolver` 读取 `content://` 图片
  - 复制到 `filesDir/notes/<noteId>/img/<uuid>.<ext>`
  - 回填 `mime/size/width/height`
- `MediaModule`（Hilt 绑定 `MediaImporter`）

并更新：

- `core-media/build.gradle.kts`：接入 Hilt 与协程依赖

## 2) 数据层（`data-note`）

`NoteRepository` 新增：

- `attachImage(noteId, sourceUriString, altText)`
- `resolveAssetAbsolutePath(assetId)`

`RoomNoteRepository` 新增：

- 图片导入后写入 `note_assets`
- 返回 Markdown 图片片段：`![alt](note://asset/<assetId>)`
- 通过 `noteAssetDao.getById` 解析 `assetId -> 本地绝对路径`

`NoteAssetDao` 新增：

- `getById(id)` 查询资产

## 3) 编辑器（`feature-note-editor`）

- `NoteEditorRoute`
  - 新增“图片”按钮
  - 使用 `ActivityResultContracts.PickVisualMedia` 选择图片
  - 选图后调用 `viewModel.insertImageFromUri(...)`
- `NoteEditorViewModel`
  - 新增 `insertImageFromUri`
  - 保存成功后将 `note://asset/...` 图片语法插入正文
  - 预览前把 `note://asset/...` 转成 `file://` 本地路径

## 4) 详情页补充

- `NoteDetailViewModel` 同步增加 `note://asset/...` 到 `file://` 的替换逻辑，保证详情页也可解析资产图片路径。

## 5) 说明

- 当前实现优先保证链路可用，图片压缩策略（长边/质量）与孤儿资源回收策略将在后续阶段增强。
