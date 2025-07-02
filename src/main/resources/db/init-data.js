// 切换/创建数据库
db = db.getSiblingDB('logistics_tracking');

// 清空现有集合
db.orders.drop();
db.couriers.drop();
db.courier_ratings.drop();
db.logistics_tracks.drop();

// 创建配送员数据
db.couriers.insertMany([
    {
        name: "张三",
        phone: "13800138001",
        idCard: "110101199001011234",
        workArea: "北京",
        active: true,
        online: true,
        lastOnlineTime: new Date(),
        totalDeliveries: 0,
        monthlyDeliveries: 0,
        dailyDeliveries: 0,
        totalDistance: 0,
        monthlyDistance: 0,
        dailyDistance: 0,
        averageRating: 0,
        totalRatings: 0,
        complaintsCount: 0,
        createTime: new Date(),
        updateTime: new Date()
    },
    {
        name: "李四",
        phone: "13800138002",
        idCard: "110101199001011235",
        workArea: "上海",
        active: true,
        online: true,
        lastOnlineTime: new Date(),
        totalDeliveries: 0,
        monthlyDeliveries: 0,
        dailyDeliveries: 0,
        totalDistance: 0,
        monthlyDistance: 0,
        dailyDistance: 0,
        averageRating: 0,
        totalRatings: 0,
        complaintsCount: 0,
        createTime: new Date(),
        updateTime: new Date()
    }
]);

// 创建订单数据
db.orders.insertMany([
    {
        orderNumber: "ORDER" + new Date().getTime() + "001",
        status: "PENDING",
        amount: 100,
        freight: 10,
        senderName: "王五",
        senderPhone: "13800138003",
        senderAddress: "北京市朝阳区建国路1号",
        senderLatitude: 39.909652,
        senderLongitude: 116.434062,
        receiverName: "赵六",
        receiverPhone: "13800138004",
        receiverAddress: "北京市海淀区中关村大街1号",
        receiverLatitude: 39.983952,
        receiverLongitude: 116.307689,
        weight: 5.0,
        description: "测试包裹1",
        deliveryDistance: 12.5,
        createTime: new Date(),
        updateTime: new Date()
    },
    {
        orderNumber: "ORDER" + new Date().getTime() + "002",
        status: "PENDING",
        amount: 150,
        freight: 15,
        senderName: "钱七",
        senderPhone: "13800138005",
        senderAddress: "上海市浦东新区陆家嘴1号",
        senderLatitude: 31.238068,
        senderLongitude: 121.501654,
        receiverName: "孙八",
        receiverPhone: "13800138006",
        receiverAddress: "上海市徐汇区衡山路1号",
        receiverLatitude: 31.216000,
        receiverLongitude: 121.445000,
        weight: 3.0,
        description: "测试包裹2",
        deliveryDistance: 8.5,
        createTime: new Date(),
        updateTime: new Date()
    }
]);