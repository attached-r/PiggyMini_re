import request from '@/utils/request'

/**
 * 交易 API
 * 严格根据后端 TransactionController 接口
 */

/**
 * 交易类型枚举
 * 对应后端 TransactionType
 */
export const TransactionType = {
  EXPENSE: 'EXPENSE',   // 支出
  INCOME: 'INCOME',      // 收入
  TRANSFER: 'TRANSFER'   // 转账
}

/**
 * 交易类型映射（用于显示）
 */
export const TransactionTypeLabels = {
  EXPENSE: { label: '支出', icon: '💸', sign: '-', color: 'red' },
  INCOME: { label: '收入', icon: '💰', sign: '+', color: 'green' },
  TRANSFER: { label: '转账', icon: '🔄', sign: '=', color: 'blue' }
}

/**
 * 消费分类
 * 对应后端 ExpenseCategory 枚举
 */
export const ExpenseCategory = {
  // 餐饮类
  FOOD_BREAKFAST: { label: '早餐', parent: '餐饮', icon: '🥐' },
  FOOD_LUNCH: { label: '午餐', parent: '餐饮', icon: '🍱' },
  FOOD_DINNER: { label: '晚餐', parent: '餐饮', icon: '🍜' },
  FOOD_SNACK: { label: '零食', parent: '餐饮', icon: '🍿' },
  FOOD_FRUIT: { label: '水果', parent: '餐饮', icon: '🍎' },
  FOOD_DRINK: { label: '饮品', parent: '餐饮', icon: '🥤' },
  FOOD_GROCERY: { label: '买菜', parent: '餐饮', icon: '🥬' },
  FOOD_TREAT: { label: '聚餐', parent: '餐饮', icon: '🍻' },
  FOOD_DELIVERY: { label: '外卖', parent: '餐饮', icon: '🛵' },
  
  // 交通类
  TRANSPORT_SUBWAY: { label: '地铁', parent: '交通', icon: '🚇' },
  TRANSPORT_BUS: { label: '公交', parent: '交通', icon: '🚌' },
  TRANSPORT_TAXI: { label: '打车', parent: '交通', icon: '🚕' },
  TRANSPORT_DIDI: { label: '网约车', parent: '交通', icon: '🚗' },
  TRANSPORT_FUEL: { label: '加油', parent: '交通', icon: '⛽' },
  TRANSPORT_PARKING: { label: '停车费', parent: '交通', icon: '🅿️' },
  TRANSPORT_TOLL: { label: '过路费', parent: '交通', icon: '🛣️' },
  TRANSPORT_BIKE: { label: '共享单车', parent: '交通', icon: '🚲' },
  TRANSPORT_TRAIN: { label: '火车/高铁', parent: '交通', icon: '🚄' },
  TRANSPORT_FLIGHT: { label: '机票', parent: '交通', icon: '✈️' },
  
  // 购物类
  SHOP_CLOTHES: { label: '服饰', parent: '购物', icon: '👔' },
  SHOP_SHOES: { label: '鞋靴', parent: '购物', icon: '👟' },
  SHOP_BAG: { label: '箱包', parent: '购物', icon: '👜' },
  SHOP_COSMETIC: { label: '美妆', parent: '购物', icon: '💄' },
  SHOP_DIGITAL: { label: '数码', parent: '购物', icon: '💻' },
  SHOP_HOME: { label: '家居', parent: '购物', icon: '🏠' },
  SHOP_DAILY: { label: '日用品', parent: '购物', icon: '🧴' },
  SHOP_BOOK: { label: '书籍', parent: '购物', icon: '📚' },
  SHOP_GIFT: { label: '礼物', parent: '购物', icon: '🎁' },
  
  // 居住类
  LIVING_RENT: { label: '房租', parent: '居住', icon: '🏢' },
  LIVING_MORTGAGE: { label: '房贷', parent: '居住', icon: '🏦' },
  LIVING_PROPERTY: { label: '物业费', parent: '居住', icon: '🔑' },
  LIVING_WATER: { label: '水费', parent: '居住', icon: '💧' },
  LIVING_ELECTRIC: { label: '电费', parent: '居住', icon: '⚡' },
  LIVING_GAS: { label: '燃气费', parent: '居住', icon: '🔥' },
  LIVING_INTERNET: { label: '网费', parent: '居住', icon: '🌐' },
  LIVING_MAINTENANCE: { label: '维修', parent: '居住', icon: '🔧' },
  
  // 娱乐类
  ENTERTAIN_MOVIE: { label: '电影', parent: '娱乐', icon: '🎬' },
  ENTERTAIN_GAME: { label: '游戏', parent: '娱乐', icon: '🎮' },
  ENTERTAIN_KTV: { label: 'KTV', parent: '娱乐', icon: '🎤' },
  ENTERTAIN_TRAVEL: { label: '旅游', parent: '娱乐', icon: '🏖️' },
  ENTERTAIN_SPORT: { label: '运动', parent: '娱乐', icon: '⚽' },
  ENTERTAIN_GYM: { label: '健身', parent: '娱乐', icon: '🏋️' },
  ENTERTAIN_STREAM: { label: '会员订阅', parent: '娱乐', icon: '📺' },
  
  // 医疗类
  MEDICAL_HOSPITAL: { label: '看病', parent: '医疗', icon: '🏥' },
  MEDICAL_MEDICINE: { label: '买药', parent: '医疗', icon: '💊' },
  MEDICAL_DENTAL: { label: '牙科', parent: '医疗', icon: '🦷' },
  MEDICAL_CHECKUP: { label: '体检', parent: '医疗', icon: '🩺' },
  MEDICAL_INSURANCE: { label: '保险', parent: '医疗', icon: '🛡️' },
  
  // 教育类
  EDUCATION_TUITION: { label: '学费', parent: '教育', icon: '🎓' },
  EDUCATION_COURSE: { label: '培训', parent: '教育', icon: '📖' },
  EDUCATION_EXAM: { label: '考试', parent: '教育', icon: '📝' },
  EDUCATION_MATERIAL: { label: '教材', parent: '教育', icon: '📚' },
  
  // 社交类
  SOCIAL_RED_PACKET: { label: '红包', parent: '社交', icon: '🧧' },
  SOCIAL_DONATION: { label: '捐赠', parent: '社交', icon: '❤️' },
  SOCIAL_TIP: { label: '小费', parent: '社交', icon: '💰' },
  
  // 其他类
  OTHER_MISC: { label: '杂项', parent: '其他', icon: '📦' },
  OTHER_LOSS: { label: '损失', parent: '其他', icon: '💸' },
  OTHER_PENALTY: { label: '罚款', parent: '其他', icon: '⚠️' }
}

