package scalikejdbc

import scala.collection.mutable.LinkedHashMap
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

private[scalikejdbc] trait OneToOneExtractor[A, B, E <: WithExtractor, Z]
    extends SQL[Z, E]
    with RelationalSQLResultSetOperations[Z] {

  private[scalikejdbc] def extractOne: WrappedResultSet => A
  private[scalikejdbc] def extractTo: WrappedResultSet => Option[B]
  private[scalikejdbc] def transform: (A, B) => Z

  private[scalikejdbc] def processResultSet(oneToOne: (LinkedHashMap[A, Option[B]]), rs: WrappedResultSet): LinkedHashMap[A, Option[B]] = {
    val o = extractOne(rs)
    oneToOne.keys.find(_ == o).map {
      case Some(found) => throw new IllegalRelationshipException(ErrorMessage.INVALID_ONE_TO_ONE_RELATION)
    }.getOrElse {
      oneToOne += (o -> extractTo(rs))
    }
  }

  private[scalikejdbc] def toTraversable(session: DBSession, sql: String, params: Seq[_], extractor: (A, B) => Z): Traversable[Z] = {
    session.foldLeft(statement, rawParameters: _*)(LinkedHashMap[A, Option[B]]())(processResultSet).map {
      case (one, Some(to)) => extractor(one, to)
      case (one, None) => one.asInstanceOf[Z]
    }
  }

}

class OneToOneSQL[A, B, E <: WithExtractor, Z](
  override val statement: String, override val rawParameters: Seq[Any]
)(val one: WrappedResultSet => A)(val toOne: WrappedResultSet => Option[B])(val extractor: (A, B) => Z)
    extends SQL[Z, E](statement, rawParameters)(SQL.noExtractor[Z]("one-to-one extractor(one(RS => A).toOne(RS => Option[B])) is specified, use #map((A,B) =>Z) instead."))
    with AllOutputDecisionsUnsupported[Z, E] {

  def map(extractor: (A, B) => Z): OneToOneSQL[A, B, HasExtractor, Z] = {
    new OneToOneSQL(statement, rawParameters)(one)(toOne)(extractor)
  }

  override def toTraversable(): OneToOneSQLToTraversable[A, B, E, Z] = {
    new OneToOneSQLToTraversable[A, B, E, Z](statement, rawParameters)(one)(toOne)(extractor)
  }
  override def toList(): OneToOneSQLToList[A, B, E, Z] = {
    new OneToOneSQLToList[A, B, E, Z](statement, rawParameters)(one)(toOne)(extractor)
  }
  override def toOption(): OneToOneSQLToOption[A, B, E, Z] = {
    new OneToOneSQLToOption[A, B, E, Z](statement, rawParameters)(one)(toOne)(extractor)(true)
  }
  override def headOption(): OneToOneSQLToOption[A, B, E, Z] = {
    new OneToOneSQLToOption[A, B, E, Z](statement, rawParameters)(one)(toOne)(extractor)(false)
  }
  override def toCollection: OneToOneSQLToCollection[A, B, E, Z] = {
    new OneToOneSQLToCollection[A, B, E, Z](statement, rawParameters)(one)(toOne)(extractor)
  }

  override def single(): OneToOneSQLToOption[A, B, E, Z] = toOption()
  override def first(): OneToOneSQLToOption[A, B, E, Z] = headOption()
  override def list(): OneToOneSQLToList[A, B, E, Z] = toList()
  override def traversable(): OneToOneSQLToTraversable[A, B, E, Z] = toTraversable()
  override def collection: OneToOneSQLToCollection[A, B, E, Z] = toCollection
}

object OneToOneSQL {
  def unapply[A, B, E <: WithExtractor, Z](sqlObject: OneToOneSQL[A, B, E, Z]): Option[(String, Seq[Any], WrappedResultSet => A, WrappedResultSet => Option[B], (A, B) => Z)] = {
    Some((sqlObject.statement, sqlObject.rawParameters, sqlObject.one, sqlObject.toOne, sqlObject.extractor))
  }
}

class OneToOneSQLToTraversable[A, B, E <: WithExtractor, Z](
  override val statement: String, override private[scalikejdbc] val rawParameters: Seq[Any]
)(val one: WrappedResultSet => A)(val toOne: WrappedResultSet => Option[B])(val extractor: (A, B) => Z)
    extends SQL[Z, E](statement, rawParameters)(SQL.noExtractor[Z]("one-to-one extractor(one(RS => A).toOne(RS => Option[B])) is specified, use #map((A,B) =>Z) instead."))
    with SQLToTraversable[Z, E]
    with AllOutputDecisionsUnsupported[Z, E]
    with OneToOneExtractor[A, B, E, Z] {

  import GeneralizedTypeConstraintsForWithExtractor._
  override def apply()(implicit session: DBSession, context: ConnectionPoolContext = NoConnectionPoolContext,
    hasExtractor: ThisSQL =:= SQLWithExtractor): Traversable[Z] = {
    executeQuery[Traversable](session, (session: DBSession) => toTraversable(session, statement, rawParameters, transform))
  }

  private[scalikejdbc] def extractOne: WrappedResultSet => A = one
  private[scalikejdbc] def extractTo: WrappedResultSet => Option[B] = toOne
  private[scalikejdbc] def transform: (A, B) => Z = extractor
}

object OneToOneSQLToTraversable {
  def unapply[A, B, E <: WithExtractor, Z](sqlObject: OneToOneSQLToTraversable[A, B, E, Z]): Option[(String, Seq[Any], WrappedResultSet => A, WrappedResultSet => Option[B], (A, B) => Z)] = {
    Some((sqlObject.statement, sqlObject.rawParameters, sqlObject.one, sqlObject.toOne, sqlObject.extractor))
  }
}

