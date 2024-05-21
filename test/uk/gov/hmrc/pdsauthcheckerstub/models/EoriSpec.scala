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

package uk.gov.hmrc.pdsauthcheckerstub.models

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json, JsonValidationError}
import uk.gov.hmrc.pdsauthcheckerstub.base.TestCommonGenerators

class EoriSpec extends AnyWordSpec with Matchers with TestCommonGenerators with ScalaCheckPropertyChecks {
  "should fail to decode when invalid country code is provided" in forAll(
    Gen.asciiStr.retryUntil(!Seq("GB", "XI").contains(_)),
    validEoriSuffix
  ) { (prefix, suffix) =>
    PdsAuthRequest.format.reads {
      Json.obj(
        "authType" -> "UKIM",
        "eoris" -> Json.arr(prefix + suffix)
      )
    } match {
      case JsError(errors) =>
        errors.flatMap(_._2) should contain only JsonValidationError("EORI format invalid")
      case other => fail(s"unexpected JS parse result: $other")
    }
  }

  "should fail to decode when more or less than 12 digits are present in the eori number" in forAll(
    validEoriPrefix,
    Gen.infiniteLazyList(Gen.numStr).flatMap(nums => Gen.chooseNum(0, 100).retryUntil(_ != 12).map(nums.take))
  ) { (prefix, suffix) =>
    PdsAuthRequest.format.reads {
      Json.obj(
        "authType" -> "UKIM",
        "eoris" -> Json.arr(prefix + suffix)
      )
    } match {
      case JsError(errors) =>
        errors.flatMap(_._2) should contain only JsonValidationError("EORI format invalid")
      case other => fail(s"unexpected JS parse result: $other")
    }
  }

  "should fail to decode when non-digits are present in the eori number" in forAll(
    validEoriPrefix,
    Gen.infiniteLazyList(Gen.asciiStr).map(_.take(12)).retryUntil(_.exists(_.toIntOption.isEmpty))
  ) { (prefix, suffix) =>
    PdsAuthRequest.format.reads {
      Json.obj(
        "authType" -> "UKIM",
        "eoris" -> Json.arr(prefix + suffix)
      )
    } match {
      case JsError(errors) =>
        errors.flatMap(_._2) should contain only JsonValidationError("EORI format invalid")
      case other => fail(s"unexpected JS parse result: $other")
    }
  }
}
