package net.codingstuffs.abilene.simulation.agent.genetics.calculators

import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams

import scala.util.Random

object IterationBehavior {
  private val random  = Random

  def pickMutatedSelfOrCrossover(genome: String, params: ExpressionParams): String = {
    if (random.nextDouble() <= params.selfParams._3) genome
    else {
      val preferences = params.groupWeights.map(item =>
        params.groupWeights.values.filter(subitem => subitem == item._2).sum ->
        params.groupWeights.filter(subitem => subitem._2 == item._2).keySet
      )

      val normalizedPref = preferences.map(item => (item._1 / preferences.keySet.sum) -> item._2)

      val pickIndex = normalizedPref.keySet.filter(item => item > random.nextDouble()).min.toInt

      val pick =
        params.groupExpressions(normalizedPref(pickIndex)
          .toVector(random.nextInt(normalizedPref(pickIndex).size)
          ))

      Mutations.attune(genome, pick)
    }
  }
}
