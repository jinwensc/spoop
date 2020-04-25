package com.hqyg.bigdata.spoop.bean

/**
 *
 * @param jobName 任务名称
 * @param dbNum   数据接入时的ETL_HIVE_FILE_RECEIVE_LIST db_num
 * @param tables  逗号分隔的表列表ETL_HIVE_TABLE_CONFIG to_table
 */
case class Param(jobName: String,
                 dbNum: String,
                 tables: String)
