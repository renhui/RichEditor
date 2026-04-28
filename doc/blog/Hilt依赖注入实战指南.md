# Hilt 依赖注入实战指南 —— 以 RichEditor 项目为例

> 本文基于 RichEditor（一款 Markdown First 富文本便签 App）的真实工程实践，系统性地介绍 Hilt 在多模块 Android 项目中的使用方式。如果你正在构建一个基于 Kotlin + Jetpack Compose + 模块化架构的项目，这篇文章会给你一个完整的参考路径。

---

## 目录

1. [什么是 Hilt](#1-什么是-hilt)
2. [为什么选择 Hilt](#2-为什么选择-hilt)
3. [环境配置](#3-环境配置)
4. [核心概念与注解详解](#4-核心概念与注解详解)
5. [Application 入口配置详解](#5-application-入口配置详解)
6. [Activity 注入详解](#6-activity-注入详解)
7. [Module 的两种写法：@Provides vs @Binds](#7-module-的两种写法provides-vs-binds)
8. [ViewModel 注入详解](#8-viewmodel-注入详解)
9. [多模块项目中的 Hilt 实践](#9-多模块项目中的-hilt-实践)
10. [依赖图总览](#10-依赖图总览)
11. [常见问题与排坑](#11-常见问题与排坑)
12. [Hilt 测试支持](#12-hilt-测试支持)
13. [总结](#13-总结)

---

## 1. 什么是 Hilt

### 1.1 Hilt 的前世今生

Hilt 是 Google 在 2020 年发布的 Android 专用依赖注入框架，其底层基于业界成熟的 Dagger。Google 选择 Dagger 而非自研的原因在于：Dagger 已经在 Square、Uber 等公司经过多年生产环境验证，其编译期代码生成机制提供了最强的类型安全保证。

```
Dagger (2012, Square)
    ↓
Dagger 2 (2016, Google 接手维护)
    ↓
Hilt (2020, Google 为 Android 量身定制)
```

**Hilt 与 Dagger 的关系**：
- Hilt 是 Dagger 2 的封装，专门为 Android 场景优化
- Dagger 2 需要手动管理 Component 和 SubComponent 的层级关系
- Hilt 自动处理 Android 组件（Application、Activity、Fragment 等）的生命周期绑定
- 代码量减少约 60%，学习曲线大幅降低

### 1.2 依赖注入的核心思想

依赖注入（Dependency Injection，简称 DI）是一种设计模式，其核心思想是**将对象依赖的创建责任从对象本身转移到外部容器**。

**没有 DI 的代码**（紧耦合）：
```kotlin
class NoteRepository {
    private val dao = NoteDao()  // NoteRepository 自己创建 DAO
    private val mediaImporter = AndroidMediaImporter()  // 自己创建媒体导入器

    fun saveNote(note: Note) {
        dao.insert(note)
    }
}
```

**问题**：
- `NoteRepository` 与 `NoteDao` 具体实现强耦合
- 无法替换为测试用的 Mock DAO
- 依赖创建逻辑分散在各个类中，难以管理

**有 DI 的代码**（松耦合）：
```kotlin
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val mediaImporter: MediaImporter
) {
    fun saveNote(note: Note) {
        dao.insert(note)
    }
}
```

**优势**：
- `NoteRepository` 只声明「我需要什么」，不关心「如何创建」
- 可以轻松注入 Mock DAO 进行单元测试
- 所有依赖的创建逻辑集中在 DI 容器中

### 1.3 Hilt 的工作原理

```
┌─────────────────────────────────────────────────────────────────┐
│                        编译期处理                                 │
│                                                                  │
│   源代码 (.kt)           Hilt Processor          生成的代码       │
│   ────────────────       ─────────────────       ─────────────  │
│   @HiltAndroidApp   →   注解处理器扫描    →    Hilt_AppComponent │
│   @AndroidEntryPoint →  生成绑定关系     →    Hilt_MainActivity   │
│   @HiltViewModel     →                  →    ViewModelFactory    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        运行时注入                                 │
│                                                                  │
│   App 启动                                                     │
│       │                                                         │
│       ↓                                                         │
│   Hilt 初始化 SingletonComponent                                │
│       │                                                         │
│       ↓                                                         │
│   当需要 NoteRepository 时：                                     │
│       1. 检查是否有缓存实例                                      │
│       2. 没有则解析依赖图：NoteRepository → NoteDao → Database  │
│       3. 按依赖顺序创建实例                                       │
│       4. 返回 NoteRepository 并缓存                              │
└─────────────────────────────────────────────────────────────────┘
```

**关键点**：所有注入逻辑在编译期就已经确定，运行时只是按图索骥创建对象，没有任何反射带来的性能损耗。

---

## 2. 为什么选择 Hilt

### 2.1 与其他 DI 框架的对比

| 特性 | Hilt | Koin | Manual DI |
|------|------|------|-----------|
| **代码生成** | 编译期生成 | 运行时 DSL 注册 | 无 |
| **性能** | 无运行时开销 | 有运行时解析开销 | 无 |
| **类型安全** | 编译期检查 | 运行时可能出错 | 手动易出错 |
| **Android 适配** | 官方推荐 | 需要手动适配 | 需要手动适配 |
| **ViewModel 支持** | 原生支持 | 需要扩展 | 需要 Factory |
| **多模块支持** | 原生支持 | 支持但配置复杂 | 需要手动传递 |

### 2.2 RichEditor 选择 Hilt 的理由详解

#### 理由一：多模块天然支持

RichEditor 项目结构：
```
app/                        ← 入口模块
core/
  core-database/           ← 数据库定义
  core-datastore/          ← 偏好设置
  core-media/             ← 媒体处理
  core-markdown/          ← Markdown 渲染
data/
  data-note/              ← 便签数据层
feature/
  feature-note-list/      ← 便签列表页
  feature-note-detail/    ← 便签详情页
  feature-note-editor/    ← 便签编辑器
  feature-settings/      ← 设置页
```

**传统做法**：需要在 app 模块中手动汇总所有模块的依赖：
```kotlin
// app 模块需要知道所有子模块的依赖
object AppModule {
    fun provideMediaImporter() = AndroidMediaImporter()
    fun provideDataStore() = ...
    fun provideNoteDao() = ...
    // ... 数十个依赖
}
```

**Hilt 做法**：每个模块独立声明，Hilt 自动聚合：
```kotlin
// core-media 模块只需声明自己的依赖
@Module @InstallIn(SingletonComponent::class)
abstract class MediaModule {
    @Binds abstract fun bindMediaImporter(impl: AndroidMediaImporter): MediaImporter
}

// core-datastore 模块独立声明
@Module @InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides fun provideDataStore(...): DataStore<Preferences> = ...
}
```

`@InstallIn(SingletonComponent::class)` 是 Hilt 实现分布式依赖声明的关键。编译时，Hilt 会扫描所有模块，自动将它们安装到 `SingletonComponent` 中，无需在 app 模块手动汇总。

#### 理由二：ViewModel 开箱即用

**传统 ViewModel Factory 模式**：
```kotlin
class NoteEditorViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    // ...
}

// 需要 Factory
class NoteEditorViewModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {
            return NoteEditorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 在 Composable 中使用
@Composable
fun NoteEditorScreen(viewModel: NoteEditorViewModel = viewModel(
    factory = NoteEditorViewModelFactory(repository)
)) {
    // ...
}
```

**Hilt 模式**：
```kotlin
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    // ...
}

// Composable 中一行搞定
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    // ...
}
```

对比结果：
| 项目 | 传统 Factory | Hilt |
|------|-------------|------|
| 代码行数 | 15+ 行 | 注解 + 一行调用 |
| 类型安全 | 需要手动检查 | 编译期保证 |
| 导航参数 | 需要手动传递 | 自动通过 SavedStateHandle 注入 |
| 测试 | 需要 mock Factory | 直接 inject mock Repository |

#### 理由三：编译期安全

```kotlin
// 如果缺少依赖，编译时报错
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,  // ✓ Hilt 能解析
    private val markdownRenderer: MarkdownRenderer,  // ✓ Hilt 能解析
    private val nonExistentService: NonExistentService  // ✗ 编译报错！
) : ViewModel()
```

编译错误示例：
```
error: [Dagger/HiltProcessingError] com.example.richtext.NonExistentService
      cannot be provided without an @Provides-annotated method.
```

这种机制让你在编译期发现所有依赖问题，而不是在用户使用 App 时崩溃。

#### 理由四：与 Jetpack 深度集成

Hilt 提供了与主流 Jetpack 组件的官方集成：

| Jetpack 组件 | Hilt 集成库 | 提供的功能 |
|--------------|-------------|------------|
| Navigation Compose | `hilt-navigation-compose` | `hiltViewModel()` 自动携带 NavBackStackEntry |
| WorkManager | `hilt-work` | `@HiltWorker` 自动注入 Worker |
| DataStore | (通过 `@Provides`) | 在 Module 中提供 `DataStore<Preferences>` 实例 |
| Room | (通过 `@Provides`) | 在 Module 中提供 Database 和 DAO 实例 |
| Compose | (通过 ViewModel) | `@HiltViewModel` + `hiltViewModel()` |

---

## 3. 环境配置

### 3.1 Version Catalog 详解

Gradle Version Catalog（从 Gradle 7.0 引入）是管理依赖版本的推荐方式。所有依赖版本集中在一个文件中，便于统一升级。

```toml
# gradle/libs.versions.toml

[versions]
hilt = "2.51.1"           # Hilt 版本定义
kotlin = "1.9.24"         # Kotlin 版本
room = "2.6.1"             # Room 版本
compose = "1.6.1"         # Compose BOM 版本

[libraries]
# Hilt 库定义
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# Navigation Compose 集成
hilt-navigation-compose = { 
    group = "androidx.hilt", 
    name = "hilt-navigation-compose", 
    version = "1.2.0" 
}

# Room（用于数据库模块）
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
android-application = { id = "com.android.application", version.ref = "androidx" }
android-library = { id = "com.android.library", version.ref = "androidx" }
```

**为什么使用 version.ref 引用？**
- 版本变更时只需修改 `[versions]` 区块
- 避免同一版本在不同库中不一致
- 便于依赖升级审计

### 3.2 根 build.gradle.kts 配置详解

```kotlin
// build.gradle.kts (Project Root)
plugins {
    // apply false 表示这些插件只声明，不自动应用到当前项目
    // 这是 Version Catalog 的标准用法
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt.android) apply false
}
```

**关键点**：
- `apply false`：这些插件将在子模块中实际应用，这里只是声明
- Hilt 插件需要 `kotlin-kapt` 配合，因为 Hilt 的注解处理器基于 kapt
- `android.application` 和 `android.library` 只需要声明一次，子模块通过 alias 应用

### 3.3 App 模块 build.gradle.kts 详解

```kotlin
// app/build.gradle.kts
plugins {
    // 应用 Application 插件，生成清单文件和签名配置
    alias(libs.plugins.android.application)
    // Kotlin Android 插件
    alias(libs.plugins.kotlin.android)
    // 启用 kapt 注解处理器
    alias(libs.plugins.kotlin.kapt)
    // Hilt 插件
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.richtext"
    
    defaultConfig {
        applicationId = "com.example.richtext"
        // ...
    }
}

dependencies {
    // Hilt Android 运行时库
    implementation(libs.hilt.android)
    
    // Hilt 注解处理器（编译时生成代码）
    // 注意：必须是 kapt，不能是 implementation
    kapt(libs.hilt.compiler)
}
```

**重要配置说明**：

| 配置项 | 必须 | 原因 |
|--------|------|------|
| `kotlin-kapt` 插件 | ✓ | Hilt 基于 kapt 处理注解 |
| `hilt-android` 插件 | ✓ | 生成 Hilt 代码 |
| `implementation(hilt-android)` | ✓ | 运行时代码 |
| `kapt(hilt-compiler)` | ✓ | 编译时代码生成器 |

**如果用 `implementation(hilt-compiler)` 会怎样？**
- 编译时不会调用注解处理器
- 生成不了 Hilt 代码
- 运行时报错：`Component was not built`

### 3.4 Library 模块配置详解

Library 模块（`core-*`、`data-*`、`feature-*`）的配置与 App 模块略有不同：

```kotlin
// core/core-markdown/build.gradle.kts
plugins {
    // Library 模块用 android.library，不是 android.application
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.richtext.core.markdown"
}

dependencies {
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

**feature 模块额外配置**：
```kotlin
// feature/feature-note-editor/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

dependencies {
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // feature 模块需要使用 Compose Navigation + Hilt 集成
    implementation(libs.hilt.navigation.compose)
}
```

---

## 4. 核心概念与注解详解

### 4.1 注解全解析

| 注解 | 作用 | 编译产物 | 注意事项 |
|------|------|----------|----------|
| `@HiltAndroidApp` | 标记 Application 类，触发 Hilt 初始化 | 生成 `Hilt_AppClass` 基类 | 整个 App 只能有一个 |
| `@AndroidEntryPoint` | 生成注入代码，使组件可接收注入 | 生成 `Hilt_Component` | Activity/Fragment 需要此注解 |
| `@HiltViewModel` | 标记 ViewModel 可被 Hilt 创建 | 生成 Factory | 必须包含 `@Inject constructor` |
| `@Inject` | 标记构造参数或字段需要注入 | 无直接产物 | 构造注入优先于字段注入 |
| `@Module` | 声明依赖提供模块 | 无直接产物 | 必须配合 `@InstallIn` |
| `@InstallIn` | 指定 Module 安装位置 | 参与 Component 组装 | 常见值：SingletonComponent、ActivityComponent |
| `@Provides` | 在 Module 中提供实例 | 注册到 Component 的 Provider 集合 | 必须是 object class |
| `@Binds` | 接口到实现的绑定 | 注册到 Component 的 Binding 集合 | 必须是 abstract class |
| `@Singleton` | 单例作用域 | 启用 Component 级别缓存 | 配合 `@Provides` 或 `@Binds` 使用 |
| `@ApplicationContext` | 标记需要 Application Context | 内置类型映射 | Hilt 内置，无需配置 |

### 4.2 Component 层级体系

Hilt 定义了严格的 Component 层级，与 Android 组件生命周期对应：

```
┌─────────────────────────────────────────────────────────────────┐
│                     SingletonComponent                          │
│                         (Application)                           │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │              ActivityRetainedComponent                  │   │
│   │                  (Activity)                             │   │
│   │                                                        │   │
│   │   ┌─────────────────────────────────────────────────┐   │   │
│   │   │              ActivityComponent                  │   │   │
│   │   │                 (Activity)                       │   │   │
│   │   │                                                │   │   │
│   │   │   ┌─────────────────────────────────────────┐   │   │   │
│   │   │   │           FragmentComponent             │   │   │
│   │   │   │              (Fragment)                   │   │   │   │
│   │   │   │                                            │   │   │   │
│   │   │   │   ┌───────────────────────────────────┐   │   │   │   │
│   │   │   │   │        ViewComponent             │   │   │   │   │
│   │   │   │   │           (View)                  │   │   │   │   │
│   │   │   │   └───────────────────────────────────┘   │   │   │   │
│   │   │   └─────────────────────────────────────────┘   │   │   │   │
│   │   └─────────────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**各 Component 的生命周期**：

| Component | 对应 Android 组件 | 实例生命周期 | 典型使用场景 |
|------------|-------------------|--------------|--------------|
| SingletonComponent | Application | App 存活期间 | Repository、Database、DataStore |
| ActivityRetainedComponent | Activity | Activity 配置变更后仍存活 | ViewModel Scoped 依赖 |
| ActivityComponent | Activity | Activity 存活期间 | Activity 级单例 |
| FragmentComponent | Fragment | Fragment 存活期间 | Fragment 级依赖 |
| ViewComponent | View | View 存活期间 | 自定义 View 注入 |

**为什么通常用 SingletonComponent？**
- 在 Compose 单 Activity 架构中，Activity/Fragment 的生命周期很短
- Repository 等数据层需要在 Activity 销毁后继续存活
- SingletonComponent 与 Application 生命周期一致，最适合数据层对象

### 4.3 @Inject 详解

`@Inject` 有两种使用方式：

#### 4.3.1 构造注入（推荐）

```kotlin
@Singleton
class RoomNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteAssetDao: NoteAssetDao,
    private val mediaImporter: MediaImporter,
) : NoteRepository {
    // ...
}
```

**原理**：
1. Hilt 检测到 `@Inject` 注解的构造函数
2. 分析构造参数：`NoteDao`、`NoteAssetDao`、`MediaImporter`
3. 尝试解析这些类型：如果有 `@Provides` 或其他 `@Inject constructor`，则可以注入
4. 生成类似以下代码：
```kotlin
// Hilt 生成的代码（伪代码）
fun provideRoomNoteRepository(
    noteDao: NoteDao,
    noteAssetDao: NoteAssetDao,
    mediaImporter: MediaImporter
): RoomNoteRepository {
    return RoomNoteRepository(noteDao, noteAssetDao, mediaImporter)
}
```

#### 4.3.2 字段注入（需要场景）

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var markdownRenderer: MarkdownRenderer  // 字段注入
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // markdownRenderer 已经在 super.onCreate() 前被赋值
        Log.d("MainActivity", "MarkdownRenderer: $markdownRenderer")
    }
}
```

**字段注入的执行时机**：
- `@AndroidEntryPoint` 生成的代码会在 `super.onCreate()` 之前注入所有 `@Inject` 字段
- 因此可以在 `onCreate()` 中安全使用注入的字段

**何时使用字段注入 vs 构造注入？**

| 场景 | 推荐方式 | 原因 |
|------|----------|------|
| 可以修改的类（自己的代码） | 构造注入 | 更安全，依赖不可变 |
| 无法修改的类（第三方库） | 字段注入 | 无法修改构造函数 |
| Android 组件（Activity/Fragment） | 字段注入 | Android 组件由系统创建，无法构造注入 |

### 4.4 @ApplicationContext 详解

```kotlin
@Singleton
class AndroidMediaImporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaImporter {
    // Hilt 会自动注入 Application Context
}
```

**@ApplicationContext 的原理**：
- Hilt 内置了对 `Context` 类型的特殊处理
- 标注 `@ApplicationContext` 后，Hilt 注入的是 `context.applicationContext`
- 这比直接注入 `Context` 更安全，避免内存泄漏

**相关限定符**：

| 限定符 | 提供类型 | 使用场景 |
|--------|----------|----------|
| `@ApplicationContext` | Application Context | 通常的 Android API 调用，避免内存泄漏 |
| `@ActivityContext` | 当前 Activity Context | 需要 Activity 能力（如启动 Activity、获取 Window）的场景 |

---

## 5. Application 入口配置详解

### 5.1 @HiltAndroidApp 的作用

```kotlin
@HiltAndroidApp
class RichNoteApplication : Application()
```

**@HiltAndroidApp 的职责**：

```
@HiltAndroidApp 注解的处理流程：

1. 触发 Hilt 注解处理器
2. 生成 Hilt_RichNoteApplication 基类：
   
   // 生成的伪代码
   open class Hilt_RichNoteApplication : Application() {
       private lateinit var entryPointMap: Map<TypeLiteral, ?>
       
       override fun onCreate() {
           super.onCreate()
           
           // 1. 初始化 SingletonComponent
           val component = DaggerSingletonComponent.builder()
               .application(this)
               .build()
           
           // 2. 存储 EntryPoint 用于视图注入
           entryPointMap = componentMapOf(
               EntryPointAccessors::class.java to component
           )
           
           // 3. 缓存 component 供后续使用
           singletonComponent = component
       }
   }

3. RichNoteApplication 继承 Hilt_RichNoteApplication
4. App 启动时自动初始化所有 @Singleton 依赖
```

### 5.2 生成的 Component 结构

```kotlin
// 生成的 SingletonComponent 伪代码
@Singleton
@Component(modules = [
    // 自动收集所有 @Module @InstallIn(SingletonComponent::class)
    MarkdownProviderModule::class,
    NoteDatabaseModule::class,
    NoteRepositoryModule::class,
    DataStoreProviderModule::class,
    DataStoreBindModule::class,
    MediaModule::class
])
interface SingletonComponent {
    // 所有 @Provides/@Binds 的 binding 方法
}
```

**Hilt 如何收集所有 Module？**
- 编译时扫描所有模块的 classpath
- 查找所有标注了 `@Module` 和 `@InstallIn(SingletonComponent::class)` 的类
- 自动生成 Component 的 `modules` 参数

### 5.3 Application 中的额外初始化

RichEditor 中的 Application 还做了额外的工作：

```kotlin
@HiltAndroidApp
class RichNoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        // 用于在非 Hilt 管理的代码中获取 Application 实例
        @JvmStatic
        lateinit var INSTANCE: RichNoteApplication
            private set

        @JvmStatic
        fun getInstance(): RichNoteApplication = INSTANCE
    }
}
```

**为什么要保留 INSTANCE？**

某些场景可能需要在 Hilt 外部获取 Application：
- 第三方 SDK 初始化需要 Context
- 某些静态工具类需要 Application

**更推荐的做法**：尽量使用依赖注入，只有在无法注入时才使用 INSTANCE：
```kotlin
// 推荐：直接在需要的地方注入
class ThirdPartySDKWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun initialize() {
        ThirdPartySDK.init(context)
    }
}

// 不推荐：使用 INSTANCE
fun someLegacyCode() {
    val app = RichNoteApplication.getInstance()
    ThirdPartySDK.init(app)
}
```

### 5.4 AndroidManifest.xml 配置

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".RichNoteApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.RichNote">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>

</manifest>
```

**android:name=".RichNoteApplication"** 是必须的：
- 如果不声明，Hilt 无法初始化
- App 启动时不会创建 Hilt 组件
- 所有 `@AndroidEntryPoint` 的类会崩溃

---

## 6. Activity 注入详解

### 6.1 @AndroidEntryPoint 的作用

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ...
}
```

**@AndroidEntryPoint 生成的代码**：

Hilt 通过字节码转换，使 `MainActivity` 实际上继承生成的 `Hilt_MainActivity`，继承链为：
`MainActivity` → `Hilt_MainActivity` → `ComponentActivity`

```kotlin
// 编译生成的伪代码（Hilt 字节码转换后的继承结构）
@Generated("Hilt")
abstract class Hilt_MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Hilt 在 super.onCreate() 之前注入所有 @Inject 字段
        injectMembers()
        super.onCreate(savedInstanceState)
    }
    
    private fun injectMembers() {
        // 从 SingletonComponent 获取/创建 MarkdownRenderer
        val entryPoint = EntryPointAccessors.fromActivity(
            this, MainActivityEntryPoint::class.java
        )
        this.markdownRenderer = entryPoint.markdownRenderer()
    }
}

// 你写的代码：
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var markdownRenderer: MarkdownRenderer
    // 经过 Hilt 字节码转换后，实际继承自 Hilt_MainActivity
}
```

### 6.2 注入时序图

```
MainActivity 生命周期与注入时序：

┌─────────────────────────────────────────────────────────────┐
│                    MainActivity                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  onCreate(savedInstanceState: Bundle?)                      │
│       │                                                      │
│       ├──┐ injectFields()  ← 在这里完成注入                  │
│       │  │                                                      │
│       │  │ Hilt 解析依赖图：                                    │
│       │  │   MarkdownRenderer                                  │
│       │  │     └─ MarkwonMarkdownRenderer                      │
│       │  │           └─ Markwon                               │
│       │  │                 └─ MarkwonBuilder.build()          │
│       │  │                                                      │
│       │  │ 如果已缓存，直接返回                                 │
│       │  │ 如果未创建，按依赖顺序创建                           │
│       │  │                                                      │
│       ├──┘ 注入完成                                           │
│       │                                                      │
│       ▼ super.onCreate()                                     │
│                                                             │
│  setContent { ... }  ← 可以安全使用 markdownRenderer         │
│                                                             │
│  onDestroy()                                                 │
│       │                                                      │
│       ▼ super.onDestroy()                                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.3 字段注入的细节

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. 基本用法
    @Inject
    lateinit var markdownRenderer: MarkdownRenderer

    // 2. 如果需要延迟初始化（少见）
    @Inject
    lateinit var lazyService: Lazy<MediaImporter>  // Lazy< T > 延迟访问
    
    // 3. 多实例（通过 Provider）
    @Inject
    lateinit var provider: Provider<NoteDao>  // Provider< T > 按需创建
}
```

**Lazy vs Provider**：

| 类型 | 用途 | 示例 |
|------|------|------|
| `T` | 立即注入 | `@Inject lateinit var renderer: MarkdownRenderer` |
| `Lazy<T>` | 延迟访问（首次访问时才创建） | `@Inject lateinit var service: Lazy<Service>` |
| `Provider<T>` | 多次创建/可选创建 | `@Inject lateinit var provider: Provider<Dao>` |

```kotlin
// Lazy 用法示例
class SomeClass @Inject constructor(
    private val lazyService: Lazy<HeavyService>
) {
    fun doSomething() {
        // 只在首次调用时才创建 HeavyService
        val service = lazyService.get()
        service.perform()
    }
}

// Provider 用法示例
class SomeClass @Inject constructor(
    private val noteDaoProvider: Provider<NoteDao>
) {
    fun optionalOperation(shouldCreate: Boolean) {
        if (shouldCreate) {
            // 每次调用 get() 都创建新实例
            val dao = noteDaoProvider.get()
            dao.deleteAll()
        }
    }
}
```

---

## 7. Module 的两种写法：@Provides vs @Binds

### 7.1 @Provides 详解

#### 7.1.1 基本语法

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MarkdownProviderModule {

    @Provides
    @Singleton
    fun provideMarkwon(@ApplicationContext context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.blockMargin(8)
                }
            })
            .build()
    }
}
```

**@Provides 方法的规则**：
1. 必须位于 `@Module` 注解的 class 或 object 中
2. 必须返回提供类型的实例
3. 方法名可以是任意名称（Hilt 按返回类型匹配）
4. 可以有参数，参数会自动作为依赖解析

#### 7.1.2 参数依赖解析

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NoteDatabaseModule {

    @Provides
    @Singleton
    fun provideRichNoteDatabase(
        @ApplicationContext context: Context  // Hilt 自动提供
    ): RichNoteDatabase {
        return Room.databaseBuilder(
            context,
            RichNoteDatabase::class.java,
            "rich_note.db"
        ).build()
    }

    // 参数 database 由上一个 @Provides 方法提供
    @Provides
    @Singleton
    fun provideNoteDao(database: RichNoteDatabase): NoteDao {
        return database.noteDao()
    }
}
```

**依赖解析流程**：
```
provideRichNoteDatabase 需要 Context
    → 找到 @ApplicationContext，注入 Application Context
    → 创建 RichNoteDatabase

provideNoteDao 需要 RichNoteDatabase
    → 由 provideRichNoteDatabase 提供
    → 创建 NoteDao
```

### 7.2 @Binds 详解

#### 7.2.1 基本语法

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindMediaImporter(impl: AndroidMediaImporter): MediaImporter
}
```

**@Binds 方法的规则**：
1. 方法必须是 `abstract`
2. Module 类必须是 `abstract class`
3. 必须返回接口类型
4. 参数必须是接口的实现类
5. 方法名可以是任意名称（Hilt 按类型匹配）

#### 7.2.2 @Binds vs @Provides 对比

| 特性 | @Binds | @Provides |
|------|--------|-----------|
| Module 类型 | `abstract class` | `object` |
| 方法类型 | `abstract` | 普通函数 |
| 返回值 | 接口类型 | 任意类型 |
| 使用场景 | 接口→实现映射 | 复杂构建逻辑 |
| 编译产物 | Binding | Provider |
| 性能 | 零开销 | 轻微开销 |

**为什么 @Binds 效率更高？**
```kotlin
// @Binds 生成的代码
// 直接返回 impl，无额外处理
fun bindMediaImporter(impl: AndroidMediaImporter): MediaImporter = impl

// @Provides 生成的代码
// 需要调用方法体
fun provideMediaImporter(): MediaImporter = Markwon.builder(context)...build()
```

### 7.3 链式依赖详解

在实际项目中，经常遇到需要从已有实例提取子依赖的场景：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NoteDatabaseModule {

    // 1. 提供 Database
    @Provides
    @Singleton
    fun provideRichNoteDatabase(
        @ApplicationContext context: Context
    ): RichNoteDatabase {
        return Room.databaseBuilder(
            context,
            RichNoteDatabase::class.java,
            "rich_note.db"
        ).build()
    }

    // 2. 从 Database 提取 NoteDao
    @Provides
    @Singleton
    fun provideNoteDao(database: RichNoteDatabase): NoteDao {
        return database.noteDao()
    }

    // 3. 从 Database 提取 NoteAssetDao
    @Provides
    @Singleton
    fun provideNoteAssetDao(database: RichNoteDatabase): NoteAssetDao {
        return database.noteAssetDao()
    }
}
```

**依赖图**：
```
provideRichNoteDatabase(Context)
        │
        ▼
   RichNoteDatabase
        │
        ├──┬──► provideNoteDao()
        │   │       │
        │   │       ▼
        │   │      NoteDao
        │   │
        └──┴──► provideNoteAssetDao()
                │
                ▼
           NoteAssetDao
```

**Hilt 如何处理依赖顺序？**
- Hilt 构建依赖图时使用拓扑排序
- 自动识别 `provideNoteDao` 依赖 `provideRichNoteDatabase`
- 按正确顺序创建实例

### 7.4 同一文件的混合写法

如文档所述，@Binds 和 @Provides 不能放在同一个 Module 中，但可以放在同一文件：

```kotlin
// data/data-note/src/main/kotlin/.../di/NoteDataModule.kt

// 第一部分：@Provides 用于 Database 和 DAO
@Module
@InstallIn(SingletonComponent::class)
object NoteDatabaseModule {

    @Provides
    @Singleton
    fun provideRichNoteDatabase(
        @ApplicationContext context: Context
    ): RichNoteDatabase {
        return Room.databaseBuilder(
            context,
            RichNoteDatabase::class.java,
            "rich_note.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: RichNoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteAssetDao(database: RichNoteDatabase): NoteAssetDao {
        return database.noteAssetDao()
    }
}

// 第二部分：@Binds 用于 Repository
@Module
@InstallIn(SingletonComponent::class)
abstract class NoteRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: RoomNoteRepository): NoteRepository
}
```

**为什么一个文件两个 Module？**
- 关注点分离：`NoteDatabaseModule` 负责数据库相关，`NoteRepositoryModule` 负责 Repository
- 便于维护：Database 变更只改第一个，Repository 变更只改第二个
- 逻辑清晰：@Provides 和 @Binds 本质上是不同的概念

### 7.5 第三方库注入示例

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ThirdPartyModule {

    // 1. 第三方类，无法修改构造函数
    @Provides
    @Singleton
    fun provideMarkwon(@ApplicationContext context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    // 2. 配置类注入
    @Provides
    fun provideAppConfig(@ApplicationContext context: Context): AppConfig {
        return AppConfig.fromAssets(context, "config.json")
    }

    // 3. 多参数配置
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

---

## 8. ViewModel 注入详解

### 8.1 @HiltViewModel 的原理

```kotlin
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {
    // ...
}
```

**@HiltViewModel 生成的代码**：

```kotlin
// Hilt 生成的 Factory 伪代码
@dagger.Module
object Hilt_NoteEditorViewModel {
    @Provides
    fun provideFactory(
        savedStateHandleHolder: SavedStateHandleProvider,
        noteRepository: NoteRepository
    ): NoteEditorViewModel {
        return NoteEditorViewModel(
            savedStateHandle = savedStateHandleHolder.get(),
            noteRepository = noteRepository
        )
    }
}

// SavedStateHandle 的提供机制
@Module
@InstallIn(SingletonComponent::class)
object SavedStateHandleModule {
    @Provides
    fun provideSavedStateHandleProvider(): SavedStateHandleProvider {
        return SavedStateHandleProvider()
    }
}
```

### 8.2 SavedStateHandle 详解

`SavedStateHandle` 是 Hilt 内置支持的类型，用于在 ViewModel 中访问导航参数：

```kotlin
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {
    
    // 从导航参数获取 noteId
    private val noteId: Long = savedStateHandle["noteId"] ?: -1L
    
    // 也可以使用 getLiveData/getFlow
    val noteTitle: String = savedStateHandle.get<String>("noteTitle") ?: ""
    
    // 更新导航参数
    fun updateTitle(newTitle: String) {
        savedStateHandle["noteTitle"] = newTitle
        // Note: 更改不会自动同步到 Navigation，需要配合 saveState 处理
    }
}
```

**SavedStateHandle 与 Navigation 配合**：

```kotlin
// Navigation 定义
@Composable
fun NavHost() {
    NavHost(navController) {
        composable(
            route = "note_editor/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            // SavedStateHandle 会自动从 NavBackStackEntry 获取参数
            NoteEditorScreen()
        }
    }
}

// ViewModel 中使用（无需手动传递）
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,  // 自动携带 noteId
    private val noteRepository: NoteRepository,
) : ViewModel() {
    private val noteId: Long = savedStateHandle["noteId"] ?: -1L
    // 自动获取导航参数，无需额外配置
}
```

### 8.3 hiltViewModel() 详解

```kotlin
@Composable
fun NoteEditorRoute(
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

**hiltViewModel() 的执行流程**：

```
hiltViewModel() 执行流程：

1. 获取当前 Compose 上下文
        │
        ▼
2. 从 LocalContext 获取 NavBackStackEntry
        │（如果没有 Navigation 环境，会抛出异常）
        ▼
3. 获取 NavBackStackEntry 的 ViewModelStoreOwner
        │
        ▼
4. 检查 ViewModelStore 中是否已有 NoteEditorViewModel 实例
        │
        ├──┬── 已有 → 返回缓存实例
        │   │
        └──┴── 没有 → 使用 Hilt 生成的 Factory 创建
                        │
                        ▼
                   Hilt 自动注入：
                   - SavedStateHandle（从 NavBackStackEntry 获取）
                   - NoteRepository（从 SingletonComponent 获取）
                        │
                        ▼
                   返回新创建的 NoteEditorViewModel
```

### 8.4 ViewModel 作用域详解

```kotlin
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    // 这里注入的依赖与 ViewModel 生命周期一致
    // ViewModel 销毁时，依赖也随之释放
    private val noteRepository: NoteRepository,
) : ViewModel() {
    // NoteRepository 是 Singleton，但如果被标记为 @ActivityRetainedScoped，
    // 则会随 ViewModel 一起销毁
}
```

**作用域层级**：

| 依赖作用域 | 实例生命周期 | 适用场景 |
|------------|--------------|----------|
| `@Singleton` | App 存活期间 | Repository、Database、Config |
| `@ActivityRetainedScoped` | Activity 配置变更期间 | ViewModel 需要的数据 |
| `@ViewModelScoped` (Hilt 2.48+) | ViewModel 存活期间 | 临时数据 |

### 8.5 多依赖注入与 @Qualifier 示例

当同一类型（如 `String`）有多个实例需要注入时，必须使用 `@Named` 或自定义 `@Qualifier` 来区分：

```kotlin
// 1. 定义 @Named 提供方式
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Named("appVersion")
    fun provideAppVersion(): String = BuildConfig.VERSION_NAME

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = "https://api.example.com"
}

// 2. 在 ViewModel 中通过 @Named 区分
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // 1. SavedStateHandle - Hilt 内置
    savedStateHandle: SavedStateHandle,
    
    // 2. 自定义 Repository - 通过 @Binds 解析
    private val settingsRepository: SettingsRepository,
    
    // 3. 另一个 Repository
    private val noteRepository: NoteRepository,
    
    // 4. 相同类型需要 @Named 限定符区分
    @Named("appVersion") private val appVersion: String,
) : ViewModel() {
    
    // Hilt 自动解析所有依赖
    // 无需手动创建任何对象
}
```

**自定义 @Qualifier 的写法**（比 @Named 更类型安全）：

```kotlin
// 定义 Qualifier
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

// 使用自定义 Qualifier
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @AppVersion
    fun provideAppVersion(): String = BuildConfig.VERSION_NAME

    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = "https://api.example.com"
}

// ViewModel 中使用
@HiltViewModel
class MyViewModel @Inject constructor(
    @AppVersion private val appVersion: String,
    @BaseUrl private val baseUrl: String,
) : ViewModel()
```

**@Named vs 自定义 @Qualifier**：

| 方式 | 优势 | 劣势 |
|------|------|------|
| `@Named("key")` | 无需定义额外类 | 字符串容易拼错，IDE 无法检查 |
| 自定义 `@Qualifier` | 编译期类型安全，IDE 可跳转 | 需要定义注解类 |
```

---

## 9. 多模块项目中的 Hilt 实践

### 9.1 模块隔离原则

RichEditor 的每个模块只声明自己的依赖，形成自治单元：

```
┌─────────────────────────────────────────────────────────────────┐
│                       app 模块                                  │
│  职责：装配入口                                                  │
│  包含：@HiltAndroidApp + @AndroidEntryPoint MainActivity        │
└─────────────────────────────────────────────────────────────────┘
          │
          │ 依赖声明（通过 Module 里的 @Binds/@Provides）
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      core 模块                                  │
│                                                                  │
│  core-markdown/                                                 │
│  ├── 提供：Markwon、MarkdownRenderer                            │
│  └── DI 文件：MarkdownModule.kt                                 │
│                                                                  │
│  core-datastore/                                                │
│  ├── 提供：DataStore、SettingsRepository                       │
│  └── DI 文件：DataStoreModule.kt                               │
│                                                                  │
│  core-media/                                                    │
│  ├── 提供：MediaImporter                                       │
│  └── DI 文件：MediaModule.kt                                   │
│                                                                  │
│  core-database/                                                 │
│  ├── 提供：RichNoteDatabase                                    │
│  └── DI 文件：DatabaseModule.kt                                │
└─────────────────────────────────────────────────────────────────┘
          │
          │ 依赖声明（@Binds 接口，@Provides 实现）
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      data 模块                                  │
│                                                                  │
│  data-note/                                                     │
│  ├── 提供：NoteDao、NoteAssetDao、NoteRepository              │
│  └── DI 文件：NoteDataModule.kt                                │
└─────────────────────────────────────────────────────────────────┘
          │
          │ 消费依赖（通过 @HiltViewModel）
          ▼
┌─────────────────────────────────────────────────────────────────┐
│                     feature 模块                                │
│                                                                  │
│  feature-note-editor/                                          │
│  ├── 消费：NoteRepository                                       │
│  └── ViewModel：NoteEditorViewModel                            │
│                                                                  │
│  feature-note-list/                                            │
│  ├── 消费：NoteRepository                                       │
│  └── ViewModel：NoteListViewModel                              │
│                                                                  │
│  feature-settings/                                             │
│  ├── 消费：SettingsRepository、NoteRepository                  │
│  └── ViewModel：SettingsViewModel                              │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 跨模块依赖链详解

以 `NoteEditorViewModel` 为例，完整依赖链路：

```
feature-note-editor/NoteEditorViewModel
│
├── 依赖：NoteRepository
│       │
│       └── 由 data-note/NoteRepositoryModule 通过 @Binds 提供
│               │
│               └── 实现：RoomNoteRepository
│                       │
│                       ├── 依赖：NoteDao
│                       │       │
│                       │       └── 由 data-note/NoteDatabaseModule 通过 @Provides 提供
│                       │               │
│                       │               └── 来自：RichNoteDatabase
│                       │                       │
│                       │                       └── 由 data-note/NoteDatabaseModule 通过 @Provides 提供
│                       │                               │
│                       │                               └── 依赖：@ApplicationContext
│                       │                                       │
│                       │                                       └── Hilt 内置提供
│                       │
│                       ├── 依赖：NoteAssetDao
│                       │       │
│                       │       └── 与 NoteDao 同一依赖链
│                       │
│                       └── 依赖：MediaImporter
│                               │
│                               └── 由 core-media/MediaModule 通过 @Binds 提供
│                                       │
│                                       └── 实现：AndroidMediaImporter
│                                               │
│                                               └── 依赖：@ApplicationContext
│                                                       │
│                                                       └── Hilt 内置提供
│
└── 依赖：SavedStateHandle
        │
        └── Hilt 内置提供（从 Navigation 传递）
```

**整个链路中，你只需要写 NoteEditorViewModel 类的声明**：
```kotlin
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel()
```

其余所有对象的创建、依赖解析、缓存管理全部由 Hilt 自动完成。

### 9.3 @EntryPoint 详解（高级用法）

当需要在非 Hilt 管理的代码中访问依赖时，使用 `@EntryPoint`：

```kotlin
// 1. 定义 EntryPoint 接口
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RichNoteEntryPoint {
    fun noteRepository(): NoteRepository
    fun settingsRepository(): SettingsRepository
}

// 2. 在非 Hilt 代码中使用
class LegacyCode {
    fun someMethod(context: Context) {
        // 获取 EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            RichNoteEntryPoint::class.java
        )
        
        // 访问依赖
        val repository = entryPoint.noteRepository()
        repository.getAllNotes()
    }
}
```

**使用场景**：
- 第三方 SDK 的回调
- ContentProvider
- 非 Hilt 管理的类
- 需要动态获取依赖的场景

**为什么需要 EntryPoint？**
- Hilt 管理的类通过 `@AndroidEntryPoint` 自动获得注入
- 非 Hilt 管理的类无法直接访问 DI 容器
- `@EntryPoint` 是访问容器的「安全门禁」

### 9.4 模块间接口隔离

```kotlin
// core/core-media/src/main/kotlin/.../MediaImporter.kt
// 接口定义在 core 模块

interface MediaImporter {
    suspend fun importMedia(uri: Uri): MediaResult
}

// core/core-media/src/main/kotlin/.../AndroidMediaImporter.kt
// 实现也在 core 模块

@Singleton
class AndroidMediaImporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaImporter {
    override suspend fun importMedia(uri: Uri): MediaResult {
        // Android 特定实现
    }
}

// data/data-note/src/main/kotlin/.../RoomNoteRepository.kt
// data 模块依赖接口，不依赖具体实现

@Singleton
class RoomNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteAssetDao: NoteAssetDao,
    private val mediaImporter: MediaImporter,  // 只依赖接口
) : NoteRepository {
    override suspend fun saveNote(note: Note): Long {
        // 使用 mediaImporter，不关心具体实现
    }
}
```

**这种设计的优势**：
- `feature-note-editor` 只需要知道 `NoteRepository` 接口
- `NoteRepository` 只需要知道 `MediaImporter` 接口
- 具体实现可以随时替换（单元测试时替换为 Mock）

---

## 10. 依赖图总览

### 10.1 完整依赖图

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           SingletonComponent                                  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ Providers (@Provides)                                                  │  │
│  │                                                                         │  │
│  │  @ApplicationContext Context ──┬──► Markwon                           │  │
│  │                                 ├──► RichNoteDatabase ──┬──► NoteDao   │  │
│  │                                 │                         └──► NoteAssetDao│  │
│  │                                 ├──► DataStore<Preferences>             │  │
│  │                                 └──► AndroidMediaImporter                 │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ Bindings (@Binds)                                                       │  │
│  │                                                                         │  │
│  │  MediaImporter      ◄─── AndroidMediaImporter                           │  │
│  │  MarkdownRenderer  ◄─── MarkwonMarkdownRenderer ◄── Markwon            │  │
│  │  SettingsRepository ◄─── DataStoreSettingsRepository                     │  │
│  │  NoteRepository    ◄─── RoomNoteRepository                             │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ ViewModels (@HiltViewModel)                                            │  │
│  │                                                                         │  │
│  │  NoteListViewModel                                                     │  │
│  │       └─► NoteRepository                                               │  │
│  │                                                                         │  │
│  │  NoteEditorViewModel                                                  │  │
│  │       ├─► SavedStateHandle                                             │  │
│  │       └─► NoteRepository                                                │  │
│  │                                                                         │  │
│  │  NoteDetailViewModel                                                  │  │
│  │       ├─► SavedStateHandle                                             │  │
│  │       └─► NoteRepository                                                │  │
│  │                                                                         │  │
│  │  SettingsViewModel                                                    │  │
│  │       ├─► SettingsRepository                                            │  │
│  │       └─► NoteRepository                                                │  │
│  │                                                                         │  │
│  │  RecycleBinViewModel                                                  │  │
│  │       └─► NoteRepository                                                │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 10.2 依赖流向图

```
依赖流向：从外部（Context）到内部（ViewModel）

外部依赖
    │
    ├── @ApplicationContext ──────────────────────────────────────┐
    │                                                               │
    └── @Provides 配置                                             │
            │                                                       │
            ▼                                                       │
    ┌─────────────────────────────────────────────────────────────┐ │
    │                     基础设施层                               │ │
    │                                                               │ │
    │   RichNoteDatabase ─────────────────────────────► NoteDao   │ │
    │   RichNoteDatabase ─────────────────────────► NoteAssetDao │ │
    │   DataStore ─────────────────────────────────► Settings    │ │
    │   Markwon ───────────────────────────────────► Renderer     │ │
    │   AndroidMediaImporter ─────────────────────► Importer     │ │
    └─────────────────────────────────────────────────────────────┘ │
            │                                                       │
            │ @Binds 接口绑定                                        │
            ▼                                                       │
    ┌─────────────────────────────────────────────────────────────┐ │
    │                     数据层                                    │ │
    │                                                               │ │
    │   NoteRepository ◄────── RoomNoteRepository ◄─────────────┐  │ │
    │   SettingsRepository ◄── DataStoreSettingsRepository ◄───┘  │ │
    │   MediaImporter ◄──────── AndroidMediaImporter ◄────────┘  │ │
    │   MarkdownRenderer ◄───── MarkwonMarkdownRenderer ◄────────┘  │ │
    └─────────────────────────────────────────────────────────────┘ │
            │                                                       │
            ▼                                                       │
    ┌─────────────────────────────────────────────────────────────┐ │
    │                     视图层                                    │ │
    │                                                               │ │
    │   NoteListViewModel ─────────────────────────────────► 显示便签列表 │ │
    │   NoteEditorViewModel ─────────────────────────────────► 编辑便签  │ │
    │   NoteDetailViewModel ─────────────────────────────────► 查看便签  │ │
    │   SettingsViewModel ───────────────────────────────────► 设置页面 │ │
    │   RecycleBinViewModel ──────────────────────────────────► 回收站    │ │
    └─────────────────────────────────────────────────────────────┘ │
```

---

## 11. 常见问题与排坑

### Q1: 编译报错 "xxx cannot be provided without an @Provides-annotated method"

**详细排查步骤**：

```
Step 1: 检查类型是否有 @Inject 构造函数
    class MyClass @Inject constructor()
        ├── ✓ 有 → 进入 Step 2
        └── ✗ 没有 → 需要添加 @Inject 或提供 @Provides

Step 2: 检查 Module 配置
    @Module
    @InstallIn(SingletonComponent::class)  ← 检查这一行
    object MyModule {
        @Provides
        fun provideMyClass(): MyClass = MyClass()  ← 检查方法注解
    }
        ├── ✓ 配置正确 → 进入 Step 3
        └── ✗ 配置错误 → 修正 Module

Step 3: 检查模块是否正确配置 Hilt
    build.gradle.kts:
        plugins {
            alias(libs.plugins.hilt.android)  ← 检查
        }
        dependencies {
            implementation(libs.hilt.android)  ← 检查
            kapt(libs.hilt.compiler)           ← 检查（必须是 kapt）
        }
        ├── ✓ 配置正确 → 进入 Step 4
        └── ✗ 配置错误 → 修正 build.gradle

Step 4: 检查依赖是否在 classpath 中
    检查模块是否被正确依赖
    app/build.gradle.kts:
        dependencies {
            implementation(project(":data:data-note"))  ← 检查
        }
```

### Q2: 新增模块后注入失败

**Checklist（确保每个模块都包含）**：

```kotlin
// build.gradle.kts (新增模块)
plugins {
    alias(libs.plugins.android.library)  // ✓ 或 android.application
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)       // ✓ 必须
    alias(libs.plugins.hilt.android)      // ✓ 必须
}

android {
    namespace = "com.example.richtext.core.xxx"
}

dependencies {
    implementation(libs.hilt.android)     // ✓ 必须
    kapt(libs.hilt.compiler)              // ✓ 必须（不是 implementation）
}
```

**常见错误**：
- 只加了 `implementation(libs.hilt.android)`，忘记 `kapt(libs.hilt.compiler)`
- 把 `kapt` 写成了 `implementation`
- Module 是 `object` 却没有添加 `@Provides` 方法

### Q3: @Binds 和 @Provides 混用报错

**原因**：Kotlin 不允许 abstract function in non-abstract class

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {  // object = 具体类，不能有 abstract function
    @Binds  // ✗ Binds 要求 abstract function
    abstract fun bindService(impl: ServiceImpl): Service  // 语法错误

    @Provides  // ✓ Provides 可以在 object 中
    fun provideDatabase(): Database = ...
}
```

**解决方案**：同一文件中定义两个 Module：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyProviderModule {  // 放 @Provides
    @Provides
    fun provideDatabase(): Database = ...
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MyBindModule {  // 放 @Binds
    @Binds
    abstract fun bindService(impl: ServiceImpl): Service
}
```

### Q4: ViewModel 中获取导航参数失败

**问题**：`SavedStateHandle["noteId"]` 返回 null

**原因**：Navigation 和 Hilt 的 SavedStateHandle 集成需要额外配置

**解决方案**：

```kotlin
// 1. 确保使用了 hilt-navigation-compose
dependencies {
    implementation(libs.hilt.navigation.compose)
}

// 2. NavHost 必须在 Hilt 管理的 Activity/Fragment 中
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavHost(...)  // ← 使用 Compose Navigation
        }
    }
}

// 3. ViewModel 正确接收 SavedStateHandle
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,  // ← 自动注入
    private val noteRepository: NoteRepository,
) : ViewModel() {
    // noteId 会自动从 Navigation arguments 获取
    private val noteId: Long = savedStateHandle["noteId"] ?: -1L
}
```

### Q5: 为什么所有 Module 都用 SingletonComponent？

**SingletonComponent vs 其他 Component**：

```kotlin
@Module
@InstallIn(SingletonComponent::class)  // ✓ 推荐
object MyModule { ... }

@Module
@InstallIn(ActivityComponent::class)    // 较少使用
object MyModule { ... }
```

| 场景 | 推荐 Component | 原因 |
|------|----------------|------|
| 数据层（Repository、Database） | SingletonComponent | 需要跨 Activity 存活 |
| 配置对象 | SingletonComponent | 全局配置 |
| UI 状态管理器 | ActivityRetainedComponent | 配置变更后仍需存活 |
| Activity 级临时对象 | ActivityComponent | 仅限单个 Activity |

**什么情况下用非 Singleton Component？**
- 需要在 Activity 销毁时清理依赖
- 依赖持有 Activity Context（应避免）
- 需要在每个 Activity 实例化一次

### Q6: kapt vs ksp

**性能对比**：

| 指标 | kapt | ksp |
|------|------|-----|
| 编译速度 | 较慢 | 较快（2-3 倍） |
| 增量编译 | 支持但效率一般 | 原生支持增量编译 |
| 内存占用 | 较高 | 较低 |
| Hilt 支持 | ✓ 完全支持 | ✓ 从 2.48 开始支持 |

**迁移到 KSP**：

```kotlin
// build.gradle.kts (Module level)
plugins {
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)      // 移除
    alias(libs.plugins.kotlin.ksp)        // 添加
}

dependencies {
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)              // 改为 ksp
    ksp(libs.hilt.compiler)               // ✓ 这样写
}
```

**注意**：迁移前检查依赖库的 ksp 支持：
```kotlin
// Room 的 KSP 支持
plugins {
    alias(libs.plugins.room) apply false  // 先在 libs.versions.toml 确认 room-ksp 版本
}
```

### Q7: 循环依赖怎么办

**问题**：A 依赖 B，B 依赖 A

```kotlin
class A @Inject constructor(val b: B)
class B @Inject constructor(val a: A)  // ✗ 循环依赖
```

**解决方案**：

| 方案 | 示例 | 适用场景 |
|------|------|----------|
| 使用 `dagger.Lazy<T>` | `class A @Inject constructor(val b: dagger.Lazy<B>)` | 打破初始化顺序依赖 |
| 提取接口 | `A` 依赖 `BInterface`，`B` 实现 `BInterface` | 循环发生在实现细节 |
| 重新设计 | 将共享依赖提取到 C | 设计问题，需要重构 |

```kotlin
// 方案 1：使用 dagger.Lazy<T> 延迟初始化
class A @Inject constructor(val b: dagger.Lazy<B>) {
    fun doSomething() {
        b.get().method()  // 首次访问时才创建 B
    }
}

// 方案 2：提取接口
interface BInterface {
    fun method()
}

class A @Inject constructor(val bInterface: BInterface)
class B @Inject constructor(val a: A) : BInterface

// 方案 3：引入第三方 C
class A @Inject constructor(val c: C)
class B @Inject constructor(val c: C)
```

### Q8: Scope 与内存泄漏

**常见陷阱**：`@Singleton` 对象持有 Activity Context

```kotlin
// ✗ 危险：Singleton 持有 Activity Context 会导致内存泄漏
@Singleton
class BadService @Inject constructor(
    @ActivityContext private val context: Context  // Activity 销毁后仍被持有！
)

// ✓ 安全：Singleton 使用 Application Context
@Singleton
class GoodService @Inject constructor(
    @ApplicationContext private val context: Context  // Application 生命周期一致
)
```

**规则**：
- `@Singleton` 标记的类只能注入 `@ApplicationContext`
- 需要 `@ActivityContext` 的类不应标记为 `@Singleton`
- 如果确实需要 Activity Context + 长生命周期，考虑使用 `@ActivityRetainedScoped`

**各 Scope 的 Context 使用规则**：

| Scope | 可用 @ApplicationContext | 可用 @ActivityContext |
|-------|--------------------------|----------------------|
| `@Singleton` | ✓ | ✗（内存泄漏） |
| `@ActivityRetainedScoped` | ✓ | ✗（配置变更后无效） |
| `@ActivityScoped` | ✓ | ✓ |
| `@FragmentScoped` | ✓ | ✓ |

### Q9: 如何调试 Hilt 依赖

**方法 1：查看生成的代码**

```
build/generated/hilt/.../
├── component/
│   └── SingletonComponent.java       ← 生成的 Component
├── modules/
│   ├── DataStoreModule_ProvideDataStoreFactory.java
│   └── NoteDatabaseModule_ProvideNoteDaoFactory.java
└── viewModels/
    └── NoteEditorViewModel_HiltComponents.java
```

**方法 2：启用 Hilt 日志**

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            // Hilt 不提供日志 API，但可以通过 Dagger 的 internal 方式查看
        }
        super.onCreate()
    }
}
```

**方法 3：使用 dagger-injector（仅调试用）**

```kotlin
// 调试时获取实例
val component = EntryPointAccessors.fromApplication(
    appContext,
    SomeEntryPoint::class.java
)
val instance = component.getSomething()
```

---

## 12. Hilt 测试支持

Hilt 提供了完善的测试基础设施，可以轻松替换依赖进行单元测试和集成测试。

### 12.1 测试环境配置

```kotlin
// build.gradle.kts (app 或 feature 模块)
dependencies {
    // Hilt 测试库
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    
    // 单元测试
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
}
```

### 12.2 @HiltAndroidTest 基本用法

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NoteRepositoryTest {

    // HiltAndroidRule 负责在测试前初始化 Hilt
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // 注入测试目标
    @Inject
    lateinit var noteRepository: NoteRepository

    @Before
    fun setup() {
        // 必须调用 inject() 完成注入
        hiltRule.inject()
    }

    @Test
    fun testSaveNote() = runTest {
        val note = Note(title = "Test", content = "Hello")
        val id = noteRepository.saveNote(note)
        assertThat(id).isGreaterThan(0)
    }
}
```

