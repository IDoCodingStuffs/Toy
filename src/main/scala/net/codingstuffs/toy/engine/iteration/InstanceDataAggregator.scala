//package net.codingstuffs.toy.engine.iteration
//
//import akka.actor.{Actor, ActorLogging, ActorRef, Props}
//
//object InstanceDataAggregator {
//  def props(
//    instantiator: ActorRef,
//    assigned: Int): Props =
//    Props(new InstanceDataAggregator())
//
//  case class GroupParams(groupId: String,
//    memberName: String,
//    attunementDecisionParams: ExpressionParams,
//    memberExpression: String)
//
//  case class AgentParams(
//    groupId: String,
//    memberName: String,
//    attunementDecisionParams: ExpressionParams,
//    memberExpression: String)
//}
//
//class InstanceDataAggregator() extends Actor with ActorLogging {
//  var actorDataPoints: Seq[ActorDataPoint] = Seq()
//  var actorRawDataPoints: Seq[ActorRawDataPoint] = Seq()
//  var groupDataPoints: Seq[GroupDataPoint] = Seq()
//
//  override def receive: Receive = {
//    case dataPoint: ActorDataPoint =>
//      actorDataPoints = actorDataPoints :+ dataPoint
//    case dataPoint: ActorRawDataPoint =>
//      actorRawDataPoints = actorRawDataPoints :+ ActorRawDataPoint(dataPoint.groupId, dataPoint
//        .memberName, dataPoint.attunementDecisionParams, dataPoint.memberExpression.mkString
//        .concat(""))
//    case dataPoint: GroupDataPoint =>
//      groupDataPoints = groupDataPoints :+ dataPoint
//      if (groupDataPoints.size == assigned) {
//        analytics ! DataAggregate(actorDataPoints, actorRawDataPoints, groupDataPoints)
//      }
//  }
//}
