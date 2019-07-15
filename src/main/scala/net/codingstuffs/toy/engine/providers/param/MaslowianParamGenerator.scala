package net.codingstuffs.toy.engine.providers.param

import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.providers.random_generators.FoldedGaussian

import scala.util.Random

object MaslowianParamGenerator {
  val mainGenerator = new Random(ConfigUtil.MAIN_GENERATOR_SEED)

  def INSTANCE = new MaslowianParamGenerator(
    ConfigUtil.MASLOWIAN_MEAN_SD
      .map(value => {
        val auxGenerator = new FoldedGaussian(value._1, value._2)
        auxGenerator.setSeed(mainGenerator.nextLong())
        auxGenerator.nextDouble
      })
  )
}


class MaslowianParamGenerator(listAttr: List[Double]) {

  private val config = ConfigFactory.load()

  implicit var self: String = _
  implicit var memberNames: Set[String] = _

  /*
  Kenrick DT, Griskevicius V, Neuberg SL, Schaller M. Renovating the Pyramid of Needs:
  Contemporary Extensions Built Upon Ancient Foundations.
  Perspect Psychol Sci. 2010;5(3):292–314. doi:10.1177/1745691610369469
 */

  def getParams: List[Double] = listAttr

  def getMaslowianSum: Double = {
    //!TODO: Customizable Maslowians?
    val exponents = config.getDoubleList("maslowian.exponents")
    val defMulti = math.pow(10, config.getDouble("maslowian.multiplier.power_of_ten"))

    val physio = math.pow(listAttr(0), exponents.get(0)) * defMulti
    val safety = math.pow(listAttr(1), exponents.get(1)) * defMulti
    val affiliation = math.pow(listAttr(2), exponents.get(2)) * defMulti
    val mate_acquisition = math.pow(listAttr(3), exponents.get(3)) * defMulti
    val mate_retention = math.pow(listAttr(4), exponents.get(4)) * defMulti
    val parenting = math.pow(listAttr(5), exponents.get(5)) * defMulti

    physio +
    physio * safety +
    physio * safety * affiliation +
    physio * safety * affiliation * mate_acquisition +
    physio * safety * affiliation * mate_acquisition * mate_retention +
    physio * safety * affiliation * mate_acquisition * mate_retention * parenting
  }
}
