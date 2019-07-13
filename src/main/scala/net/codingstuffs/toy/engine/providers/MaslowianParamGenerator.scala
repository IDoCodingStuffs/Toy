package net.codingstuffs.toy.engine.providers

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
  def getParams: Map[String, Double] = listAttr

  def getMaslowianSum: Double = {
    //!TODO: Customizable Maslowians?
    val exponents = config.getDoubleList("maslowian.exponents")
    val defMulti = math.pow(10, config.getDouble("maslowian.multiplier.power_of_ten"))

    val physio = math.pow(getParams("physio"), exponents.get(0)) * defMulti
    val safety = math.pow(getParams("safety"), exponents.get(1)) * defMulti
    val affiliation = math.pow(getParams("affiliation"), exponents.get(2)) * defMulti
    val mate_acquisition = math.pow(getParams("mate_acquisition"), exponents.get(3)) * defMulti
    val mate_retention = math.pow(getParams("mate_retention"), exponents.get(4)) * defMulti
    val parenting = math.pow(getParams("parenting"), exponents.get(5)) * defMulti

    physio +
    physio * safety +
    physio * safety * affiliation +
    physio * safety * affiliation * mate_acquisition +
    physio * safety * affiliation * mate_acquisition * mate_retention +
    physio * safety * affiliation * mate_acquisition * mate_retention * parenting
  }
}
