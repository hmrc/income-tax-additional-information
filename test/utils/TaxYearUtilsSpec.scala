

package utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxYearUtilsSpec extends AnyWordSpec with Matchers {

  "IFTaxYearHelper" should {

    "return a string containing the last year and the last two digits of this year" in {
      val taxYear = 2024
      val result = TaxYearUtils.convertStringTaxYear(taxYear)
      result mustBe "2023-24"
    }

  }
}

