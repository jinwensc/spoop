package com.hqyg.bigdata.spoop.utils

import java.util.Calendar

object DateUtil {
  /**
   * 给月份和日期整成mm或者dd的格式，例如：7转换成07
   **/
  def stringComplement(time: Integer): String = if (time < 10) "0" + time else time.toString

  /**
   * 求前一天的日志
   *
   * @param dt 日期：20200401
   * @return 前一天的日期
   */
  def get_inc_start_last(dt: String): String = {
    val calendar = Calendar.getInstance
    calendar.set(dt.substring(0, 4).toInt, dt.substring(4, 6).toInt - 1, dt.substring(6, 8).toInt, 0, 0)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.get(Calendar.YEAR) + stringComplement(calendar.get(Calendar.MONDAY) + 1) + stringComplement(calendar.get(Calendar.DAY_OF_MONTH))
  }

}
