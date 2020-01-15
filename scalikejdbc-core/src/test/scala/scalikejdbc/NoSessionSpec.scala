package scalikejdbc

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NoSessionSpec extends AnyFlatSpec with Matchers {

  behavior of "NoSession"

  it should "be available" in {
    val singleton = NoSession
    singleton should not be null
  }

}
