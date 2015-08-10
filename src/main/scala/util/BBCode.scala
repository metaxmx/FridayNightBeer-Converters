package util

import scala.collection.mutable.Stack

object BBCode {

  val bbTagPattern = """\[(/)?([a-zA-Z*]+)(=([\s]))?\]""".r

  case class MatchData(start: Int, end: Int, value: String, tag: String, param: Option[String], closing: Boolean)

  def parseTagMatches(input: String): Seq[MatchData] = {
    val matches = for {
      bbMatch <- bbTagPattern.findAllMatchIn(input)
    } yield MatchData(bbMatch.start, bbMatch.end, bbMatch.matched, bbMatch.group(2), Option(bbMatch.group(4)), "/" == bbMatch.group(1))
    matches.toSeq
  }

  trait ParseSegment

  case class VerbatimSegment(value: String) extends ParseSegment

  case class TagSegment(value: String, tag: String, param: Option[String], closing: Boolean) extends ParseSegment

  def tagSegment(matchData: MatchData) = TagSegment(matchData.value, matchData.tag, matchData.param, matchData.closing)

  def parseSegments(input: String, matches: Seq[MatchData]): Seq[ParseSegment] = {
    val firstUnmatched = matches.lastOption.map(_.end + 1).getOrElse(0)
    Range(0, matches.size).toSeq.flatMap {
      index =>
        val currentMatch = matches(index)
        val copyFrom = if (index == 0) 0 else matches(index - 1).end
        val copyTo = currentMatch.start - 1
        if (copyFrom > copyTo)
          Seq(VerbatimSegment(input.substring(copyFrom, copyTo)), tagSegment(currentMatch))
        else
          Seq(tagSegment(currentMatch))
    } ++ Seq(VerbatimSegment(input.substring(firstUnmatched)))
  }

}

trait Component {

}

trait BBHandler {

  def tag: String

  def empty: Boolean

  def renderOpeningTag(instance: BBInstance): String

  def renderClosingTag(instance: BBInstance): String

}

class BBInstance(handler: BBHandler, attributes: Map[String, String]) {

  def renderOpeningTag: String = handler renderOpeningTag this

  def renderClosingTag: String = handler renderClosingTag this

}

class BBSimpleHandler(val tag: String, htmlTag: String) extends BBHandler {

  def this(tag: String) = this(tag, tag)

  override def empty = false

  override def renderOpeningTag(instance: BBInstance) = s"<$htmlTag>"

  override def renderClosingTag(instance: BBInstance) = s"</$htmlTag>"

}

class BBSimpleEmptyHandler(val tag: String, htmlTag: String) extends BBHandler {

  def this(tag: String) = this(tag, tag)

  override def empty = true

  override def renderOpeningTag(instance: BBInstance) = s"<$htmlTag />"

  override def renderClosingTag(instance: BBInstance) = ""

}

object BBHandlers {

  val bold = new BBSimpleHandler("b")
  val italic = new BBSimpleHandler("i")
  val underline = new BBSimpleHandler("u")

  val basicHandlers = Seq(bold, italic, underline)

  val handlers = basicHandlers

}