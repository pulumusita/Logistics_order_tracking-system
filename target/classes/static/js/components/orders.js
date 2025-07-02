const orders = {
    name: 'orders',
    template: `
        <div class="orders-container">
            <el-card class="orders-header">
                <div class="header-actions">
                    <el-button type="primary" @click="createOrder">
                        新建订单
                    </el-button>
                    <el-input
                        v-model="searchQuery"
                        placeholder="搜索订单号/邮箱/地址"
                        clearable
                        class="search-input"
                        @input="handleSearch">
                    </el-input>
                </div>
            </el-card>

            <el-card class="orders-table">
                <el-table
                    v-loading="loading"
                    :data="filteredOrders"
                    style="width: 100%">
                    <el-table-column prop="id" label="订单号" width="180"></el-table-column>
                    <el-table-column prop="createTime" label="创建时间" width="180">
                        <template #default="scope">
                            {{ formatTime(scope.row.createTime) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template #default="scope">
                            <el-tag :type="getStatusType(scope.row.status)">
                                {{ getStatusText(scope.row.status) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="senderName" label="发件人"></el-table-column>
                    <el-table-column prop="senderAddress" label="发件地址"></el-table-column>
                    <el-table-column prop="receiverName" label="收件人"></el-table-column>
                    <el-table-column prop="receiverAddress" label="收件地址"></el-table-column>
                    <el-table-column label="操作" width="200">
                        <template #default="scope">
                            <el-button-group>
                                <el-button size="small" @click="viewOrder(scope.row)">
                                    查看
                                </el-button>
                                <el-button 
                                    size="small" 
                                    type="primary" 
                                    @click="showDispatchDialog(scope.row)"
                                    v-if="scope.row.status === 'PENDING'">
                                    派发
                                </el-button>
                                <el-button size="small" type="danger" @click="deleteOrder(scope.row)">
                                    删除
                                </el-button>
                            </el-button-group>
                        </template>
                    </el-table-column>
                </el-table>

                <div class="pagination-container">
                    <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :page-sizes="[10, 20, 50, 100]"
                        layout="total, sizes, prev, pager, next"
                        :total="total"
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange">
                    </el-pagination>
                </div>
            </el-card>

            <!-- 订单详情对话框 -->
            <el-dialog
                v-model="detailDialogVisible"
                title="订单详情"
                width="60%">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="订单号">{{currentOrder.id}}</el-descriptions-item>
                    <el-descriptions-item label="创建时间">{{formatTime(currentOrder.createTime)}}</el-descriptions-item>
                    <el-descriptions-item label="订单状态">
                        <el-tag :type="getStatusType(currentOrder.status)">
                            {{getStatusText(currentOrder.status)}}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="派发时间" v-if="currentOrder.dispatchTime">
                        {{formatTime(currentOrder.dispatchTime)}}
                    </el-descriptions-item>
                    <el-descriptions-item label="取件时间" v-if="currentOrder.pickupTime">
                        {{formatTime(currentOrder.pickupTime)}}
                    </el-descriptions-item>
                    <el-descriptions-item label="送达时间" v-if="currentOrder.deliveryTime">
                        {{formatTime(currentOrder.deliveryTime)}}
                    </el-descriptions-item>
                    <el-descriptions-item label="发件人">{{currentOrder.senderName}}</el-descriptions-item>
                    <el-descriptions-item label="发件人邮箱">{{currentOrder.senderEmail}}</el-descriptions-item>
                    <el-descriptions-item label="发件地址">{{currentOrder.senderAddress}}</el-descriptions-item>
                    <el-descriptions-item label="收件人">{{currentOrder.receiverName}}</el-descriptions-item>
                    <el-descriptions-item label="收件人邮箱">{{currentOrder.receiverEmail}}</el-descriptions-item>
                    <el-descriptions-item label="收件地址">{{currentOrder.receiverAddress}}</el-descriptions-item>
                    <el-descriptions-item label="物品重量">{{currentOrder.weight}} kg</el-descriptions-item>
                    <el-descriptions-item label="运费">{{currentOrder.amount}} 元</el-descriptions-item>
                    <el-descriptions-item label="快递员" v-if="currentOrder.courierName">
                        {{currentOrder.courierName}}
                    </el-descriptions-item>
                    <el-descriptions-item label="快递员电话" v-if="currentOrder.courierPhone">
                        {{currentOrder.courierPhone}}
                    </el-descriptions-item>
                    <el-descriptions-item label="备注" :span="2">{{currentOrder.remarks}}</el-descriptions-item>
                </el-descriptions>
            </el-dialog>

            <!-- 派发订单对话框 -->
            <el-dialog
                v-model="dispatchDialogVisible"
                title="派发订单"
                width="40%">
                <el-form>
                    <el-form-item label="选择快递员">
                        <el-select v-model="selectedCourier" placeholder="请选择快递员">
                            <el-option
                                v-for="courier in couriers"
                                :key="courier.id"
                                :label="courier.name"
                                :value="courier.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="dispatchDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="dispatchOrder">确定</el-button>
                    </span>
                </template>
            </el-dialog>

            <!-- 创建订单对话框 -->
            <el-dialog
                v-model="orderDialogVisible"
                title="创建订单"
                width="60%">
                <el-form
                    ref="orderForm"
                    :model="currentOrder"
                    :rules="orderRules"
                    label-width="100px">
                    <el-form-item label="发件人" prop="senderName">
                        <el-input v-model="currentOrder.senderName"></el-input>
                    </el-form-item>
                    <el-form-item label="发件人邮箱" prop="senderEmail">
                        <el-input v-model="currentOrder.senderEmail" type="email"></el-input>
                    </el-form-item>
                    <el-form-item label="发件地址" prop="senderAddress">
                        <el-input v-model="currentOrder.senderAddress"></el-input>
                    </el-form-item>
                    <el-form-item label="收件人" prop="receiverName">
                        <el-input v-model="currentOrder.receiverName"></el-input>
                    </el-form-item>
                    <el-form-item label="收件人邮箱" prop="receiverEmail">
                        <el-input v-model="currentOrder.receiverEmail" type="email"></el-input>
                    </el-form-item>
                    <el-form-item label="收件地址" prop="receiverAddress">
                        <el-input v-model="currentOrder.receiverAddress"></el-input>
                    </el-form-item>
                    <el-form-item label="物品重量" prop="weight">
                        <el-input-number v-model="currentOrder.weight" :min="0.1" :step="0.1"></el-input-number>
                        <span class="unit">kg</span>
                    </el-form-item>
                    <el-form-item label="备注" prop="remarks">
                        <el-input type="textarea" v-model="currentOrder.remarks"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="orderDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="saveOrder">确定</el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
    `,
    data() {
        return {
            loading: false,
            orders: [],
            couriers: [], // 快递员列表
            searchQuery: '',
            currentPage: 1,
            pageSize: 10,
            total: 0,
            orderDialogVisible: false,
            detailDialogVisible: false,
            dispatchDialogVisible: false,
            dialogTitle: '',
            currentOrder: {
                senderName: '',
                senderEmail: '',
                senderAddress: '',
                receiverName: '',
                receiverEmail: '',
                receiverAddress: '',
                weight: 1,
                remarks: ''
            },
            selectedCourier: '',
            orderRules: {
                senderName: [
                    { required: true, message: '请输入发件人姓名', trigger: 'blur' }
                ],
                senderEmail: [
                    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
                    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
                ],
                senderAddress: [
                    { required: true, message: '请输入发件地址', trigger: 'blur' }
                ],
                receiverName: [
                    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
                ],
                receiverEmail: [
                    { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
                    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
                ],
                receiverAddress: [
                    { required: true, message: '请输入收件地址', trigger: 'blur' }
                ],
                weight: [
                    { required: true, message: '请输入物品重量', trigger: 'blur' },
                    { type: 'number', min: 0.1, message: '重量必须大于0.1kg', trigger: 'blur' }
                ]
            }
        }
    },
    computed: {
        filteredOrders() {
            if (!this.searchQuery) {
                return this.orders;
            }
            const query = this.searchQuery.toLowerCase();
            return this.orders.filter(order => {
                return order.id?.toLowerCase().includes(query) ||
                    order.senderEmail?.includes(query) ||
                    order.receiverEmail?.includes(query) ||
                    order.senderAddress?.toLowerCase().includes(query) ||
                    order.receiverAddress?.toLowerCase().includes(query);
            });
        }
    },
    mounted() {
        this.fetchOrders();
        this.fetchCouriers();
        // 启动定时任务，每分钟检查一次订单状态
        this.startStatusCheckTimer();
    },
    beforeUnmount() {
        // 清除定时器
        if (this.statusCheckTimer) {
            clearInterval(this.statusCheckTimer);
        }
    },
    methods: {
        startStatusCheckTimer() {
            // 每分钟检查一次订单状态
            this.statusCheckTimer = setInterval(() => {
                this.checkOrdersStatus();
            }, 60000);
        },

        async checkOrdersStatus() {
            try {
                const response = await axios.get('/api/orders/check-status');
                if (response.data.updated) {
                    // 如果有订单状态更新，重新获取订单列表
                    this.fetchOrders();
                }
            } catch (error) {
                console.error('检查订单状态失败:', error);
            }
        },

        async fetchOrders() {
            this.loading = true;
            try {
                const response = await axios.get('/api/orders');
                this.orders = response.data;
                this.total = response.data.length;
            } catch (error) {
                console.error('获取订单列表失败:', error);
                ElementPlus.ElMessage.error('获取订单列表失败');
            } finally {
                this.loading = false;
            }
        },

        async fetchCouriers() {
            try {
                const response = await axios.get('/api/couriers');
                this.couriers = response.data;
            } catch (error) {
                console.error('获取快递员列表失败:', error);
                ElementPlus.ElMessage.error('获取快递员列表失败');
            }
        },

        formatTime(timestamp) {
            if (!timestamp) return '';
            // 处理 ISO 8601 格式的时间字符串
            const date = new Date(timestamp);
            if (isNaN(date.getTime())) {
                return '';
            }
            // 格式化为 YYYY-MM-DD HH:mm:ss
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        },

        getStatusText(status) {
            const statusMap = {
                'PENDING': '待处理',
                'DISPATCHED': '已派发',
                'PICKED_UP': '已取件',
                'IN_TRANSIT': '运输中',
                'DELIVERED': '已送达'
            };
            return statusMap[status] || status;
        },

        getStatusType(status) {
            const typeMap = {
                'PENDING': 'info',
                'DISPATCHED': 'warning',
                'PICKED_UP': 'warning',
                'IN_TRANSIT': 'primary',
                'DELIVERED': 'success'
            };
            return typeMap[status] || 'info';
        },

        handleSearch() {
            this.currentPage = 1;
        },

        handleSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1;
        },

        handleCurrentChange(val) {
            this.currentPage = val;
        },

        createOrder() {
            this.currentOrder = {
                senderName: '',
                senderEmail: '',
                senderAddress: '',
                receiverName: '',
                receiverEmail: '',
                receiverAddress: '',
                weight: 1,
                remarks: ''
            };
            this.orderDialogVisible = true;
        },

        viewOrder(order) {
            this.currentOrder = {...order};
            this.detailDialogVisible = true;
        },

        showDispatchDialog(order) {
            this.currentOrder = order;
            this.selectedCourier = '';
            this.dispatchDialogVisible = true;
        },

        async dispatchOrder() {
            if (!this.selectedCourier) {
                ElementPlus.ElMessage.warning('请选择快递员');
                return;
            }

            try {
                const courier = this.couriers.find(c => c.id === this.selectedCourier);
                const response = await axios.put(`/api/orders/${this.currentOrder.id}/dispatch`, {
                    courierId: this.selectedCourier,
                    courierName: courier.name
                });

                if (response.data) {
                    ElementPlus.ElMessage.success('订单派发成功');
                    this.dispatchDialogVisible = false;
                    this.fetchOrders();
                }
            } catch (error) {
                console.error('派发订单失败:', error);
                ElementPlus.ElMessage.error('派发订单失败');
            }
        },

        async saveOrder() {
            try {
                const response = await axios.post('/api/orders', this.currentOrder);
                if (response.data) {
                    ElementPlus.ElMessage.success('订单创建成功');
                    this.orderDialogVisible = false;
                    this.fetchOrders();
                }
            } catch (error) {
                console.error('保存订单失败:', error);
                ElementPlus.ElMessage.error('保存订单失败');
            }
        },

        async deleteOrder(order) {
            try {
                await ElementPlus.ElMessageBox.confirm(
                    '确定要删除这个订单吗？',
                    '警告',
                    {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning',
                    }
                );

                await axios.delete(`/api/orders/${order.id}`);
                ElementPlus.ElMessage.success('订单删除成功');
                this.fetchOrders();
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('删除订单失败:', error);
                    ElementPlus.ElMessage.error('删除订单失败');
                }
            }
        }
    }
}; 