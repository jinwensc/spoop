package com.hqyg.bigdata.spoop.utils;

import jodd.util.StringUtil;

public class CastUtil {
    /**
     * 转换成字符串
     *
     * @param obj
     * @param defalutValue
     *            默认值
     * @return
     */
    public static String castString(Object obj, String defalutValue) {
        return obj != null ? String.valueOf(obj) : defalutValue;
    }

    /**
     * 转换成字符串
     *
     * @param obj
     * @return
     */

    public static String castString(Object obj) {
        return castString(obj);
    }

    /**
     * 转换成正型
     *
     * @param obj
     * @param defalutValue
     * @return
     */
    public static int castInt(Object obj, int defalutValue) {
        int iniValue = defalutValue;
        if (obj != null) {
            String strValue = castString(obj);
            if (StringUtil.isNotEmpty(strValue)) {
                try {
                    iniValue = Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    iniValue = defalutValue;
                }
            }
        }
        return iniValue;
    }

    /**
     * 转换成正型
     *
     * @param obj
     * @return
     */
    public static int castInt(Object obj) {
        return castInt(obj, 0);
    }

    /**
     * 转换成duoble
     *
     * @param obj
     * @param defalutValue
     * @return
     */
    public static double castDuoble(Object obj, double defalutValue) {
        double iniValue = defalutValue;
        if (obj != null) {
            String strValue = castString(obj);
            if (StringUtil.isNotEmpty(strValue)) {
                try {
                    iniValue = Double.parseDouble(strValue);
                } catch (NumberFormatException e) {
                    iniValue = defalutValue;
                }
            }
        }
        return iniValue;
    }

    /**
     * 转换成duoble
     *
     * @param obj
     * @return
     */

    public static double castDuoble(Object obj) {
        return castDuoble(obj, 0);
    }

    /**
     * 转换成long
     *
     * @param obj
     * @param defalutValue
     * @return
     */
    public static long castLong(Object obj, long defalutValue) {
        long iniValue = defalutValue;
        if (obj != null) {
            String strValue = castString(obj);
            if (StringUtil.isNotEmpty(strValue)) {
                try {
                    iniValue = Long.parseLong(strValue);
                } catch (NumberFormatException e) {
                    iniValue = defalutValue;
                }
            }
        }
        return iniValue;
    }

    /**
     * 转换成long
     *
     * @param obj
     * @return
     */
    public static long castLong(Object obj) {
        return castLong(obj, 0);
    }

    /**
     * 转换成boolean
     *
     * @param obj
     * @param defalutValue
     * @return
     */
    public static boolean castBoolean(Object obj, boolean defalutValue) {
        boolean iniValue = defalutValue;
        if (obj != null) {
            String strValue = castString(obj);
            iniValue = Boolean.parseBoolean(strValue);
        }
        return iniValue;
    }

    /**
     * 转换成boolean
     *
     * @param obj
     * @return
     */
    public static boolean castBoolean(Object obj) {
        return castBoolean(obj, false);
    }
}
