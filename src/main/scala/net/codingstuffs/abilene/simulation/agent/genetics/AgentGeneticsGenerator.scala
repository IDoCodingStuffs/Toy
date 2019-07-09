package net.codingstuffs.abilene.simulation.agent.genetics

import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.simulation.environment.AgentWorld

import scala.util.Random

object AgentGeneticsGenerator {
  //!TODO: Add matching for different models
  private val config = ConfigFactory.load()
  private val random = Random

  //!TODO: Move these into a config util
  private final val GENOME_LENGTH = config.getInt("agent.genome.length")
  private final val GENE_COUNT = config.getInt("agent.genome.gene.count")

  private final val GENE_BOOST_MEAN = config.getDouble("agent.genome.boost.mean")
  private final val GENE_BOOST_SD = config.getDouble("agent.genome.boost.sd")


  val GENE_SET: Map[String, (String, Double)] = 1.to(GENE_COUNT)
    .map(_ => random.alphanumeric.take(GENOME_LENGTH).mkString("") ->
      (random.alphanumeric.take(AgentWorld.FACTOR_LENGTH).mkString(""),
        random.nextGaussian() * GENE_BOOST_SD + GENE_BOOST_MEAN)).toMap

  def get: String = GENE_SET.keySet.toVector(random.nextInt(GENE_SET.size))
}