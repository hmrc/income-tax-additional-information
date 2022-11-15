

package models

import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import testUtils.TestSuite

class UserSpec extends TestSuite {

  ".isAgent" should {

    "return true" when {

      "user has an arn" in {
        User[AnyContent]("23456789", Some("123456789"))(FakeRequest()).isAgent mustBe true
      }

    }

    "return false" when {

      "user does not have an arn" in {
        User[AnyContent]("23456789", None)(FakeRequest()).isAgent mustBe false
      }

    }

  }

}