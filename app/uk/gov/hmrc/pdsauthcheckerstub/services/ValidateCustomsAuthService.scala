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

import uk.gov.hmrc.pdsauthcheckerstub.models.{AuthType, Eori, PdsAuthResponse, PdsAuthResponseResult}

import java.time.LocalDate
import javax.inject.Singleton

@Singleton()
class ValidateCustomsAuthService() {
  def validateCustoms(eoris: Seq[Eori], authType: AuthType, dateOption: Option[LocalDate]): PdsAuthResponse = {
    val pdsAuthResponseResults: Seq[PdsAuthResponseResult] = eoris.map { eori =>
      val valid = !eori.value.endsWith("999")
      PdsAuthResponseResult(eori, valid, if (valid) 0 else 1)
    }
    PdsAuthResponse(dateOption.getOrElse(LocalDate.now), authType, pdsAuthResponseResults)
  }
}
