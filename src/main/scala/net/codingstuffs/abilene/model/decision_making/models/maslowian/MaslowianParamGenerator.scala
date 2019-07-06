package net.codingstuffs.abilene.model.decision_making.models.maslowian

import scala.util.Random

object MaslowianParamGenerator {

}

class MaslowianParamGenerator(random: Random) {

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  def getParams(name: String): Map[String, Double] = Map(
    "physio" -> random.nextDouble(),
    "safety" -> random.nextDouble(),
    "affiliation" -> random.nextDouble(),
    "mate_acquisition" -> random.nextDouble(),
    "mate_retention" -> random.nextDouble(),
    "parenting" -> random.nextDouble()
  )

  def getMaslowianSum(name: String): Double = getParams(name).values.sum
}
