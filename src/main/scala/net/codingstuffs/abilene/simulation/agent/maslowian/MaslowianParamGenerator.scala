package net.codingstuffs.abilene.simulation.agent.maslowian

import net.codingstuffs.abilene.simulation.generators.random.Uniform

import scala.util.Random

object MaslowianParamGenerator {
  val random: Random = new Uniform()
  val instance: MaslowianParamGenerator = new MaslowianParamGenerator(List(
    random.nextDouble(),
    random.nextDouble(),
    random.nextDouble(),
    random.nextDouble(),
    random.nextDouble(),
    random.nextDouble(),
  )
  )
}

class MaslowianParamGenerator(listAttr: List[Double]) {

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  /*
  Kenrick DT, Griskevicius V, Neuberg SL, Schaller M. Renovating the Pyramid of Needs:
  Contemporary Extensions Built Upon Ancient Foundations.
  Perspect Psychol Sci. 2010;5(3):292–314. doi:10.1177/1745691610369469
 */
  def getParams(name: String): Map[String, Double] = Map(
    "physio" -> listAttr.head,
    "safety" -> listAttr(1),
    "affiliation" -> listAttr(2),
    "mate_acquisition" -> listAttr(3),
    "mate_retention" -> listAttr(4),
    "parenting" -> listAttr(5)
  )

  def getMaslowianSum(name: String): Double = getParams(name).values.sum
}
