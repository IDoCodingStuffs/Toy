package net.codingstuffs.abilene.simulation.agent.genetics.calculators

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

object Mutations {
  val random: Random = Random
  val config: Config = ConfigFactory.load

  def attune(genome_1   : String, genome_2: String): String =
    0.until(genome_1.length).map(index =>
      if (random.nextBoolean) genome_1.charAt(index) else genome_2.charAt(index)).mkString


  def mutate(genome: String): String = {
    val mutationStrength = config.getInt("agent.genome.mutation.strength")
    genome.map(char =>
      if (random.nextBoolean)
        (char.toInt + random.nextInt(mutationStrength) * (if (random.nextBoolean) -1 else 1)).toChar
      else char)
  }
}