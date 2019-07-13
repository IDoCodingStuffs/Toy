package net.codingstuffs.toy.engine.providers

import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil
import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator
import net.codingstuffs.toy.engine.phenetics.calculators.Mutations
import net.codingstuffs.toy.engine.providers.random_generators.FoldedGaussian

import scala.util.Random

object AgentParamGenerator {

  final case class ExpressionParams(selfParams: (Int, String, Double),
    groupExpressions                          : Map[Int, String],
    groupWeights                              : Map[Int, Double])

  var groupPhenomes: Map[String, Map[Int, String]] = Map()

}

class AgentParamGenerator(
  groupId                           : String,
  memberIndices                     : Set[Int],
  agentName                         : String,
  randomGenerators                  : (Random, Random),
  randomSeed                        : Long) {

  import AgentParamGenerator._

  val preferenceGenerator: Random = randomGenerators._1
  val weightsGenerator: Random = randomGenerators._2

  private val config = ConfigFactory.load
  private val random = new Random(randomSeed)


  val agentGenes: Map[Int, String] =
    if (groupPhenomes.keySet.contains(groupId)) groupPhenomes(groupId)
    else memberIndices.map(index => index -> AgentPheneticsGenerator.get).toMap

  val initialParams: ExpressionParams = get
  val initialPhenome: String = initialParams.selfParams._2
  val mutatedPhenome: (String, Int) = Mutations.mutate(initialPhenome, new Random(random.nextLong))

  def getSelfParams(name: String): (Int, String, Double) = (agentName.toInt, agentGenes(agentName.toInt),
    weightsGenerator.nextDouble())

  def groupExpressions(implicit groupMembers: Set[Int]): Map[Int, String] =
    groupMembers.filter(member => member != agentName.toInt)
      .map(member => member -> agentGenes(member)).toMap

  def groupWeights(implicit groupMembers: Set[Int],
    //!TODO: REMOVE HARDCODED VAL HERE DAFUQ
    max_deviation                       : Int = 3): Map[Int, Double] =
    groupMembers.filter(member => member != agentName.toInt).map(member => member -> weightsGenerator
      .nextDouble).toMap

  val maslowianParams: Map[String, Double] = ConfigUtil.MASLOWIAN_MEAN_SD.map(
    mapping => mapping._1 -> FoldedGaussian.GENERATOR(mapping._2._1, mapping._2._2).nextDouble
  )

  val adjustedParams: ExpressionParams = {
    val adjustedForSelf = ExpressionParams(
      (initialParams.selfParams._1, mutatedPhenome._1,
        //!TODO: Refactor into its own method in a util
        if (AgentPheneticsGenerator.GENE_SET.contains(mutatedPhenome._1))
          initialParams.selfParams._3 * AgentPheneticsGenerator.GENE_SET(mutatedPhenome._1)
        else config.getDouble("agent.phenome.base_utility")),

      initialParams.groupExpressions,
      initialParams.groupWeights
    )

    //!TODO : Move this to member param generation
    val maslowianGenerator = new MaslowianParamGenerator(maslowianParams)

    ExpressionParams(
      (adjustedForSelf.selfParams._1, adjustedForSelf.selfParams._2, adjustedForSelf
        .selfParams._3),
      adjustedForSelf.groupExpressions,
      adjustedForSelf.groupWeights.map(weight =>
        weight._1 -> (1 / maslowianGenerator.getMaslowianSum) * weight._2))

  }

  def get: ExpressionParams = ExpressionParams(getSelfParams(agentName), groupExpressions(memberIndices),
    groupWeights(memberIndices))
}
