package hoge

import java.time._

import org.slf4j._
import scalikejdbc.SQLSyntaxSupportFeature.ResultName
import scalikejdbc._

import scala.collection.concurrent.TrieMap
import scala.util.control.NonFatal
import scala.language.implicitConversions
import scalikejdbc.interpolation.Implicits.scalikejdbcSQLSyntaxToStringImplicitDef

object User extends SQLSyntaxSupport[User] {

  override val tableName = "users"

  // Both of columns and columnNames are OK
  //override val columns = Seq("id", "first_name", "group_id")
  override val columnNames: Seq[String] = Seq("id", "first_name", "group_id")

  override val nameConverters: Map[String, String] = Map("uid" -> "id")
  override val delimiterForResultName = "_Z_"
  override val forceUpperCase = true

  def apply(rs: WrappedResultSet, u: ResultName[User]): User = {
    User(id = rs.int(u.id), firstName = rs.stringOpt(u.firstName), groupId = rs.intOpt(u.groupId))
  }

  def apply(rs: WrappedResultSet, u: ResultName[User], g: ResultName[Group]): User = {
    apply(rs, u).copy(group = rs.intOpt(g.id).map(id => Group(id = id, websiteUrl = rs.stringOpt(g.field("websiteUrl")))))
  }
}

case class User(id: Int, firstName: Option[String], groupId: Option[Int] = None, group: Option[Group] = None)

object Group extends SQLSyntaxSupport[Group] {
  override val tableName = "groups"
  override val columns: Seq[String] = Seq("id", "website_url")
  def apply(rs: WrappedResultSet, g: ResultName[Group]): Group = Group(id = rs.int(g.id), websiteUrl = rs.stringOpt(g.field("websiteUrl")))
}
case class Group(id: Int, websiteUrl: Option[String], members: collection.Seq[User] = Nil)

object GroupMember extends SQLSyntaxSupport[GroupMember] {
  override val tableName = "group_members"
  override val columns: Seq[String] = Seq("user_id", "group_id")
}
// case class GroupMember(userId: Int, groupId: Int) // works!
class GroupMember(val userId: Int, val groupId: Int)
// class GroupMember(userId: Int, groupId: Int) // works!
// class GroupMember(userId: Int) // compilation error

class NotFoundEntity(val id: Long, val name: String)
object NotFoundEntity extends SQLSyntaxSupport[NotFoundEntity]

case class NamedDBEntity(id: Long)
object NamedDBEntity extends SQLSyntaxSupport[NamedDBEntity] {
  override def connectionPoolName: Symbol = Symbol("yetanother")
}

