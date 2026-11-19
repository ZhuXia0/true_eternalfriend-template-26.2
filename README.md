# True_EternalFriend

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

## 像素画生成器

打开 `tools/pixel-art-generator.html` 即可使用独立的像素画工作台，不需要启动 Minecraft 或安装额外依赖。工具支持 16、32、64、96、128 像素画布，提供手绘和灵感生成模式，并可导出 PNG。

### JSON 数据格式

导出的 JSON 可被其他工具直接读取，结构如下：

```json
{
  "format": "pixel-art-studio",
  "version": 1,
  "width": 32,
  "height": 32,
  "palette": ["#c4f36b"],
  "pixels": [[null, "#c4f36b"]]
}
```

`pixels[y][x]` 是像素颜色（六位十六进制颜色或 `null`）。因此后续可以很容易接入资源生成脚本、网页展示或 Minecraft 材质转换流程。
