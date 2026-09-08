# 请求日志与本地验证码验收

## 请求 ID

后端 `RequestLoggingFilter` 在 Spring Security 前执行，为每个请求生成 UUID，响应头返回 `X-Request-ID`。MDC 使用 `requestId`，日志级别区域显示 `[requestId=...]`，同一请求中的业务日志可按该 ID 查找。

服务端不直接信任传入的 X-Request-ID，而是重新生成。日志不记录 Authorization、Cookie、请求/响应体、原始 URL 或查询字符串；只记录方法、匹配的路由模板、过滤器退出时的响应状态、耗时和是否有未处理异常。未进入 Controller 的安全拒绝记录 `route=unmatched`，不将用户输入路径直接写入日志。请求完成后清理 MDC，防止线程复用污染下一条请求。

这是当前同步 MVC 请求的日志上下文，不是分布式链路追踪。启动/后台日志无请求 ID 时显示 `none`；异步任务、跨服务传播与框架二次错误分派不在本轮范围。`unhandled=true` 时后续错误分派可能改变最终 HTTP 状态，不将过滤器退出时状态当最终错误响应结论。

在 Apifox/Postman 的响应 Headers 中复制 X-Request-ID，再到服务端日志检索同一值。记录 ID 即可，不必附带真实 Token 或完整请求体。

## 当前验证码方式

当前服务仍为**本地开发取码**：验证码写入 Redis，未接通真实邮件发送。此前注释中的 MAIL_MODE=log 不是实际配置开关，现已移除；API 原有“验证码已发送”文案暂未调整，不能据此声称邮件已投递。部署前需另行配置并实现真实发送方式。

`EmailCodeService` 日志只记录“邮箱验证码已生成并缓存”，不包含邮箱或验证码；响应体也不返回验证码。TTL 5 分钟、发送间隔 60 秒、注册成功删除验证码的业务规则保持不变。

### macOS 本地取码（不输出到终端或日志）

1. 在 Apifox/网页发送验证码，使用自己的本地测试邮箱。
2. 在本机终端执行以下 Python 3 片段，按隐藏输入提示填写**同一个邮箱**。需要已安装 `redis-cli` 和系统 `pbcopy`；默认连接 localhost:6379、DB 0，与当前默认配置一致。若更换 Redis 配置，应使用匹配的本地客户端，不把连接密码写进共享命令。
3. 粘贴到注册表单或 Apifox 本地变量。注册完成后清空剪贴板与验证码变量，不将值保存到集合或截图。

```bash
python3 - <<'PY'
import getpass
import re
import subprocess

email = getpass.getpass('本地测试邮箱（隐藏输入）：').strip()
result = subprocess.run(
    ['redis-cli', '--raw', 'GET', 'auth:email-code:' + email],
    capture_output=True, text=True,
)
code = result.stdout.strip()
if result.returncode != 0 or not re.fullmatch(r'[0-9]{6}', code):
    raise SystemExit('未取得有效验证码，请检查 Redis 连接或重新发送。')
subprocess.run(['pbcopy'], input=code, text=True, check=True)
print('验证码已复制到本机剪贴板，请在有效期内粘贴。')
PY
```

取码只用于有本机 Redis 权限的开发者，不新增公开取码接口。剪贴板内是短期敏感值，不应共享或同步保存。注册后可执行 `pbcopy < /dev/null` 清空。

## 本轮验证（2026-09-08）

- 后端 `mvn -o -DskipTests compile` 成功，未运行自动测试。
- 临时 8081 实例实测匿名分类 200、非法分页 400、匿名个人信息 401、验证码生成 200、有效验证码配合已存在用户名注册 400/code=1002。
- 五个响应 X-Request-ID 均为不同 UUID，与各自日志一致；传入的自定义 ID 被替换。测试查询、请求体标记和 Redis 验证码均未出现在该实例日志。
- 验证码仅在验收进程内存中读取，注册请求到达重复用户检查，证明验证码校验通过；没有创建账户，没有发送邮件。专用验证码与限频键已清理。
- 剪贴板指引未自动执行，以免覆盖用户剪贴板；它不等同邮件功能验收。
- 本轮验证已修改日志路径，不代表所有第三方库、未来新增日志或所有异常分支都已完成敏感数据审计。
