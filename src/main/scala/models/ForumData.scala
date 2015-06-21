package models

import app.AggregateData

case class ViscachaForumData(
  users: Seq[ViscachaUser],
  categories: Seq[ViscachaCategory],
  forums: Seq[ViscachaForum],
  topics: Seq[ViscachaTopic],
  replies: Seq[ViscachaReply]) {

  def withUsers(users: Seq[ViscachaUser]) = ViscachaForumData(users, categories, forums, topics, replies)

  def withCategories(categories: Seq[ViscachaCategory]) = ViscachaForumData(users, categories, forums, topics, replies)

  def withForums(forums: Seq[ViscachaForum]) = ViscachaForumData(users, categories, forums, topics, replies)

  def withTopics(topics: Seq[ViscachaTopic]) = ViscachaForumData(users, categories, forums, topics, replies)

  def withReplies(replies: Seq[ViscachaReply]) = ViscachaForumData(users, categories, forums, topics, replies)

}

object ViscachaForumData {

  def apply(): ViscachaForumData = ViscachaForumData(null, null, null, null, null)

}

case class FnbForumData(
  users: Seq[User],
  groups: Seq[Group],
  categories: Seq[ForumCategory],
  forums: Seq[Forum])