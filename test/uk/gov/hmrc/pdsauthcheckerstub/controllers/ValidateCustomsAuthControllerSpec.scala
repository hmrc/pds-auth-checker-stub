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

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.pdsauthcheckerstub.base.TestCommonGenerators
import uk.gov.hmrc.pdsauthcheckerstub.models.{ErrorDetail, PdsAuthRequest}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core.{
  AuthConnector,
  AuthorisationException,
  NoActiveSession
}
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.pdsauthcheckerstub.services.ValidateCustomsAuthService
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateCustomsAuthControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with TestCommonGenerators
    with ScalaCheckPropertyChecks
    with MockitoSugar {

  trait Setup {
    val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val service = new ValidateCustomsAuthService
    val controllerComponents: ControllerComponents =
      Helpers.stubControllerComponents()
    val controller =
      new ValidateCustomsAuthController(
        controllerComponents,
        service,
        fixedClock,
        mockAuthConnector
      )
  }

  "AuthorisationsController" should {

    "return 200 OK with empty body for valid JSON request with date populated and user is authorised" in new Setup {
      when(
        mockAuthConnector
          .authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any())
      )
        .thenReturn(Future.successful(()))

      forAll { authRequest: PdsAuthRequest =>
        val authRequestWithDate =
          authRequest.copy(validityDate = Some(LocalDate.now()))
        val request = FakeRequest().withBody(authRequestWithDate)
        val result: Future[Result] = controller.validateCustomsAuth(request)
        status(result) mustBe OK
      }
    }

    "return 200 OK when no date is provided and user is authorised with empty body for valid JSON request" in new Setup {
      when(
        mockAuthConnector
          .authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any())
      )
        .thenReturn(Future.successful(()))

      forAll { authRequest: PdsAuthRequest =>
        val authRequestWithoutDate = authRequest.copy(validityDate = None)
        val request = FakeRequest().withBody(authRequestWithoutDate)
        val result: Future[Result] = controller.validateCustomsAuth(request)

        status(result) mustBe OK
      }
    }

    "return 401 UNAUTHORIZED when there is no active session" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new NoActiveSession("No active session") {}))
      forAll { authRequest: PdsAuthRequest =>
        val request = FakeRequest().withBody(authRequest)
        val result: Future[Result] = controller.validateCustomsAuth(request)
        status(result) mustBe UNAUTHORIZED
        contentAsString(result) mustBe Json
          .toJson(
            ErrorDetail(
              fixedClock.instant(),
              "401",
              "You are not allowed to access this resource",
              "uri=/pds/cnit/validatecustomsauth/v1"
            )
          )
          .toString
      }
    }

    "return 403 FORBIDDEN when user is not authorised" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new AuthorisationException("Forbidden") {}))
      forAll { authRequest: PdsAuthRequest =>
        val request = FakeRequest().withBody(authRequest)
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