// 按父分类分组
export const ExpenseCategoryGroups = {
  '餐饮': ['FOOD_BREAKFAST', 'FOOD_LUNCH', 'FOOD_DINNER', 'FOOD_SNACK', 'FOOD_FRUIT', 'FOOD_DRINK', 'FOOD_GROCERY', 'FOOD_TREAT', 'FOOD_DELIVERY'],
  '交通': ['TRANSPORT_SUBWAY', 'TRANSPORT_BUS', 'TRANSPORT_TAXI', 'TRANSPORT_DIDI', 'TRANSPORT_FUEL', 'TRANSPORT_PARKING', 'TRANSPORT_TOLL', 'TRANSPORT_BIKE', 'TRANSPORT_TRAIN', 'TRANSPORT_FLIGHT'],
  '购物': ['SHOP_CLOTHES', 'SHOP_SHOES', 'SHOP_BAG', 'SHOP_COSMETIC', 'SHOP_DIGITAL', 'SHOP_HOME', 'SHOP_DAILY', 'SHOP_BOOK', 'SHOP_GIFT'],
  '居住': ['LIVING_RENT', 'LIVING_MORTGAGE', 'LIVING_PROPERTY', 'LIVING_WATER', 'LIVING_ELECTRIC', 'LIVING_GAS', 'LIVING_INTERNET', 'LIVING_MAINTENANCE'],
  '娱乐': ['ENTERTAIN_MOVIE', 'ENTERTAIN_GAME', 'ENTERTAIN_KTV', 'ENTERTAIN_TRAVEL', 'ENTERTAIN_SPORT', 'ENTERTAIN_GYM', 'ENTERTAIN_STREAM'],
  '医疗': ['MEDICAL_HOSPITAL', 'MEDICAL_MEDICINE', 'MEDICAL_DENTAL', 'MEDICAL_CHECKUP', 'MEDICAL_INSURANCE'],
  '教育': ['EDUCATION_TUITION', 'EDUCATION_COURSE', 'EDUCATION_EXAM', 'EDUCATION_MATERIAL'],
  '社交': ['SOCIAL_RED_PACKET', 'SOCIAL_DONATION', 'SOCIAL_TIP'],
  '其他': ['OTHER_MISC', 'OTHER_LOSS', 'OTHER_PENALTY']
}

/**
 * 获取分类信息
 */
export const getCategoryInfo = (category) => {
  return ExpenseCategory[category] || { label: category, icon: '📦', parent: '其他' }
}

/**
 * 创建交易记录
 * POST /api/transactions
 * 请求头: X-User-Id
 * 请求体: { accountId, type, amount, category?, description?, tradeTime, remark?, tags?, targetAccountId? }
 */
export const createTransaction = (data) => {
  // 转换字段名：description -> remark (后端用 remark)
  const payload = {
    accountId: data.accountId,
    type: data.type,
    amount: data.amount,
    category: data.category || null,
    description: data.description || '',
    tradeTime: data.tradeTime || data.transactionTime,
    remark: data.remark || '',
    tags: data.tags || '',
    targetAccountId: data.targetAccountId || null
  }
  return request.post('/api/transactions', payload)
}

/**
 * 查询交易列表（分页）
 * GET /api/transactions?accountId=&type=&startDate=&endDate=&page=1&size=10
 * 请求头: X-User-Id
 */
export const getTransactions = (params) => {
  return request.get('/api/transactions', { params })
}

/**
 * 查询交易详情
 * GET /api/transactions/{id}
 * 请求头: X-User-Id
 */
export const getTransactionById = (id) => {
  return request.get(`/api/transactions/${id}`)
}

/**
 * 更新交易记录
 * PUT /api/transactions/{id}
 * 请求头: X-User-Id
 * 请求体: { accountId?, type?, amount?, category?, description?, tradeTime?, remark?, tags?, targetAccountId? }
 */
export const updateTransaction = (id, data) => {
  const payload = {
    ...data,
    tradeTime: data.tradeTime || data.transactionTime
  }
  return request.put(`/api/transactions/${id}`, payload)
}

/**
 * 删除交易记录
 * DELETE /api/transactions/{id}
 * 请求头: X-User-Id
 */
export const deleteTransaction = (id) => {
  return request.delete(`/api/transactions/${id}`)
}

/**
 * AI 智能分类
 * POST /api/transactions/ai-classify
 * 请求体: { text }
 */
export const aiClassify = (text) => {
  return request.post('/api/transactions/ai-classify', { text })
}
