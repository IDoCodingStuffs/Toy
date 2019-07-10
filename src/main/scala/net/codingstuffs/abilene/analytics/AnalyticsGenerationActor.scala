package net.codingstuffs.abilene.analytics

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.analytics.AnalyticsGenerationActor.Generate
import net.codingstuffs.abilene.analytics.DataAggregatorActor.{
  ActorDataPoint, ActorRawDataPoint,
  DataAggregate
}
import net.codingstuffs.abilene.simulation.Group.GroupDataPoint
import org.apache.spark.sql.SparkSession

object AnalyticsGenerationActor {
  def props: Props = Props[AnalyticsGenerationActor]

  case object Generate

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
      import sparkSession.implicits._
      import org.apache.spark.sql.functions._

      val memberStats = actorDataPoints.distinct.toDF
      val memberPreferenceStats = actorRawDataPoints.distinct.toDF
      val groupDecisionStats = groupDataPoints.distinct.toDF

      val fullAggregate = memberStats.join(
        memberPreferenceStats.join(
          groupDecisionStats,
          "groupId"
        ),
        Seq("memberName", "groupId"))

      fullAggregate.show(50)

      println("group decisions with no splits")
      println(groupDecisionStats
        .filter($"acceptance" =!= 0.5)
        .count)

      println("group decisions with conflict")
      println(fullAggregate
        .filter($"acceptance" =!= 0.5)
        .filter($"memberDecision" =!= $"groupDecision")
        .select("groupId").distinct.count)

      println("distribution of conflict")
      fullAggregate
        .filter($"acceptance" =!= 0.5)
        .withColumn("conflict", $"memberDecision" =!= $"groupDecision")
        .groupBy("conflict")
        .count()
        .show()


      println("group decisions with split")
      println(groupDecisionStats
        .filter($"acceptance" === 0.5)
        .count)

    //    groupDecisionCompositionAnalytics.decisionParadoxes.write.csv(s"
    //    ./data/decision_composition/$jobRunAtDateTime/decisionParadoxStats")
    //    groupDecisionStats.coalesce(1).write.json(s"
    //    ./data/decision_composition/$jobRunAtDateTime/yes_vote_counts")
    //    memberStats.write.json(s"./data/member_behavior/$jobRunAtDateTime/full")

  }
}
