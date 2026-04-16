import request from '@/utils/request';

/**
 * Create a new transaction record
 */
export const createTransaction = (data) => {
  return request.post('/api/transactions', data);
};

/**
 * Get list of transactions with filtering and pagination
 * @param {Object} params { accountId, type, startDate, endDate, page, size }
 */
export const getTransactions = (params) => {
  return request.get('/api/transactions', { params });
};

/**
 * Get transaction details by ID
 */
export const getTransactionById = (id) => {
  return request.get(`/api/transactions/${id}`);
};

/**
 * Update existing transaction record
 */
export const updateTransaction = (id, data) => {
  return request.put(`/api/transactions/${id}`, data);
};

/**
 * Delete a transaction record
 */
export const deleteTransaction = (id) => {
  return request.delete(`/api/transactions/${id}`);
};

/**
 * AI intelligent classification of transaction text
 * @param {string} text 
 */
export const aiClassify = (text) => {
  return request.post('/api/transactions/ai-classify', { text });
};