### 12.3 @UninstallModules 替换依赖

在测试中替换真实模块为测试用模块：

```kotlin
@HiltAndroidTest
@UninstallModules(NoteRepositoryModule::class)  // 移除生产环境的绑定
class NoteListViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // 提供测试用的 Module
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestNoteRepositoryModule {
        @Binds
        @Singleton
        abstract fun bindNoteRepository(impl: FakeNoteRepository): NoteRepository
    }

    @Inject
    lateinit var noteRepository: NoteRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testNoteList() {
        // noteRepository 此时是 FakeNoteRepository
        assertThat(noteRepository).isInstanceOf(FakeNoteRepository::class.java)
    }
}
```

### 12.4 @BindValue 快速替换单个依赖

当只需替换一个依赖时，`@BindValue` 比 `@UninstallModules` + 新 Module 更简洁：

```kotlin
@HiltAndroidTest
class NoteEditorViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // 直接绑定 Mock 对象，覆盖 NoteRepository 的默认绑定
    @BindValue
    val noteRepository: NoteRepository = FakeNoteRepository()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Test
    fun testViewModel() {
        // ViewModel 使用的是 FakeNoteRepository
    }
}
```

### 12.5 测试中的自定义 TestRunner

```kotlin
// 自定义 TestRunner（在 build.gradle.kts 中配置）
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        testInstrumentationRunner = "com.example.richtext.HiltTestRunner"
    }
}
```

