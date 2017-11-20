# MokaMvvm
基于家里狗狗 摩卡（Mocha）名字命名的mvvm框架



##### 愿景

希望能够摆脱android的DataBinding而实现数据绑定，完成ViewController 和 ViewModel之前的数据绑定



##### 注意

必须使用Kotlin

> 中间有一个环节必须要用到Kotlin的特性，必须支持Java和Kotlin混合开发，如果不知道如何接入Kotlin请移步[Kotlin and Android](https://developer.android.com/kotlin/index.html)



##### 引用

build.gradle中加入下面的引用，version请查看[Release](https://github.com/wanpg/MokaMvvm/releases)

```groovy
    dependencies {
        implementation "org.jetbrains.kotlin:kotlin-stdlib-jre7:$kotlin_version"
        implementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

        implementation 'com.github.wanpg.MokaMvvm:mvvm:version'
        implementation 'com.github.wanpg.MokaMvvm:base:version'
        kapt 'com.github.wanpg.MokaMvvm:compile:version'

        kapt 'com.google.auto.service:auto-service:1.0-rc3'
        kapt 'com.squareup:javapoet:1.9.0'
    }
```


##### 使用

