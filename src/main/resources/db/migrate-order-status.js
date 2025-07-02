// 更新订单状态从 ASSIGNED 到 DISPATCHED
db.orders.updateMany(
    { "status": "ASSIGNED" },
    { "$set": { "status": "DISPATCHED" } }
);

// 打印更新结果
print("订单状态迁移完成");
print("更新的订单数量: " + db.orders.find({"status": "DISPATCHED"}).count()); 