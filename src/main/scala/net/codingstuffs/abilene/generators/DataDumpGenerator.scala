package net.codingstuffs.abilene.generators

import akka.actor.{Actor, ActorLogging, Props}
import net.codingstuffs.abilene.analytics.GroupDecisionComposition
import net.codingstuffs.abilene.generators.DataDumpGenerator.{ActorDataPoint, CreateDump}
import org.apache.spark.sql.SparkSession

object DataDumpGenerator {
  def props: Props = Props[DataDumpGenerator]

  case class ActorDataPoint(groupId: Double, memberName: String, memberWeights: Map[String, Double],
                            assumedOrKnownPreferences: Map[String, Double], decision: Boolean)

  case class CreateDump()

}

class DataDumpGenerator extends Actor with ActorLogging {
  var caseClasses: Seq[ActorDataPoint] = Seq()

  val sparkSession: SparkSession = SparkSession.builder().master("local").getOrCreate()

  import sparkSession.implicits._

  override def receive: Receive = {
    case dataPoint: ActorDataPoint =>
      caseClasses = caseClasses :+ dataPoint
    case CreateDump =>
      val dump = caseClasses.toDF()
      dump.show(5)
      log.info(s"No of groups simulated: ${dump.count}")
      log.info(s"No of agents simulated: ${dump.groupBy("groupId").count}")

      //!TODO: When groupSize is parameterized this needs to be updated
      log.info(GroupDecisionComposition.getConsensusVariance(dump).toString)
  }
}
