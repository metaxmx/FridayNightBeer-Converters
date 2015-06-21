package app

import models._
import util.Logging
import util.Converter._
import org.joda.time.DateTime
import scala.language.postfixOps

class AggregateData(viscachaData: ViscachaForumData) extends Logging {

  lazy val userMap = viscachaData.users map { user => user.name -> user.id } toMap

  def aggregate: FnbForumData = {

    logger info "Aggregating data ..."

    val users = viscachaData.users map {
      user => User(user.id, user.name.toLowerCase, user.pw, user.name, checkEmpty(user.fullname).map(unescapeViscacha), None)
    }

    // TODO
    val groups = Seq[Group]()

    val categories = viscachaData.categories map {
      cat => ForumCategory(cat.id, unescapeViscacha(cat.name), cat.position, None)
    }

    val forums = viscachaData.forums map {
      forum =>
        Forum(forum.id, unescapeViscacha(forum.name), Some(forum.description).map(unescapeViscacha),
          forum.parent, forum.position, forum.readonly > 0, None)
    }

    // TODO: Create real type
    val threads = viscachaData.topics map {
      topic => FnbThread(topic.id, topic.board, unescapeViscacha(topic.topic), topic.name, new DateTime(topic.date * 1000), topic.sticky > 0)
    }

    // TODO: Create real type
    val posts = viscachaData.replies map {
      reply =>
        FnbPost(
          reply.id,
          reply.topic_id,
          unescapeViscacha(reply.comment),
          reply.name.toInt,
          new DateTime(reply.date * 1000),
          parseEdits(reply.edit))
    }

    FnbForumData(users, groups, categories, forums)
  }

  def parseEdits(edits: String): Option[Seq[FnbPostEdit]] = checkEmpty(edits) flatMap {
    edit =>
      {
        val editObjs = wrapRefArray(edit.split('\n')) map {
          _.split('\t')
        } filter {
          parts => userMap contains parts(0)
        } map {
          parts => FnbPostEdit(userMap get parts(0) get, new DateTime(parts(1).toLong * 1000), checkEmpty(parts(2)), parts(3))
        }
        checkEmpty(editObjs)
      }
  }

}