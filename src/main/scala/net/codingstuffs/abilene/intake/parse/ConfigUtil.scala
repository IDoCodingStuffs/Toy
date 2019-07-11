package net.codingstuffs.abilene.intake.parse

import com.typesafe.config.{Config, ConfigFactory}
import net.codingstuffs.abilene.simulation.generators.random.{Beta, Discrete, FoldedGaussian,
  Uniform}

import scala.util.Random

object ConfigUtil {
  val config: Config = ConfigFactory.load()


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
    val means = config.getString("maslowian.means").split(",").map(i => i.toDouble).toSeq
    val sd = config.getString("maslowian.sd").split(",").map(i => i.toDouble).toSeq

    val labels = Seq("physio", "safety", "affiliation", "mate_acquisition", "mate_retention",
      "parenting")

    labels.zipWithIndex.map(label => label._1 -> (means(label._2), sd(label._2))).toMap
  }
}
