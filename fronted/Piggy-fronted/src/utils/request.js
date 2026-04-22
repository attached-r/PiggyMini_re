import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:9000',
  timeout: 10000,
});

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('piggy_token');
    const userId = localStorage.getItem('piggy_user_id');
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    if (userId) {
      config.headers['X-User-Id'] = userId;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

let isRefreshing = false;
let requests = [];

// Response interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      // In a real app, we'd use a toast library here
      console.error(res.message || 'Error');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  async (error) => {
    const { config, response } = error;
    
    if (response && response.status === 401) {
      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const refreshToken = localStorage.getItem('piggy_refresh_token');
          if (!refreshToken) throw new Error('No refresh token');

          // 调用刷新 token 接口
          const res = await axios.post('http://localhost:9000/api/auth/refresh', {
            refreshToken
          });

          if (res.data.code === 200) {
            const { token, refreshToken: newRefreshToken, userId } = res.data.data;
            localStorage.setItem('piggy_token', token);
            localStorage.setItem('piggy_refresh_token', newRefreshToken);
            localStorage.setItem('piggy_user_id', userId);
            
            // Retry queued requests
            requests.forEach((cb) => cb(token));
            requests = [];
            
            return request(config);
          } else {
            throw new Error('Refresh token failed');
          }
        } catch (err) {
          console.error('Session expired, logging out...');
          localStorage.clear();
          window.location.href = '/login';
          return Promise.reject(err);
        } finally {
          isRefreshing = false;
        }
      } else {
        // Return a promise that resolves when the token is refreshed
        return new Promise((resolve) => {
          requests.push((token) => {
            config.headers['Authorization'] = `Bearer ${token}`;
            resolve(request(config));
          });
        });
      }
    }
    
    return Promise.reject(error);
  }
);

export default request;
