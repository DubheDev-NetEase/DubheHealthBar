# Dubhe Health Bar 多版本实现规范

本文是各版本模块的唯一实现依据。所有模块必须按本文实现同一套视觉效果与行为，只在 Minecraft 与加载器 API 上做版本适配。

## 目标与版本矩阵

模组名称 `Dubhe Health Bar`，模组 id `dubhe_health_bar`，包名 `dev.dubhe.healthbar`，作者 `Gugle`，版本 `1.0.0`。代码协议 LGPL-3.0-only，资源与着色器协议 ARR。

| 模块目录 | 加载器 | Minecraft | 加载器版本 | Java | 资源包格式 |
| --- | --- | --- | --- | --- | --- |
| `forge-1.20.1` | Forge | 1.20.1 | 47.4.29 | 17 | 15 |
| `forge-1.21.0` | Forge | 1.21 | 51.0.33 | 21 | 34 |
| `neoforge-1.21.8` | NeoForge | 1.21.8 | 21.8.53 | 21 | 64 |
| `neoforge-1.21.10` | NeoForge | 1.21.10 | 21.10.64 | 21 | 69 |
| `neoforge-1.21.11` | NeoForge | 1.21.11 | 21.11.48 | 21 | 75 |
| `neoforge-26.1.2` | NeoForge | 26.1.2 | 26.1.2.126 | 25 | 84 |

每个模块是独立 Gradle 工程，都位于 `C:\Projects\self\netease\DubheHealthBar\<模块目录>`。不要与其他模块共享 Gradle 配置，不要修改仓库根文件。

## 功能需求

1. 在每一个可见的 `LivingEntity` 头顶，name tag 上方显示一个世界空间血条。背景为半透明灰色，血量填充按阵营着色：友好绿色、中立黄色、敌对红色。血条右侧显示 `当前生命值/最大生命值` 文本。
2. 每客户端 tick 缓存生物生命值，与上一 tick 比较。增加时在实体头顶生成绿色 `+xxx`，减少时生成红色 `-xxx`。数字以随机方向斜抛并在 GUI 层渲染，接近生物脚部 Y 坐标时渐隐。
3. 纯客户端模组，不发送网络包，不注册服务端内容，不在服务端加载客户端类。

## 视觉规范

所有长度单位分为两类：世界空间血条使用“像素后缩放”布局，等级与 name tag 一致；GUI 数字使用 GUI 缩放坐标。

```text
血条宽 40 像素，高 4 像素，整体缩放 0.025（与 name tag 的 0.025 一致）
血条与右侧文本间距 3 像素
血条圆角半径 1.2 像素，抗锯齿宽度 0.75 像素
血条背景色 0x99202020（半透明灰色）
友好颜色 0xFF3CCB5A
中立颜色 0xFFE8C33A
敌对颜色 0xFFE04747
生命值文本颜色 0xFFFFFFFF，带阴影
```

血条位置：取 render state 的 `nameTagAttachment`；为空时使用 `(0, boundingBoxHeight + 0.5, 0)`。当 `nameTag` 非空时，血条中心在附着点上方 `0.23` 格；否则在附着点上方 `0.12` 格。name tag 不可见的生物也要显示血条，此时按“无 name tag”规则处理。相机实体自身不显示血条。

血条布局以实体头顶中点为水平中心：总宽度 = `40 + 3 + font.width(生命值文本)` 像素，血条从 `-total/2` 开始，文本紧随血条右侧。文本垂直居中，使用 `y = -4` 作为文本顶部（字体行高 9 像素）。

生命值文本的格式：数值为整数时直接显示整数，否则保留一位小数（例如 `18.5`）。显示 `当前/最大`，当前生命值按 `0..max` 截断，最大生命值为 0 时不渲染血条。

## 数值与动画规范

伤害数字在客户端 tick 中生成，世界坐标动画，GUI 投影渲染。

