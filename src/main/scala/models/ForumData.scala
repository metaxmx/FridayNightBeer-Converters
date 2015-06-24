package models

import app.AggregateData

case class ViscachaForumData(
  users: Seq[ViscachaUser],
  groups: Seq[ViscachaGroup],
  categories: Seq[ViscachaCategory],
  forums: Seq[ViscachaForum],
  topics: Seq[ViscachaTopic],
  replies: Seq[ViscachaReply]) {

  def withUsers(users: Seq[ViscachaUser]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

  def withGroups(groups: Seq[ViscachaGroup]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

  def withCategories(categories: Seq[ViscachaCategory]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

  def withForums(forums: Seq[ViscachaForum]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

  def withTopics(topics: Seq[ViscachaTopic]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

  def withReplies(replies: Seq[ViscachaReply]) = ViscachaForumData(users, groups, categories, forums, topics, replies)

}

object ViscachaForumData {

  def apply(): ViscachaForumData = ViscachaForumData(null, null, null, null, null, null)

}

case class FnbForumData(
  users: Seq[User],
  groups: Seq[Group],
  categories: Seq[ForumCategory],
  forums: Seq[Forum])