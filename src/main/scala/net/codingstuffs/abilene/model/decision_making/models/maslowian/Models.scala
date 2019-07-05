package net.codingstuffs.abilene.model.decision_making.models.maslowian

import net.codingstuffs.abilene.model.decision_making.models.DecisionMakingModel

object Models {

  final case class OriginalMaslowian(params: Map[String, Double]) extends DecisionMakingModel

  /*
  Kenrick DT, Griskevicius V, Neuberg SL, Schaller M. Renovating the Pyramid of Needs:
  Contemporary Extensions Built Upon Ancient Foundations.
  Perspect Psychol Sci. 2010;5(3):292–314. doi:10.1177/1745691610369469
 */
  final case class KNSUpdated(params: Map[String, Double]) extends DecisionMakingModel

}