# MEMORY.md - 长期记忆

## Git 配置

**仓库推送信息配置在 `git-workflow` skill 中：**

- 配置文件：`~/.openclaw/openclaw.json`
- 环境变量：`GIT_HUB_KEY`（GitHub token）、`BASE_GIT_HUB_URL`（默认 github.com）
- 用户名自动推断：从 `git config user.name` 获取
- 仓库名自动推断：从项目目录名获取
- 地址自动拼接：`https://${GIT_HUB_KEY}@${BASE_GIT_HUB_URL}/${GIT_USERNAME}/${目录名}.git`

**重要：** 用户要求推送代码时，直接读取 git-workflow skill 配置，不要再问仓库地址！

## 用户偏好

- GitHub 用户名：huyuans
- Email：1090116461@qq.com
- 喜欢「活人感」强的对话风格
- 熟悉终端和编程，喜欢直接的技术回答