---
name: git-workflow
description: 安全可控的 Git 工作流自动化。支持：分支创建、提交、rebase、push。触发词：git push、git commit、创建分支、推送代码、推送到远程、推送到远程仓库、推送仓库、git workflow、提交并推送。每个关键步骤需用户确认，token 隐藏显示。
---

# Git Workflow

安全、交互式的 Git 工作流自动化 Skill。

## 环境变量

| 变量 | 必填 | 说明 |
|------|------|------|
| `GIT_HUB_KEY` | ✅ | Git 访问令牌（隐藏显示） |
| `BASE_GIT_HUB_URL` | ❌ | GitHub URL，默认 `github.com` |

**用户名自动推断：** 从 `git config user.name` 或 `git remote -v` 获取

**仓库地址自动拼接：** `https://${GIT_HUB_KEY}@${BASE_GIT_HUB_URL}/${GIT_USERNAME}/${目录名}.git`

### 环境变量读取流程

```
1. 读取 GIT_HUB_TOKEN（必填）、BASE_GIT_HUB_URL（可选，默认 github.com）
2. 从 git config 或 remote 获取 GIT_USERNAME
3. 获取当前项目目录名作为仓库名
4. 首次推送前确认地址：
   ━━━━━━━━━━━━━━━━━━━━
   远程仓库地址确认
   ━━━━━━━━━━━━━━━━━━━━
   用户名: ${GIT_USERNAME}
   仓库名: ${目录名}
   完整地址: https://***@${BASE_GIT_HUB_URL}/${GIT_USERNAME}/${目录名}.git
   ━━━━━━━━━━━━━━━━━━━━
   地址是否正确？(y/n)
```

### 获取用户名

优先级：
1. `git remote -v` 中 origin 的用户名
2. `git config user.name`

```bash
# 方法1: 从 remote 获取
git remote get-url origin 2>/dev/null | sed -E 's|.*github\.com[/:]([^/]+)/.*|\1|'

# 方法2: 从 git config 获取
git config user.name
```

### 获取仓库名

```bash
basename $(git rev-parse --show-toplevel 2>/dev/null || echo "$PWD")
```

### 配置示例（openclaw.json）

```json
{
  "skills": {
    "entries": {
      "git-workflow": {
        "enabled": true,
        "env": {
          "GIT_HUB_TOKEN": "ghp_xxxxxxxx",
          "BASE_GIT_HUB_URL": "github.com"
        }
      }
    }
  }
}
```

**注意：** GIT_USERNAME 自动推断，不需要配置。

## 工作流程

### ⚡ 第一步：确认修改方式（必须）

**在执行任何修改操作之前，必须先询问用户：**

```
🤔 请选择修改方式
━━━━━━━━━━━━━━━━━━━━
当前分支: <当前分支名>
━━━━━━━━━━━━━━━━━━━━
1. 创建新分支修改
2. 在当前分支修改
━━━━━━━━━━━━━━━━━━━━
请选择 (1/2)：
```

**用户确认后才能执行后续操作。**

---

### ⚡ 第二步：检测 Git 环境（必须）

**在任何 git 操作之前，必须先执行此检测：**

```bash
git rev-parse --git-dir 2>/dev/null && echo "GIT_EXISTS" || echo "NO_GIT"
```

**如果返回 `NO_GIT`，立即初始化：**

```
📁 检测到非 Git 仓库，自动初始化
━━━━━━━━━━━━━━━━━━━━
目录: /path/to/project
━━━━━━━━━━━━━━━━━━━━
```

**自动执行初始化：**

```bash
cd /path/to/project
git init
git config user.name "$(git config --global user.name || echo 'huyuans')"
git config user.email "$(git config --global user.email || echo 'user@example.com')"
# 此时暂不 add 和 commit，等待用户后续操作
```

**如果返回 `GIT_EXISTS`，继续下一步流程。**

### 0. 检测并初始化仓库

**首次进入项目目录时自动检测：**

```bash
git rev-parse --git-dir 2>/dev/null
```

**如果非 Git 仓库，自动初始化（仅提示，不询问）：**

```
📁 检测到非 Git 仓库，自动初始化
━━━━━━━━━━━━━━━━━━━━
目录: /path/to/project
━━━━━━━━━━━━━━━━━━━━
```

**自动执行：**

```bash
cd /path/to/project
git init
git config user.name "$(git config --global user.name || echo 'huyuans')"
git config user.email "$(git config --global user.email || echo 'user@example.com')"
git add .
git commit -m "chore: initial commit"
```

### 1. 创建分支

**确认提示：**
```
📌 分支操作确认
━━━━━━━━━━━━━━━━━━━━
功能分支: <功能分支名>
基准分支: <基准分支>
━━━━━━━━━━━━━━━━━━━━
是否创建该分支？(y/n)
```

