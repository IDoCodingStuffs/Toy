package net.codingstuffs.abilene.analytics

import org.apache.spark.sql.DataFrame

class MemberBehavior(df: DataFrame) {
  import org.apache.spark.sql.functions._
  import df.sparkSession.implicits._

  def averagedPreferenceKnowledge: DataFrame = df
    .withColumn("AveragedGroupPref", avg(explode($"groupPreference")))
    .withColumn("AveragedGroupWeights", avg(explode($"groupWeights")))
    .select("selfPreference", "selfWeight", "AveragedGroupPref", "AveragedGroupWeights", "decision")
}
