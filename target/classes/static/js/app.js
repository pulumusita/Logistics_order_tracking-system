// 等待页面加载完成
document.addEventListener('DOMContentLoaded', () => {
    // 配置Axios默认值
    axios.defaults.baseURL = 'http://localhost:8080';
    axios.defaults.headers.common['Content-Type'] = 'application/json';
    
    // 添加请求拦截器
    axios.interceptors.request.use(config => {
        console.log('发送请求:', config);
        return config;
    }, error => {
        console.error('请求错误:', error);
        return Promise.reject(error);
    });

    // 添加响应拦截器
    axios.interceptors.response.use(response => {
        console.log('收到响应:', response);
        return response;
    }, error => {
        console.error('响应错误:', error);
        ElementPlus.ElMessage.error(error.response?.data?.message || '请求失败');
        return Promise.reject(error);
    });
    
    // 创建Vue应用
    const app = Vue.createApp({
        data() {
            return {
                currentView: 'orders'
            }
        }
    });

    // 使用Element Plus
    app.use(ElementPlus);

    // 注册组件
    app.component('orders', orders);
    app.component('couriers', couriers);
    app.component('tracking', tracking);
    app.component('statistics', statistics);
    app.component('operationLogs', {
        data() {
            return {
                logComponent: window.OperationLogComponent
            }
        },
        mounted() {
            this.logComponent.init();
        },
        template: `
            <div class="operation-logs-container">
                <h2>操作日志</h2>
                <div id="operation-log-list"></div>
            </div>
        `
    });

    // 挂载应用
    app.mount('#app');
}); 