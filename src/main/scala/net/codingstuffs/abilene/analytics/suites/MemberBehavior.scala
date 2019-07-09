package net.codingstuffs.abilene.analytics.suites

import org.apache.spark.sql.DataFrame

class MemberBehavior(df: DataFrame) {
  import df.sparkSession.implicits._
  import org.apache.spark.sql.functions._

  def averagedPreferenceKnowledge: DataFrame = df
    .withColumn("AveragedGroupPref", avg(explode($"groupPreference")))
    .withColumn("AveragedGroupWeights", avg(explode($"groupWeights")))
    .select("selfPreference", "selfWeight", "AveragedGroupPref", "AveragedGroupWeights", "decision")
}
