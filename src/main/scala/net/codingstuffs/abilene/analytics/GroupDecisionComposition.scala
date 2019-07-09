package net.codingstuffs.abilene.analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.IntegerType

class GroupDecisionComposition(df: DataFrame) {
  case class ConsensusVariance(consensus: Long, opposition: Long, conflict: Long)
  case class YesVotesCounts(zero: Long, one: Long, two: Long, three: Long, four: Long)

  import org.apache.spark.sql.functions._

  val processedDf: DataFrame = df
    .withColumn("decision", col("decision").cast(IntegerType))
    .groupBy( "memberName")
    .agg(mean("decision").alias("acceptance"))
    .orderBy("memberName")

  def getYesVoteCounts: DataFrame = processedDf.groupBy("acceptance").count

  import df.sparkSession.implicits._
  import org.apache.spark.sql.functions._

  def memberDecisionBreakdown: DataFrame = df.groupBy( "decision").count()

  //Two-way vote
  def preferencePerGroup: DataFrame = df
    .withColumn("decision", col("decision").cast(IntegerType))
    .groupBy("groupId")
    .agg(mean("decision"))

  def preferencePerMember: DataFrame = df
    .select("groupId", "memberName", "selfPreference", "decision")
    .withColumn("inclination", $"selfPreference" > 0.5)
    .withColumn("paradox", $"decision" =!= $"inclination")

  def decisionParadoxes: DataFrame = preferencePerMember
    .groupBy("memberName")
    .agg(mean($"paradox".cast(IntegerType)).alias("counts"))
    .orderBy("memberName")
}
