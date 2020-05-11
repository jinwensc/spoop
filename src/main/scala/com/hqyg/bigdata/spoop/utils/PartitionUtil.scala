package com.hqyg.bigdata.spoop.utils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object PartitionUtil {
  def partitionNumber(min: Long, max: Long, partition: Int) = {
    val step = scala.math.ceil((max - min + 1) * 1.0 / partition).toLong
    val partitionRange = ListBuffer[(Long, Long)]()
    for (i <- 0 until partition) {
      partitionRange.append((step * i + min, (i + 1) * step + min))
    }
    partitionRange
  }

  def getNumberPredicates(keyCol: String, range: mutable.Seq[(Long, Long)]): Array[String] = {
    val rangeMax = range.last._2
    val predicates = range.map(r => {
      val min = r._1
      val max = r._2
      if (max == rangeMax) {
        s"${keyCol} >= ${min} and ${keyCol} <= ${max}"
      } else {
        s"${keyCol} >= ${min} and ${keyCol} <  ${max}"
      }
    }).toArray
    predicates
  }

  def getNumberPredicates(keyCol: String, min: Long, max: Long, partition: Int): Array[String] = {
    getNumberPredicates(keyCol, partitionNumber(min, max, partition))
  }

  def partitionDecimal(min: java.math.BigDecimal, max: java.math.BigDecimal, partition: Int) = {
    val step = new java.math.BigDecimal(Math.ceil((max.subtract(min).subtract(new java.math.BigDecimal(1))).divide(new java.math.BigDecimal(partition)).doubleValue()).toLong)
    val partitionRange = ListBuffer[(java.math.BigDecimal, java.math.BigDecimal)]()
    for (i <- 0 until partition) {
      partitionRange.append((step.multiply(new java.math.BigDecimal(i)).add(min), step.multiply(new java.math.BigDecimal(i + 1)).add(min)))
    }
    partitionRange
  }

  def getDecimalPredicates(keyCol: String, range: mutable.Seq[(java.math.BigDecimal, java.math.BigDecimal)]): Array[String] = {
    val rangeMax = range.last._2
    val predicates = range.map(r => {
      val min = r._1
      val max = r._2
      if (max == rangeMax) {
        s"${keyCol} >= ${min} and ${keyCol} <= ${max}"
      } else {
        s"${keyCol} >= ${min} and ${keyCol} <  ${max}"
      }
    }).toArray
    predicates
  }

  def getDecimalPredicates(keyCol: String, min: java.math.BigDecimal, max: java.math.BigDecimal, partition: Int): Array[String] = {
    getDecimalPredicates(keyCol, partitionDecimal(min, max, partition))
  }

  def main(args: Array[String]): Unit = {

    getNumberPredicates("key", partitionNumber(99, 100786, 10))
  }
}
