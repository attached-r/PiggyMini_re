import request from '@/utils/request';

/**
 * Create a new bank account
 * @param {Object} data account details
 */
export const createAccount = (data) => {
  return request.post('/api/accounts', data);
};

/**
 * Get list of accounts with pagination
 */
export const getAccounts = (page = 1, size = 10) => {
  return request.get('/api/accounts', { params: { page, size } });
};

/**
 * Get account details by ID
 */
export const getAccountById = (id) => {
  return request.get(`/api/accounts/${id}`);
};

/**
 * Update existing account
 */
export const updateAccount = (id, data) => {
  return request.put(`/api/accounts/${id}`, data);
};

/**
 * Delete an account
 */
export const deleteAccount = (id) => {
  return request.delete(`/api/accounts/${id}`);
};
