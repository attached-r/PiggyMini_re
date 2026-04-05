package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消费分类枚举
 *
 * @author: rj
 */
@Getter
@AllArgsConstructor
public enum ExpenseCategory {

    // 餐饮类 (100-199)
    FOOD_BREAKFAST(101, "早餐", "breakfast", "🥐", "餐饮"),
    FOOD_LUNCH(102, "午餐", "lunch", "🍱", "餐饮"),
    FOOD_DINNER(103, "晚餐", "dinner", "🍜", "餐饮"),
    FOOD_SNACK(104, "零食", "snack", "🍿", "餐饮"),
    FOOD_FRUIT(105, "水果", "fruit", "🍎", "餐饮"),
    FOOD_DRINK(106, "饮品", "drink", "🥤", "餐饮"),
    FOOD_GROCERY(107, "买菜", "grocery", "🥬", "餐饮"),
    FOOD_TREAT(108, "聚餐", "treat", "🍻", "餐饮"),
    FOOD_DELIVERY(109, "外卖", "delivery", "🛵", "餐饮"),

    // 交通类 (200-299)
    TRANSPORT_SUBWAY(201, "地铁", "subway", "🚇", "交通"),
    TRANSPORT_BUS(202, "公交", "bus", "🚌", "交通"),
    TRANSPORT_TAXI(203, "打车", "taxi", "🚕", "交通"),
    TRANSPORT_DIDI(204, "网约车", "didi", "🚗", "交通"),
    TRANSPORT_FUEL(205, "加油", "fuel", "⛽", "交通"),
    TRANSPORT_PARKING(206, "停车费", "parking", "🅿️", "交通"),
    TRANSPORT_TOLL(207, "过路费", "toll", "🛣️", "交通"),
    TRANSPORT_BIKE(208, "共享单车", "bike", "🚲", "交通"),
    TRANSPORT_TRAIN(209, "火车/高铁", "train", "🚄", "交通"),
    TRANSPORT_FLIGHT(210, "机票", "flight", "✈️", "交通"),

    // 购物类 (300-399)
    SHOP_CLOTHES(301, "服饰", "clothes", "👔", "购物"),
    SHOP_SHOES(302, "鞋靴", "shoes", "👟", "购物"),
    SHOP_BAG(303, "箱包", "bag", "👜", "购物"),
    SHOP_COSMETIC(304, "美妆", "cosmetic", "💄", "购物"),
    SHOP_DIGITAL(305, "数码", "digital", "💻", "购物"),
    SHOP_HOME(306, "家居", "home", "🏠", "购物"),
    SHOP_DAILY(307, "日用品", "daily", "🧴", "购物"),
    SHOP_BOOK(308, "书籍", "book", "📚", "购物"),
    SHOP_GIFT(309, "礼物", "gift", "🎁", "购物"),

    // 居住类 (400-499)
    LIVING_RENT(401, "房租", "rent", "🏢", "居住"),
    LIVING_MORTGAGE(402, "房贷", "mortgage", "🏦", "居住"),
    LIVING_PROPERTY(403, "物业费", "property", "🔑", "居住"),
    LIVING_WATER(404, "水费", "water", "💧", "居住"),
    LIVING_ELECTRIC(405, "电费", "electric", "⚡", "居住"),
    LIVING_GAS(406, "燃气费", "gas", "🔥", "居住"),
    LIVING_INTERNET(407, "网费", "internet", "🌐", "居住"),
    LIVING_MAINTENANCE(408, "维修", "maintenance", "🔧", "居住"),

    // 娱乐类 (500-599)
    ENTERTAIN_MOVIE(501, "电影", "movie", "🎬", "娱乐"),
    ENTERTAIN_GAME(502, "游戏", "game", "🎮", "娱乐"),
    ENTERTAIN_KTV(503, "KTV", "ktv", "🎤", "娱乐"),
    ENTERTAIN_TRAVEL(504, "旅游", "travel", "🏖️", "娱乐"),
    ENTERTAIN_SPORT(505, "运动", "sport", "⚽", "娱乐"),
    ENTERTAIN_GYM(506, "健身", "gym", "🏋️", "娱乐"),
    ENTERTAIN_STREAM(507, "会员订阅", "stream", "📺", "娱乐"),

    // 医疗类 (600-699)
    MEDICAL_HOSPITAL(601, "看病", "hospital", "🏥", "医疗"),
    MEDICAL_MEDICINE(602, "买药", "medicine", "💊", "医疗"),
    MEDICAL_DENTAL(603, "牙科", "dental", "🦷", "医疗"),
    MEDICAL_CHECKUP(604, "体检", "checkup", "🩺", "医疗"),
    MEDICAL_INSURANCE(605, "保险", "insurance", "🛡️", "医疗"),

    // 教育类 (700-799)
    EDUCATION_TUITION(701, "学费", "tuition", "🎓", "教育"),
    EDUCATION_COURSE(702, "培训", "course", "📖", "教育"),
    EDUCATION_EXAM(703, "考试", "exam", "📝", "教育"),
    EDUCATION_MATERIAL(704, "教材", "material", "📚", "教育"),

    // 社交类 (800-899)
    SOCIAL_RED_PACKET(801, "红包", "red_packet", "🧧", "社交"),
    SOCIAL_DONATION(802, "捐赠", "donation", "❤️", "社交"),
    SOCIAL_TIP(803, "小费", "tip", "💰", "社交"),

    // 其他类 (900-999)
    OTHER_MISC(901, "杂项", "misc", "📦", "其他"),
    OTHER_LOSS(902, "损失", "loss", "💸", "其他"),
    OTHER_PENALTY(903, "罚款", "penalty", "⚠️", "其他");

    private final Integer code;  //消费码
    private final String description;
    private final String value;
    private final String icon;
    private final String parentCategory;

    /**
     * 根据code获取消费分类
     *
     * @param code 分类编码
     * @return ExpenseCategory枚举，未找到返回null
     */
    public static ExpenseCategory getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(category -> category.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据value获取消费分类
     *
     * @param value 分类值
     * @return ExpenseCategory枚举，未找到返回null
     */
    public static ExpenseCategory getByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(category -> category.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据父分类获取所有子分类
     *
     * @param parentCategory 父分类名称
     * @return 消费分类列表
     */
    public static List<ExpenseCategory> getByParentCategory(String parentCategory) {
        if (parentCategory == null || parentCategory.isEmpty()) {
            return Arrays.asList(values());
        }
        return Arrays.stream(values())
                .filter(category -> category.getParentCategory().equals(parentCategory))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有父分类列表
     *
     * @return 父分类列表（去重）
     */
    public static List<String> getAllParentCategories() {
        return Arrays.stream(values())
                .map(ExpenseCategory::getParentCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有消费分类列表
     *
     * @return 消费分类列表
     */
    public static List<ExpenseCategory> getAllCategories() {
        return Arrays.asList(values());
    }

    /**
     * 判断是否为日常必需消费
     *
     * @return true-必需消费，false-非必需消费
     */
    public boolean isEssential() {
        return parentCategory.equals("餐饮")
                || parentCategory.equals("居住")
                || parentCategory.equals("交通")
                || parentCategory.equals("医疗");
    }

    /**
     * 判断是否为弹性消费
     *
     * @return true-弹性消费，false-非弹性消费
     */
    public boolean isFlexible() {
        return parentCategory.equals("娱乐")
                || parentCategory.equals("购物")
                || parentCategory.equals("社交");
    }
}
