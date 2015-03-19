package sample

object Test {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scalikejdbc.TxBoundary.Future._
  scalikejdbc.DB.localTx(session => scala.concurrent.Future.successful(1))

}
