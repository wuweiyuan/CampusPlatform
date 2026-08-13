# 阶段 1：认证接口文档

> 基础地址：`http://localhost:8080`
>
> 接口统一以 `/api` 开头。

## 一、通用约定

### 成功响应

  ```json
  {
    "code": 0,
    "message": "操作成功",
    "data": {}
  }

  ### 失败响应

  {
    "code": 1001,
    "message": "验证码无效或已过期",
    "data": null
  }

  ### 参数规则

   参数      规则
  ━━━━━━━━  ━━━━━━━━━━━━━━
   用户名    3 到 32 位
  ────────  ──────────────
   密码      8 到 64 位
  ────────  ──────────────
   邮箱      合法邮箱格式
  ────────  ──────────────
   验证码    6 位数字

  ### 错误码

   code    含义
  ━━━━━━  ━━━━━━━━━━━━━━━━━━━━━
   1001    验证码无效或已过期
  ──────  ─────────────────────
   1002    用户名或邮箱已存在
  ──────  ─────────────────────
   1003    账号已禁用
  ──────  ─────────────────────
   1004    用户名或密码错误
  ──────  ─────────────────────
   401     未登录或 Token 无效
  ──────  ─────────────────────
   403     无权访问

  ———

  ## 二、发送邮箱验证码

  - 请求地址：POST /api/auth/email-code
  - 是否登录：否
  - 作用：向指定邮箱发送 6 位注册验证码。

  ### 请求体

  {
    "email": "student@example.com"
  }

  ### 成功响应

  {
    "code": 0,
    "message": "验证码已发送",
    "data": null
  }

  ### 失败情况

  - 邮箱格式不正确。
  - 同一邮箱 60 秒内重复发送。

  ———

  ## 三、用户注册

  - 请求地址：POST /api/auth/register
  - 是否登录：否
  - 作用：验证邮箱验证码后，创建普通用户账号。

  ### 请求体

  {
    "username": "zhangsan",
    "email": "student@example.com",
    "password": "Password123",
    "emailCode": "123456"
  }

  ### 成功响应

  {
    "code": 0,
    "message": "注册成功",
    "data": null
  }

  ### 失败情况

  - 用户名不符合 3 到 32 位规则。
  - 密码不符合 8 到 64 位规则。
  - 邮箱格式不正确。
  - 验证码错误或已过期，返回 1001。
  - 用户名或邮箱已存在，返回 1002。

  > 注册接口只创建普通用户，前端不能传入角色，更不能注册管理员。

  ———

  ## 四、用户登录

  - 请求地址：POST /api/auth/login
  - 是否登录：否
  - 作用：验证用户名和密码，成功后返回 JWT。

  ### 请求体

  {
    "username": "zhangsan",
    "password": "Password123"
  }

  ### 成功响应

  {
    "code": 0,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9.xxx.xxx",
      "tokenType": "Bearer",
      "expiresIn": 7200,
      "user": {
        "id": 1,
        "username": "zhangsan",
        "email": "student@example.com",
        "role": "USER"
      }
    }
  }

  ### 失败情况

  - 用户名不存在或密码错误，返回 1004。
  - 账号被禁用，返回 1003。

  ———

  ## 五、获取当前登录用户

  - 请求地址：GET /api/auth/me
  - 是否登录：是
  - 作用：根据请求 Header 中的 JWT 获取当前用户资料。

  ### 请求 Header

  Authorization: Bearer 你的JWT令牌

  ### 成功响应

  {
    "code": 0,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "student@example.com",
      "role": "USER"
    }
  }

  ### 失败情况

  - 未携带 Token、Token 无效或已过期，返回 401。
  - 已退出登录的旧 Token，返回 401。

  ———

  ## 六、退出登录

  - 请求地址：POST /api/auth/logout
  - 是否登录：是
  - 作用：使当前 JWT 立即失效。

  ### 请求 Header

  Authorization: Bearer 你的JWT令牌

  ### 成功响应

  {
    "code": 0,
    "message": "退出成功",
    "data": null
  }

  ### 失败情况

  - 未携带 Token、Token 无效或已过期，返回 401。