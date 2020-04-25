package com.hqyg.bigdata.spoop.service

import com.hqyg.bigdata.spoop.Spoop
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{Row, SparkSession}

import scala.actors.threadpool.Callable

/**
 * 获取mysql数据写hive表
 * by yangjinwen 2020-04-23
 */
class SpoopService extends Callable with Logging {
  var spark: SparkSession = _
  var tableInfo: Row = _

  def init(spark: SparkSession, tableInfo: Row): Unit = {
    this.spark = spark
    this.tableInfo = tableInfo
  }

  override def call() = {
    try {
      val stgTableName = tableInfo.getAs[String]("TO_TABLE").replaceFirst("^ODS_", "STG_")

      val catalogService: CatalogService = CatalogFactory.getCatalog("mysql")

      val dataFrom = tableInfo.getAs[java.math.BigDecimal]("DATA_FROM").floatValue().toInt
      //获取ods表的字段名称和类型，转换成序列
      val stgColsString = spark.catalog.listColumns("stg." + stgTableName).select("name", "dataType").rdd.map(r => {
        (r.getAs[String]("name"), r.getAs[String]("dataType"))
      }).collect().map(r => {
        //字段类型转换，审计字段处理
        if (r._1.equals("data_from")) {
          s"cast( '${dataFrom}' as ${r._2}) as `${r._1}`"
        } else if (r._1.equals("etl_insert_date")) {
          s"cast( '${Spoop.etl_insert_date}' as ${r._2}) as `${r._1}`"
        } else if (r._1.equals("pt_date")) {
          //分区字段单独的
          ""
        } else {
          s"cast( `${r._1}` as ${r._2}) as `${r._1}`"
        }
      }).toBuffer.mkString(",").replace(",,", ",").replaceAll(",$", "")

      //获取源数据
      val sourceFrame = catalogService.source(spark, tableInfo)
      //创建临时表
      sourceFrame.createOrReplaceTempView(stgTableName + "_temp")

      //结果吓写到hive表
      val insertSql =
        s"""
           |insert overwrite table stg.${stgTableName} partition(pt_date='${Spoop.date}')
           |select ${stgColsString}
           |from ${stgTableName}_temp
           |""".stripMargin
      spark.sql(insertSql)

      //删除临时表
      spark.catalog.dropTempView(stgTableName + "_temp")

      "success"
    } catch {
      case e: Throwable => {
        e.printStackTrace()
        "failed"
      }
      case _ => {
        logError("-------------------------------")
        "failed"
      }
    }
  }
}

object SpoopService {
  def apply(spark: SparkSession, tableInfo: Row): SpoopService = {
    val service = new SpoopService()
    service.init(spark, tableInfo)
    service
  }
}