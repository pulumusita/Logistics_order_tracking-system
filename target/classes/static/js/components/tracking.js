const tracking = {
    template: `
        <div class="tracking-container">
            <div class="tracking-left">
                <el-card class="tracking-search-card">
                    <div class="tracking-search">
                        <el-input
                            v-model="trackingNumber"
                            placeholder="请输入运单号查询"
                            class="tracking-input"
                            clearable>
                        </el-input>
                        <el-button type="primary" @click="searchOrder" :loading="loading">
                            查询
                        </el-button>
                    </div>
                </el-card>
                
                <el-card v-if="orderInfo" class="tracking-info-card">
                    <template #header>
                        <div class="card-header">
                            <span>订单信息</span>
                            <el-tag :type="getStatusType(orderInfo.status)">
                                {{ getStatusText(orderInfo.status) }}
                            </el-tag>
                        </div>
                    </template>
                    <div class="order-info">
                        <div class="info-item">
                            <h4>发件信息</h4>
                            <p><i class="el-icon-user"></i> 发件人：{{ orderInfo.senderName }}</p>
                            <p><i class="el-icon-location"></i> 发件地址：{{ orderInfo.senderAddress }}</p>
                            <p><i class="el-icon-phone"></i> 电子邮箱：{{ orderInfo.senderEmail }}</p>
                        </div>
                        <div class="info-item">
                            <h4>收件信息</h4>
                            <p><i class="el-icon-user"></i> 收件人：{{ orderInfo.receiverName }}</p>
                            <p><i class="el-icon-location"></i> 收件地址：{{ orderInfo.receiverAddress }}</p>
                            <p><i class="el-icon-phone"></i> 电子邮箱：{{ orderInfo.receiverEmail }}</p>
                        </div>
                        <div class="info-item">
                            <h4>物流信息</h4>
                            <div v-if="orderInfo.status === 'DELIVERED'" class="delivery-info">
                                <p><i class="el-icon-success"></i> 订单已送达</p>
                                <p><i class="el-icon-time"></i> 送达时间：{{ formatTime(orderInfo.deliveryTime) }}</p>
                            </div>
                            <div v-else-if="remainingDistance !== null" class="delivery-info">
                                <p><i class="el-icon-location-information"></i> 距离目的地还有：{{ (remainingDistance / 1000).toFixed(2) }} 公里</p>
                                <p v-if="estimatedArrivalTime"><i class="el-icon-time"></i> 预计送达时间：{{ formatTime(estimatedArrivalTime) }}</p>
                            </div>
                            <el-timeline>
                                <el-timeline-item
                                    v-for="(track, index) in trackingList"
                                    :key="index"
                                    :timestamp="formatTime(track.timestamp)"
                                    :type="getTimelineItemType(track.status)">
                                    <h4>{{ getStatusText(track.status) }}</h4>
                                    <p>{{ track.description }}</p>
                                    <p v-if="track.location">位置：{{ track.location }}</p>
                                </el-timeline-item>
                            </el-timeline>
                        </div>
                    </div>
                </el-card>
            </div>
            
            <div class="tracking-right">
                <el-card class="map-card">
                    <div id="container" class="amap-container"></div>
                </el-card>
            </div>
        </div>
    `,
    data() {
        return {
            trackingNumber: '',
            orderInfo: null,
            trackingList: [],
            loading: false,
            map: null,
            driving: null,
            remainingDistance: null,
            estimatedArrivalTime: null,
            courierMarker: null,
            pathSimplifier: null,
            navg: null,
            routePath: [],
            updateTimer: null,
            totalPathDistance: 0,
            pathPoints: [],
            distanceFinsh: null,
            courierLabel: null
        }
    },
    mounted() {
        this.initMap();
    },
    methods: {
        initMap() {
            console.log('初始化地图...');
            this.map = new AMap.Map("container", {
                resizeEnable: true,
                center: [116.397428, 39.90923],
                zoom: 13
            });
            console.log('地图初始化完成');
            
            this.driving = new AMap.Driving({
                map: this.map,
                hideMarkers: false,
                showTraffic: false,
                autoFitView: true
            });
            console.log('驾车导航插件初始化完成');



            // // 创建文本标签
            // this.courierLabel = new AMap.Text({
            //     text: '快递正在流转',
            //     anchor: 'bottom-center',
            //     draggable: false,
            //     cursor: 'pointer',
            //     angle: 0,
            //     style: {
            //         'padding': '5px 10px',
            //         'margin-bottom': '25px',
            //         'border-radius': '3px',
            //         'background-color': '#fff',
            //         'border': '1px solid #ccc',
            //         'box-shadow': '0 2px 6px 0 rgba(114, 124, 245, .5)',
            //         'color': '#333',
            //         'font-size': '12px',
            //         'white-space': 'nowrap'
            //     },
            //     offset: new AMap.Pixel(0, -40)
            // });

            // 将标签添加到地图
            this.courierLabel.setMap(this.map);
        },
        async searchOrder() {
            if (!this.trackingNumber) {
                this.$message.warning('请输入运单号');
                return;
            }
            
            this.loading = true;
            try {
                console.log('开始查询订单:', this.trackingNumber);
                const response = await axios.get(`/api/orders/${this.trackingNumber}/tracking`);
                const data = response.data;
                console.log('获取到订单数据:', JSON.stringify(data, null, 2));
                
                this.orderInfo = data.order;
                this.distanceFinsh = data.distanceFinsh;
                this.totalPathDistance = data.totalDistance;
                console.log('订单基本信息:', JSON.stringify(this.orderInfo, null, 2));
                console.log('后端返回的数据:', data);
                console.log('剩余配送距离 (distanceFinsh):', this.distanceFinsh);
                console.log('总路径距离:', this.totalPathDistance);
                
                this.trackingList = await this.getTrackingList(this.trackingNumber);
                console.log('物流轨迹信息:', JSON.stringify(this.trackingList, null, 2));
                
                if (data.senderLocation && data.receiverLocation) {
                    console.log('===== 位置信息 =====');
                    console.log('发件地坐标:', [data.senderLocation.longitude, data.senderLocation.latitude]);
                    console.log('收件地坐标:', [data.receiverLocation.longitude, data.receiverLocation.latitude]);
                    
                    var arr = [
                        new AMap.LngLat(data.senderLocation.longitude, data.senderLocation.latitude),
                        new AMap.LngLat(data.receiverLocation.longitude, data.receiverLocation.latitude)
                    ];
                    console.log('坐标点数组已创建');
                    
                    console.log('清除之前的路线...');
                    this.driving.clear();
                    distancePlus = parseInt(this.distanceFinsh / 1000)
                    timePlus = parseInt(distancePlus / 80)
                    timePlus = timePlus + '小时' + timePlus % 60 + '分钟'
                    
            // 创建快递员标记
            this.courierMarker = new AMap.Marker({
                map: this.map,
                position: [116.397428, 39.90923],
                icon: new AMap.Icon({
                    size: new AMap.Size(32, 32),
                    image: '../images/start.png',
                    imageSize: new AMap.Size(32, 32),
                }),
                offset: new AMap.Pixel(-16, -16),
                label: {
                    content: '快递正在流转，距终点' + distancePlus + '千米，' + timePlus + '送达',
                    direction: 'top',
                    offset: new AMap.Pixel(0, -30),
                    style: {
                        padding: '5px 10px',
                        'border-radius': '3px',
                        'background-color': '#fff',
                        'border': '1px solid #ccc',
                        'box-shadow': '0 2px 6px 0 rgba(114, 124, 245, .5)',
                        'color': '#333',
                        'font-size': '12px',
                        'white-space': 'nowrap'
                    }
                }
            });
                    
                    // 清除之前的定时器
                    if (this.updateTimer) {
                        clearInterval(this.updateTimer);
                        this.updateTimer = null;
                    }
                    
                    this.driving.search(
                        new AMap.LngLat(data.senderLocation.longitude, data.senderLocation.latitude),
                        new AMap.LngLat(data.receiverLocation.longitude, data.receiverLocation.latitude),
                        (status, result) => {
                            console.log('===== 路线规划结果 =====');
                            console.log('状态:', status);
                            
                            if (status === 'complete') {
                                console.log('路线规划完成，开始计算距离和时间');
                                
                                // 获取路径点
                                this.pathPoints = result.routes[0].steps.map(step => {
                                    return step.path;
                                }).flat();
                                
                                // 计算总路径距离
                                this.totalPathDistance = 0;
                                for (let i = 0; i < this.pathPoints.length - 1; i++) {
                                    this.totalPathDistance += AMap.GeometryUtil.distance(
                                        [this.pathPoints[i].lng, this.pathPoints[i].lat],
                                        [this.pathPoints[i + 1].lng, this.pathPoints[i + 1].lat]
                                    );
                                }
                                console.log('计算得到的总路径距离:', this.totalPathDistance);

                                // 根据后端返回的剩余距离计算当前位置
                                if (this.orderInfo.status === 'DELIVERED') {
                                    // 已送达订单，骑手位置设为终点
                                    this.courierMarker.setPosition(this.pathPoints[this.pathPoints.length - 1]);
                                    this.remainingDistance = 0;
                                    console.log('订单已送达，设置骑手位置到终点');
                                } else if (this.orderInfo.status === 'PENDING') {
                                    // 未开始配送的订单，骑手位置设为起点
                                    this.courierMarker.setPosition(this.pathPoints[0]);
                                    this.remainingDistance = this.totalPathDistance;
                                    console.log('订单未开始配送，设置骑手位置到起点');
                                } else if (this.orderInfo.pickupTime && this.distanceFinsh !== null) {
                                    // 使用后端计算的剩余距离来确定骑手位置
                                    const traveledDistance = this.totalPathDistance - this.distanceFinsh;
                                    this.remainingDistance = this.distanceFinsh;
                                    console.log('订单配送中 - 总距离:', this.totalPathDistance);
                                    console.log('剩余距离:', this.distanceFinsh);
                                    console.log('已行驶距离:', traveledDistance);
                                    
                                    // 找到对应的路径点
                                    let accumulatedDistance = 0;
                                    let currentIndex = 0;
                                    
                                    while (currentIndex < this.pathPoints.length - 1) {
                                        const nextDistance = AMap.GeometryUtil.distance(
                                            [this.pathPoints[currentIndex].lng, this.pathPoints[currentIndex].lat],
                                            [this.pathPoints[currentIndex + 1].lng, this.pathPoints[currentIndex + 1].lat]
                                        );
                                        
                                        if (accumulatedDistance + nextDistance > traveledDistance) {
                                            // 找到了当前路径段
                                            const remainingSegmentDistance = traveledDistance - accumulatedDistance;
                                            const ratio = remainingSegmentDistance / nextDistance;
                                            
                                            // 计算插值位置
                                            const currentPoint = this.pathPoints[currentIndex];
                                            const nextPoint = this.pathPoints[currentIndex + 1];
                                            const newLng = currentPoint.lng + (nextPoint.lng - currentPoint.lng) * ratio;
                                            const newLat = currentPoint.lat + (nextPoint.lat - currentPoint.lat) * ratio;
                                            
                                            // 更新快递员位置
                                            this.courierMarker.setPosition([newLng, newLat]);
                                            console.log('更新骑手位置 - 路段:', currentIndex + 1);
                                            console.log('位置比例:', ratio);
                                            console.log('新位置:', [newLng, newLat]);
                                            break;
                                        }
                                        
                                        accumulatedDistance += nextDistance;
                                        currentIndex++;
                                        
                                        // 如果已经到达终点
                                        if (currentIndex === this.pathPoints.length - 1) {
                                            this.courierMarker.setPosition(this.pathPoints[this.pathPoints.length - 1]);
                                            console.log('骑手已到达终点');
                                        }
                                    }
                                    
                                    // 开始定时更新位置
                                    if (this.orderInfo.status !== 'DELIVERED') {
                                        this.startPositionUpdate();
                                        console.log('启动定时位置更新');
                                    }
                                }
                                
                                console.log('===== 状态检查与时间计算 =====');
                                if (this.orderInfo.status === 'PENDING') {
                                    console.log('订单状态: 待处理');
                                    this.$message.info('请耐心等待快递接单');
                                } else if (this.orderInfo.status === 'DELIVERED') {
                                    console.log('订单状态: 已送达');
                                    this.$message.success('包裹已送达目的地');
                                } else if (this.orderInfo.status === 'EXCEPTION') {
                                    console.log('订单状态: 异常');
                                    this.$message.error('快件异常，请致电客服');
                                } else if (this.orderInfo.status && this.orderInfo.pickupTime) {
                                    console.log('订单状态:', this.orderInfo.status);
                                    console.log('接单时间:', new Date(this.orderInfo.pickupTime).toLocaleString());
                                    
                                    // 使用后端返回的剩余距离来计算预计到达时间
                                    const speedKmPerHour = 80;
                                    const distanceKm = this.distanceFinsh / 1000; // 使用后端返回的剩余距离
                                    const travelTimeHours = distanceKm / speedKmPerHour;
                                    const travelTimeMs = travelTimeHours * 60 * 60 * 1000;
                                    
                                    console.log('剩余距离（公里）:', distanceKm);
                                    console.log('预计剩余行驶时间（小时）:', travelTimeHours);
                                    console.log('预计剩余行驶时间（毫秒）:', travelTimeMs);
                                    
                                    // 从当前时间开始计算
                                    const now = new Date().getTime();
                                    this.estimatedArrivalTime = now + travelTimeMs;
                                    this.remainingDistanceKm = parseFloat(distanceKm.toFixed(2));
                                    console.log('预计送达时间:', new Date(this.estimatedArrivalTime).toLocaleString());
                                    console.log('剩余距离:', this.remainingDistanceKm, '公里');
                                } else {
                                    console.warn('未找到接单记录，无法计算预计送达时间');
                                }
                            } else {
                                console.error('路线规划失败，详细信息:', result);
                                this.$message.error('路线规划失败');
                            }
                        }
                    );
                } else {
                    console.warn('位置信息不完整');
                    console.log('发件地位置:', data.senderLocation);
                    console.log('收件地位置:', data.receiverLocation);
                    this.$message.warning('未获取到完整的地理位置信息');
                }
            } catch (error) {
                console.error('查询失败，详细错误:', error);
                console.error('错误堆栈:', error.stack);
                this.$message.error('查询失败，请检查运单号是否正确');
            } finally {
                this.loading = false;
                console.log('查询操作完成');
            }
        },
        startPositionUpdate() {
            // 清除之前的定时器
            if (this.updateTimer) {
                clearInterval(this.updateTimer);
            }
            
            // 每分钟更新一次位置
            this.updateTimer = setInterval(() => {
                // 使用订单的实际配送开始时间计算已经过的时间
                const pickupTime = new Date(this.orderInfo.pickupTime).getTime();
                const elapsedMinutes = (new Date() - pickupTime) / (1000 * 60);
                
                // 计算应该行驶的距离（米）
                // 80km/h = 1333.33m/min
                const shouldTravelDistance = elapsedMinutes * (80 * 1000 / 60);
                
                // 如果已经到达终点，停止更新
                if (shouldTravelDistance >= this.totalPathDistance) {
                    clearInterval(this.updateTimer);
                    this.courierMarker.setPosition(this.pathPoints[this.pathPoints.length - 1]);
                    return;
                }
                
                // 找到当前应该在的路径点
                let traveledDistance = 0;
                let currentIndex = 0;
                
                while (currentIndex < this.pathPoints.length - 1) {
                    const nextDistance = AMap.GeometryUtil.distance(
                        [this.pathPoints[currentIndex].lng, this.pathPoints[currentIndex].lat],
                        [this.pathPoints[currentIndex + 1].lng, this.pathPoints[currentIndex + 1].lat]
                    );
                    
                    if (traveledDistance + nextDistance > shouldTravelDistance) {
                        // 找到了当前路径段
                        const remainingDistance = shouldTravelDistance - traveledDistance;
                        const ratio = remainingDistance / nextDistance;
                        
                        // 计算插值位置
                        const currentPoint = this.pathPoints[currentIndex];
                        const nextPoint = this.pathPoints[currentIndex + 1];
                        const newLng = currentPoint.lng + (nextPoint.lng - currentPoint.lng) * ratio;
                        const newLat = currentPoint.lat + (nextPoint.lat - currentPoint.lat) * ratio;
                        
                        // 更新快递员位置
                        this.courierMarker.setPosition([newLng, newLat]);
                        break;
                    }
                    
                    traveledDistance += nextDistance;
                    currentIndex++;
                }
                
                // 计算剩余距离
                this.remainingDistance = this.totalPathDistance - shouldTravelDistance;
                
                console.log('位置已更新 - 已行驶:', (shouldTravelDistance/1000).toFixed(2), 'km, 剩余:', (this.remainingDistance/1000).toFixed(2), 'km');
            }, 60000); // 每分钟执行一次
        },
        async getTrackingList(orderId) {
            try {
                console.log('开始获取物流轨迹, 订单号:', orderId);
                const response = await axios.get(`/api/logistics/track/${orderId}`);
                console.log('获取到物流轨迹数据:', response.data);
                return response.data;
            } catch (error) {
                console.error('获取物流轨迹失败:', error);
                console.error('错误堆栈:', error.stack);
                return [];
            }
        },
        formatTime(timestamp) {
            if (!timestamp) {
                console.warn('时间戳为空');
                return '';
            }
            const date = new Date(timestamp);
            console.log('格式化时间:', timestamp, '→', date.toLocaleString());
            return date.toLocaleString();
        },
        getStatusText(status) {
            const statusMap = {
                'PENDING': '待处理',
                'ACCEPTED': '已接单',
                'PICKED_UP': '已取件',
                'IN_TRANSIT': '运输中',
                'OUT_FOR_DELIVERY': '派送中',
                'DELIVERED': '已送达',
                'EXCEPTION': '异常'
            };
            return statusMap[status] || status;
        },
        getStatusType(status) {
            const typeMap = {
                'PENDING': 'info',
                'ACCEPTED': 'primary',
                'PICKED_UP': 'warning',
                'IN_TRANSIT': 'warning',
                'OUT_FOR_DELIVERY': 'warning',
                'DELIVERED': 'success',
                'EXCEPTION': 'danger'
            };
            return typeMap[status] || 'info';
        },
        getTimelineItemType(status) {
            const typeMap = {
                'DELIVERED': 'success',
                'EXCEPTION': 'danger'
            };
            return typeMap[status] || 'primary';
        },
        // 更新快递员位置和标签信息
        updateCourierPosition(position, remainingDistance, remainingTime) {
            if (this.courierMarker) {
                // 更新标记位置
                this.courierMarker.setPosition(position);
                
                // 格式化距离和时间
                const distance = (remainingDistance / 1000).toFixed(1); // 转换为千米并保留一位小数
                const hours = Math.floor(remainingTime / 60);
                const minutes = remainingTime % 60;
                
                // 更新标签文本
                const labelText = `快递正在流转，距终点${distance}千米，${hours}小时${minutes}分钟`;
                this.courierMarker.setLabel({
                    content: labelText,
                    direction: 'top',
                    offset: new AMap.Pixel(0, -30),
                    style: {
                        padding: '5px 10px',
                        'border-radius': '3px',
                        'background-color': '#fff',
                        'border': '1px solid #ccc',
                        'box-shadow': '0 2px 6px 0 rgba(114, 124, 245, .5)',
                        'color': '#333',
                        'font-size': '12px',
                        'white-space': 'nowrap'
                    }
                });
            }
        },
        // 计算当前位置（根据起点、终点和剩余距离）
        calculateCurrentPosition(data) {
            const startPoint = [data.senderLocation.longitude, data.senderLocation.latitude];
            const endPoint = [data.receiverLocation.longitude, data.receiverLocation.latitude];
            
            // 如果已经送达，返回终点位置
            if (data.order.status === 'DELIVERED') {
                return endPoint;
            }
            
            // 计算已行进的比例
            const progress = 1 - (this.distanceFinsh / this.totalPathDistance);
            
            // 线性插值计算当前位置
            return [
                startPoint[0] + (endPoint[0] - startPoint[0]) * progress,
                startPoint[1] + (endPoint[1] - startPoint[1]) * progress
            ];
        }
    }
}; 