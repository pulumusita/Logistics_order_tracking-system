/**
 * 操作日志组件
 * 用于显示系统敏感操作日志记录
 */
const operationLogs = {
    data() {
        return {
            logs: [],
            logTypes: ['DELETE_ORDER', 'UPDATE_ORDER_STATUS', 'OTHER'],
            startDate: '',
            endDate: '',
            loading: false,
            dialogVisible: false,
            currentLog: null,
            selectedType: 'ALL'
        };
    },
    mounted() {
        this.loadLogs();
    },
    methods: {
        /**
         * 加载日志数据
         */
        loadLogs() {
            this.loading = true;
            axios.get('/api/operation-logs')
                .then(response => {
                    this.logs = response.data;
                })
                .catch(error => {
                    console.error('加载操作日志失败:', error);
                    ElementPlus.ElMessage.error('加载操作日志失败，请稍后重试');
                })
                .finally(() => {
                    this.loading = false;
                });
        },

        /**
         * 根据类型过滤日志
         */
        filterLogsByType(type) {
            if (!type || type === 'ALL') {
                this.loadLogs();
                return;
            }

            this.loading = true;
            axios.get(`/api/operation-logs/type/${type}`)
                .then(response => {
                    this.logs = response.data;
                })
                .catch(error => {
                    console.error('过滤日志失败:', error);
                    ElementPlus.ElMessage.error('过滤日志失败，请稍后重试');
                })
                .finally(() => {
                    this.loading = false;
                });
        },

        /**
         * 根据日期范围过滤日志
         */
        filterLogsByDateRange() {
            if (!this.startDate || !this.endDate) {
                ElementPlus.ElMessage.warning('请选择开始和结束日期');
                return;
            }
            
            // 转换为ISO格式的日期时间
            const start = new Date(this.startDate).toISOString();
            const end = new Date(this.endDate).toISOString();

            this.loading = true;
            axios.get(`/api/operation-logs/time-range?start=${start}&end=${end}`)
                .then(response => {
                    this.logs = response.data;
                })
                .catch(error => {
                    console.error('按时间范围过滤日志失败:', error);
                    ElementPlus.ElMessage.error('按时间范围过滤日志失败，请稍后重试');
                })
                .finally(() => {
                    this.loading = false;
                });
        },

        /**
         * 显示日志详情
         */
        showLogDetails(logId) {
            this.loading = true;
            axios.get(`/api/operation-logs/${logId}`)
                .then(response => {
                    this.currentLog = response.data;
                    this.dialogVisible = true;
                })
                .catch(error => {
                    console.error('获取日志详情失败:', error);
                    ElementPlus.ElMessage.error('获取日志详情失败，请稍后重试');
                })
                .finally(() => {
                    this.loading = false;
                });
        },

        /**
         * 格式化日期
         */
        formatDate(dateString) {
            const date = new Date(dateString);
            return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
        },

        /**
         * 获取操作类型显示文本
         */
        getOperationTypeText(type) {
            const typeMap = {
                'DELETE_ORDER': '删除订单',
                'UPDATE_ORDER_STATUS': '更新订单状态',
                'OTHER': '其他操作'
            };
            return typeMap[type] || type;
        },

        /**
         * 获取操作类型标签样式
         */
        getOperationTypeStyle(type) {
            const styleMap = {
                'DELETE_ORDER': 'danger',
                'UPDATE_ORDER_STATUS': 'warning',
                'OTHER': 'info'
            };
            return styleMap[type] || 'info';
        }
    },
    template: `
        <div class="operation-logs-container p-4">
            <div class="mb-4">
                <h2 class="text-2xl font-bold text-gray-800 mb-4">操作日志</h2>
                
                <el-card class="filter-card mb-4" shadow="hover">
                    <div class="filter-section">
                        <el-row :gutter="20">
                            <el-col :span="24" class="mb-4">
                                <div class="filter-group">
                                    <span class="mr-2 font-medium">日志类型：</span>
                                    <el-radio-group v-model="selectedType" @change="filterLogsByType" size="large">
                                        <el-radio-button label="ALL">全部</el-radio-button>
                                        <el-radio-button v-for="type in logTypes" :key="type" :label="type">
                                            {{getOperationTypeText(type)}}
                                        </el-radio-button>
                                    </el-radio-group>
                                </div>
                            </el-col>
                            <el-col :span="24">
                                <div class="date-filter flex items-center">
                                    <span class="mr-2 font-medium">时间范围：</span>
                                    <el-date-picker
                                        v-model="startDate"
                                        type="datetime"
                                        placeholder="开始日期"
                                        format="YYYY-MM-DD HH:mm:ss"
                                        class="mr-2"
                                        size="large"
                                    ></el-date-picker>
                                    <span class="mx-2">至</span>
                                    <el-date-picker
                                        v-model="endDate"
                                        type="datetime"
                                        placeholder="结束日期"
                                        format="YYYY-MM-DD HH:mm:ss"
                                        class="mr-2"
                                        size="large"
                                    ></el-date-picker>
                                    <el-button type="primary" @click="filterLogsByDateRange" size="large">
                                        查询
                                    </el-button>
                                </div>
                            </el-col>
                        </el-row>
                    </div>
                </el-card>
            </div>
            
            <el-table 
                :data="logs" 
                style="width: 100%" 
                v-loading="loading"
                border
                stripe
                class="mb-4"
                :header-cell-style="{
                    background: '#f5f7fa',
                    color: '#606266',
                    fontWeight: 'bold'
                }"
            >
                <el-table-column prop="operationTime" label="操作时间" width="180" fixed>
                    <template #default="scope">
                        {{ formatDate(scope.row.operationTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="operationType" label="操作类型" width="150">
                    <template #default="scope">
                        <el-tag 
                            :type="getOperationTypeStyle(scope.row.operationType)"
                            effect="plain"
                        >
                            {{getOperationTypeText(scope.row.operationType)}}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="operationDescription" label="操作描述" min-width="200" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作人" width="180">
                    <template #default="scope">
                        <el-tooltip 
                            :content="scope.row.operatorId" 
                            placement="top" 
                            effect="light"
                        >
                            <span class="operator-name">
                                {{ scope.row.operatorName || '-' }}
                            </span>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="targetId" label="目标ID" width="180" show-overflow-tooltip></el-table-column>
                <el-table-column label="状态" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.success ? 'success' : 'danger'" effect="dark">
                            {{ scope.row.success ? '成功' : '失败' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right" align="center">
                    <template #default="scope">
                        <el-button 
                            type="primary" 
                            link
                            @click="showLogDetails(scope.row.id)"
                        >
                            详情
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            
            <!-- 日志详情对话框 -->
            <el-dialog
                v-model="dialogVisible"
                title="操作日志详情"
                width="60%"
                destroy-on-close
                :close-on-click-modal="false"
                class="operation-log-detail-dialog"
            >
                <div class="px-4" style="overflow: hidden">
                    <el-descriptions 
                        :column="2" 
                        border 
                        class="operation-log-descriptions"
                    >
                        <el-descriptions-item label="操作时间">
                            {{ formatDate(currentLog?.operationTime) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="操作类型">
                            <el-tag 
                                :type="getOperationTypeStyle(currentLog?.operationType)"
                                effect="plain"
                            >
                                {{ getOperationTypeText(currentLog?.operationType) }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="操作人">
                            {{ currentLog?.operatorName }} ({{ currentLog?.operatorId }})
                        </el-descriptions-item>
                        <el-descriptions-item label="操作状态">
                            <el-tag :type="currentLog?.success ? 'success' : 'danger'" effect="dark">
                                {{ currentLog?.success ? '成功' : '失败' }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="目标对象" :span="2">
                            {{ currentLog?.targetType }} - {{ currentLog?.targetId }}
                        </el-descriptions-item>
                        <el-descriptions-item label="IP地址">
                            {{ currentLog?.ip || 'N/A' }}
                        </el-descriptions-item>
                        <el-descriptions-item label="操作描述">
                            {{ currentLog?.operationDescription }}
                        </el-descriptions-item>
                        <el-descriptions-item label="详细信息" :span="2">
                            <pre class="bg-gray-50 p-4 rounded-lg text-sm font-mono whitespace-pre-wrap">{{ currentLog?.details || '无详细信息' }}</pre>
                        </el-descriptions-item>
                    </el-descriptions>
                </div>
                <template #footer>
                    <div class="dialog-footer px-4">
                        <el-button @click="dialogVisible = false">关闭</el-button>
                    </div>
                </template>
            </el-dialog>

            <style>
            .operation-log-detail-dialog .el-dialog__body {
                overflow: hidden;
                padding: 10px 0;
            }
            .operation-log-descriptions {
                margin: 0;
            }
            </style>
        </div>
    `
};

// 导出组件
window.operationLogs = operationLogs; 