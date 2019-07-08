package net.codingstuffs.abilene.analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, sum}
import org.apache.spark.sql.types.IntegerType

class GroupDecisionComposition(df: DataFrame) {
  case class ConsensusVariance(consensus: Long, opposition: Long, conflict: Long)
  case class YesVotesCounts(zero: Long, one: Long, two: Long, three: Long, four: Long)

  import org.apache.spark.sql.functions._

  val processedDf: DataFrame = df
    .withColumn("decision", col("decision").cast(IntegerType))
    .groupBy( "memberName")
    .agg(mean("decision").alias("acceptance"))


  def getYesVoteCounts: DataFrame = processedDf.groupBy("acceptance").count

  import df.sparkSession.implicits._
  import org.apache.spark.sql.functions._

  def preferencePerMember: DataFrame = df
    .select("groupId", "memberName", "selfPreference", "decision")
    .withColumn("inclination", $"selfPreference" > 0.5)
    .withColumn("paradox", $"decision" =!= $"inclination")

  def decisionParadoxes: DataFrame = preferencePerMember
    .groupBy("memberName")
    .agg(sum($"paradox".cast(IntegerType)).alias("counts"))
}
