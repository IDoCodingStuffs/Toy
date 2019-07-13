package net.codingstuffs.abilene.simulation.agent.phenetics.calculators

import net.codingstuffs.abilene.intake.parse.ConfigUtil
import net.codingstuffs.abilene.simulation.agent.AgentParamGenerator.ExpressionParams
import net.codingstuffs.abilene.simulation.generators.random.Central

object IterationBehavior {
  def pickMutatedSelfOrAttune(mutatedPhenome: (String, Int), initialPhenome: String, params: ExpressionParams): String = {
    if (Central.GENERATOR.nextDouble() <= params.selfParams._3 / params.groupWeights.values.sum) mutatedPhenome._1
    else {
      val preferences = params.groupWeights.map(item =>
        params.groupWeights.values.filter(subitem => subitem == item._2).sum ->
          params.groupWeights.filter(subitem => subitem._2 == item._2).keySet
      )

      val normalizedPref = preferences.map(item => (item._1 / preferences.keySet.sum) -> item._2)
      val pickPossibilities = normalizedPref(probabilisticPick(normalizedPref.keySet.toList.sorted))

      val pick = params.groupExpressions(
        pickPossibilities.toVector(Central.GENERATOR.nextInt(pickPossibilities.size)))

      //!TODO: Attunement happens at the mutated loc
      Mutations.attune(mutatedPhenome, pick)
    }
  }

  //!TODO: Refactor?
  def probabilisticPick(myList: List[Double]): Double = {
    val roll = Central.GENERATOR.nextDouble()
    var sum = 0.0

    myList.indices.foreach( index =>
      {
        sum += myList(index)
        if (roll < sum) {
          return myList(index)
        }
      }
    )
    0
  }
}