```text
生命周期 1.1 秒
初始竖直速度 0.16..0.21 格/tick
水平速度 0.035..0.065 格/tick，方向随机（0..2π）
重力 0.02 格/tick²
生成位置：实体 X/Z 加 ±0.15 随机偏移，Y = entity.getY() + entity.getBbHeight() + 0.25
脚部 Y：entity.getY()
最近 25% 生命周期线性淡出
脚部淡出：alpha *= clamp((y - feetY) / 0.55, 0, 1)
同时生效取较小值
```

位置计算使用秒数换算 tick：`tick = seconds * 20`，`y = startY + vy * tick - 0.5 * 0.02 * tick * tick`。所有世界坐标使用 `double` 保存与计算，只在传给投影 API 时转为 `Vec3` 或 `float`。这样数字在屏幕上的运动不受实体 tick 位置跳变影响，也不会因浮点精度在大坐标下抖动。

数字文本为 `+` 或 `-` 加数值，增加为绿色 `0xFF55FF55`，减少为红色 `0xFFFF5555`。屏幕上水平居中于投影点，文本顶部相对投影点偏移 `-4` 像素。投影点位于相机后方或屏幕外（留 32 像素边距）时跳过。淡出通过 ARGB 颜色的 alpha 通道实现，禁止通过修改字体透明度实现。

活动数字最多保留 256 条，超出时移除最早生成的数字。世界切换或断开连接时清空数字和生命值缓存。

## 行为细节

客户端 tick 处理：

1. `Minecraft.getInstance().level` 为空时清空缓存并返回。
2. level 实例变化时清空生命值缓存与数字列表，并记录新 level。
3. 遍历 `level.entitiesForRendering()`，只处理 `LivingEntity`，跳过相机实体、已死亡实体与 `getMaxHealth() <= 0` 的实体。
4. 以 `UUID` 为键缓存生命值。第一次看到的实体只记录，不生成数字。
5. 生命值差值的绝对值小于 `0.01` 时忽略；否则生成数字，数值取差值的绝对值，颜色由增减决定。
6. 实体从渲染集合消失时移除对应缓存项，防止实体 id/uuid 复用导致误判。
7. 生命值从有到无（死亡）时按 `0 - 上一生命值` 生成减少数字，数字使用生成时的位置与脚部 Y。

血条数据每帧读取实体的当前生命值和最大生命值，不依赖 tick 缓存。

阵营判定，按优先级：

```text
敌对：entity instanceof Enemy 或 entity.getType().getCategory() == MobCategory.MONSTER
中立：entity instanceof NeutralMob
友好：其余
```

若某版本映射改了 `Enemy`、`NeutralMob`、`MobCategory` 的包名或方法名，按该版本源码修正，语义保持不变。玩家、村民、动物与 Boss 必须得到正确颜色；敌对 Boss（凋灵、末影龙等）为红色。

## 类结构

每模块至少包含以下职责，类名保持一致，包名可按版本调整：

```text
dev.dubhe.healthbar.DubheHealthBar                模组入口与常量
dev.dubhe.healthbar.HealthTracker                 客户端 tick、生命值缓存、数字列表
dev.dubhe.healthbar.FloatingNumber                单条数字动画数据与 alpha 计算
dev.dubhe.healthbar.HealthBarKind                 阵营枚举与颜色
dev.dubhe.healthbar.client.HealthBarRenderer      世界空间血条与文本渲染
dev.dubhe.healthbar.client.FloatingNumberRenderer GUI 数字渲染与投影
dev.dubhe.healthbar.client.ClientTickEvents       tick 事件入口
dev.dubhe.healthbar.client.GuiEvents              GUI 事件入口
dev.dubhe.healthbar.render.HealthBarStyle         布局、颜色、文本格式常量与工具
dev.dubhe.healthbar.render.HealthBarRenderTypes   着色器注册与 RenderType 创建
```

