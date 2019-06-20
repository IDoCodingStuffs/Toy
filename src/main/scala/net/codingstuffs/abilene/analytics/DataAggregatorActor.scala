package net.codingstuffs.abilene.analytics

import akka.actor.{Actor, ActorLogging, Props}
import net.codingstuffs.abilene.analytics.DataAggregatorActor.{ActorDataPoint, CreateDump}
import net.codingstuffs.abilene.model.decision_making.generators.AgentParamGenerator.DecisionParams
import org.apache.spark.sql.SparkSession

object DataAggregatorActor {
  def props: Props = Props[DataAggregatorActor]

  case class ActorDataPoint(groupId: String,
                            decisionParams: DecisionParams,
                            decision: Boolean)

  case class CreateDump()

}

class DataAggregatorActor extends Actor with ActorLogging {
  var caseClasses: Seq[ActorDataPoint] = Seq()

  val sparkSession: SparkSession = SparkSession.builder()
    .config("spark.cores.max", 8)
    .config("spark.executor.cores", 2)
    .master("local").getOrCreate()

  override def receive: Receive = {
    case dataPoint: ActorDataPoint =>
      caseClasses = caseClasses :+ dataPoint
    case CreateDump =>
      import sparkSession.implicits._
      val dump = caseClasses.toDF()
      dump.show(25, false)

      //!TODO: When groupSize is parameterized this needs to be updated
      val groupDecisionCompositionAnalytics =  new GroupDecisionComposition(dump)

      log.info(groupDecisionCompositionAnalytics.getConsensusVariance.toString)
      groupDecisionCompositionAnalytics.getYesVoteCounts.show
  }
}
