package net.codingstuffs.abilene.model.logic

object DecisionMakingModels {

  sealed abstract class LogicModel

  final case object Selfish extends LogicModel

  final case object SimpleConsensusSeeking extends LogicModel

  final case object WeightedConsensusSeeking extends LogicModel

}
