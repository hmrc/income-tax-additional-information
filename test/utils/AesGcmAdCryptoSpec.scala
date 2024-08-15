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

import support.stubs.AppConfigStub
import testUtils.TestSuite
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, EncryptedValue}

class AesGcmAdCryptoSpec extends TestSuite {

  implicit private val associatedText: String = "some-associated-text"
  private val mockAesGcmAdCryptoFactory = mock[AesGcmAdCryptoFactory]
  private val nonce = "some-nonce"
  private val valueToEncrypt = "value-to-encrypt"
  private val decryptedValue = "decrypted-value"
  private val mockAesGcmAdCrypto = new AdEncrypter with AdDecrypter {
    override def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue = EncryptedValue("some-value", nonce)

    override def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = decryptedValue
  }

  ".encrypt" when {
    "useEncryption is true" should {
      val underTest: AesGcmAdCrypto = new AesGcmAdCrypto(new AppConfigStub().config(), mockAesGcmAdCryptoFactory)

      "return encrypted value" in {
        (() => mockAesGcmAdCryptoFactory.instance())
          .expects()
          .returning(mockAesGcmAdCrypto)

        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue("some-value", nonce)
      }
    }

    "useEncryption is false" should {
      val appConfig = new AppConfigStub().noEncryptionConfig()
      val underTest: AesGcmAdCrypto = new AesGcmAdCrypto(appConfig, mockAesGcmAdCryptoFactory)

      "return encrypted value" in {
        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue(valueToEncrypt, valueToEncrypt + "-Nonce")
      }
    }
  }

  ".decrypt" when {
    "useEncryption is true" should {
      val underTest: AesGcmAdCrypto = new AesGcmAdCrypto(new AppConfigStub().config(), mockAesGcmAdCryptoFactory)

      "return encrypted value" in {
        (() => mockAesGcmAdCryptoFactory.instance())
          .expects()
          .returning(mockAesGcmAdCrypto)

        underTest.decrypt(EncryptedValue("value-to-decrypt", nonce)) mustBe decryptedValue
      }
    }

    "useEncryption is false" should {
      val appConfig = new AppConfigStub().noEncryptionConfig()
      val underTest: AesGcmAdCrypto = new AesGcmAdCrypto(appConfig, mockAesGcmAdCryptoFactory)

      "return encrypted value" in {
        underTest.decrypt(EncryptedValue(valueToEncrypt, nonce)) mustBe valueToEncrypt
      }
    }
  }
}
