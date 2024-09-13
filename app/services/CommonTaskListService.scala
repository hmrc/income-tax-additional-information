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
    service.getInsurancePolicies(nino, taxYear).map {
      case Left(_) => None
      case Right(policies) => Some(policies)
    }.map {
      case Some(policies) =>
        val tasks: Seq[TaskListSectionItem] = getTasks(policies, taxYear)
        TaskListSection(SectionTitle.InsuranceGainsTitle, Some(tasks))
      case None => TaskListSection(SectionTitle.InsuranceGainsTitle, None)
    }
  }

  private def getTasks(insurancePolicies: InsurancePoliciesModel, taxYear: Int): Seq[TaskListSectionItem] = {

    def gainsSummaryUrl(param: String): String =
      s"${appConfig.addInfoFEBaseUrl}/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary?$param"

    val lifeInsuranceUrl: String = gainsSummaryUrl("policyType=Life+Insurance")
    val lifeAnnuityUrl: String = gainsSummaryUrl("policyType=Life+Annuity")
    val capitalRedemptionUrl: String = gainsSummaryUrl("policyType=Capital+Redemption")
    val voidedIsaBaseUrl: String = gainsSummaryUrl("policyType=Voided+ISA")

    Seq(
      createTaskListItem(insurancePolicies.lifeInsurance, TaskTitle.LifeInsurance, lifeInsuranceUrl),
      createTaskListItem(insurancePolicies.lifeAnnuity, TaskTitle.LifeAnnuity, lifeAnnuityUrl),
      createTaskListItem(insurancePolicies.capitalRedemption, TaskTitle.CapitalRedemption, capitalRedemptionUrl),
      createTaskListItem(insurancePolicies.voidedIsa, TaskTitle.VoidedISA, voidedIsaBaseUrl)
    ).flatten
  }

  private def createTaskListItem(optItems: Option[Seq[_]], taskTitle: TaskTitle, url: String): Option[TaskListSectionItem] = {
    optItems.map(_ => TaskListSectionItem(taskTitle, TaskStatus.Completed, Some(url)))
  }
}
