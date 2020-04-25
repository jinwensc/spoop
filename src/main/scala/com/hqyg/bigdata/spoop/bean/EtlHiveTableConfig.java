package com.hqyg.bigdata.spoop.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * hive同步配置表(EtlHiveTableConfig)实体类
 *
 * @author makejava
 * @since 2020-03-24 09:13:08
 */
@Data
public class EtlHiveTableConfig implements Serializable {
    private static final long serialVersionUID = -85658692906958295L;
    /**
    * 所有者
    */
    private String owner;
    /**
    * 站点
    */
    @NotNull
    private Integer dataFrom;
    /**
    * 来源表
    */
    private String fromTable;
    /**
    * 目标表
    */
    @NotNull
    private String toTable;
    /**
    * 加载类型 0 全表全量/2 创建时间增量/3 更新时间增量/4 拉链历史
    */
    private Integer tableLoadType;
    /**
    * 创建字段
    */
    private String createCol;
    /**
    * 更新字段
    */
    private String updateCol;
    /**
    * 主键
    */
    private String keyCol;
    /**
    * 字段连接串(stg-->ods映射关系)
    */
    private String colConcat;
    /**
    * 字段连接串添加加工处理（源系统-->stg层映射关系）
    */
    private String colConcatSqoop;
    /**
    * where条件，当全量同步只要写where即可
    */
    private String colWhere;
    /**
    * 设置mapreduce map数目： 全量表默认10 map，数据量<50w 给1 map;200w>数据量>50w 给4 map;500w>数据量>200w 给8 map;数据量>500w 给10 map。增量表给4 map 
    */
    private Integer mapNum;
    /**
    * 创建时间
    */
    private Timestamp createTime;
    /**
    * 0 sqlload有效/ 1 无效(下线)  /2 归档数据手工抽取
    */
    @NotNull
    private Integer isVaild;
    /**
    * 修改时间
    */
    private Date updateTime;
    /**
    * 加工日期偏移量
    */
    private Integer etlMoveDays;
    /**
    * 创建人
    */
    private String createUser;
    /**
    * 修改人
    */
    private String updateUser;
    /**
    * 国内外标识(1-国内 2-国外)
    */
    private String etlGroup;
    /**
    * 源系统原始表名(有部分表名超30长度，有做转换)
    */
    private String sourceTable;
    /**
    * 物理删除主键
    */
    private String deleteKeyCol;
    /**
    * 需要转换的日期字段
    */
    private String dateConcat;
    /**
    * 源库编号-关联字段
    */
    private String dbNum;
    /**
    * 设置内存大小：1--map 1G    2--map 2G   3--map 3G
    */
    private Integer mapMemSize;
    /**
    * 源系统抽数备注：0-0点同步并卡结束时间，1-1点同步并卡结束时间，null-同步不卡结束时间
    */
    private String synRemark;
    /**
    * 是否可以初始化表；0--可以全量初始化表；1--不能全量初始化表
    */
    private Integer isCanInit;
    /**
    * 1.物理删打标关联stg.stg_delete_data_oracle，2.物理删打标关联stg.stg_delete_data_increment
    */
    private Integer deleteDataTable;
    /**
    * 是否需要执行业务主键去重（ods层处理） 1--需要 
    */
    private Integer isNeedEntfernen;
    /**
    * 业务主键（逗号分隔）
    */
    private String buKey;

}