package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 收支类型枚举
 *
 * @author: rj
 */
@Getter
@AllArgsConstructor
public enum TransactionType {

    EXPENSE(1, "支出", "expense", "-", "💸"),
    INCOME(2, "收入", "income", "+", "💰"),
    TRANSFER(3, "转账", "transfer", "=", "🔄");

    private final Integer code;
    private final String description;
    private final String value;
    private final String sign;
    private final String icon;

    /**
     * 根据code获取收支类型
     *
     * @param code 类型编码
     * @return TransactionType枚举，未找到返回null
     */
    public static TransactionType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据value获取收支类型
     *
     * @param value 类型值
     * @return TransactionType枚举，未找到返回null
     */
    public static TransactionType getByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有收支类型列表
     *
     * @return 收支类型列表
     */
    public static List<TransactionType> getAllTypes() {
        return Arrays.asList(values());
    }

    /**
     * 判断是否为支出
     *
     * @return true-支出，false-非支出
     */
    public boolean isExpense() {
        return this == EXPENSE;
    }

    /**
     * 判断是否为收入
     *
     * @return true-收入，false-非收入
     */
    public boolean isIncome() {
        return this == INCOME;
    }

    /**
     * 判断是否为转账
     *
     * @return true-转账，false-非转账
     */
    public boolean isTransfer() {
        return this == TRANSFER;
    }

    /**
     * 根据类型转换金额符号
     * 支出返回负数，收入返回正数，转账返回原值
     *
     * @param amount 原始金额
     * @return 转换后的金额
     */
    public BigDecimal convertAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (this == EXPENSE) {
            return amount.negate();
        }
        return amount.abs();
    }

    /**
     * 获取显示金额（带符号）
     *
     * @param amount 金额
     * @return 带符号的金额字符串
     */
    public String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "¥0.00";
        }
        return sign + "¥" + amount.abs().toString();
    }
}