**执行：**
```bash
git checkout -b <功能分支名> <基准分支>
```

### 2. 添加文件并提交

**步骤：**

1. 显示待提交文件列表：`git status --short`
2. 自动生成 commit message（遵循 Conventional Commits 规范）
3. **弹出确认提示：**

```
📝 Commit 信息确认
━━━━━━━━━━━━━━━━━━━━
Commit Message:
"<自动生成的 commit message>"

[可修改] 请输入新的 commit message 或直接回车确认：
━━━━━━━━━━━━━━━━━━━━
```

4. 用户可修改 commit message
5. 执行提交：

```bash
git add .
git commit -m "<确认后的 commit message>"
```

### 3. Rebase 最新代码

**每次 commit 后自动执行：**

```bash
git fetch origin
git rebase origin/<基准分支>
```

**冲突处理：**
```
⚠️ Rebase 冲突
━━━━━━━━━━━━━━━━━━━━
检测到冲突，请手动解决后执行：
  git add <冲突文件>
  git rebase --continue

或放弃 rebase：
  git rebase --abort
━━━━━━━━━━━━━━━━━━━━
```

### 4. Push 分支

**首次推送确认地址：**
```
🚀 远程仓库地址确认
━━━━━━━━━━━━━━━━━━━━
用户名: ${GIT_USERNAME}
仓库名: ${目录名}
完整地址: https://***@${BASE_GIT_HUB_URL}/${GIT_USERNAME}/${目录名}.git
目标分支: <功能分支名>
━━━━━━━━━━━━━━━━━━━━
地址是否正确？(y/n)
```

**执行（带网络重试）：**
```bash
# 网络重试机制：最多重试 3 次，指数退避
for i in 1 2 3; do
  if git push https://${GIT_HUB_TOKEN}@${BASE_GIT_HUB_URL}/${GIT_USERNAME}/${目录名}.git <功能分支名>; then
    break
  fi
  if [ $i -lt 3 ]; then
    delay=$((i * i))
    echo "⚠️ 推送失败，${delay}秒后重试 (第${i}/3次)..."
    sleep $delay
  fi
done
```

**重试策略说明：**
- 第 1 次失败后等待 1 秒重试
- 第 2 次失败后等待 4 秒重试
- 第 3 次失败后停止，提示用户检查网络

## Conventional Commits 规范

自动生成的 commit message 格式：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**常用 type：**
- `feat:` 新功能
- `fix:` 修复 bug
- `docs:` 文档变更
- `style:` 代码格式（不影响功能）
- `refactor:` 重构
- `test:` 测试相关
- `chore:` 构建/工具链相关

## 完整流程示例

```
用户: 帮我创建一个新功能分支并提交代码

1. 检查环境变量
   ✓ GIT_HUB_KEY: ***
   ✓ BASE_GIT_HUB_URL: github.com
   ✓ GIT_USERNAME: huyuans (从 remote 推断)
   ✓ 仓库名: claude-practise
   
2. 首次推送确认地址 → 用户确认 y

3. 创建分支
   功能分支名: feature/add-login
   基准分支: main
   → 用户确认 y
   → git checkout -b feature/add-login main

4. 添加文件
   显示 git status
   → 用户确认要提交的文件

5. Commit
   自动生成: "feat: add login functionality"
   → 用户确认/修改
   → git add . && git commit -m "..."

6. Rebase
   → git fetch origin
   → git rebase origin/main
   → 如有冲突，提示用户处理

7. Push
   → 用户确认地址和分支
   → git push https://***@github.com/huyuans/claude-practise.git feature/add-login

✅ 完成！
```

## 安全原则

1. **Token 隐藏** - 始终用 `***` 遮蔽，不在任何输出中显示完整 token
2. **地址确认** - 首次推送前确认远程仓库地址是否正确
3. **每步确认** - 分支创建、commit、push 都需要用户明确确认
4. **可回退** - 每步操作前告知用户如何撤销
5. **冲突提示** - rebase 冲突时提供清晰的解决指引

## ⚠️ 执行原则

**读取此 skill 后，必须严格遵循流程执行：**

1. **不要再问已在 skill 中定义的信息** - 如仓库地址、用户名等，应自动从配置和环境推断
2. **自动读取配置** - 从 `~/.openclaw/openclaw.json` 获取 `GIT_HUB_KEY` 和 `BASE_GIT_HUB_URL`
3. **自动推断** - 用户名从 `git config` 获取，仓库名从目录名获取
4. **直接执行** - 按流程走，只在必要步骤（确认地址、确认 commit）询问用户
5. **禁止重复询问** - 不要问「仓库地址是什么」「用户名是什么」这类 skill 已定义好的内容

## 使用场景

- 创建新功能分支并推送代码
- 安全地提交代码到远程仓库
- 规范化的 Git 工作流
- 需要 rebase 最新代码的提交流程