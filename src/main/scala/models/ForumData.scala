package models

import app.AggregateData

case class ViscachaForumData(
  users: Seq[ViscachaUser],
  groups: Seq[ViscachaGroup],
  categories: Seq[ViscachaCategory],
  forums: Seq[ViscachaForum],
  topics: Seq[ViscachaTopic],
  replies: Seq[ViscachaReply],
  forumPermissions: Seq[ViscachaForumPermission]) {

  def withUsers(users: Seq[ViscachaUser]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withGroups(groups: Seq[ViscachaGroup]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withCategories(categories: Seq[ViscachaCategory]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withForums(forums: Seq[ViscachaForum]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withTopics(topics: Seq[ViscachaTopic]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withReplies(replies: Seq[ViscachaReply]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

  def withForumPermission(forumPermissions: Seq[ViscachaForumPermission]) = ViscachaForumData(users, groups, categories, forums, topics, replies, forumPermissions)

}

object ViscachaForumData {

  def apply(): ViscachaForumData = ViscachaForumData(null, null, null, null, null, null, null)

}

case class FnbForumData(
  users: Seq[User],
  groups: Seq[Group],
  categories: Seq[ForumCategory],
  forums: Seq[Forum],
  threads: Seq[Thread],
  posts: Seq[Post])