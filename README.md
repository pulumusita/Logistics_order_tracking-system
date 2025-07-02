# 物流订单跟踪系统

## 项目简介

物流订单跟踪系统是一个基于 Spring Boot 的现代化物流管理平台，提供实时订单跟踪、配送员管理、路线规划等功能。系统集成了高德地图服务，为用户提供直观的物流跟踪体验。本系统采用MongoDB作为数据存储，Redis用于缓存，RabbitMQ处理异步消息，全面支持物流业务流程的各个环节。

## 功能特性

### 订单管理
- 订单创建与管理
- 实时订单状态跟踪
- 订单历史记录查询
- 多维度订单搜索
- 订单状态自动更新
- 订单运费自动计算

### 配送员管理
- 配送员信息管理
- 实时在线状态监控
- 工作区域分配
- 配送员评分系统
- 绩效统计分析
- 配送员工作量监控

### 物流跟踪
- 实时位置追踪
- 配送路线规划
- 预计送达时间计算
- 轨迹回放功能
- 异常情况预警
- 多渠道状态更新

### 地图服务
- 高德地图集成
- 智能路线规划
- 地理编码服务
- 距离计算
- 实时路况信息
- 地图可视化展示

### 数据统计
- 配送员绩效报告
- 订单完成率统计
- 配送时效分析
- 区域订单分布
- 自定义时间段统计
- 统计数据可视化

### 消息通知
- 订单状态变更通知
- 异常情况提醒
- 邮件通知服务
- 实时消息推送
- 自定义邮件模板

### 系统安全
- 敏感操作日志记录
- 操作审计追踪
- 用户行为监控
- 异常操作预警
- 权限分级控制

## 技术栈

### 后端技术
- **核心框架**: Spring Boot
- **数据库**: MongoDB
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **API文档**: Swagger
- **日志框架**: Logback
- **切面编程**: Spring AOP

### 前端技术
- **框架**: Vue.js
- **UI组件**: Element Plus
- **地图服务**: 高德地图 JavaScript API
- **HTTP客户端**: Axios
- **状态管理**: Vuex

### 第三方服务
- **地图服务**: 高德地图 API
- **邮件服务**: Spring Mail
- **对象存储**: 阿里云 OSS（可选）

## 系统要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MongoDB 4.4 或更高版本
- Redis 6.0 或更高版本
- RabbitMQ 3.8 或更高版本
- Node.js 14 或更高版本

## 快速开始

### 环境准备
1. 安装必要的开发工具：
   ```bash
   # 安装 JDK
   brew install openjdk@17
   
   # 安装 Maven
   brew install maven
   
   # 安装 MongoDB
   brew install mongodb-community
   
   # 安装 Redis
   brew install redis
   
   # 安装 RabbitMQ
   brew install rabbitmq
   ```

2. 启动必要的服务：
   ```bash
   # 启动 MongoDB
   brew services start mongodb-community
   
   # 启动 Redis
   brew services start redis
   
   # 启动 RabbitMQ
   brew services start rabbitmq
   ```

### 配置修改

