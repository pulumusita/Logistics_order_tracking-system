// 定义统计报表组件
const statistics = {
    name: 'statistics',
    template: `
        <div class="container">
            <h2 class="mb-4">统计报表</h2>
            
            <!-- 时间范围选择 -->
            <el-form :inline="true" class="mb-4">
                <el-form-item>
                    <el-date-picker
                        v-model="dateRange"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD">
                    </el-date-picker>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="generateReport">生成报表</el-button>
                </el-form-item>
            </el-form>
            
            <!-- 统计卡片 -->
            <el-row :gutter="20" class="mb-4">
                <el-col :span="8">
                    <el-card shadow="hover">
                        <template #header>
                            <div class="d-flex justify-content-between align-items-center">
                                <span>总订单数</span>
                                <el-tag type="info">{{stats.totalOrders}}</el-tag>
                            </div>
                        </template>
                        <div class="text-center">
                            <h3>{{stats.totalOrders}}</h3>
                            <p class="text-muted">订单总量</p>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="8">
                    <el-card shadow="hover">
                        <template #header>
                            <div class="d-flex justify-content-between align-items-center">
                                <span>完成订单数</span>
                                <el-tag type="success">{{stats.completedOrders}}</el-tag>
                            </div>
                        </template>
                        <div class="text-center">
                            <h3>{{stats.completedOrders}}</h3>
                            <p class="text-muted">已完成订单</p>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="8">
                    <el-card shadow="hover">
                        <template #header>
                            <div class="d-flex justify-content-between align-items-center">
                                <span>总配送距离</span>
                                <el-tag type="warning">{{(Number(stats.totalDistance)/1000).toFixed(1)}} km</el-tag>
                            </div>
                        </template>
                        <div class="text-center">
                            <h3>{{(Number(stats.totalDistance)/1000).toFixed(1)}}</h3>
                            <p class="text-muted">公里</p>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
            
            <!-- 配送员排行榜 -->
            <el-card class="mb-4" style="margin-bottom: 20px;">
                <template #header>
                    <div class="d-flex justify-content-between align-items-center">
                        <span>配送员排行榜</span>
                        <span class="text-muted">按配送单数排序</span>
                    </div>
                </template>
                <div style="padding: 0;">
                    <el-table 
                        :data="courierRankings" 
                        border 
                        style="width: 100%; margin: 0;"
                        :max-height="500">
                        <el-table-column type="index" label="排名" width="120"></el-table-column>
                        <el-table-column prop="courierName" label="配送员" width="180"></el-table-column>
                        <el-table-column prop="deliveries" label="配送单数" width="180">
                            <template #default="scope">
                                <el-tag type="success">{{scope.row.deliveries}}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="总配送距离" width="200">
                            <template #default="scope">
                                <el-tag type="warning">{{(Number(scope.row.totalDistance)/1000).toFixed(1)}} km</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="评分" min-width="220">
                            <template #default="scope">
                                <el-rate
                                    v-model="scope.row.averageRating"
                                    disabled
                                    show-score
                                    text-color="#ff9900"
                                    score-template="{value}">
                                </el-rate>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-card>
        </div>
    `,
    
    data() {
        return {
            dateRange: [],
            stats: {
                totalOrders: 0,
                completedOrders: 0,
                totalDistance: 0
            },
            courierRankings: []
        }
    },
    
    methods: {
        // 生成报表
        async generateReport() {
            if (!this.dateRange || this.dateRange.length !== 2) {
                this.$message.warning('请选择时间范围');
                return;
            }
            
            const [startDate, endDate] = this.dateRange;
            
            try {
                // 获取统计数据
                const [summary, couriers] = await Promise.all([
                    axios.get('/api/statistics/summary', {
                        params: {startDate, endDate}
                    }),
                    axios.get('/api/couriers')
                ]);
                
                this.stats = summary.data;
                
                // 处理配送员数据
                this.courierRankings = couriers.data.map(courier => ({
                    courierName: courier.name,
                    deliveries: courier.completedOrders,
                    totalDistance: courier.totalDistance,
                    averageRating: courier.averageRating
                }));
                
                // 按配送单数排序
                this.courierRankings.sort((a, b) => b.deliveries - a.deliveries);
                
            } catch (error) {
                this.$message.error('获取统计数据失败');
                console.error(error);
            }
        }
    },
    
    mounted() {
        // 设置默认时间范围为最近7天
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - 7);
        
        this.dateRange = [
            start.toISOString().split('T')[0],
            end.toISOString().split('T')[0]
        ];
        
        this.generateReport();
    }
}; 