NeoForge 1.21.8/1.21.10/1.21.11/26.1.2 额外需要：

```text
dev.dubhe.healthbar.mixin.LivingEntityRendererMixin    在 extractRenderState 末尾绑定数据
dev.dubhe.healthbar.mixin.LivingEntityRenderStateMixin 用 @Unique 字段实现数据接口
dev.dubhe.healthbar.mixin.HealthBarRenderState         渲染状态数据接口
```

接口方法统一使用 `dubhe$` 前缀，例如 `dubhe$getHealth`、`dubhe$setHealth`、`dubhe$getMaxHealth`、`dubhe$getKind`、`dubhe$isVisible`。

Forge 1.20.1 与 Forge 1.21.0 不需要 mixin：`RenderLivingEvent.Post` 直接提供实体。

## 渲染实现要求

### 世界空间血条

血条必须使用自定义着色器绘制，禁止用贴图或 GUI 矩形冒充。

1. 构建 `RenderType`，顶点格式使用 `POSITION_COLOR_TEX` 或该版本属性集合相同的官方常量（例如 `POSITION_TEX_COLOR`），绘制模式 `QUADS`。
2. 顶点 Position 使用血条局部像素坐标：x 从 `-20` 到 `20`，y 从 `-2` 到 `2`；UV0 携带局部像素坐标 `(Position.x, Position.y)`；Color 的 RGB 为阵营颜色，Color 的 alpha 为血量比例（四个顶点相同）。由于 `VertexConsumer` 会把 PoseStack 矩阵烘焙进 Position，着色器不能从 Position 取局部坐标，必须使用 UV0。
3. 在实体头顶位置执行 `pushPose`、`translate(0, 血条高度偏移, 0)`、`mulPose(camera.rotation())`、`mulPose(Axis.YP.rotationDegrees(180))`、`scale(-0.025, -0.025, 0.025)`。
4. 血条与文本在同一坐标系下绘制，文本使用 `Font` 渲染，文本位置按像素计算。
5. `RenderType` 的深度测试使用 `LEQUAL_DEPTH_TEST`，关闭深度写入，关闭背面剔除，半透明混合。血条应被前方方块遮挡。
6. 着色器必须同时输出灰色背景与阵营颜色填充，填充宽度随血量比例变化，边缘做圆角与抗锯齿。
7. 每个模块必须通过 `Util.getMillis()` 记录数字生成时间，GUI 渲染时按当前毫秒计算位置与 alpha；禁止在客户端 tick 中累加动画时间后供 GUI 每帧读取，否则会出现 20Hz 阶梯抖动。
8. 血条布局以实体头顶中点为水平中心，血条在左、文本在右；文本与血条整体居中。

顶点着色器逻辑（现代版本，1.21.8 使用 `#version 150`，1.21.10 及以上使用 `#version 330`）：

```glsl
#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec3 fillColor;
out vec2 localPos;
out float fillProgress;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    fillColor = Color.rgb;
    localPos = UV0;
    fillProgress = Color.a;
}
```

片元着色器使用 `localPos` 与 `fillProgress`：

```glsl
#version 330

in vec3 fillColor;
in vec2 localPos;
in float fillProgress;

out vec4 fragColor;

const vec2 HALF_SIZE = vec2(20.0, 2.0);
const float RADIUS = 1.2;
const float EDGE = 0.75;

float roundedBox(vec2 point, vec2 halfSize, float radius) {
    vec2 distance = abs(point) - halfSize + radius;
    return min(max(distance.x, distance.y), 0.0) + length(max(distance, 0.0)) - radius;
}

void main() {
    float backgroundMask = 1.0 - smoothstep(-EDGE, EDGE, roundedBox(localPos, HALF_SIZE, RADIUS));
    if (backgroundMask <= 0.0) {
        discard;
    }

    float fillMask = 0.0;
    if (fillProgress > 0.0) {
        float fillHalfWidth = HALF_SIZE.x * fillProgress;
        vec2 fillCenter = vec2(-HALF_SIZE.x + fillHalfWidth, 0.0);
        vec2 fillPoint = localPos - fillCenter;
        float innerRadius = max(RADIUS - 0.8, 0.0);
        fillMask = 1.0 - smoothstep(-EDGE, EDGE, roundedBox(fillPoint, vec2(fillHalfWidth, HALF_SIZE.y - 0.8), innerRadius));
    }

    vec3 backgroundColor = vec3(0.125, 0.125, 0.125);
    vec3 color = mix(backgroundColor, fillColor, fillMask);
    float alpha = mix(0.60, 0.92, fillMask) * backgroundMask;
    fragColor = vec4(color, alpha);
}
```