1. 修改 `application.yml` 配置文件：
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb://localhost:27017/logistics
     redis:
       host: localhost
       port: 6379
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
   
   amap:
     key: 您的高德地图API密钥
   ```

2. 配置邮件服务（可选）：
   ```yaml
   spring:
     mail:
       host: smtp.example.com
       port: 587
       username: your-email@example.com
       password: your-password
       properties:
         mail.smtp.auth: true
         mail.smtp.starttls.enable: true
   ```

   除此之外，`Logistics_order_tracking system/src/main/resources/templates/index.html`中也需要修改你自己的高德地图密钥和api，关于这方面请自行百度

### 项目构建与运行

1. 克隆项目：
   ```bash
   git clone https://github.com/pulumusitaLogistics_order_tracking-system.git
   cd logistics-tracking-system
   ```

2. 导入数据库文件

   ```
   如果使用navicat等数据库管理软件，直接运行mongdb/logistics_tracking.js即可
   如果是命令行，登录进mongdb然后运行即可
   ```

   **推荐clone导入数据库修改配置后直接导入IDEA运行，更快捷方便，无需手动构建前端和后端**

3. 构建后端服务：

   ```bash
   mvn clean install
   ```

4. 运行后端服务：

   ```bash
   java -jar target/logistics-tracking-system.jar
   ```

5. 构建前端项目：

   ```bash
   cd frontend
   npm install
   npm run build
   ```

6. 访问系统：

- 打开浏览器访问: `http://localhost:8080`

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/logistics/tracking/
│   │       ├── aspect/       # AOP切面类
│   │       ├── config/       # 配置类
│   │       ├── controller/   # 控制器
│   │       ├── listener/     # 消息监听器
│   │       ├── model/        # 数据模型
│   │       ├── repository/   # 数据访问层
│   │       ├── service/      # 业务逻辑层
│   │       │   └── impl/     # 服务实现类
│   │       └── task/         # 定时任务
│   └── resources/
│       ├── static/           # 静态资源
│       │   ├── css/          # 样式文件
│       │   ├── js/           # JavaScript文件
│       │   └── images/       # 图片资源
│       ├── templates/        # 页面模板
│       │   └── email/        # 邮件模板
│       └── application.yml   # 配置文件
```

## API 文档

详细的 API 文档请参考 [API.md](API.md)

## 使用指南

### 订单跟踪
1. 在首页输入订单号
2. 点击"查询"按钮
3. 查看订单实时位置和状态
4. 可查看历史轨迹信息

### 配送员管理
1. 登录管理后台
2. 进入"配送员管理"模块
3. 可进行添加、编辑、删除配送员
4. 查看配送员实时状态和绩效

### 统计分析
1. 进入"统计报表"模块
2. 选择统计时间范围
3. 查看各类统计数据
4. 导出报表（可选）

### 操作日志查询
1. 进入"操作日志"模块
2. 可按操作类型、操作人、时间范围查询
3. 查看敏感操作的详细信息
4. 导出日志记录（可选）

## 常见问题

1. **系统无法启动**
   - 检查必要服务是否启动（MongoDB、Redis、RabbitMQ）
   - 检查配置文件中的连接信息是否正确
   - 查看日志文件了解具体错误信息

2. **地图无法显示**
   - 检查高德地图 API 密钥是否正确
   - 确认是否有正确的网络连接
   - 检查浏览器控制台是否有错误信息

3. **消息通知失败**
   - 检查 RabbitMQ 连接状态
   - 验证邮件服务器配置
   - 查看日志中的具体错误信息

## 开发指南

### 代码规范
- 遵循 Java 代码规范
- 使用 4 空格缩进
- 类名使用 PascalCase
- 方法名使用 camelCase
- 常量使用全大写下划线分隔

### 提交规范
- feat: 新功能
- fix: 修复问题
- docs: 文档修改
- style: 代码格式修改
- refactor: 代码重构
- test: 测试用例修改
- chore: 其他修改

### 分支管理
- master: 主分支，用于生产环境
- develop: 开发分支，用于功能集成
- feature/*: 功能分支
- hotfix/*: 紧急修复分支

## 部署指南

### 开发环境
1. 按照系统要求安装必要组件
2. 修改配置文件为开发环境配置
3. 使用开发工具运行项目

### 测试环境
1. 准备测试服务器
2. 配置测试环境数据库和中间件
3. 使用 Jenkins 等工具自动部署

### 生产环境
1. 准备生产服务器
2. 配置生产环境数据库和中间件
3. 使用 Docker 容器化部署
4. 配置负载均衡和高可用

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交变更
4. 推送到远程分支
5. 创建 Pull Request

## 版本历史

- v1.1.0 (2024-06-01)
  - 增加敏感操作日志记录功能
  - 优化统计报表性能
  - 修复订单跟踪中的距离计算问题
  - 增强配送员绩效评估系统

- v1.0.0 (2024-01-01)
  - 初始版本发布
  - 基础功能实现

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者：Winter
- 邮箱：1275895583@qq.com
- 项目地址：

## 致谢

感谢所有为本项目做出贡献的开发者。 