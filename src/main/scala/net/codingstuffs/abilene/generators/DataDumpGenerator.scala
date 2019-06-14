package net.codingstuffs.abilene.generators

import akka.actor.{Actor, Props}
import net.codingstuffs.abilene.generators.DataDumpGenerator.{ActorDataPoint, CreateDump}
import net.codingstuffs.abilene.model.Member.MemberParams
import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

object DataDumpGenerator {
  def props: Props = Props[DataDumpGenerator]

  case class ActorDataPoint(groupId: Double, memberName: String, memberWeights: Map[String, Double],
                            assumedOrKnownPreferences: Map[String, Double], decision: Boolean)

  case class CreateDump()

}

class DataDumpGenerator extends Actor {
  var caseClasses: Seq[ActorDataPoint] = Seq()

  val sparkSession: SparkSession = SparkSession.builder().master("local").getOrCreate()

  import sparkSession.implicits._

  override def receive: Receive = {
    case dataPoint: ActorDataPoint =>
      caseClasses = caseClasses :+ dataPoint
    case CreateDump =>
      val dump = caseClasses.toDF()
      dump.show(5)
  }
}
