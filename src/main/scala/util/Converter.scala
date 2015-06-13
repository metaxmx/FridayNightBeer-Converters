package util

import org.apache.commons.lang.StringEscapeUtils

object Converter {

  def unescapeViscacha(input: String) = StringEscapeUtils.unescapeHtml(input)

  def checkEmpty(input: String): Option[String] = input match {
    case "" | null => None
    case s         => Some(s)
  }

}