package net.codingstuffs.abilene.simulation.agent.maslowian

import com.typesafe.config.ConfigFactory

class MaslowianParamGenerator(listAttr: Map[String, Double]) {

  private val config = ConfigFactory.load()

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  /*
  Kenrick DT, Griskevicius V, Neuberg SL, Schaller M. Renovating the Pyramid of Needs:
  Contemporary Extensions Built Upon Ancient Foundations.
  Perspect Psychol Sci. 2010;5(3):292–314. doi:10.1177/1745691610369469
 */
  def getParams(name: String): Map[String, Double] = listAttr

  def getMaslowianSum(name: String): Double = {
    //!TODO: Customizable Maslowians?
    val exponents = config.getDoubleList("maslowian.exponents")

    val physio = math.pow(getParams(name)("physio"), exponents.get(0))
    val safety = math.pow(getParams(name)("safety"), exponents.get(1))
    val affiliation = math.pow(getParams(name)("affiliation"), exponents.get(2))
    val mate_acquisition = math.pow(getParams(name)("mate_acquisition"), exponents.get(3))
    val mate_retention = math.pow(getParams(name)("mate_retention"), exponents.get(4))
    val parenting = math.pow(getParams(name)("parenting"), exponents.get(5))

    physio +
    physio * safety +
    physio * safety * affiliation +
    physio * safety * affiliation * mate_acquisition +
    physio * safety * affiliation * mate_acquisition * mate_retention +
    physio * safety * affiliation * mate_acquisition * mate_retention * parenting
  }
}
