# 实验室环境监测系统前端

这是一个基于Vue 3 + Element Plus的实验室环境监测系统前端项目，用于与后端微服务进行联调测试。

## 功能特性

- 🔐 **用户认证**: 登录、注册、token验证
- 🏠 **仪表板**: 系统概览、实时统计、图表展示
- 🏢 **实验室管理**: 实验室信息的增删改查
- 📊 **环境监控**: 实时环境数据展示、历史数据查询
- 🚨 **告警管理**: 告警查看、确认、解决、批量操作

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Element Plus** - Vue 3组件库
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP请求库
- **ECharts** - 图表库
- **Vite** - 构建工具

## 项目结构

```
lab-data-platform-frontend/
├── src/
│   ├── api/           # API接口
│   │   ├── auth.js    # 认证接口
│   │   ├── laboratory.js # 实验室接口
│   │   ├── monitor.js # 监控接口
│   │   └── alarm.js   # 告警接口
│   ├── components/    # 组件
│   │   └── Layout.vue # 主布局组件
│   ├── router/        # 路由
│   │   └── index.js   # 路由配置
│   ├── stores/        # 状态管理
│   │   └── user.js    # 用户状态
│   ├── utils/         # 工具
│   │   └── request.js # 请求封装
│   ├── views/         # 页面
│   │   ├── Login.vue      # 登录页面
│   │   ├── Dashboard.vue  # 仪表板
│   │   ├── Laboratory.vue # 实验室管理
│   │   ├── Monitor.vue    # 环境监控
│   │   └── Alarms.vue     # 告警管理
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── index.html         # HTML模板
├── package.json       # 依赖配置
├── vite.config.js     # Vite配置
└── README.md          # 说明文档
```

## 快速开始

### 1. 安装依赖

```bash
cd lab-data-platform-frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

### 3. 构建生产版本

```bash
npm run build
```

## API配置

项目已配置代理，前端请求会自动转发到后端服务：

- `vite.config.js` 中配置了 `/api` 代理到 `http://localhost:8080`
- 实际API路径会自动去除 `/api` 前缀

### API服务映射

| 前端API路径 | 后端服务 | 端口 |
|-------------|----------|------|
| `/auth-service/*` | auth-service | 8081 |
| `/system-service/*` | system-service | 8082 |
| `/monitor-service/*` | monitor-service | 8083 |
| `/gateway-service/*` | gateway-service | 8080 |

## 使用说明

### 1. 登录注册

- 访问 `http://localhost:3000/login`
- 可以注册新用户或使用现有用户登录
- 默认角色：OPERATOR

### 2. 仪表板

- 显示系统概览统计
- 实时图表数据
- 最近告警列表
- 可以快速处理告警

### 3. 实验室管理

- 查看、新增、编辑、删除实验室信息
- 支持按名称和状态筛选
- 分页显示

### 4. 环境监控

- 实时显示各实验室环境数据
- 支持手动上传测试数据
- 查看历史数据
- 数据状态可视化（颜色标识）

### 5. 告警管理

- 查看所有告警信息
- 支持确认、解决、忽略操作
- 批量处理告警
- 告警详情查看

## 数据测试

### 环境数据测试

在监控页面可以使用以下功能发送测试数据：

1. **发送正常数据**: 生成正常的实验室环境数据
2. **发送告警数据**: 生成会触发告警的环境数据
3. **手动上传数据**: 自定义环境数据上传

### 告警阈值

- **温度**: 正常 18-28°C，告警 >28°C 或 <18°C
- **湿度**: 正常 40-70%，告警 >70% 或 <40%
- **PM2.5**: 正常 <75，警告 75-150，危险 >150
- **CO2**: 正常 <1000ppm，警告 1000-1500ppm，危险 >1500ppm
- **光照**: 正常 >300lux，警告 200-300lux，危险 <200lux
- **人数**: 正常 <40人，警告 40-50人，危险 >50人

## 联调说明

### 1. 确保后端服务启动

```bash
# 在后端项目根目录
docker-compose up -d
```

### 2. 检查服务状态

- auth-service: http://localhost:8081/auth-service/auth/health
- system-service: http://localhost:8082/system-service/health
- monitor-service: http://localhost:8083/monitor-service/lab-monitor/health

### 3. 测试接口连接

1. 打开前端登录页面
2. 注册一个新用户或使用现有用户登录
3. 如果登录成功，说明API连接正常

### 4. 测试数据流

1. 在监控页面上传环境数据
2. 在告警页面查看是否产生告警
3. 在仪表板查看统计数据更新

## 开发注意事项

1. **跨域问题**: 项目已配置代理，如仍有跨域问题请检查后端CORS配置
2. **Token管理**: Token存储在localStorage，会自动添加到请求头
3. **路由守卫**: 未登录用户会自动跳转到登录页
4. **错误处理**: 统一在request拦截器中处理，显示错误消息
5. **响应式设计**: 使用Element Plus组件，支持响应式布局

## 故障排查

### 常见问题

1. **登录失败**: 检查auth-service是否启动，数据库连接是否正常
2. **数据不显示**: 检查对应微服务是否启动，是否有测试数据
3. **图表不显示**: 检查ECharts是否正确加载，数据格式是否正确
4. **接口404**: 检查API路径配置，代理设置是否正确

### 调试技巧

1. 打开浏览器开发者工具查看网络请求
2. 检查Console是否有错误信息
3. 在API接口中添加console.log查看请求数据
4. 使用后端Swagger文档测试接口

## 扩展开发

### 添加新页面

1. 在 `src/views/` 创建新组件
2. 在 `src/router/index.js` 添加路由
3. 在 `Layout.vue` 中添加菜单项

### 添加新API

1. 在 `src/api/` 创建新的API文件
2. 在页面中引入并使用
3. 添加相应的错误处理

## License

MIT