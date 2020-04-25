package com.hqyg.bigdata.spoop

import java.text.SimpleDateFormat

import com.hqyg.bigdata.spoop.service.{CatalogFactory, SpoopService}
import com.hqyg.bigdata.spoop.service.impl.MysqlCatalogService
import com.hqyg.bigdata.spoop.utils.{CommandUtil, DateUtil, PropsUtil}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions._

import scala.actors.threadpool.{ExecutorService, Executors, Future}
import scala.collection.mutable.ListBuffer

/**
 * spark 版sqoop
 * by yangjinwen 2020-04-23
 */
class Spoop extends Logging {
  var spark: SparkSession = _
  var executors: ExecutorService = _

  /**
   * 数据抽取
   */
  def runImport(dbNum: String, tables: Array[String]): Unit = {
    //获取表配置信息
    val tableInfos: Array[Row] = getMeta(dbNum, tables)

    //多线程处理
    executors = Executors.newFixedThreadPool(Integer.valueOf(PropsUtil.getString("executor.pool.size")))
    val resultList = ListBuffer[Future]()
    tableInfos.foreach(info => {
      //单表处理
      val service = SpoopService(spark, info)
      val future = executors.submit(service)
      resultList.append(future)
    })
    //结果处理
    resultList.map(_.get().toString).foreach(println(_))
  }

  //TODO 导出
  def runExport(): Unit = {

  }


  /**
   * spark初始化
   *
   * @param conf
   */
  def init(conf: SparkConf): Unit = {
    spark = SparkSession.builder().config(conf)
      .master("yarn")
      .enableHiveSupport()
      .getOrCreate()
  }

  /**
   * spark初始化开发环境，不可用于生产环境
   *
   * @param conf
   */
  def initDev(conf: SparkConf): Unit = {
    spark = SparkSession.builder().config(conf)
      .master("local[1]")
      .getOrCreate()

    //生成测试环境要使用的表
    spark.sql("create database if not exists stg")
    spark.read.orc("D:\\workspace\\hqyg\\data\\spoop_test").limit(1)
      .withColumn("pt_date", lit(DateUtil.get_inc_start_last(Spoop.date)))
      .write.mode(SaveMode.Overwrite)
      .format("orc")
      .partitionBy("pt_date")
      .saveAsTable("stg.stg_oms_s_oms_users")
  }

  def stop(): Unit = {
    executors.shutdownNow()
    spark.stop()
  }

  /**
   * 获取oracle中表的元数据信息
   **/
  def getMeta(dbNum: String, tables: Array[String]) = {
    val tableStr = tables.mkString("','")
    val tableSql =
      s"""
         | (select a.FROM_TABLE, a.TO_TABLE, a.TABLE_LOAD_TYPE, a.MAP_NUM, a.COL_CONCAT_SQOOP, a.UPDATE_COL, a.DATA_FROM, a.KEY_COL, b.SOURCE_TYPE, b.DB_IP, b.DB_USER, b.DB_PWD, b.DB_PORT, b.DB_DATABASE
         |    from ${PropsUtil.getString("stg.metal.tblsTable")} a
         |  left join ${PropsUtil.getString("stg.metal.dbsTable")} b
         |    on a.DB_NUM = b.DB_NUM
         |  where a.IS_VAILD=0 and b.IS_VAILD=0 and a.DB_NUM='${dbNum}' and b.DB_NUM='${dbNum}' and a.FROM_TABLE in ('${tableStr}'))  tmp
            """.stripMargin

    logInfo(tableSql)
    //读取全表
    val tableInfo: Array[Row] = spark.read.format("jdbc")
      .option("driver", PropsUtil.getString("stg.metal.driverName"))
      .option("url", PropsUtil.getString("stg.metal.jdbcUrl"))
      .option("user", PropsUtil.getString("stg.metal.user"))
      .option("password", PropsUtil.getString("stg.metal.password"))
      .option("dbtable", tableSql)
      .load.filter(r => {
      tables.contains(r.getAs[String]("FROM_TABLE"))
    }).collect()
    tableInfo.foreach(println(_))
    if (tableInfo.size != tables.size) {
      logWarning(s"@@find from_table:---------------------------\n${tableInfo.map(_.getAs[String]("FROM_TABLE")).toBuffer}")
      logWarning(s"@@need from_table:---------------------------\n${tables}")
      logError("@@table not exists in ETL_HIVE_TABLE_CONFIG")
      System.exit(1)
    }

    tableInfo
  }