---

## 13. 总结

### 13.1 核心要点回顾

| 层级 | 职责 | Hilt 用法 | 关键文件 |
|------|------|-----------|----------|
| **app** | 装配入口 | `@HiltAndroidApp` + `@AndroidEntryPoint` | RichNoteApplication.kt, MainActivity.kt |
| **core-*** | 提供基础设施 | `@Module` + `@Provides`/`@Binds` + `@Inject constructor` | 各模块的 `di/*.kt` |
| **data-*** | 提供数据层 | `@Module` + `@Provides`(DB/DAO) + `@Binds`(Repository) | NoteDataModule.kt, RoomNoteRepository.kt |
| **feature-*** | 消费依赖 | `@HiltViewModel` + `hiltViewModel()` | 各 ViewModel.kt |

### 13.2 Hilt 使用黄金法则

```
┌─────────────────────────────────────────────────────────────────┐
│                    Hilt 使用黄金法则                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  法则 1：每个模块只管声明「我能提供什么」                         │
│          → 模块自治，不需要汇总                                  │
│                                                                  │
│  法则 2：接口绑定用 @Binds，具体构建用 @Provides                 │
│          → @Binds 用于接口，@Provides 用于复杂配置               │
│                                                                  │
│  法则 3：实现类标注 @Inject constructor + @Singleton            │
│          → 让 Hilt 知道如何创建                                  │
│                                                                  │
│  法则 4：ViewModel 用 @HiltViewModel，Compose 用 hiltViewModel() │
│          → 配套使用，一行获取                                    │
│                                                                  │
│  法则 5：编译期验证 = 运行时零惊吓                                │
│          → 错误在编译时暴露，不是运行时崩溃                       │
│                                                                  │
│  法则 6：使用 @InstallIn 控制作用域                               │
│          → SingletonComponent 最常用                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 13.3 进阶学习路径

| 阶段 | 学习内容 | 推荐资源 |
|------|----------|----------|
| 入门 | 本文内容 + 官方 Codelab | [Hilt 官方文档](https://developer.android.com/training/dependency-injection/hilt-android) |
| 进阶 | @EntryPoint、Component 定制 | Dagger 官方文档 |
| 精通 | 自定义 Scope、SubComponent | Hilt 源码分析 |
| 大师 | 编译插件开发、ASM 字节码 | Dagger 编译期源码 |

### 13.4 Hilt 的价值

Hilt 的价值不在于「让你少写几行代码」，而在于：

1. **依赖关系显式化**：所有依赖在代码中明确定义，不需要追踪 `new` 调用链
2. **依赖关系可追踪**：依赖图在编译期构建完成，可以可视化查看
3. **依赖关系可验证**：编译期检查所有依赖是否满足，错误提前暴露
4. **测试友好**：轻松替换依赖为 Mock，无侵入测试

**当项目膨胀到十几个模块、几十个依赖节点时，这种编译期的确定性就是工程质量的护城河。**

---

> 本文对应项目：[RichEditor](https://github.com/renhui/RichEditor) · Hilt 2.51.1 · Kotlin 1.9.24 · Jetpack Compose
