/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.pdsauthcheckerstub.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pdsauthcheckerstub.models.{AuthType, Eori}


class ValidateCustomsAuthServiceSpec extends AnyWordSpec with Matchers {

  "ValidateCustomsAuthService" should {
    val service = new ValidateCustomsAuthService()

    "return 'valid' as true for results not ending in 999" in {

      val eoris = Seq(Eori("GB123456789000"), Eori("GB123456789001"))
      val authType = AuthType.UKIM

      val response = service.validateCustoms(eoris, authType)

      response.results.foreach { result =>
        result.valid shouldBe true
        result.code shouldBe 0
      }
      response.authType shouldBe authType
    }

    "return 'valid' as false for results for EORIs ending with '999'" in {

      val eoris = Seq(Eori("GB123456789999"), Eori("GB987654321999"))
      val authType = AuthType.UKIM

      val response = service.validateCustoms(eoris, authType)

      response.results.foreach { result =>
        result.valid shouldBe false
        result.code shouldBe 1
      }
      response.authType shouldBe authType
    }

    "handle a mix of valid and invalid EORIs" in {

      val eoris = Seq(Eori("GB123456789000"), Eori("GB987654321999"))
      val authType = AuthType.UKIM

      val response = service.validateCustoms(eoris, authType)
      response.results.head.valid shouldBe true
      response.results.head.code shouldBe 0

      response.results(1).valid shouldBe false
      response.results(1).code shouldBe 1

      response.authType shouldBe authType

    }
  }
}
