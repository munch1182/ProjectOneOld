---
    html:
        toc: true
---
# P2

## TODO
- [ ] 使用插件即自动依赖声明
- [x] 方法耗时测量
    - [x] 基本实现
    - [ ] 将输出方法交由App实现(ksp)
    - [ ] 测量的注解可以放在类上
    - [ ] 耗时判断
- [x] 方法调用顺序 (ASM不会访问其父类的方法, 即只会访问类下显示方法)
    - [x] 方法调用
    - [ ] 输出方法栈
    - [ ] 方法的注解可以放在类上 
- [ ] 运行日志生成
    - 关键参数, 关键方法
    - 日志压缩加密

## GRADLE插件开发

时间 : 2022.9.8 
gradle版本: 7.3.3 

### 插件结构

单独项目作为插件:
1. 单独项目是一个Java项目
2. 需要配置插件库,明确插件实现类

### 插件生成
##### 使用GradlePlugin
 - 配置`gradlePlugin`
    ```groovy
    group '[groupName]' //必须声明, 否则会使用项目文件名

    gradlePlugin {
        plugins {
            taskPluginAndroid { //自定义任务名
                id = '[pluginName]'
                version = '[pluginVersion]'
                implementationClass = '[pluginImpClass]'
            }
        }
    }
    ```
    此配置**使用任务`publishPluginMavenPublicationToMaven~`会生成`[groupName]:[libName]:[version]`的文件**, 其中`libName`为插件项目的名称,目前未发现更改方法.  

    然后**使用`publish[taskName]PluginMarkerMavenPublicationToMaven~`会生成
    `[pluginName]:[pluginName].gradle.plugin:[version]`
    的文件**, 但文件中没有`jar`文件,只有`pom`文件,其中依赖了`[groupName]:[libName]:[version]`的项目.  

    使用`publish`会同时生成所有需要生成的任务.  

 - 使用  

    1. 在`project`中的`build.gradle`加入依赖地址
        ```groovy
        buildscript {
            dependencies {
                classpath '[groupName]:[libName]:[version]'
            }
        }
        ```
    2. 在需要的模块中使用插件
        ```groovy
        plugins {
            id '[pluginName]'
        }
        ```

##### 使用Publishing

直接生成`[pluginName]:[pluginName].gradle.plugin:[version]`然后引入插件

 - 配置`publishing`
    ```groovy
    gradlePlugin {
        plugins {
            taskPluginAndroid { 
                id = '[pluginName]'
                implementationClass = '[pluginImpClass]'
            }
        }
    }
    publishing {
        publications {
            release(MavenPublication) {
                groupId = '[pluginName]'
                artifactId = '[pluginName].gradle.plugin'
                version = [version]

                from components.java
            }
        }
    }
    ```
    **执行`publishReleasePublicationToMaven~`会生成`[groupId]:[artifactId]:[version]`的文件.**      

    此种方法仍要配置`gradlePlugin`以声明插件的实现类, 否则则要使用`META-INF`
    ```text
    //文件地址
    src/main/resources/META-INF/gradle-plugins/[pluginName].properties

    implementation-class=[pluginImpClass]
    ```
 - 使用  
   直接在需要的模块中使用插件, 但是要带上版本
   ```groovy
   plugins {
        id '[pluginName]' version '[version]'
    }
   ```
    此种使用会自动依赖`[pluginName]:[pluginName].gradle.plugin:[version]`的项目, 所以无需添加依赖地址. 

相关链接:
[gradle官网使用独立项目作为插件](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:custom_plugins_standalone_project)