package models

case class ViscachaForumData(
  users: Seq[ViscachaUser],
  groups: Seq[ViscachaGroup],
  categories: Seq[ViscachaCategory],
  forums: Seq[ViscachaForum],
  topics: Seq[ViscachaTopic],
  replies: Seq[ViscachaReply],
  uploads: Seq[ViscachaUpload],
  forumPermissions: Seq[ViscachaForumPermission])

case class FnbForumData(
  users: Seq[User],
  groups: Seq[Group],
  categories: Seq[ForumCategory],
  forums: Seq[Forum],
  threads: Seq[Thread],
  posts: Seq[Post])
