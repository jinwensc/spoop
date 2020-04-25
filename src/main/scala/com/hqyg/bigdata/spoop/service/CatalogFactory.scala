package com.hqyg.bigdata.spoop.service

import com.hqyg.bigdata.spoop.common.SpoopException
import org.apache.spark.internal.Logging

import scala.collection.mutable

object CatalogFactory extends Logging {
  val catalogs = mutable.HashMap[String, CatalogService]()

  def getCatalog(catalog: String) = {
    val service = catalogs.getOrElse(catalog, null)
    if (service == null) {
      throw new Exception(s"${catalog} is not implements!")
    }
    service
  }
}
