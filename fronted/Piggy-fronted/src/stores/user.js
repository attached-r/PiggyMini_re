import { defineStore } from 'pinia';
import { ref } from 'vue';
import request from '@/utils/request';

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('piggy_token') || '');
  const refreshToken = ref(localStorage.getItem('piggy_refresh_token') || '');
  const userId = ref(localStorage.getItem('piggy_user_id') || '');
  const username = ref(localStorage.getItem('piggy_username') || '');
  const nickname = ref(localStorage.getItem('piggy_nickname') || '');

  // Actions
  const setUserInfo = (data) => {
    token.value = data.token;
    refreshToken.value = data.refreshToken;
    userId.value = data.userId;
    username.value = data.username;
    nickname.value = data.nickname;

    localStorage.setItem('piggy_token', data.token);
    localStorage.setItem('piggy_refresh_token', data.refreshToken);
    localStorage.setItem('piggy_user_id', data.userId);
    localStorage.setItem('piggy_username', data.username);
    localStorage.setItem('piggy_nickname', data.nickname);
  };

  const clearUserInfo = () => {
    token.value = '';
    refreshToken.value = '';
    userId.value = '';
    username.value = '';
    nickname.value = '';

    localStorage.removeItem('piggy_token');
    localStorage.removeItem('piggy_refresh_token');
    localStorage.removeItem('piggy_user_id');
    localStorage.removeItem('piggy_username');
    localStorage.removeItem('piggy_nickname');
  };

  /**
   * Login action
   * @param {Object} userData { username, password }
   */
  const login = async (userData) => {
    try {
      const res = await request.post('/api/auth/login', userData);
      if (res.code === 200) {
        setUserInfo(res.data);
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  /**
   * Register action
   * @param {Object} userData { username, password, nickname }
   */
  const register = async (userData) => {
    try {
      const res = await request.post('/api/auth/register', userData);
      if (res.code === 200) {
        setUserInfo(res.data);
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  };

  /**
   * Logout action
   */
  const logout = () => {
    clearUserInfo();
    window.location.href = '/login';
  };

  /**
   * Refresh token action
   */
  const refreshAuthToken = async () => {
    try {
      const res = await request.post('/api/auth/refresh', {
        refreshToken: refreshToken.value
      });
      if (res.code === 200) {
        setUserInfo(res.data);
        return res.data;
      }
      throw new Error(res.message);
    } catch (error) {
      clearUserInfo();
      window.location.href = '/login';
      throw error;
    }
  };

  return {
    token,
    refreshToken,
    userId,
    username,
    nickname,
    login,
    register,
    logout,
    refreshAuthToken
  };
});
