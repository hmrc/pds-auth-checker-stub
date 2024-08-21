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

package uk.gov.hmrc.pdsauthcheckerstub.controllers

import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.pdsauthcheckerstub.base.TestCommonGenerators
import uk.gov.hmrc.pdsauthcheckerstub.models.{ErrorDetail, PdsAuthRequest}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.pdsauthcheckerstub.services.ValidateCustomsAuthService
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.Json
import uk.gov.hmrc.pdsauthcheckerstub.actions.BearerTokenAction
import uk.gov.hmrc.pdsauthcheckerstub.config.AppConfig
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class ValidateCustomsAuthControllerSpec
    extends AnyWordSpec
      with Matchers
    with TestCommonGenerators
    with ScalaCheckPropertyChecks
    with MockitoSugar
    with BeforeAndAfterEach {

    private val configuration = Configuration(
      "appName" -> "pds-auth-checker-stub",
      "authorisation.token" -> "mockBearerToken"
    )
    private val wiremockServerConfig = new AppConfig(
      configuration
    )
    val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    val bearerTokenAction = new BearerTokenAction(
      new BodyParsers.Default(Helpers.stubControllerComponents().parsers),
      wiremockServerConfig,
      fixedClock
    )
    val mockService: ValidateCustomsAuthService =
      mock[ValidateCustomsAuthService]
    val controllerComponents: ControllerComponents =
      Helpers.stubControllerComponents()
    val controller =
      new ValidateCustomsAuthController(
        controllerComponents,
        mockService,
        bearerTokenAction
      )

  "AuthorisationsController" should {

    "return 200 OK and pdsAuthResponse when no date is provided and user is authorised" in {
      val authRequestWithoutDate =
        Arbitrary.arbitrary[PdsAuthRequest].sample.get.copy(validityDate = None)
      val request = FakeRequest()
        .withBody(authRequestWithoutDate)
        .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer mockBearerToken")
      val expectedResult =
        authorisationResponseGen(authRequestWithoutDate).sample.get
      when(
        mockService.validateCustoms(any(), any())
      ).thenReturn(expectedResult)
      val result: Future[Result] = controller.validateCustomsAuth(request)
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(expectedResult)
    }

    "return 200 OK with PdsAuthResponse for valid JSON request with date populated and user is authorised" in {

        val authRequestWithDate =
          Arbitrary.arbitrary[PdsAuthRequest].sample.get.copy(validityDate = Some(LocalDate.now()))
        val request = FakeRequest()
          .withBody(authRequestWithDate)
          .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer mockBearerToken")
        val expectedResult = authorisationResponseGen(authRequestWithDate).sample.get
        when(
          mockService.validateCustoms(any(), any())
        ).thenReturn(expectedResult)
        val result: Future[Result] = controller.validateCustomsAuth(request)
        status(result) mustBe OK

        contentAsJson(result) mustBe Json.toJson(expectedResult)

    }


    "return 403 UNAUTHORIZED when an invalid token is provided" in {
      forAll { authRequest: PdsAuthRequest =>
        val request = FakeRequest()
          .withBody(authRequest)
          .withHeaders(HeaderNames.AUTHORIZATION -> "Bearer incorrectToken")
        val result: Future[Result] = controller.validateCustomsAuth(request)
        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe Json
          .toJson(
            ErrorDetail(
              fixedClock.instant(),
              "403",
              "Authorisation not found",
              "uri=/pds/cnit/validatecustomsauth/v1"
            )
          )
          .toString
      }
    }
    "return 403 UNAUTHORIZED when no auth token is provided" in {
      forAll { authRequest: PdsAuthRequest =>
        val request = FakeRequest()
          .withBody(authRequest)
        val result: Future[Result] = controller.validateCustomsAuth(request)
        status(result) mustBe FORBIDDEN
        contentAsString(result) mustBe Json
          .toJson(
            ErrorDetail(
              fixedClock.instant(),
              "403",
              "Authorisation not found",
              "uri=/pds/cnit/validatecustomsauth/v1"
            )
          )
          .toString
      }
    }

  }
}
