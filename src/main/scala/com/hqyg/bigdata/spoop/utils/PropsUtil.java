package com.hqyg.bigdata.spoop.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropsUtil {
    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(PropsUtil.class);

    static {
        properties = loadProps("config.properties");
    }

    /**
     * 加载配置文件
     */
    public static Properties loadProps(String fileName) {
        Properties props = null;
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if (is == null) {
                throw new FileNotFoundException(fileName + "file is not found");
            }
            props = new Properties();
            props.load(is);
        } catch (IOException e) {
            logger.error("load properties file failure ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("close input stream failure", e);
                }
            }
        }
        return props;
    }

    /**
     * 获取结果为字符串
     *
     * @param prop
     * @param key
     * @param defalutValue
     * @return
     */
    public static String getString(Properties prop, String key, String defalutValue) {
        String value = defalutValue;
        if (prop.containsKey(key)) {
            value = prop.getProperty(key);
        }
        return value;
    }

    /**
     * 获取结果为字符串
     *
     * @param key
     * @param defalutValue
     * @return
     */
    public static String getString(String key, String defalutValue) {
        return getString(properties, key, defalutValue);
    }

    /**
     * 获取结果为字符串
     *
     * @param prop
     * @param key
     * @return
     */

    public static String getString(Properties prop, String key) {
        return getString(prop, key, "");
    }

    /**
     * 获取结果为字符串
     *
     * @param key
     * @return
     */

    public static String getString(String key) {
        return getString(properties, key, "");
    }

    /**
     * 获取结果为int
     *
     * @param prop
     * @param key
     * @return
     */
    public static int getInt(Properties prop, String key) {
        return getInt(prop, key, 0);
    }

    /**
     * 获取结果为int
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        return getInt(properties, key, 0);
    }

    /**
     * 获取结果为int
     *
     * @param key
     * @param defalutValue
     * @return
     */
    public static int getInt(String key, int defalutValue) {
        return getInt(properties, key, defalutValue);
    }

    /**
     * 获取结果为int
     *
     * @param prop
     * @param key
     * @param defalutValue
     * @return
     */
    public static int getInt(Properties prop, String key, int defalutValue) {
        int value = defalutValue;
        if (prop.containsKey(key)) {
            value = CastUtil.castInt(prop.getProperty(key));
        }
        return value;
    }

    /**
     * 获取结果为boolean
     *
     * @param prop
     * @param key
     * @return
     */

    public static boolean getBoolean(Properties prop, String key) {
        return getBoolean(prop, key, false);
    }

    /**
     * 获取结果为boolean
     *
     * @param key
     * @return
     */

    public static boolean getBoolean(String key) {
        return getBoolean(properties, key, false);
    }

    /**
     * 获取结果为boolean
     *
     * @param prop
     * @param key
     * @param defalutValue
     * @return
     */

    public static boolean getBoolean(Properties prop, String key, boolean defalutValue) {
        boolean value = defalutValue;
        if (prop.containsKey(key)) {
            value = CastUtil.castBoolean(prop.getProperty(key));
        }
        return value;
    }

    /**
     * 获取结果为boolean
     *
     * @param key
     * @param defalutValue
     * @return
     */

    public static boolean getBoolean(String key, boolean defalutValue) {
        return getBoolean(properties, key, defalutValue);
    }
}
