# Dubhe Health Bar

纯客户端 Minecraft 血条模组，本分支对应 NeoForge 26.1.2。

## 功能

- 在生物头顶 name tag 上方用自定义着色器绘制圆角血条，背景半透明灰色，友好绿色、中立黄色、敌对红色。
- 血条右侧显示当前生命值/最大生命值。
- 每客户端 tick 缓存生命值，变化时生成随机斜抛的绿色/红色数字，GUI 投影渲染并在接近脚部时渐隐。
- 纯客户端，不需要安装在服务端。

## 构建

需要 JDK 25，使用 Gradle 9.8。

```powershell
.\gradlew.bat build
```

产物：`build/libs/dubhe_health_bar-1.0.0-mc26.1.2.jar`。

## 其他版本

[releases/1.20.1](https://github.com/DubheDev-NetEase/DubheHealthBar/tree/releases/1.20.1)、[releases/1.21.0](https://github.com/DubheDev-NetEase/DubheHealthBar/tree/releases/1.21.0)、[releases/1.21.8](https://github.com/DubheDev-NetEase/DubheHealthBar/tree/releases/1.21.8)、[releases/1.21.10](https://github.com/DubheDev-NetEase/DubheHealthBar/tree/releases/1.21.10)、[releases/1.21.11](https://github.com/DubheDev-NetEase/DubheHealthBar/tree/releases/1.21.11)

## 协议

Java 代码以 [LGPL-3.0-only](./LICENSE) 发布，贴图、着色器等资源以 [All Rights Reserved](./LICENSE_ASSETS) 发布。