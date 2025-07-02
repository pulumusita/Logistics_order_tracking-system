// 定义配送员管理组件
const couriers = {
    name: 'couriers',
    data() {
        return {
            couriers: [],
            searchName: '',
            searchWorkArea: '',
            dialogVisible: false,
            detailDialogVisible: false,
            dialogType: 'create',
            courierForm: {
                name: '',
                phone: '',
                idCard: '',
                workArea: '',
                online: false
            },
            selectedCourier: {}
        }
    },
    template: `
        <div class="container">
            <h2 class="mb-4">配送员管理</h2>
            
            <!-- 配送员搜索 -->
            <el-form :inline="true" class="mb-4">
                <el-form-item>
                    <el-input v-model="searchName" placeholder="配送员姓名"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-select v-model="searchWorkArea" placeholder="工作区域">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="北京" value="北京"></el-option>
                        <el-option label="上海" value="上海"></el-option>
                        <el-option label="广州" value="广州"></el-option>
                        <el-option label="深圳" value="深圳"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="searchCouriers">搜索</el-button>
                    <el-button type="success" @click="showCreateDialog">新增配送员</el-button>
                </el-form-item>
            </el-form>
            
            <!-- 配送员列表 -->
            <el-table :data="couriers" border style="width: 100%">
                <el-table-column prop="name" label="姓名" width="120"></el-table-column>
                <el-table-column prop="phone" label="电话" width="150"></el-table-column>
                <el-table-column prop="workArea" label="工作区域" width="120"></el-table-column>
                <el-table-column prop="online" label="在线状态" width="100">
                    <template #default="scope">
                        <el-tag :type="scope.row.online ? 'success' : 'info'">
                            {{scope.row.online ? '在线' : '离线'}}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="averageRating" label="平均评分" width="100">
                    <template #default="scope">
                        <el-rate v-model="scope.row.averageRating" disabled show-score></el-rate>
                    </template>
                </el-table-column>
                <el-table-column label="总配送单数" width="120">
                    <template #default="scope">
                        {{scope.row.completedOrders || 0}}
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="scope">
                        <el-button size="small" @click="showDetailDialog(scope.row)">详情</el-button>
                        <el-button size="small" type="primary" @click="showEditDialog(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="deleteCourier(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            
            <!-- 新增/编辑配送员对话框 -->
            <el-dialog v-model="dialogVisible" :title="dialogType === 'create' ? '新增配送员' : '编辑配送员'" width="50%">
                <el-form :model="courierForm" label-width="100px">
                    <el-form-item label="姓名">
                        <el-input v-model="courierForm.name"></el-input>
                    </el-form-item>
                    <el-form-item label="电话">
                        <el-input v-model="courierForm.phone"></el-input>
                    </el-form-item>
                    <el-form-item label="身份证号">
                        <el-input v-model="courierForm.idCard"></el-input>
                    </el-form-item>
                    <el-form-item label="工作区域">
                        <el-select v-model="courierForm.workArea">
                            <el-option label="北京" value="北京"></el-option>
                            <el-option label="上海" value="上海"></el-option>
                            <el-option label="广州" value="广州"></el-option>
                            <el-option label="深圳" value="深圳"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="在线状态">
                        <el-switch
                            v-model="courierForm.online"
                            active-text="在线"
                            inactive-text="离线"
                            :active-value="true"
                            :inactive-value="false">
                        </el-switch>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="dialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="saveCourier">确定</el-button>
                    </span>
                </template>
            </el-dialog>
            
            <!-- 配送员详情对话框 -->
            <el-dialog v-model="detailDialogVisible" title="配送员详情" width="60%">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="姓名">{{selectedCourier.name}}</el-descriptions-item>
                    <el-descriptions-item label="电话">{{selectedCourier.phone}}</el-descriptions-item>
                    <el-descriptions-item label="工作区域">{{selectedCourier.workArea}}</el-descriptions-item>
                        <el-switch
                            v-model="selectedCourier.online"
                            active-text="在线"
                            inactive-text="离线"
                            :active-value="true"
                            :inactive-value="false"
                            @change="updateOnlineStatus">
                        </el-switch>
                    </el-descriptions-item>
                    <el-descriptions-item label="平均评分">
                        <el-rate v-model="selectedCourier.averageRating" disabled show-score></el-rate>
                    </el-descriptions-item>
                    <el-descriptions-item label="总配送单数">{{selectedCourier.completedOrders || 0}}</el-descriptions-item>
                    <el-descriptions-item label="今日配送单数">{{selectedCourier.dailyDeliveries || 0}}</el-descriptions-item>
                    <el-descriptions-item label="本月配送单数">{{selectedCourier.monthlyDeliveries || 0}}</el-descriptions-item>
                    <el-descriptions-item label="总配送距离">{{selectedCourier.totalDistance || 0}} 公里</el-descriptions-item>
                    <el-descriptions-item label="今日配送距离">{{selectedCourier.dailyDistance || 0}} 公里</el-descriptions-item>
                    <el-descriptions-item label="本月配送距离">{{selectedCourier.monthlyDistance || 0}} 公里</el-descriptions-item>
                    <el-descriptions-item label="投诉次数">{{selectedCourier.complaintsCount || 0}}</el-descriptions-item>
                </el-descriptions>
            </el-dialog>
        </div>
    `,
    methods: {
        // 获取配送员列表
        fetchCouriers() {
            axios.get('/api/couriers').then(response => {
                this.couriers = response.data;
            });
        },
        
        // 搜索配送员
        searchCouriers() {
            if (this.searchWorkArea) {
                // 如果选择了工作区域，使用工作区域API
                axios.get(`/api/couriers/work-area/${this.searchWorkArea}`).then(response => {
                    let filteredCouriers = response.data;
                    
                    // 如果还输入了姓名，在区域筛选的基础上继续筛选姓名
                    if (this.searchName) {
                        filteredCouriers = this.filterCouriersByName(filteredCouriers, this.searchName);
                    }
                    
                    this.couriers = filteredCouriers;
                }).catch(() => {
                    this.$message.error('获取配送员数据失败');
                });
            } else {
                // 如果没有选择工作区域，获取所有配送员
                axios.get('/api/couriers').then(response => {
                    let filteredCouriers = response.data;
                    
                    // 如果输入了姓名，筛选姓名
                    if (this.searchName) {
                        filteredCouriers = this.filterCouriersByName(filteredCouriers, this.searchName);
                    }
                    
                    this.couriers = filteredCouriers;
                }).catch(() => {
                    this.$message.error('获取配送员数据失败');
                });
            }
        },
        
        // 根据姓名筛选配送员（本地筛选）
        filterCouriersByName(couriers, name) {
            const searchName = name.toLowerCase();
            return couriers.filter(courier => 
                courier.name.toLowerCase().includes(searchName)
            );
        },
        
        // 显示新增对话框
        showCreateDialog() {
            this.dialogType = 'create';
            this.courierForm = {
                name: '',
                phone: '',
                idCard: '',
                workArea: '',
                online: false
            };
            this.dialogVisible = true;
        },
        
        // 显示编辑对话框
        showEditDialog(courier) {
            this.dialogType = 'edit';
            this.courierForm = {
                ...courier,
                online: courier.online || false
            };
            this.dialogVisible = true;
        },
        
        // 显示详情对话框
        showDetailDialog(courier) {
            this.selectedCourier = courier;
            this.detailDialogVisible = true;
        },
        
        // 保存配送员
        saveCourier() {
            const request = this.dialogType === 'create' 
                ? axios.post('/api/couriers', this.courierForm)
                : axios.put(`/api/couriers/${this.courierForm.id}`, this.courierForm);
                
            request.then(() => {
                this.$message.success(this.dialogType === 'create' ? '配送员添加成功' : '配送员信息更新成功');
                this.dialogVisible = false;
                this.fetchCouriers();
            });
        },
        
        // 删除配送员
        deleteCourier(courier) {
            this.$confirm('确认删除该配送员?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                axios.delete(`/api/couriers/${courier.id}`).then(() => {
                    this.$message.success('配送员删除成功');
                    this.fetchCouriers();
                });
            });
        },
        
        // 更新在线状态
        updateOnlineStatus(status) {
            axios.put(`/api/couriers/${this.selectedCourier.id}/online-status?online=${status}`).then(() => {
                this.$message.success(`快递员状态已更新为${status ? '在线' : '离线'}`);
                this.fetchCouriers();
            }).catch(() => {
                this.$message.error('状态更新失败');
                this.selectedCourier.online = !status; // 恢复原状态
            });
        }
    },
    mounted() {
        this.fetchCouriers();
    }
}; 