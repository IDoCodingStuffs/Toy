package net.codingstuffs.toy.analytics

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.toy.analytics.DataAggregatorActor.{ActorDataPoint, ActorRawDataPoint, DataAggregate}
import net.codingstuffs.toy.iteration.agent.ConductorActor.GroupDataPoint
import net.codingstuffs.toy.phenetics.AgentPheneticsGenerator
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

  var actorDataPoints: Seq[ActorDataPoint] = Seq()
  var actorRawDataPoints: Seq[ActorRawDataPoint] = Seq()
  var groupDataPoints: Seq[GroupDataPoint] = Seq()

  override def receive: Receive = {
    case receipt: DataAggregate =>
      actorDataPoints = actorDataPoints ++ receipt.actorDataPoints
      actorRawDataPoints = actorRawDataPoints ++ receipt.actorRawDataPoints
      groupDataPoints = groupDataPoints ++ receipt.groupDataPoints

      aggregatesReceived += 1

      if (aggregatesReceived == config.getInt("data.aggregator.count"))
        self ! Generate

    case Generate =>
      import org.apache.spark.sql.functions._
      import sparkSession.implicits._

      val memberStats = actorDataPoints.distinct.toDF
      val memberPreferenceStats = actorRawDataPoints.distinct.toDF
      val groupDecisionStats = groupDataPoints.distinct.toDF

      val geneticsStats = AgentPheneticsGenerator.GENE_SET
        .map(item => GeneticsStat(item._1, item._2))
        .toSeq
        .toDF

      val fullAggregate = memberStats.join(
        memberPreferenceStats.join(
          groupDecisionStats,
          "groupId"
        ),
        Seq("memberName", "groupId"))

      fullAggregate.groupBy($"memberExpression").count()
        .join(geneticsStats,
        $"memberExpression" === $"pattern", "left_outer")
        .select("memberExpression", "utility", "count")
        .orderBy(desc("utility"))
        .show()
  }
}
