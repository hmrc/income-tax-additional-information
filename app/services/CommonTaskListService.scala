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

package services

import config.AppConfig
import models.InsurancePoliciesModel
import models.tasklist.{SectionTitle, TaskListSection, TaskListSectionItem, TaskStatus, TaskTitle}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      service: GetInsurancePoliciesService) {

  def get(taxYear: Int, nino: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {

    val gains: Future[InsurancePoliciesModel] = service.getInsurancePolicies(nino, taxYear).map {
      case Left(_) => InsurancePoliciesModel("")
      case Right(value) => value
    }

    gains.map { g =>

      val tasks: Option[Seq[TaskListSectionItem]] = {

        val optionalTasks: Seq[TaskListSectionItem] = getTasks(g, taxYear)

        if (optionalTasks.nonEmpty) {
          Some(optionalTasks)
        } else {
          None
        }
      }

      TaskListSection(SectionTitle.InsuranceGainsTitle, tasks)
    }
  }

  private def getTasks(g: InsurancePoliciesModel, taxYear: Int): Seq[TaskListSectionItem] = {

    // TODO: these will be links to the new individual CYA pages when they are made
    val lifeInsuranceUrl: String = s"${appConfig.addInfoFEBaseUrl}/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"
    val lifeAnnuityUrl: String = s"${appConfig.addInfoFEBaseUrl}/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"
    val capitalRedemptionUrl: String = s"${appConfig.addInfoFEBaseUrl}/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"
    val voidedUrl: String = s"${appConfig.addInfoFEBaseUrl}/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"

    val lifeInsurance: Option[TaskListSectionItem] = if (g.lifeInsurance.isDefined) {
      Some(TaskListSectionItem(TaskTitle.LifeInsurance, TaskStatus.Completed, Some(lifeInsuranceUrl)))
    } else {
      None
    }

    val lifeAnnuity: Option[TaskListSectionItem] = if (g.lifeAnnuity.isDefined) {
      Some(TaskListSectionItem(TaskTitle.LifeAnnuity, TaskStatus.Completed, Some(lifeAnnuityUrl)))
    } else {
      None
    }

    val capitalRedemption: Option[TaskListSectionItem] = if (g.capitalRedemption.isDefined) {
      Some(TaskListSectionItem(TaskTitle.CapitalRedemption, TaskStatus.Completed, Some(capitalRedemptionUrl)))
    } else {
      None
    }

    val voided: Option[TaskListSectionItem] = if (g.voidedIsa.isDefined) {
      Some(TaskListSectionItem(TaskTitle.VoidedISA, TaskStatus.Completed, Some(voidedUrl)))
    } else {
      None
    }

    Seq[Option[TaskListSectionItem]](lifeInsurance, lifeAnnuity, capitalRedemption, voided).flatten
  }
}
