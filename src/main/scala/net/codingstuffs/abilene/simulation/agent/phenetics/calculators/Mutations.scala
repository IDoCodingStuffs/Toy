package net.codingstuffs.abilene.simulation.agent.phenetics.calculators

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

object Mutations {
  val random: Random = Random
  val config: Config = ConfigFactory.load

  def attune(phenome_1   : String, phenome_2: String): String =
    0.until(phenome_1.length).map(index =>
      if (random.nextBoolean) phenome_1.charAt(index) else phenome_2.charAt(index)).mkString


  def mutate(phenome: String): String = {
    val mutationStrength = config.getInt("agent.phenome.mutation.strength")
    val location = random.nextInt(phenome.length)
    phenome.substring(0, location) +
        (phenome.charAt(location).toInt + random.nextInt(mutationStrength) *
          (if (random.nextBoolean) -1 else 1)).toChar +
    phenome.substring(location + 1)
  }
}