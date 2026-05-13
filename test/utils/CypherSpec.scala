/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import testUtils.TestSuite
import uk.gov.hmrc.crypto.EncryptedValue
import utils.Cypher.{bigDecimalCypher, booleanCypher, monthCypher, stringCypher}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when

import java.time.Month

class CypherSpec extends TestSuite {

  private val encryptedBoolean    = mock[EncryptedValue]
  private val encryptedString     = mock[EncryptedValue]
  private val encryptedBigDecimal = mock[EncryptedValue]
  private val encryptedMonth      = mock[EncryptedValue]
  private val encryptedValue      = EncryptedValue("some-value", "some-nonce")

  private implicit val aesGcmAdCrypto: AesGcmAdCrypto = mock[AesGcmAdCrypto]
  private implicit val associatedText: String         = "some-associated-text"

  "stringCypher" should {
    val stringValue = "some-string-value"
    "encrypt string values" in {
      when(
        aesGcmAdCrypto.encrypt(
          eqTo(stringValue)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(encryptedString)

      stringCypher.encrypt(stringValue) mustBe encryptedString
    }

    "decrypt to string values" in {
      when(
        aesGcmAdCrypto.decrypt(
          eqTo(encryptedValue)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(stringValue)

      stringCypher.decrypt(encryptedValue) mustBe stringValue
    }
  }

  "booleanCypher" should {
    val someBoolean = true
    "encrypt boolean values" in {
      when(
        aesGcmAdCrypto.encrypt(
          eqTo(someBoolean.toString)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(encryptedBoolean)

      booleanCypher.encrypt(someBoolean) mustBe encryptedBoolean
    }

    "decrypt to boolean values" in {
      when(
        aesGcmAdCrypto.decrypt(
          eqTo(encryptedValue)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(someBoolean.toString)

      booleanCypher.decrypt(encryptedValue) mustBe someBoolean
    }
  }

  "bigDecimalCypher" should {
    val bigDecimalValue: BigDecimal = 500.0
    "encrypt BigDecimal values" in {
      when(
        aesGcmAdCrypto.encrypt(
          eqTo(bigDecimalValue.toString)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(encryptedBigDecimal)

      bigDecimalCypher.encrypt(bigDecimalValue) mustBe encryptedBigDecimal
    }

    "decrypt to BigDecimal values" in {
      when(
        aesGcmAdCrypto.decrypt(
          eqTo(encryptedValue)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(bigDecimalValue.toString)

      bigDecimalCypher.decrypt(encryptedValue) mustBe bigDecimalValue
    }
  }

  "monthCypher" should {
    val monthValue: Month = Month.APRIL
    "encrypt Month values" in {
      when(
        aesGcmAdCrypto.encrypt(
          eqTo(monthValue.toString)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(encryptedMonth)

      monthCypher.encrypt(monthValue) mustBe encryptedMonth
    }

    "decrypt to Month values" in {
      when(
        aesGcmAdCrypto.decrypt(
          eqTo(encryptedValue)
        )(
          eqTo(associatedText)
        )
      ).thenReturn(monthValue.toString)

      monthCypher.decrypt(encryptedValue) mustBe monthValue
    }
  }
}
