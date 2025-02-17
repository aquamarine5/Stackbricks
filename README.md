# Stackbricks

[![wakatime](https://wakatime.com/badge/github/aquamarine5/Stackbricks.svg)](https://wakatime.com/badge/github/aquamarine5/Stackbricks)

## 引入项目

### 添加Github Packages源

> [!NOTE]
> 建议遵循[Github官方文档](https://docs.github.com/zh/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)进行部署。

- 在项目根目录的`setting.gradle`文件中添加如下内容：
```groovy
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/aquamarine5/Stackbricks")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GPR_USERNAME")
                password = project.findProperty("gpr.key") ?: System.getenv("GPR_TOKEN")
            }
        }
    }
}
```
> [!WARNING]
> 请确保`gpr.user`和`gpr.key`的值已经在`gradle.properties`配置，或通过环境变量配置。

### 添加依赖

```groovy
dependencies {
    implementation 'org.aquamarine5.brainspark:stackbricks:$latest_version'
}
```

- 最新版本可以在[Github Packages源](https://github.com/aquamarine5/Stackbricks/packages/2402860)查询。

## 使用Gradle插件

- 使用[stackbricks-gradle-plugin](https://github.com/aquamarine5/stackbricks-gradle-plugin)来一键式设置Stackbricks在服务器上的配置。
```groovy
plugins {
    id "org.aquamarine5.brainspark.stackbricks-gradle-plugin" version "$latest_sgp_version"
}
```

> [!WARNING]
> `$latest_sgp_version`和`$latest_version`并不同步。

> [!TIPS]
> Github Packages源可以设置为`uri("https://maven.pkg.github.com/aquamarine5/*")`从而引入所有和Stackbricks相关的库。

## 使用

### 对于Jetpack Compose

- 推荐使用`StackbricksStateService`，通过`StackbricksStateService`的`state`属性获取状态。
- 使用`StackbricksComponent()`渲染Compose组件。

```kotlin
val messageProvider: StackbricksMessageProvider = TODO()
val packageProvider: StackbricksPackageProvider = TODO()
val stackbricksState = rememberStackbricksStatus()
StackbricksComponent(
    StackbricksStateService(
        LocalContext.current,
        messageProvider,
        packageProvider,
        stackbricksState
    )
)
```

### 对于其他框架

- 使用`StackbricksService`获取服务。

## 实现逻辑

### `.getLatestPackageInfo()`

- 通过`StackbricksPackageProvider`获取最新的包信息（`messageProvider.getLatestVersionData()`），返回一个`StackbricksVersionData`接口类。

### `.isNeedUpdate()`

- 进行版本比较，判断是否需要更新。

### `.downloadPackage(StackbricksVersionData, MutableFloatState?)`

- 下载安装包，下载进度将通过`MutableFloatState`传递。

### `.installPackage()`

- 发送`Intent`，调用`FileProvider`安装安装包。

## Providers

### `Qiniu`（七牛云）

```kotlin
val qiniuConfiguration = QiniuConfiguration("Your Qiniu CDN Host without http/https")
val messageProvider = QiniuMessageProvider(qiniuConfiguration)
val packageProvider = QiniuPackageProvider(qiniuConfiguration)
```