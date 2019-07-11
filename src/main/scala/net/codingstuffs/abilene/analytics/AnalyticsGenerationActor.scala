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
import org.apache.spark.sql.types.IntegerType

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

  override def receive: Receive = {
    case receipt: DataAggregate =>
      actorDataPoints = actorDataPoints ++ receipt.actorDataPoints
      actorRawDataPoints = actorRawDataPoints ++ receipt.actorRawDataPoints

      aggregatesReceived += 1

      if (aggregatesReceived == config.getInt("data.aggregator.count"))
        self ! Generate

    case Generate =>
      import sparkSession.implicits._
      import org.apache.spark.sql.functions._

      val memberStats = actorDataPoints.distinct.toDF
      val memberPreferenceStats = actorRawDataPoints.distinct.toDF

      val fullAggregate = memberStats.join(
        memberPreferenceStats, Seq("memberName", "groupId"))

      fullAggregate.show()

  }
}
