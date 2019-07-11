package net.codingstuffs.abilene.simulation.agent.phenetics.calculators

import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams

import scala.util.Random

object IterationBehavior {
  private val random  = Random

  def pickMutatedSelfOrCrossover(phenome: String, params: ExpressionParams): String = {
    if (random.nextDouble() <= params.selfParams._3) phenome
    else {
      val preferences = params.groupWeights.map(item =>
        params.groupWeights.values.filter(subitem => subitem == item._2).sum ->
          params.groupWeights.filter(subitem => subitem._2 == item._2).keySet
      )

      val normalizedPref = preferences.map(item => (item._1 / preferences.keySet.sum) -> item._2)
      val pickPossibilities = normalizedPref.keySet.toList.sorted
      //!TODO: Refactor this ugliness
      val pickIndex = probabilisticPick(pickPossibilities)

      val pick = (params.groupExpressions + (params.selfParams._1 -> params.selfParams._2))(pickIndex)

      Mutations.attune(phenome, pick)
    }
  }

  def probabilisticPick(myList: List[Double]): Int = {
    val roll = random.nextDouble()
    var sum = 0.0

    myList.indices.foreach( index =>
      {
        sum += myList(index)
        if (roll < sum) {
          return index + 1
        }
      }
    )
    0
  }
}
