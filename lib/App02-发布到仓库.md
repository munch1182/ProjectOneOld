# 发布到仓库

### 1. 配置发布

```java
apply plugin: 'maven-publish'

//生成sources jar
task generateSourcesJar(type: Jar) {
    from android.sourceSets.main.java.srcDirs
    classifier 'sources'
}

//必须在此生命周期内才能使用components.release
afterEvaluate {
    publishing {
        publications {
            release(MavenPublication) {
                //发布buildTypes中的release的aar文件
                from components.release
                groupId "com.munch.lib"
                artifactId "lib"
                version "0.1.0"
                //发布的文件带SourcesJar
                artifact generateSourcesJar
            }
        }
        repositories {
            maven {
                //发布到本地路径
                url uri('../repository')
            }
        }
    }
}
```
### 2. 使用
1. 引用本地仓库位置，位置在build.gradle的allprojects或者settings.gradle
```java
    repositories {
        maven {
            url 'file://...'
        }
    }
```
2. 使用时按正常的库引入即可

>>>
参考文档
- [使用 Maven Publish 插件](https://developer.android.com/studio/build/maven-publish-plugin)
- [Gradle 插件上传 Maven 库配置详解](https://blog.csdn.net/u011578734/article/details/114104495)
- [Gradle 7.X Maven publish 适配](http://i.lckiss.com/?p=7351)