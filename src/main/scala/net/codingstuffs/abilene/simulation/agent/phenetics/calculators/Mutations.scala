package net.codingstuffs.abilene.simulation.agent.phenetics.calculators

import com.typesafe.config.{Config, ConfigFactory}
import net.codingstuffs.abilene.intake.parse.ConfigUtil

import scala.util.Random

object Mutations {
  val random: Random = Random
  random.setSeed(ConfigUtil.MUTATION_GENERATOR_SEED)
  val config: Config = ConfigFactory.load

  def attune(phenome_1: (String, Int), phenome_2: String): String = {
    val attunementHalf = config.getInt("agent.phenome.attunement.half_size")
    0.until(phenome_1._1.length).map(index =>
      if (index < phenome_1._2 - attunementHalf || index > phenome_1._2 + attunementHalf)
        phenome_1._1.charAt(index)
      else phenome_2.charAt(index)).mkString

  }

  def randomCrossover(phenome_1: String, phenome_2: String): (String, String) = {
    val pairedChars = 0.until(phenome_1.length).map(index =>
      if (random.nextBoolean) (phenome_1.charAt(index), phenome_2.charAt(index))
      else (phenome_2.charAt(index), phenome_1.charAt(index)))
    (pairedChars.map(item => item._1).mkString, pairedChars.map(item => item._2).mkString)
  }

  def mutate(phenome: String): (String, Int) = {
    val mutationStrength = random.nextInt(config.getInt("agent.phenome.mutation.strength")) + 1
    val location = random.nextInt(phenome.length)
    (phenome.substring(0, location) +
      (phenome.charAt(location).toInt + random.nextInt(mutationStrength) *
        (if (random.nextBoolean) -1 else 1)).toChar +
      phenome.substring(location + 1), location)
  }
}