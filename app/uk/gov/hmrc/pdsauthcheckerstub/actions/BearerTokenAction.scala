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

package uk.gov.hmrc.pdsauthcheckerstub.actions

import play.api.mvc._
import play.api.libs.json.Json
import play.api.mvc.Results.Forbidden
import uk.gov.hmrc.pdsauthcheckerstub.config.AppConfig
import uk.gov.hmrc.pdsauthcheckerstub.models.ErrorDetail
import sttp.model.HeaderNames

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BearerTokenAction @Inject() (
    val parser: BodyParsers.Default,
    appConfig: AppConfig,
    clock: Clock
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent] {
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

  override def invokeBlock[A](
      request: Request[A],
      block: Request[A] => Future[Result]
  ): Future[Result] = {

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
            block(request)
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
