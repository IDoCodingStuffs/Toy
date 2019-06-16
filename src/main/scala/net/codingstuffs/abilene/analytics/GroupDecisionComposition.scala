package net.codingstuffs.abilene.analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, sum}
import org.apache.spark.sql.types.IntegerType

object GroupDecisionComposition {
  case class ConsensusVariance(consensus: Long, opposition: Long, conflict: Long)

  def getConsensusVariance(df: DataFrame): ConsensusVariance = {
    val processedDf = df
      .withColumn("decision", col("decision").cast(IntegerType))
      .groupBy("groupId")
      .agg(sum("decision").alias("acceptance"))

    val consensusCount = processedDf.filter(col("acceptance") === 4 or col("acceptance") === 0).count
    val oppositionCount = processedDf.filter(col("acceptance") === 3 or col("acceptance") === 1).count
    val conflictCount = processedDf.filter(col("acceptance") === 2).count

    ConsensusVariance(consensusCount, oppositionCount, conflictCount)
  }
}
