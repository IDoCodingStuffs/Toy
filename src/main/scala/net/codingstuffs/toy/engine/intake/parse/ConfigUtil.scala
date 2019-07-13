package net.codingstuffs.toy.engine.intake.parse

import com.typesafe.config.{Config, ConfigFactory}
import net.codingstuffs.toy.engine.providers.random_generators.{Beta, Discrete, FoldedGaussian, Uniform}

import scala.util.Random

object ConfigUtil {
  val config: Config = ConfigFactory.load()
  System.setProperty("hadoop.home.dir", config.getString("hadoop.home.dir"))

  final val EXTRA_ITERATIONS: Int = config.getInt("group.count")
  final val GROUP_MAX = config.getInt("group.size.max")
  final val GROUP_MIN = config.getInt("group.size.min")
  final val AGGREGATOR_COUNT = config.getInt("data.aggregator.count")

  final val MAIN_GENERATOR_SEED = config.getLong("generator.seed.main")
  final val PREFERENCE_GENERATOR: Random = {
    config.getString("generator.preference") match {
      case "Uniform" => Uniform.GENERATOR
      case discrete: String if discrete.startsWith("Discrete") =>
        Discrete.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(discrete).get.split(",").map
          (_.toDouble).toSeq)
      case beta: String if beta.startsWith("Beta") =>
        Beta.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble,
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(1).toDouble)
      case gaussian: String if gaussian.startsWith("FoldedGaussian") =>
        FoldedGaussian.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(gaussian).get.toDouble)
    }
  }
  final val WEIGHTS_GENERATOR: Random = {
    config.getString("generator.weights") match {
      case "Uniform" => Uniform.GENERATOR
      case discrete: String if discrete.startsWith("Discrete") =>
        Discrete.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(discrete).get.split(",").map
          (_.toDouble).toSeq)
      case beta: String if beta.startsWith("Beta") =>
        Beta.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble,
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble)
      case gaussian: String if gaussian.startsWith("FoldedGaussian") =>
        FoldedGaussian.GENERATOR(
          """(?<=\()(.*?)(?=\))""".r.findFirstIn(gaussian).get.toDouble)
    }
  }

  final val MASLOWIAN_MEAN_SD: Map[String, (Double, Double)] = {
    val means = config.getDoubleList("maslowian.means").toArray
    val sd = config.getDoubleList("maslowian.sd").toArray

    val labels = Seq("physio", "safety", "affiliation", "mate_acquisition", "mate_retention",
      "parenting")

    labels.zipWithIndex.map(label => label._1 ->
      (means(label._2).asInstanceOf[Double], sd(label._2).asInstanceOf[Double])).toMap
  }
}
