package net.codingstuffs.abilene.simulation.agent.phenetics

import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.intake.parse.ConfigUtil
import net.codingstuffs.abilene.simulation.generators.random.Central

import scala.util.Random

object AgentPheneticsGenerator {
  //!TODO: Add matching for different models
  private val config = ConfigFactory.load()

  //!TODO: Move these into a config util
  private final val PHENOME_LENGTH = config.getInt("agent.phenome.length")
  private final val GENE_COUNT = config.getInt("agent.phenome.gene.count")

  private final val GENE_BOOST_MEAN = config.getDouble("agent.phenome.boost.mean")
  private final val GENE_BOOST_SD = config.getDouble("agent.phenome.boost.sd")


  val GENE_SET: Map[String, Double] = 1.to(GENE_COUNT)
    .map(_ => Central.GENERATOR.alphanumeric.take(PHENOME_LENGTH).mkString ->
      (Central.GENERATOR.nextGaussian() * GENE_BOOST_SD + GENE_BOOST_MEAN)).toMap

  def get: String = {
    if (config.getBoolean("agent.phenome.use_standard"))
      return config.getString("agent.phenome.standard")
    GENE_SET.keySet.toVector(Central.GENERATOR.nextInt(GENE_SET.size))
  }
}