  def registerCatalogs(): Unit = {
    //注册mysql的数据库服务
    CatalogFactory.catalogs.put("mysql", MysqlCatalogService.getInstance())
    //注册sqlserver
    //注册postgresql
    //注册oracle 。。。
  }


}

object Spoop {
  private val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  var etl_insert_date = format.format(new java.util.Date())
  var date: String = _

  /**
   * spoop程序入口
   *
   * @param args 参数：-m import -db_num 103_01 -tables S_OMS_USERS -date 20200423
   */
  def main(args: Array[String]): Unit = {
    val command = CommandUtil.parse(args)
    val mode: String = command.getOptionValue("m")
    val dbNum: String = command.getOptionValue("db_num")
    val tables = command.getOptionValues("tables")
    date = command.getOptionValue("date")
    val spoop = new Spoop()
    spoop.registerCatalogs()

    //-m import -job testJob -db_num 103_01 -tables S_OMS_USERS -date 20200423
    spoop.initDev(getSparkConf())
    //spoop.init(getSparkConf())
    if (mode.equals("import")) {
      spoop.runImport(dbNum, tables)
    } else if (mode.equals("export")) {
      //TODO
      spoop.runExport()
    } else {
      //TODO
    }
    spoop.stop()
  }

  def getSparkConf(): SparkConf = {
    val conf = new SparkConf()
      .set("spark.speculation", "false")
      .set("spark.speculation.interval", "1000ms")
      .set("spark.task.reaper.enabled", "true")
      .set("spark.blacklist.enabled", "false")
      .set("spark.io.compression.codec", "lz4")
      .set("spark.io.compression.lz4.blockSize", "256k")
      .set("spark.shuffle.sort.bypassMergeThreshold", "200")
      .set("spark.network.timeout", "300s")
      .set("spark.locality.wait", "3s")
      .set("spark.shuffle.file.buffer", "256k")
      .set("spark.sql.shuffle.partitions", "200")
      .set("spark.default.parallelism", "50")
      .set("spark.serialize", classOf[KryoSerializer].getName())
      .set("spark.kryoserializer.buffer.max", "128m")
      .set("spark.rdd.compress", "true")
      .set("spark.reducer.maxSizeInFlight", "128m")
      .set("spark.sql.adaptive.enabled", "true")
      .set("spark.sql.adaptive.shuffle.targetPostShuffleInputSize", "164217728b")
      .set("spark.sql.cbo.enabled", "true")
      .set("hive.exec.orc.split.strategy", "ETL")
      .set("spark.sql.orc.compression.codec", "snappy")
      .set("spark.sql.autoBroadcastJoinThreshold", "10428800")
      .set("spark.hive.mapred.supports.subdirectories", "true")
      .set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")
      .set("spark.executor.memoryOverhead", "500M")
      .set("spark.scheduler.mode", "FIFO")
      .set("spark.sql.broadcastTimeout", "1500000ms")
      .set("spark.ui.retainedJobs", "500")
      .set("spark.ui.retainedStages", "500")
      .set("spark.network.timeout", "600s")

      .set("spark.yarn.am.waitTime", "600s")
      .set("spark.sql.hive.merge.mapfiles", "true")
      .set("spark.sql.hive.merge.mapredfiles", "true")
      .set("spark.sql.hive.merge.size.per.task", "134217728")
      .set("spark.sql.hive.merge.smallfiles.avgsize", "134217728")

    conf
  }
}
