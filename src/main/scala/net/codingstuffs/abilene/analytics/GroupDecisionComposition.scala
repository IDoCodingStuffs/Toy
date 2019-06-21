package net.codingstuffs.abilene.analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, sum}
import org.apache.spark.sql.types.IntegerType

class GroupDecisionComposition(df: DataFrame) {
  case class ConsensusVariance(consensus: Long, opposition: Long, conflict: Long)
  case class YesVotesCounts(zero: Long, one: Long, two: Long, three: Long, four: Long)

  val processedDf: DataFrame = df
    .withColumn("decision", col("decision").cast(IntegerType))
    .groupBy("groupId")
    .agg(sum("decision").alias("acceptance"))


  def getYesVoteCounts: DataFrame = processedDf.groupBy("acceptance").count

}
