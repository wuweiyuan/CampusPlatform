 # 第一阶段：构建 Vue 页面
FROM node:20.19.5-alpine AS builder

WORKDIR /build

 # 先复制依赖清单，安装锁定版本的依赖
COPY package.json package-lock.json ./
RUN npm ci

 # 再复制源代码并构建
COPY . .
RUN npm run build


 # 第二阶段：用 Nginx 提供页面访问
FROM nginx:stable-alpine

 # 把构建产物复制到 Nginx 的静态文件目录
COPY --from=builder /build/dist/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]