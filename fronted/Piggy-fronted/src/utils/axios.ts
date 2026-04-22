import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 不需要 Token 的白名单路径
const whiteList = ['/api/auth/login', '/api/auth/register'];

// 从 localStorage 获取 token
const getToken = () => localStorage.getItem('piggy_token');

// 清除登录状态
const clearAuth = () => {
  localStorage.removeItem('piggy_token');
  localStorage.removeItem('piggy_refresh_token');
  localStorage.removeItem('piggy_user_id');
  localStorage.removeItem('userRole');
  localStorage.removeItem('username');
  localStorage.removeItem('nickname');
  localStorage.removeItem('selectedAccount');
};

// 请求拦截器 - 自动添加 Token
apiClient.interceptors.request.use(
  (config) => {
    // 检查是否在白名单中
    const isWhiteListed = whiteList.some((path) => config.url.includes(path));

    if (!isWhiteListed) {
      const token = getToken();
      if (token) {
        // 直接放 token 字符串，不加 "Bearer " 前缀
        config.headers['Authorization'] = token;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理响应和 401
apiClient.interceptors.response.use(
  (response) => {
    // 返回完整的 response，让调用方通过 response.data 获取实际数据
    return response;
  },
  (error) => {
    const { response } = error;

    // 如果返回 401，自动调用 logout 并跳转 /login
    if (response && response.status === 401) {
      console.warn('Unauthorized, logging out...');
      clearAuth();
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

export default apiClient;