1.20.1 与 1.21.0 使用 `ShaderInstance` 与 shader JSON（`assets/dubhe_health_bar/shaders/core/health_bar.json`、`health_bar.vsh`、`health_bar.fsh`）。JSON 的属性为 `Position`、`Color`、`UV0`，uniform 包含 `ModelViewMat` 与 `ProjMat`，不声明 sampler。GLSL 使用 `#version 150` 和显式 uniform：

```glsl
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
```

顶点着色器 `gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);`，并输出 `localPos = UV0`、`fillProgress = Color.a`、`fillColor = Color.rgb`。片元着色器使用上面的圆角逻辑并输出 `fragColor`，不需要 `ColorModulator`。

### 各版本 RenderType 与着色器注册

- Forge 1.20.1/1.21.0：监听 `RegisterShadersEvent`，用 `ShaderInstance(resourceProvider, 名称, DefaultVertexFormat.POSITION_COLOR_TEX)` 创建实例并保存；`RenderType.create(...)` 的 `CompositeState` 使用该 shader 的 `ShaderStateShard`，开启 `TRANSLUCENT_TRANSPARENCY`、`LEQUAL_DEPTH_TEST`、`NO_CULL`，关闭深度写入。
- NeoForge 1.21.8：监听 `RegisterRenderPipelinesEvent`，用 `RenderPipelines.MATRICES_PROJECTION_SNIPPET` 构建自定义 `RenderPipeline`，shader 位置为 `dubhe_health_bar:core/health_bar`，顶点格式 `POSITION_COLOR_TEX`，`QUADS`，半透明混合，关闭剔除与深度写入，深度测试 `LEQUAL_DEPTH_TEST`。用该版本的旧式 `RenderType.create` 接收 pipeline 与 CompositeState。
- NeoForge 1.21.10/1.21.11/26.1.2：同样监听 `RegisterRenderPipelinesEvent`，用 `RenderSetup.builder(pipeline).createRenderSetup()` 生成 setup，再 `RenderType.create("dubhe_health_bar_bar", setup)`。世界空间几何通过 `SubmitNodeCollector.submitCustomGeometry(poseStack, renderType, callback)` 提交。
- 着色器注册事件属于 mod 事件总线；渲染与 tick 事件属于游戏事件总线。
- 着色器未加载或 pipeline 未注册时跳过血条渲染，不要抛异常。

### 世界空间文本

- Forge 1.20.1/1.21.0/NeoForge 1.21.8：`Font.drawInBatch(...)`，使用当前 PoseStack 的 `Matrix4f`、事件的 `MultiBufferSource`、`Font.DisplayMode.NORMAL`、`LightTexture.FULL_BRIGHT`、阴影开启。
- NeoForge 1.21.10/1.21.11/26.1.2：`SubmitNodeCollector.submitText(...)`，文本为 `FormattedCharSequence.forward(text, Style.EMPTY)`，`dropShadow = true`，`DisplayMode.NORMAL`，packedLight 为 `LightTexture.FULL_BRIGHT`，背景色与描边色为 0。
- 文本与血条在同一个 PoseStack 缩放下绘制。