class OneToOneSQLToList[A, B, E <: WithExtractor, Z](
  override val statement: String, override private[scalikejdbc] val rawParameters: Seq[Any]
)(val one: WrappedResultSet => A)(val toOne: WrappedResultSet => Option[B])(val extractor: (A, B) => Z)
    extends SQL[Z, E](statement, rawParameters)(SQL.noExtractor[Z]("one-to-one extractor(one(RS => A).toOne(RS => Option[B])) is specified, use #map((A,B) =>Z) instead."))
    with SQLToList[Z, E]
    with AllOutputDecisionsUnsupported[Z, E]
    with OneToOneExtractor[A, B, E, Z] {

  import GeneralizedTypeConstraintsForWithExtractor._
  override def apply()(implicit session: DBSession, context: ConnectionPoolContext = NoConnectionPoolContext,
    hasExtractor: ThisSQL =:= SQLWithExtractor): List[Z] = {
    executeQuery[List](session, (session: DBSession) => toTraversable(session, statement, rawParameters, extractor).toList)
  }

  private[scalikejdbc] def extractOne: WrappedResultSet => A = one
  private[scalikejdbc] def extractTo: WrappedResultSet => Option[B] = toOne
  private[scalikejdbc] def transform: (A, B) => Z = extractor
}

object OneToOneSQLToList {
  def unapply[A, B, E <: WithExtractor, Z](sqlObject: OneToOneSQLToList[A, B, E, Z]): Option[(String, Seq[Any], WrappedResultSet => A, WrappedResultSet => Option[B], (A, B) => Z)] = {
    Some((sqlObject.statement, sqlObject.rawParameters, sqlObject.one, sqlObject.toOne, sqlObject.extractor))
  }
}

class OneToOneSQLToCollection[A, B, E <: WithExtractor, Z](
  override val statement: String, override private[scalikejdbc] val rawParameters: Seq[Any]
)(val one: WrappedResultSet => A)(val toOne: WrappedResultSet => Option[B])(val extractor: (A, B) => Z)
    extends SQL[Z, E](statement, rawParameters)(SQL.noExtractor[Z]("one-to-one extractor(one(RS => A).toOne(RS => Option[B])) is specified, use #map((A,B) =>Z) instead."))
    with SQLToCollection[Z, E]
    with AllOutputDecisionsUnsupported[Z, E]
    with OneToOneExtractor[A, B, E, Z] {

  import GeneralizedTypeConstraintsForWithExtractor._
  override def apply[C[_]]()(implicit session: DBSession, context: ConnectionPoolContext = NoConnectionPoolContext,
    hasExtractor: ThisSQL =:= SQLWithExtractor, cbf: CanBuildFrom[Nothing, Z, C[Z]]): C[Z] = {
    executeQuery(session, (session: DBSession) => toTraversable(session, statement, rawParameters, extractor).to[C])
  }

  private[scalikejdbc] def extractOne: WrappedResultSet => A = one
  private[scalikejdbc] def extractTo: WrappedResultSet => Option[B] = toOne
  private[scalikejdbc] def transform: (A, B) => Z = extractor
}

object OneToOneSQLToCollection {
  def unapply[A, B, E <: WithExtractor, Z](sqlObject: OneToOneSQLToCollection[A, B, E, Z]): Option[(String, Seq[Any], WrappedResultSet => A, WrappedResultSet => Option[B], (A, B) => Z)] = {
    Some((sqlObject.statement, sqlObject.rawParameters, sqlObject.one, sqlObject.toOne, sqlObject.extractor))
  }
}

class OneToOneSQLToOption[A, B, E <: WithExtractor, Z](
  override val statement: String, override private[scalikejdbc] val rawParameters: Seq[Any]
)(val one: WrappedResultSet => A)(val toOne: WrappedResultSet => Option[B])(val extractor: (A, B) => Z)(val isSingle: Boolean = true)
    extends SQL[Z, E](statement, rawParameters)(SQL.noExtractor[Z]("one-to-one extractor(one(RS => A).toOne(RS => Option[B])) is specified, use #map((A,B) =>Z) instead."))
    with SQLToOption[Z, E]
    with AllOutputDecisionsUnsupported[Z, E]
    with OneToOneExtractor[A, B, E, Z] {

  import GeneralizedTypeConstraintsForWithExtractor._
  override def apply()(implicit session: DBSession, context: ConnectionPoolContext = NoConnectionPoolContext, hasExtractor: ThisSQL =:= SQLWithExtractor): Option[Z] = {
    executeQuery[Option](session, (session: DBSession) => toSingle(toTraversable(session, statement, rawParameters, extractor)))
  }

  private[scalikejdbc] def extractOne: WrappedResultSet => A = one
  private[scalikejdbc] def extractTo: WrappedResultSet => Option[B] = toOne
  private[scalikejdbc] def transform: (A, B) => Z = extractor
}

object OneToOneSQLToOption {
  def unapply[A, B, E <: WithExtractor, Z](sqlObject: OneToOneSQLToOption[A, B, E, Z]): Option[(String, Seq[Any], WrappedResultSet => A, WrappedResultSet => Option[B], (A, B) => Z, Boolean)] = {
    Some((sqlObject.statement, sqlObject.rawParameters, sqlObject.one, sqlObject.toOne, sqlObject.extractor, sqlObject.isSingle))
  }
}
