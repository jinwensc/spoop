package com.hqyg.bigdata.spoop.bean;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * 源系统信息表(EtlHiveFileReceiveList)实体类
 *
 * @author makejava
 * @since 2020-03-24 18:07:35
 */
@Data
public class EtlHiveFileReceiveList implements Serializable {
    private static final long serialVersionUID = -54101021900082222L;
    /**
    * 源系统编号
    */
    private String dataFrom;
    /**
    * 源系统简称
    */
    private String dataType;
    /**
    * 登陆ip
    */
    private String dbIp;
    /**
    * 登陆用户
    */
    private String dbUser;
    /**
    * 密码
    */
    private String dbPwd;
    /**
    * 源库端口号
    */
    private String dbPort;
    /**
    * 源库名称
    */
    private String dbDatabase;
    /**
    * 源库编号
    */
    private String dbNum;
    /**
    * 源系统名称
    */
    private String sourceSystem;
    /**
    * 源系统类型
    */
    private String sourceType;
    /**
    * 是否有效 1无效，0国内站点有效，2海外站点有效
    */
    private Integer isVaild;
    /**
    * 中转服务器ip
    */
    private String sourceIp;
    /**
    * 登陆路径
    */
    private String sourcePath;
    /**
    * ssh
    */
    private String sourceSsh;
    /**
    * 创建时间
    */
    private Date createTime;
    /**
    * 创建用户
    */
    private String createUser;
    /**
    * 源系统服务器ip
    */
    private String serverIp;
    /**
    * 源系统端口号
    */
    private String serverPort;
    /**
    * 源系统中文名称
    */
    private String sourceZhName;
    /**
    * 是否检测，1是0否，2直接从47.6连接数据库检测
    */
    private Integer checkFlag;
    /**
    * 主从检测登陆ip
    */
    private String checkDbIp;

}