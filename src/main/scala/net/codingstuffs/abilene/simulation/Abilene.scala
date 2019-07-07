package net.codingstuffs.abilene.simulation

import akka.actor.{ActorRef, ActorSystem}
import net.codingstuffs.abilene.analytics.DataAggregatorActor
import net.codingstuffs.abilene.analytics.DataAggregatorActor.CreateDump

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.codingstuffs.abilene.simulation.decision_making.generators.random.{Beta, Discrete, FoldedGaussian, Uniform}
import net.codingstuffs.abilene.simulation.decision_making.models.{DecisionMakingModel, MaslowianAgent, SimpleAgent}
import net.codingstuffs.abilene.simulation.decision_making.models.simplified.ArithmeticRoundup.{EgalitarianRoundup, SelfishRoundup, WeightedRoundup}
import net.codingstuffs.abilene.intake.parse.ConfigUtil._

import scala.concurrent.duration.FiniteDuration

object Abilene extends App {

  import Member._

  val config = ConfigFactory.load()
  val extraIterations: Int = config.getInt("numberGroupsSimulated")
  System.setProperty("hadoop.home.dir", "C:\\hadoop-2.8.0")


  val studyModel = MaslowianAgent

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
        Member.props(group, studyModel, DECISION_MODELS.head, (PREFERENCE_GENERATORS.head, WEIGHTS_GENERATORS.head)),
        s"$groupId@@@father")
      mother = system.actorOf(
        Member.props(group, studyModel, DECISION_MODELS(1), (PREFERENCE_GENERATORS(1), WEIGHTS_GENERATORS(1))),
        s"$groupId@@@mother")
      wife = system.actorOf(
        Member.props(group, studyModel, DECISION_MODELS(2), (PREFERENCE_GENERATORS(2), WEIGHTS_GENERATORS(2))),
        s"$groupId@@@wife")
      husband = system.actorOf(
        Member.props(group, studyModel, DECISION_MODELS(3), (PREFERENCE_GENERATORS(3), WEIGHTS_GENERATORS(3))),
        s"$groupId@@@husband")

      father ? Declare
    })
  }
  finally {
    Thread.sleep(120000)
    dataDumpGenerator ! CreateDump
  }
}