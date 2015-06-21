package models

import reactivemongo.bson._
import org.joda.time.DateTime

/*
 * --------------- Obsolete - keep until missing classes in FNB are included ------------
 */

/*
 * --- Thread ---
 */

case class FnbThread(
  _id: Int,
  forum: Int,
  topic: String,
  userCreated: Int,
  dateCreated: DateTime,
  sticky: Boolean)

/*
 * --- Post/Reply ---
 */

case class FnbPostEdit(
  user: Int,
  date: DateTime,
  reason: Option[String],
  ip: String)

case class FnbPost(
  _id: Int,
  thread: Int,
  text: String,
  userCreated: Int,
  dateCreated: DateTime,
  edits: Option[Seq[FnbPostEdit]])
  