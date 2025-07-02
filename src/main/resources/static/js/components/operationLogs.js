/**
 * 操作日志组件
 * 用于显示系统敏感操作日志记录
 */
class OperationLogComponent {
    constructor() {
        this.logs = [];
        this.logTypes = ['DELETE_ORDER', 'UPDATE_ORDER_STATUS', 'OTHER'];
    }

    /**
     * 初始化组件
     */
    init() {
        this.loadLogs();
        this.initEventListeners();
    }

    /**
     * 加载日志数据
     */
    loadLogs() {
        fetch('/api/operation-logs')
            .then(response => response.json())
            .then(data => {
                this.logs = data;
                this.renderLogs();
            })
            .catch(error => {
                console.error('加载操作日志失败:', error);
                this.showError('加载操作日志失败，请稍后重试');
            });
    }

    /**
     * 初始化事件监听器
     */
    initEventListeners() {
        // 日志类型过滤
        document.querySelectorAll('.log-type-filter').forEach(element => {
            element.addEventListener('click', event => {
                const type = event.target.getAttribute('data-type');
                this.filterLogsByType(type);
            });
        });

        // 时间范围查询
        document.getElementById('log-date-filter-btn')?.addEventListener('click', () => {
            const startDate = document.getElementById('log-start-date')?.value;
            const endDate = document.getElementById('log-end-date')?.value;

            if (startDate && endDate) {
                this.filterLogsByDateRange(startDate, endDate);
            } else {
                this.showError('请选择开始和结束日期');
            }
        });
    }

    /**
     * 根据类型过滤日志
     */
    filterLogsByType(type) {
        if (!type || type === 'ALL') {
            this.loadLogs();
            return;
        }

        fetch(`/api/operation-logs/type/${type}`)
            .then(response => response.json())
            .then(data => {
                this.logs = data;
                this.renderLogs();
            })
            .catch(error => {
                console.error('过滤日志失败:', error);
                this.showError('过滤日志失败，请稍后重试');
            });
    }

    /**
     * 根据日期范围过滤日志
     */
    filterLogsByDateRange(startDate, endDate) {
        // 转换为ISO格式的日期时间
        const start = new Date(startDate).toISOString();
        const end = new Date(endDate).toISOString();

        fetch(`/api/operation-logs/time-range?start=${start}&end=${end}`)
            .then(response => response.json())
            .then(data => {
                this.logs = data;
                this.renderLogs();
            })
            .catch(error => {
                console.error('按时间范围过滤日志失败:', error);
                this.showError('按时间范围过滤日志失败，请稍后重试');
            });
    }

    /**
     * 渲染日志列表
     */
    renderLogs() {
        const logContainer = document.getElementById('operation-log-list');
        if (!logContainer) return;

        logContainer.innerHTML = '';

        if (this.logs.length === 0) {
            logContainer.innerHTML = '<div class="alert alert-info">暂无操作日志记录</div>';
            return;
        }

        // 创建日志表格
        const table = document.createElement('table');
        table.className = 'table table-striped table-hover';

        // 表头
        const thead = document.createElement('thead');
        thead.innerHTML = `
            <tr>
                <th>操作时间</th>
                <th>操作类型</th>
                <th>操作描述</th>
                <th>操作人</th>
                <th>目标ID</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
        `;
        table.appendChild(thead);

        // 表体
        const tbody = document.createElement('tbody');
        this.logs.forEach(log => {
            const tr = document.createElement('tr');

            // 格式化时间
            const date = new Date(log.operationTime);
            const formattedDate = `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;

            tr.innerHTML = `
                <td>${formattedDate}</td>
                <td>${log.operationType || '-'}</td>
                <td>${log.operationDescription || '-'}</td>
                <td>${log.operatorName || '-'} (${log.operatorId || '-'})</td>
                <td>${log.targetId || '-'}</td>
                <td>${log.success ? '<span class="text-success">成功</span>' : '<span class="text-danger">失败</span>'}</td>
                <td>
                    <button class="btn btn-sm btn-info view-log-details" data-id="${log.id}">查看详情</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);

        logContainer.appendChild(table);

        // 为详情按钮添加事件监听
        document.querySelectorAll('.view-log-details').forEach(button => {
            button.addEventListener('click', event => {
                const logId = event.target.getAttribute('data-id');
                this.showLogDetails(logId);
            });
        });
    }

    /**
     * 显示日志详情
     */
    showLogDetails(logId) {
        fetch(`/api/operation-logs/${logId}`)
            .then(response => response.json())
            .then(log => {
                // 显示详情模态框
                const modal = document.getElementById('log-details-modal');
                if (!modal) {
                    this.createLogDetailsModal();
                }

                document.getElementById('log-details-title').textContent = `操作日志详情 - ${log.operationDescription}`;
                document.getElementById('log-details-time').textContent = new Date(log.operationTime).toLocaleString();
                document.getElementById('log-details-type').textContent = log.operationType;
                document.getElementById('log-details-operator').textContent = `${log.operatorName} (${log.operatorId})`;
                document.getElementById('log-details-target').textContent = `${log.targetType} - ${log.targetId}`;
                document.getElementById('log-details-status').textContent = log.success ? '成功' : '失败';
                document.getElementById('log-details-ip').textContent = log.ip || 'N/A';
                document.getElementById('log-details-content').textContent = log.details || '无详细信息';

                // 显示模态框
                const modalElement = new bootstrap.Modal(document.getElementById('log-details-modal'));
                modalElement.show();
            })
            .catch(error => {
                console.error('获取日志详情失败:', error);
                this.showError('获取日志详情失败，请稍后重试');
            });
    }

    /**
     * 创建日志详情模态框
     */
    createLogDetailsModal() {
        const modalDiv = document.createElement('div');
        modalDiv.className = 'modal fade';
        modalDiv.id = 'log-details-modal';
        modalDiv.tabIndex = '-1';
        modalDiv.setAttribute('aria-labelledby', 'log-details-title');
        modalDiv.setAttribute('aria-hidden', 'true');

        modalDiv.innerHTML = `
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="log-details-title">操作日志详情</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">操作时间:</div>
                            <div class="col-md-9" id="log-details-time"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">操作类型:</div>
                            <div class="col-md-9" id="log-details-type"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">操作人:</div>
                            <div class="col-md-9" id="log-details-operator"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">目标对象:</div>
                            <div class="col-md-9" id="log-details-target"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">操作结果:</div>
                            <div class="col-md-9" id="log-details-status"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">IP地址:</div>
                            <div class="col-md-9" id="log-details-ip"></div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-md-3 fw-bold">详细信息:</div>
                            <div class="col-md-9">
                                <pre id="log-details-content" class="bg-light p-2"></pre>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(modalDiv);
    }

    /**
     * 显示错误信息
     */
    showError(message) {
        alert(message);
    }
}

// 导出组件
window.OperationLogComponent = new OperationLogComponent(); 