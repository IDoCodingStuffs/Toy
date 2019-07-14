package net.codingstuffs.toy.engine.analytics

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.agent.AgentConductor.GroupDataPoint
import net.codingstuffs.toy.engine.iteration.DataAggregatorActor.DataAggregate
import net.codingstuffs.toy.engine.phenetics.AgentPheneticsGenerator
import org.apache.spark.sql.SparkSession

object AnalyticsGenerationActor {
  def props: Props = Props[AnalyticsGenerationActor]

  case object Generate

  case class GeneticsStat(pattern: String, utility: Double)

  var aggregatesReceived = 0
}

class AnalyticsGenerationActor extends Actor with ActorLogging {
  private val config = ConfigFactory.load()

  private val sparkSession: SparkSession = SparkSession.builder()
    .config("spark.cores.max", 8)
    .config("driver-memory", "16g")
    .config("spark.executor.cores", 2)
    .master("local[*]").getOrCreate()

  import AnalyticsGenerationActor._

  var actorDataPoints: Seq[AgentParams] = Seq()
  var groupDataPoints: Seq[GroupDataPoint] = Seq()

  override def receive: Receive = {
    case receipt: DataAggregate =>
      actorDataPoints = actorDataPoints ++ receipt.actorDataPoints
      groupDataPoints = groupDataPoints ++ receipt.groupDataPoints

      aggregatesReceived += 1

    case Generate =>
      import org.apache.spark.sql.functions._
      import sparkSession.implicits._

      val memberStats = actorDataPoints.distinct.toDF
      val groupDecisionStats = groupDataPoints.distinct.toDF

      val geneticsStats = AgentPheneticsGenerator.GENE_SET
        .map(item => GeneticsStat(item._1, item._2))
        .toSeq
        .toDF

      actorDataPoints = Seq()
      groupDataPoints = Seq()

      memberStats.groupBy($"phenome").count()
        .join(geneticsStats,
          $"phenome" === $"pattern", "left_outer")
        .select("phenome", "utility", "count")
        .orderBy(desc("count"))
        .show()
  }
}
