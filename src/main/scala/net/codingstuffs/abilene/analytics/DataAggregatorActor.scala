package net.codingstuffs.abilene.analytics

import akka.actor.{Actor, ActorLogging, Props}
import net.codingstuffs.abilene.analytics.DataAggregatorActor.{ActorDataPoint, CreateDump}
import org.apache.spark.sql.SparkSession

object DataAggregatorActor {
  def props: Props = Props[DataAggregatorActor]

  case class ActorDataPoint(groupId: Double, memberName: String, decisionThreshold: Double, memberWeights: Map[String, Double],
                            assumedOrKnownPreferences: Map[String, Double], decision: Boolean)

  case class CreateDump()

}

class DataAggregatorActor extends Actor with ActorLogging {
  var caseClasses: Seq[ActorDataPoint] = Seq()

  val sparkSession: SparkSession = SparkSession.builder().master("local").getOrCreate()

  override def receive: Receive = {
    case dataPoint: ActorDataPoint =>
      caseClasses = caseClasses :+ dataPoint
    case CreateDump =>
      import sparkSession.implicits._
      val dump = caseClasses.toDF()
      dump.show(25, false)

      //!TODO: When groupSize is parameterized this needs to be updated
      log.info(GroupDecisionComposition.getConsensusVariance(dump).toString)
  }
}
