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

## 使用

### 对于Jetpack Compose

```kotlin
val service = StackbricksService(
    LocalContext.current,
    messageProvider = TODO(),
    packageProvider = TODO(),
    rememberStackbricksStatus(),
    buildConfig = ApplicationBuildConfig(
        versionName = BuildConfig.VERSION_NAME,
        isAllowedToDisableCheckUpdateOnLaunch = false
    )
)
StackbricksComponent(
    service,
    modifier = Modifier,
    trigger = object : StackbricksEventTrigger() {
        override fun onChannelChanged(isTestChannel: Boolean) { }

        override fun onCheckUpdate(isTestChannel: Boolean) { }

        override fun onCheckUpdateOnLaunchChanged(isChecked: Boolean) { }

        override fun onDownloadPackage() { }

        override fun onInstallPackage(isTestChannel: Boolean, versionData: StackbricksVersionData) { }
    }
)
```

## Providers

### `Qiniu`（七牛云）

```kotlin
val qiniuConfiguration = QiniuConfiguration("Your Qiniu CDN Host without http/https")
val messageProvider = QiniuMessageProvider(qiniuConfiguration)
val packageProvider = QiniuPackageProvider(qiniuConfiguration)
```