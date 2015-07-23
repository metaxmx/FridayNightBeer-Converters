package util

import scala.language.postfixOps

import org.apache.commons.lang.StringEscapeUtils

object Converter {

  def unescapeViscacha(input: String) = StringEscapeUtils.unescapeHtml(input)

  def checkEmpty(input: String): Option[String] = input match {
    case "" | null => None
    case s         => Some(s)
  }

  def checkEmpty[T](seq: Seq[T]): Option[Seq[T]] =
    if (seq isEmpty) None else Some(seq)

  def convertContent(input: String): String = nl2br(input)

  def nl2br(input: String): String = input.replaceAll("\n|\r|\r\n", "<br>")

}