### GUI 数字

- Forge 1.20.1/1.21.0：监听 `RenderGuiEvent.Post`，使用 `GuiGraphics.drawString(font, text, x, y, color, true)`。
- NeoForge 1.21.8/1.21.10/1.21.11：同样监听 `RenderGuiEvent.Post`，使用 `GuiGraphics.drawString(...)`。
- NeoForge 26.1.2：`RenderGuiEvent.Post` 提供 `GuiGraphicsExtractor`，使用 `text(font, text, x, y, color, true)`。
- 投影优先使用 `Minecraft.getInstance().gameRenderer.projectPointToScreen(Vec3)`：它返回 NDC 坐标，屏幕 X = `(ndc.x * 0.5 + 0.5) * guiWidth`，屏幕 Y = `(1 - (ndc.y * 0.5 + 0.5)) * guiHeight`。使用前检查坐标有限、`ndc.z` 在 `[-1, 1]` 内，并用相机前向量判断点在相机前方。
- Forge 1.20.1/1.21.0 若没有 `projectPointToScreen`，在 `RenderLevelStageEvent`（`AFTER_LEVEL`）缓存当帧的投影矩阵、相机旋转与相机位置，GUI 事件中使用缓存值完成同样的矩阵计算。禁止在 GUI 阶段使用 `RenderSystem.getProjectionMatrix()` 作为世界投影，该阶段通常是 GUI 正交矩阵。
- 数字使用默认 1.0 GUI 缩放，整数屏幕坐标四舍五入。不要对世界坐标做 tick 插值后再投影，数字的位置由自身动画时间决定。

## 元数据与资源

- Forge 模块使用 `src/main/resources/META-INF/mods.toml`，`displayTest = "IGNORE_ALL_VERSION"`，forge 与 minecraft 依赖 `side = "CLIENT"`，版本范围已在构建脚手架中设置。
- NeoForge 模块使用 `src/main/resources/META-INF/neoforge.mods.toml`，`[[mixins]] config = "dubhe_health_bar.mixins.json"`，neoforge 与 minecraft 依赖 `side = "CLIENT"`。
- `pack.mcmeta` 已按上表设置资源包格式。
- 着色器文件放在 `src/main/resources/assets/dubhe_health_bar/shaders/core/`。
- 不要添加图标、语言文件、配置文件或未要求的功能。纯客户端模组不要注册网络通道。

## 项目约束

- 简体中文注释与文档；公开类型写 Javadoc 简介，内部实现按需注释，禁止行尾注释，禁止注释掉的代码，禁止占位代码与 `TODO`。
- `if`/`else`/`for`/`while` 必须加大括号；整数常量不写 `.0`。
- 禁止反射、`@SuppressWarnings` 绕过检查、空 catch 块；异常必须记录或向上抛出。
- 只修改本模块目录；不要修改根目录文件或其他模块。
- 不要执行 `git` 写操作，由 Lead 统一提交。
- 不要运行游戏客户端或打开图形界面，只做编译与 jar 内容检查。

## 构建与验证

构建必须使用共享锁脚本，避免并发 Gradle 构建耗尽内存：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\Projects\self\netease\.dsh\temp\gradle-lock.ps1 -Module <模块目录> -Tasks build
```

锁脚本会按模块 `gradle.properties` 中的 `java_version` 选择 JDK，并串行执行构建。首次构建可能下载 Minecraft 与加载器依赖，耗时较长。

每个模块交付前必须：

1. `build` 成功，生成 `build/libs/dubhe_health_bar-1.0.0-mc<版本>.jar`。
2. 用 `jar tf` 确认 jar 包含模组元数据、`pack.mcmeta`、着色器文件、全部类与 mixin 配置。
3. 确认没有服务端类被主入口静态引用（Forge 模块）。
4. 报告构建命令、耗时、jar 路径与任何未完成的验证项。
