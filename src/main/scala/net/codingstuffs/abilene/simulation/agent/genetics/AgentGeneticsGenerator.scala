package net.codingstuffs.abilene.simulation.agent.genetics

import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.simulation.environment.AgentWorld
import net.codingstuffs.abilene.simulation.generators.random.Uniform

import scala.util.Random

object AgentGeneticsGenerator {
  //!TODO: Add matching for different models
  private val config = ConfigFactory.load()
  private val random = Random

  private final val GENOME_LENGTH = config.getInt("agent.genome.length")
  private final val GENE_COUNT = config.getInt("agent.genome.gene.count")

  val GENE_SET: Map[String, (String, Double)] = 1.to(GENE_COUNT)
    .map(_ => random.nextString(GENOME_LENGTH) ->
      (random.nextString(AgentWorld.FACTOR_LENGTH), Uniform.GENERATOR.nextDouble)).toMap

  def get: String = GENE_SET.keySet.toVector(random.nextInt(GENE_SET.size))
}