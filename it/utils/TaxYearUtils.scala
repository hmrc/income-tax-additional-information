

package utils

import play.api.Logging

object TaxYearUtils extends Logging {

  def convertStringTaxYear(taxYear: Int): String = {
    s"${taxYear - 1}-${taxYear.toString takeRight 2}"
  }
}