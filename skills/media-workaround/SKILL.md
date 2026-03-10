# media-workaround Skill

## 触发条件

当用户要求发送本地媒体文件（图片、视频、音频等），且遇到以下错误时：

```
LocalMediaAccessError: Local media path is not under an allowed directory
```

## 解决方案

**必须先将文件复制到工作目录的 `tmp/` 文件夹，然后再发送。**

### 标准流程

1. 复制文件到工作目录：
   ```bash
   cp /path/to/original/file.ext ~/.openclaw/workspace/tmp/file.ext
   ```

2. 使用工作目录路径发送：
   ```
   filePath: ~/.openclaw/workspace/tmp/file.ext
   ```

### 代码示例

```javascript
// 错误做法 - 直接使用原始路径
message({
  filePath: "/Users/yunsan/Desktop/a.png"  // ❌ 会报错
})

// 正确做法 - 先复制到 tmp 目录
// 步骤1: exec 复制文件
exec({ command: "cp /Users/yunsan/Desktop/a.png ~/.openclaw/workspace/tmp/a.png" })

// 步骤2: 使用 tmp 路径发送
message({
  filePath: "~/.openclaw/workspace/tmp/a.png"  // ✅ 正确
})
```

## 原因

OpenClaw 的 `mediaLocalRoots` 配置限制了可访问的本地路径。虽然配置中包含了：
- `/tmp`
- `~/.openclaw/workspace`
- `/Users/yunsan/Desktop`

但 Desktop 路径可能因为权限或路径展开问题（`~` vs 完整路径）导致失败。工作目录路径 `~/.openclaw/workspace` 是最可靠的。

## 目录结构

```
~/.openclaw/workspace/
├── tmp/           # 临时媒体文件存放处
│   ├── images/    # 可选：按类型分类
│   ├── videos/
│   └── ...
├── skills/
│   └── media-workaround/
│       └── SKILL.md
└── ...
```

## 注意事项

- `tmp/` 目录下的文件是临时的，可以定期清理
- 发送完成后不需要立即删除，可以保留一段时间
- 如果文件名可能冲突，使用时间戳或随机字符串重命名

## 自动化

未来可以创建一个脚本自动处理这个流程，但目前手动两步操作即可。