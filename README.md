# Dubhe Health Bar

纯客户端 Minecraft 血条模组：在生物头顶的 name tag 上方绘制自定义着色器血条，并在生命值变化时于实体头顶生成随机斜抛的绿色/红色数字。

## 支持版本

| 加载器 | Minecraft | 构建模块 | Java |
| --- | --- | --- | --- |
| Forge | 1.20.1 | `forge-1.20.1` | 17 |
| Forge | 1.21 | `forge-1.21.0` | 21 |
| NeoForge | 1.21.8 | `neoforge-1.21.8` | 21 |
| NeoForge | 1.21.10 | `neoforge-1.21.10` | 21 |
| NeoForge | 1.21.11 | `neoforge-1.21.11` | 21 |
| NeoForge | 26.1.2 | `neoforge-26.1.2` | 25 |

每个版本是独立 Gradle 工程，构建产物为对应模块 `build/libs/dubhe_health_bar-1.0.0-mc<版本>.jar`。

## 功能

- 在可见 `LivingEntity` 的 name tag 上方显示圆角着色器血条，背景半透明灰色。
- 友好生物为绿色，中立生物为黄色，敌对生物为红色。
- 血条右侧显示 `当前生命值/最大生命值`。
- 每客户端 tick 缓存生命值并与上一 tick 比较，增加显示绿色 `+xxx`，减少显示红色 `-xxx`。
- 数字使用世界坐标动画与随机水平方向斜抛，在 GUI 层按当前帧相机投影渲染；接近生物脚部 Y 坐标时渐隐。
- 纯客户端，不发送网络包，不注册服务端内容。

## 安装

把与游戏版本和加载器匹配的 jar 放入客户端 `mods` 目录。模组不需要安装在服务端；Forge 1.20.1 与 Forge 1.21.0 的入口只在客户端注册事件，安装在服务端不会提供功能。

## 构建

进入目标模块目录，确认 `JAVA_HOME` 指向该模块要求的 JDK，然后执行：

```powershell
cd dubhe_health_bar\forge-1.20.1
.\gradlew.bat build
```

其他模块同理，把目录替换为 `forge-1.21.0`、`neoforge-1.21.8`、`neoforge-1.21.10`、`neoforge-1.21.11` 或 `neoforge-26.1.2`。首次构建会下载对应 Minecraft 与加载器依赖。

| 模块 | 构建 JDK | Gradle | 额外说明 |
| --- | --- | --- | --- |
| `forge-1.20.1` | 17 | 8.8 | ForgeGradle 6.0.54 |
| `forge-1.21.0` | 21 | 9.8.0 | ForgeGradle 7.0.40；其 Mavenizer 需要 JDK 8 可执行文件在 `PATH` 中 |
| `neoforge-1.21.8` / `1.21.10` / `1.21.11` | 21 | 8.14.3 | ModDevGradle 2.0.148 |
| `neoforge-26.1.2` | 25 | 9.8.0 | ModDevGradle 2.0.148，编译目标 Java 25 |

## 实现说明

血条由自定义 GLSL 着色器输出圆角背景与按血量比例裁剪的阵营色填充，顶点格式为 `POSITION_COLOR_TEX`，通过 `RenderType` 或 `RenderPipeline` 接入所在版本的渲染管线。NeoForge 1.21.8 及以上版本通过 mixin 把实体数据附加到渲染状态，避免在渲染事件中反查实体。

伤害数字在 GUI 阶段按世界坐标重新投影。位置以 `double` 保存并按动画时间推进，不使用实体逐 tick 位置做屏幕坐标插值，从而避免大坐标下的精度抖动和实体移动造成的数字乱晃。数字接近生成时记录的脚部 Y 坐标时降低 ARGB alpha，实现脚部渐隐。

## 已知限制

- 血条通过 `LivingEntityRenderer` 的渲染入口绘制。末影龙使用独立的 `EnderDragonRenderer`，不经过该入口，因此不会显示血条；玩家、凋灵、普通怪物、动物与村民等正常覆盖。
- 构建产物尚未在真实客户端中启动验证。着色器已用 `glslangValidator` 做语法检查，Mixin 目标签名与编译后的 class 已做静态核对；实际画面、深度遮挡与模组加载行为需要在测试客户端确认。

## 协议

Java 代码以 [LGPL-3.0-only](./LICENSE) 发布。贴图、着色器、语言文件等资源以 [All Rights Reserved](./LICENSE_ASSETS) 发布，未经书面许可不得复制、修改或再分发。
