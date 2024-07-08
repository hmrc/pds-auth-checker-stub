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

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pdsauthcheckerstub.models.{ErrorDetail, PdsAuthRequest}
import uk.gov.hmrc.pdsauthcheckerstub.services.ValidateCustomsAuthService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.pdsauthcheckerstub.config.AppConfig
import sttp.model.HeaderNames

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
@Singleton()
class ValidateCustomsAuthController @Inject() (
    cc: ControllerComponents,
    validateCustomsAuthService: ValidateCustomsAuthService,
    clock: Clock,
    appConfig: AppConfig
) extends BackendController(cc) {
  private val bearerTokenPattern = "^Bearer (\\S+)$".r
  private val authTokenError: ErrorDetail =
    ErrorDetail(
      clock.instant(),
      "403",
      "Authorisation not found",
      "uri=/pds/cnit/validatecustomsauth/v1"
    )

  private def validateBearerToken(value: String): Option[ErrorDetail] =
    value match {
      case bearerTokenPattern(v) =>
        if (appConfig.authToken != v) {
          Some(authTokenError)
        } else None
      case _ =>
        Some(authTokenError)
    }

  def validateCustomsAuth: Action[PdsAuthRequest] =
    Action.async(parse.json[PdsAuthRequest]) { implicit request =>
      request.headers.get(HeaderNames.Authorization) match {
        case Some(value) =>
          validateBearerToken(value) match {
            case Some(error) =>
              Future.successful(
                Forbidden(
                  Json.toJson(
                    error
                  )
                )
              )
            case _ =>
              val pdsAuthResponse =
                validateCustomsAuthService.validateCustoms(
                  request.body.eoris,
                  request.body.authType,
                  request.body.validityDate
                )
              Future.successful(Ok(Json.toJson(pdsAuthResponse)))
          }
        case None =>
          Future.successful(
            Forbidden(
              Json.toJson(
                authTokenError
              )
            )
          )
      }
    }
}
