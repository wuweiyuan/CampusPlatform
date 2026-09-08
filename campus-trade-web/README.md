# 校园二手交易平台前端

使用 Vue 3、TypeScript、Element Plus、Pinia 和 Vue Router。Node 版本以 `.nvmrc` 的 **20.19.5** 为准。

在本目录执行 `nvm use`、`npm ci`、`npm run dev`，浏览器访问 Vite 输出的地址。`/api` 通过 `vite.config.ts` 代理到 `http://localhost:8080`，需先启动后端。

构建命令是 `npm run build`，包含类型检查，产物在 `dist/`。此说明不表示构建已经验收通过。

后端配置、数据库迁移、本地验证码与完整交易验收见 [根目录 README](../README.md)，请求模板见 [Postman/Apifox 使用说明](../docs/postman/README.md)。
