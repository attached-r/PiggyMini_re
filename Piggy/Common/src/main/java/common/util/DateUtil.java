package common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期格式化工具类
 *
 * @author: rj
 */
public class DateUtil {

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN_SLASH = "yyyy/MM/dd";
    private static final String DATETIME_PATTERN_SLASH = "yyyy/MM/dd HH:mm:ss";
    private static final String DATE_PATTERN_COMPACT = "yyyyMMdd";
    private static final String DATETIME_PATTERN_COMPACT = "yyyyMMddHHmmss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER_SLASH = DateTimeFormatter.ofPattern(DATE_PATTERN_SLASH);
    private static final DateTimeFormatter DATETIME_FORMATTER_SLASH = DateTimeFormatter.ofPattern(DATETIME_PATTERN_SLASH);
    private static final DateTimeFormatter DATE_FORMATTER_COMPACT = DateTimeFormatter.ofPattern(DATE_PATTERN_COMPACT);
    private static final DateTimeFormatter DATETIME_FORMATTER_COMPACT = DateTimeFormatter.ofPattern(DATETIME_PATTERN_COMPACT);

    /**
     * 格式化日期为 yyyy-MM-dd
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 日期时间
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期为 yyyy/MM/dd
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateWithSlash(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER_SLASH);
    }

    /**
     * 格式化日期时间为 yyyy/MM/dd HH:mm:ss
     *
     * @param dateTime 日期时间
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateTimeWithSlash(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER_SLASH);
    }

    /**
     * 格式化日期为 yyyyMMdd
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateCompact(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER_COMPACT);
    }

    /**
     * 格式化日期时间为 yyyyMMddHHmmss
     *
     * @param dateTime 日期时间
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateTimeCompact(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER_COMPACT);
    }

    /**
     * 解析日期字符串 yyyy-MM-dd
     *
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 日期加减天数
     *
     * @param date  原始日期
     * @param days  天数（正数加，负数减）
     * @return 计算后的日期
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 日期加减月数
     *
     * @param date   原始日期
     * @param months 月数（正数加，负数减）
     * @return 计算后的日期
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) {
            return null;
        }
        return date.plusMonths(months);
    }

    /**
     * 获取某月的第一天
     *
     * @param date 日期
     * @return 该月第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * 获取某月的最后一天
     *
     * @param date 日期
     * @return 该月最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }
}
