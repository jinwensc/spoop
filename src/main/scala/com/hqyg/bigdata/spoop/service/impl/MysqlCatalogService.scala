package com.hqyg.bigdata.spoop.service.impl

import java.util.Properties

import com.hqyg.bigdata.spoop.Spoop
import com.hqyg.bigdata.spoop.service.CatalogService
import com.hqyg.bigdata.spoop.utils.{DateUtil, PartitionUtil, PropsUtil}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable.ListBuffer

/**
 * mysql 数据库的处理类实现
 * by yangjinwen 2020-04-23
 */
class MysqlCatalogService extends CatalogService with Logging {
  val DRIVER: String = "com.mysql.jdbc.Driver"

  /**
   * catalog名称
   *
   * @return 名称
   */
  override def getName(): String = MysqlCatalogService.name

  /**
   * 获取源数据dataframe
   * 如果并行度大于1，需要按照某个字段分区
   * 如果分区字段是数值，根据最大最小值和分区数，平均的计算每个分区的数值范围
   * 如果是字符串，根据最大最小值和抽样获取的分区字段值来计算出分区的范围，需要多进行一次抽样数据的拉取过程
   *
   * @return DataFrame
   */
  override def source(spark: SparkSession,
                      tableInfo: Row
                      //                      tableLoadType: Integer, updateCol: String, dbName: String, tableName: String, partitionKey: String, dbIp: String, dbPort: String, dbUser: String, dbPwd: String
                     ): DataFrame = {

    val tableLoadType = tableInfo.getAs[java.math.BigDecimal]("TABLE_LOAD_TYPE").floatValue().toInt
    val fromTable = tableInfo.getAs[String]("FROM_TABLE").toLowerCase
    val sourceType = tableInfo.getAs[String]("SOURCE_TYPE").toLowerCase
    val dbIp = tableInfo.getAs[String]("DB_IP")
    val dbPort = tableInfo.getAs[String]("DB_PORT")
    val dbName = tableInfo.getAs[String]("DB_DATABASE")
    val dbUser = tableInfo.getAs[String]("DB_USER")
    val dbPwd = tableInfo.getAs[String]("DB_PWD")
    val keyCol = tableInfo.getAs[String]("KEY_COL")
    val updateCol = tableInfo.getAs[String]("UPDATE_COL")
    val colConcatSqoop = tableInfo.getAs[String]("COL_CONCAT_SQOOP")
    logError(s"colConcatSqoop:${colConcatSqoop}")
    val mapNum = tableInfo.getAs[java.math.BigDecimal]("MAP_NUM").toString.toInt
    val maxSampleCount = PropsUtil.getInt("tring-key-col.max-sample-count")
    val minSampleCount = PropsUtil.getInt("tring-key-col.min-sample-count")
    val maxPartition = PropsUtil.getInt("partition.max")
    //    val mapNum = 10

    var tableSql: String = null
    var rangeSql: String = null
    var sampleSql: String = null
    var whereConditions: Array[String] = null
    val lastDay = DateUtil.get_inc_start_last(Spoop.date)

    //计算采样的数量
    var sampleCount: Int = maxSampleCount
    if (mapNum < maxPartition) {
      sampleCount = mapNum * (maxSampleCount / maxPartition)
    }
    if (sampleCount < minSampleCount) sampleCount = minSampleCount

    //生成拉取数据的sql
    if (tableLoadType == 0) {
      //全量，拉取素有数据
      tableSql = s"(select ${colConcatSqoop} from ${fromTable}) t"
      rangeSql = s"(select min(${keyCol}) as min, max(${keyCol}) as max from ${fromTable}) t"
      sampleSql = s"(select ${keyCol} from ${fromTable} order by rand() limit ${sampleCount}) t"
    } else {
      //增量，根据时间来获取部分数据
      val startTime = s"${lastDay.substring(0, 4)}-${lastDay.substring(4, 6)}-${lastDay.substring(6, 8)} 23:00:00"
      val endTime = s"${Spoop.date.substring(0, 4)}-${Spoop.date.substring(4, 6)}-${Spoop.date.substring(6, 8)} 23:59:59"
      tableSql = s"(select ${colConcatSqoop} from ${fromTable} where ${updateCol}>'${startTime}' and ${updateCol} <= '${endTime}') t"
      rangeSql = s"(select min(${keyCol}) as min, max(${keyCol}) as max from ${fromTable} where ${updateCol}>'${startTime}' and ${updateCol} <= '${endTime}') t"
      sampleSql = s"(select ${keyCol} as key_col from ${fromTable} where ${updateCol}>'${startTime}' and ${updateCol} <= '${endTime}' order by rand() limit ${sampleCount}) t"
    }
    logWarning(s"@@tableSql ${tableSql}")

    if (mapNum > 1) {
      //分区数大于1时，计算分区规则，通过最大最小值来平衡分区的数据量，可能数据倾斜，可增加分区数来解决
      val range = spark.read.format("jdbc")
        .option("driver", DRIVER)
        .option("url", s"jdbc:${sourceType}://${dbIp}:${dbPort}/${dbName}?useSSL=false")
        .option("user", dbUser)
        .option("password", dbPwd)
        .option("dbtable", rangeSql)
        .option("numPartitions", "1").load()
        .collect().map(r => (r.get(0), r.get(1))).toList(0)
      //根据分区字段的类型，来计算各个分区的范围
      range._1 match {
        case i: Number => {
          whereConditions = PartitionUtil.getNumberPredicates(keyCol, scala.math.floor(i.doubleValue()).toLong, scala.math.ceil(range._2.asInstanceOf[Number].doubleValue()).toLong, mapNum)
        }
        case b: java.math.BigDecimal => {
          whereConditions = PartitionUtil.getDecimalPredicates(keyCol, b, range._2.asInstanceOf[java.math.BigDecimal], mapNum)
        }
        case s: String => {
          //先进行采样
          spark.read.format("jdbc")
            .option("driver", DRIVER)
            .option("url", s"jdbc:${sourceType}://${dbIp}:${dbPort}/${dbName}?useSSL=false")
            .option("user", dbUser)
            .option("password", dbPwd)
            .option("dbtable", sampleSql)
            .option("numPartitions", "1").load().createOrReplaceTempView(s"${fromTable}_sample")
          //采样结果排序
          val sampleWithId = spark.sql(s"select key_col,row_number() over(partition by 1 order  by key_col) as row_num from ${fromTable}_sample")
          val step: Int = sampleCount / mapNum
          val whereConditionsList = ListBuffer[String]()
          //采样结果获取平均分隔点
          val keyColArray: Array[String] = sampleWithId.where(s"pmod(row_num, ${mapNum}) = pmod(t.row_num,${mapNum})=cast(${step} as bigint)/2").select("key_col").collect()
            .map(_.getAs[String]("key_col"))

          //构造whereConditions
          keyColArray.reduce((a, b) => {
            whereConditionsList.append(s"${keyCol} >= ${a} and ${keyCol} < ${b}")
          })
          whereConditionsList.append(s"${keyCol} >= ${range._1} and ${keyCol} < ${keyColArray(0)}")
          whereConditionsList.append(s"${keyCol} >= ${keyColArray.last} and ${keyCol} <= ${range._2}")
          whereConditions = whereConditionsList.toArray

        }

        //TODO 分区字段是时间的话，转换为时间戳，安装数值的方式
      }

      logWarning(s"@@predicates: ${whereConditions.toBuffer}")

      //根据分区规则，拉取数据
      val properties = new Properties()
      properties.setProperty("driver", DRIVER)
      properties.setProperty("user", dbUser)
      properties.setProperty("password", dbPwd)
      properties.setProperty("fetchsize", "1000")
      val sourceFrame: DataFrame = spark.read.jdbc(url = s"jdbc:${sourceType}://${dbIp}:${dbPort}/${dbName}?useSSL=false&zeroDateTimeBehavior=convertToNull", table = tableSql, predicates = whereConditions, connectionProperties = properties)

      sourceFrame
    } else {
      //只有一个分区的情况
      val sourceFrame = spark.read.format("jdbc")
        .option("driver", DRIVER)
        .option("url", s"jdbc:mysql://${dbIp}:${dbPort}/${dbName}?useSSL=false&zeroDateTimeBehavior=convertToNull")
        .option("user", dbUser)
        .option("password", dbPwd)
        .option("dbtable", tableSql)
        .option("numPartitions", "1")
        .option("fetchsize", "1000").load()

      sourceFrame
    }

  }

}

object MysqlCatalogService {
  val name = "mysql"

  def getInstance(): MysqlCatalogService = {
    new MysqlCatalogService()
  }
}