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

import enumeratum._
import play.api.libs.json.{Json, OFormat}

sealed trait AuthType extends EnumEntry

object AuthType extends Enum[AuthType] with PlayJsonEnum[AuthType] {
  case object EIR extends AuthType
  case object EIDR extends AuthType
  case object UKD extends AuthType
  case object AEO extends AuthType
  case object CGU extends AuthType
  case object CW extends AuthType
  case object EUS extends AuthType
  case object EPSS extends AuthType
  case object IPO extends AuthType
  case object OPO extends AuthType
  case object SDE extends AuthType
  case object CSE extends AuthType
  case object SIVA extends AuthType
  case object TEA extends AuthType
  case object NIT1 extends AuthType
  case object MOU extends AuthType
  case object UKTS extends AuthType
  case object CNOR extends AuthType
  case object CNEE extends AuthType
  case object CTIR extends AuthType
  case object TS extends AuthType
  case object UKC extends AuthType
  case object UKIM extends AuthType

  override val values = findValues

}