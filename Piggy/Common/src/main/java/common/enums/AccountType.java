package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 账户类型枚举
 *
 * @author: rj
 */
@Getter
@AllArgsConstructor
public enum AccountType {

    CASH(1, "现金", "cash", "💵"),
    DEBIT_CARD(2, "储蓄卡", "debit_card", "💳"),
    CREDIT_CARD(3, "信用卡", "credit_card", "💎"),
    ALIPAY(4, "支付宝", "alipay", "🔵"),
    WECHAT(5, "微信", "wechat", "💚"),
    BANK_TRANSFER(6, "银行转账", "bank_transfer", "🏦"),
    INVESTMENT(7, "投资账户", "investment", "📈"),
    VIRTUAL(8, "虚拟账户", "virtual", "🎮"),
    OTHER(9, "其他", "other", "📦");

    private final Integer code;
    private final String description;
    private final String value;
    private final String icon;

    /**
     * 根据code获取账户类型
     *
     * @param code 账户类型编码
     * @return AccountType枚举，未找到返回null
     */
    public static AccountType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据value获取账户类型
     *
     * @param value 账户类型值
     * @return AccountType枚举，未找到返回null
     */
    public static AccountType getByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有账户类型列表
     *
     * @return 账户类型列表
     */
    public static List<AccountType> getAllTypes() {
        return Arrays.asList(values());
    }

    /**
     * 判断是否为电子支付账户
     *
     * @return true-电子支付，false-非电子支付
     */
    public boolean isElectronicPayment() {
        return this == ALIPAY || this == WECHAT || this == BANK_TRANSFER;
    }

    /**
     * 判断是否为实体账户
     *
     * @return true-实体账户，false-非实体账户
     */
    public boolean isPhysicalAccount() {
        return this == CASH || this == DEBIT_CARD || this == CREDIT_CARD;
    }
}
