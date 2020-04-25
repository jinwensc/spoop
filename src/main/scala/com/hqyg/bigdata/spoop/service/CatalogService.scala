package com.hqyg.bigdata.spoop.service

import java.text.SimpleDateFormat

import org.apache.spark.sql.{DataFrame, DataFrameReader, Row, SparkSession}

import scala.collection.mutable

trait CatalogService {

  /**
   * catalog名称
   *
   * @return 名称
   */
  def getName(): String;

  /**
   * 获取DataFrameReader
   *
   * @return DataFrameReader
   */
  def source(spark: SparkSession, tableInfo: Row): DataFrame;


  //  def sink(spark: SparkSession, tableLoadType: Integer, updateCol: String, dbName: String, tableName: String, partitionKey: String, dbIp: String, dbPort: String, dbUser: String, dbPwd: String): DataFrame;

//  def getPredicates(keyCol: String, range: mutable.Seq[(Long, Long)]): Array[String] = {
//    val predicates = new Array[String](range.size)
//    val last = range.last
//    range.map(r => {
//      val min = r._1
//      val max = r._2
//      s"${keyCol} >= ${min} and keyCol} < ${max}"
//    })
//    predicates
//  }
}
