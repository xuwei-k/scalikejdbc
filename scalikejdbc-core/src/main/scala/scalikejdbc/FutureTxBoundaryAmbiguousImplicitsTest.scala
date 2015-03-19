package scalikejdbc

object FutureTxBoundaryAmbiguousImplicitsTest {

  def test: Unit = {
    import TxBoundary.Future._
    DB.localTx(session => scala.concurrent.Future.successful(1))
  }

}
