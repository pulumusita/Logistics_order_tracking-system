# 物流跟踪系统 API 文档

## 目录
- [页面接口](#页面接口)
- [订单接口](#订单接口)
- [物流轨迹接口](#物流轨迹接口)
- [配送员接口](#配送员接口)
- [地图服务接口](#地图服务接口)
- [统计接口](#统计接口)
- [操作日志接口](#操作日志接口)

## 页面接口

### 获取首页
- **GET** `/`
- **响应**: 返回首页视图

### 获取跟踪页面
- **GET** `/tracking`
- **响应**: 返回跟踪页面视图

## 订单接口

### 获取所有订单
- **GET** `/api/orders`
- **响应**: 返回所有订单列表

### 创建订单
- **POST** `/api/orders`
- **请求体**: Order对象
- **响应**: 创建的订单信息

### 更新订单状态
- **PUT** `/api/orders/{orderId}/status`
- **参数**:
  - `orderId`: 订单ID
  - `status`: 订单状态
- **响应**: 更新后的订单信息

### 分配配送员
- **PUT** `/api/orders/{orderId}/courier`
- **参数**:
  - `orderId`: 订单ID
  - `courierId`: 配送员ID
- **响应**: 更新后的订单信息

### 获取订单详情
- **GET** `/api/orders/{orderId}`
- **参数**: `orderId`: 订单ID
- **响应**: 订单详细信息

### 获取指定状态的订单
- **GET** `/api/orders/status/{status}`
- **参数**: `status`: 订单状态
- **响应**: 符合状态的订单列表

### 获取指定邮箱的订单
- **GET** `/api/orders/email/{email}`
- **参数**: `email`: 邮箱地址
- **响应**: 该邮箱关联的订单列表

### 删除订单
- **DELETE** `/api/orders/{orderId}`
- **参数**: `orderId`: 订单ID
- **响应**: 无内容

### 获取订单跟踪信息
- **GET** `/api/orders/{orderId}/tracking`
- **参数**: `orderId`: 订单ID
- **响应**: 订单跟踪详细信息，包括：
  - 订单信息
  - 发件人位置
  - 收件人位置
  - 剩余配送距离
  - 总配送距离

### 派送订单
- **PUT** `/api/orders/{orderId}/dispatch`
- **参数**:
  - `orderId`: 订单ID
  - 请求体: `{ "courierId": "xxx", "courierName": "xxx" }`
- **响应**: 更新后的订单信息

### 检查订单状态
- **GET** `/api/orders/check-status`
- **响应**: `{ "updated": true/false }`

## 物流轨迹接口

### 获取订单轨迹
- **GET** `/api/logistics/track/{orderId}`
- **参数**: `orderId`: 订单ID
- **响应**: 订单的物流轨迹列表

### 创建轨迹记录
- **POST** `/api/logistics/track`
- **请求体**: LogisticsTrack对象
- **响应**: 创建的轨迹记录

### 获取配送员轨迹
- **GET** `/api/logistics/track/courier/{courierId}`
- **参数**: `courierId`: 配送员ID
- **响应**: 配送员的物流轨迹列表

### 创建状态变更轨迹
- **POST** `/api/logistics/track/status-change`
- **参数**:
  - `orderId`: 订单ID
  - `status`: 订单状态
  - `location`: 位置描述
  - `latitude`: 纬度（可选）
  - `longitude`: 经度（可选）
  - `operatorId`: 操作人ID
  - `operatorName`: 操作人姓名
  - `operatorType`: 操作人类型
  - `description`: 描述信息
- **响应**: 创建的状态变更记录

### 获取操作员轨迹
- **GET** `/api/logistics/track/operator/{operatorId}`
- **参数**: `operatorId`: 操作员ID
- **响应**: 操作员的物流轨迹列表

## 配送员接口

### 创建配送员
- **POST** `/api/couriers`
- **请求体**: Courier对象
- **响应**: 创建的配送员信息

### 更新配送员信息
- **PUT** `/api/couriers/{id}`
- **参数**: 
  - `id`: 配送员ID
  - 请求体: Courier对象
- **响应**: 更新后的配送员信息

### 删除配送员
- **DELETE** `/api/couriers/{id}`
- **参数**: `id`: 配送员ID
- **响应**: 无内容

### 获取配送员信息
- **GET** `/api/couriers/{id}`
- **参数**: `id`: 配送员ID
- **响应**: 配送员详细信息

### 获取所有在职配送员
- **GET** `/api/couriers`
- **响应**: 所有在职配送员列表

### 添加配送员评分
- **POST** `/api/couriers/{id}/ratings`
- **参数**:
  - `id`: 配送员ID
  - 请求体: CourierRating对象
- **响应**: 创建的评分记录

### 获取配送员评分
- **GET** `/api/couriers/{id}/ratings`
- **参数**: `id`: 配送员ID
- **响应**: 配送员的评分列表

### 获取配送员日统计
- **GET** `/api/couriers/{id}/daily-stats`
- **参数**: `id`: 配送员ID
- **响应**: 配送员的日统计数据

### 获取配送员月统计
- **GET** `/api/couriers/{id}/monthly-stats`
- **参数**: `id`: 配送员ID
- **响应**: 配送员的月统计数据

### 获取配送员绩效报告
- **GET** `/api/couriers/{id}/performance`
- **参数**:
  - `id`: 配送员ID
  - `startTime`: 开始时间 (ISO格式)
  - `endTime`: 结束时间 (ISO格式)
- **响应**: 配送员的绩效报告

### 更新配送员在线状态
- **PUT** `/api/couriers/{id}/online-status`
- **参数**:
  - `id`: 配送员ID
  - `online`: 是否在线
- **响应**: 无内容

### 获取在线配送员
- **GET** `/api/couriers/online`
- **响应**: 当前在线的配送员列表

### 获取指定工作区域的配送员
- **GET** `/api/couriers/work-area/{area}`
- **参数**: `area`: 工作区域
- **响应**: 指定区域的配送员列表

### 更新配送员工作区域
- **PUT** `/api/couriers/{id}/work-area`
- **参数**:
  - `id`: 配送员ID
  - `workArea`: 工作区域
- **响应**: 无内容

## 地图服务接口

### 地址转坐标
- **GET** `/api/map/geocode`
- **参数**: `address`: 地址
- **响应**: 经纬度坐标

### 获取地址详细地理信息
- **GET** `/api/map/geocode/detail`
- **参数**: `address`: 地址
- **响应**: 详细地理位置信息

### 计算地址间距离
- **GET** `/api/map/distance`
- **参数**:
  - `startAddress`: 起始地址
  - `endAddress`: 目标地址
- **响应**: 距离（米）

### 计算坐标间距离
- **GET** `/api/map/distance/coordinates`
- **参数**:
  - `startLat`: 起始纬度
  - `startLng`: 起始经度
  - `endLat`: 目标纬度
  - `endLng`: 目标经度
- **响应**: 距离（米）

### 获取配送路线
- **GET** `/api/map/route/{orderId}`
- **参数**: `orderId`: 订单ID
- **响应**: 配送路线信息

### 坐标转地址
- **GET** `/api/map/reverse-geocode`
- **参数**:
  - `latitude`: 纬度
  - `longitude`: 经度
- **响应**: 地址描述

## 统计接口

### 获取统计摘要
- **GET** `/api/statistics/summary`
- **参数**:
  - `startDate`: 开始日期 (yyyy-MM-dd)
  - `endDate`: 结束日期 (yyyy-MM-dd)
- **响应**: 统计摘要信息，包含：
  - 总订单数
  - 完成订单数
  - 总配送距离

### 获取配送员排行榜
- **GET** `/api/statistics/courier-rankings`
- **参数**:
  - `startDate`: 开始日期 (yyyy-MM-dd)
  - `endDate`: 结束日期 (yyyy-MM-dd)
  - `limit`: 返回数量，默认10
- **响应**: 配送员排行数据列表 

## 操作日志接口

### 获取所有操作日志
- **GET** `/api/operation-logs`
- **响应**: 默认返回最近一个月的所有操作日志列表

### 获取日志详情
- **GET** `/api/operation-logs/{id}`
- **参数**: `id`: 日志ID
- **响应**: 日志详细信息

### 根据操作类型查询日志
- **GET** `/api/operation-logs/type/{type}`
- **参数**: `type`: 操作类型
- **响应**: 符合类型的日志列表

### 根据操作人查询日志
- **GET** `/api/operation-logs/operator/{operatorId}`
- **参数**: `operatorId`: 操作人ID
- **响应**: 该操作人的操作日志列表

### 根据目标ID查询日志
- **GET** `/api/operation-logs/target/{targetId}`
- **参数**: `targetId`: 目标对象ID
- **响应**: 与该目标相关的操作日志列表

### 根据时间范围查询日志
- **GET** `/api/operation-logs/time-range`
- **参数**:
  - `start`: 开始时间 (ISO格式)
  - `end`: 结束时间 (ISO格式)
- **响应**: 指定时间范围内的操作日志列表 