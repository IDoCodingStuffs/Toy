package net.codingstuffs.abilene.model

import akka.actor.{ActorRef, ActorSystem}
import net.codingstuffs.abilene.analytics.DataAggregatorActor
import net.codingstuffs.abilene.analytics.DataAggregatorActor.CreateDump

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.model.decision_making.generators.random.{Beta, Discrete, FoldedGaussian, Uniform}
import net.codingstuffs.abilene.model.decision_making.models.{DecisionMakingModel, MaslowianAgent, StochasticAgent}
import net.codingstuffs.abilene.model.decision_making.models.simplified.ArithmeticRoundup.{EgalitarianRoundup, SelfishRoundup, WeightedRoundup}

import scala.concurrent.duration.FiniteDuration

object Abilene extends App {

  import Member._

  val config = ConfigFactory.load()

  val extraIterations: Int = config.getInt("numberGroupsSimulated")
  System.setProperty("hadoop.home.dir", "C:\\hadoop-2.8.0")
  //!TODO: Refactor this plz future me thx

  val studyModel = MaslowianAgent
  val decisionModels: Seq[DecisionMakingModel] = {
    config.getString("decisionModels").split(";")
      .map({
        case "SelfishRoundup" => SelfishRoundup
        case "EgalitarianRoundup" => EgalitarianRoundup
        case "WeightedRoundup" => SelfishRoundup
        case sasScale: String if sasScale.startsWith("WeightedRoundup(") =>
          WeightedRoundup(
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(sasScale).get.split(",")(0).toDouble,
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(sasScale).get.split(",")(1).toDouble
          )
      })
  }
  val preferenceGenerators: Seq[Random] = {
    config.getString("preferenceGenerators").split(";")
      .map({
        case "Uniform" => Uniform.GENERATOR
        case discrete: String if discrete.startsWith("Discrete") =>
          Discrete.GENERATOR("""(?<=\()(.*?)(?=\))""".r.findFirstIn(discrete).get.split(",").map(_.toDouble).toSeq)
        case beta: String if beta.startsWith("Beta") =>
          Beta.GENERATOR(
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble,
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(1).toDouble)
        case gaussian: String if gaussian.startsWith("FoldedGaussian") =>
          FoldedGaussian.GENERATOR(
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(gaussian).get.toDouble)
      }).toSeq
  }
  val weightsGenerators: Seq[Random] = {
    config.getString("preferenceGenerators").split(";")
      .map({
        case "Uniform" => Uniform.GENERATOR
        case discrete: String if discrete.startsWith("Discrete") =>
          Discrete.GENERATOR("""(?<=\()(.*?)(?=\))""".r.findFirstIn(discrete).get.split(",").map(_.toDouble).toSeq)
        case beta: String if beta.startsWith("Beta") =>
          Beta.GENERATOR(
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble,
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(beta).get.split(",")(0).toDouble)
        case gaussian: String if gaussian.startsWith("FoldedGaussian") =>
          FoldedGaussian.GENERATOR(
            """(?<=\()(.*?)(?=\))""".r.findFirstIn(gaussian).get.toDouble)
      }).toSeq
  }

  val system: ActorSystem = ActorSystem("Abilene0")
  val dataDumpGenerator = system.actorOf(DataAggregatorActor.props, "dataDumper")
  var group, father, mother, wife, husband: ActorRef = _
  val groupMembers = Set("father", "mother", "wife", "husband")

  val random = new Random

  try {
    1.to(extraIterations).foreach(_ => {
      var groupId = math.abs(random.nextLong)
      implicit val timeout: Timeout = Timeout(FiniteDuration.apply(5, "seconds"))

      group = system.actorOf(Group.props(groupMembers, dataDumpGenerator), s"$groupId---group")

      father = system.actorOf(
        Member.props(group, studyModel, decisionModels.head, (preferenceGenerators.head, weightsGenerators.head)),
        s"$groupId@@@father")
      mother = system.actorOf(
        Member.props(group, studyModel, decisionModels(1), (preferenceGenerators(1), weightsGenerators(1))),
        s"$groupId@@@mother")
      wife = system.actorOf(
        Member.props(group, studyModel, decisionModels(2), (preferenceGenerators(2), weightsGenerators(2))),
        s"$groupId@@@wife")
      husband = system.actorOf(
        Member.props(group, studyModel, decisionModels(3), (preferenceGenerators(3), weightsGenerators(3))),
        s"$groupId@@@husband")

      father ? Declare
    })
  }
  finally {
    Thread.sleep(120000)
    dataDumpGenerator ! CreateDump
  }
}