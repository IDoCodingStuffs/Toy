package net.codingstuffs.toy.engine.iteration.behavior

import net.codingstuffs.toy.engine.agent.Agent.AgentParams
import net.codingstuffs.toy.engine.agent.AgentConductor.GroupDataPoint
import net.codingstuffs.toy.engine.intake.parse.ConfigUtil

trait GroupDynamics {
  def updateWeights(agentData: Seq[AgentParams],
    groupData                : Seq[GroupDataPoint]): Seq[AgentParams] = {
    val agents = agentData.groupBy(_.group)
    val distanceStats = groupData.groupBy(_.groupId)

    val updatedWeights = agents.keySet.map(
      group => group -> agents(group).map(
        agent => agent.turnInGroup -> agent.groupWeights
          //.filter(item => item._1 != agent.turnInGroup)
          .map(
            weight => {
                //Here be the good stuff
                //!TODO: This desperately needs refactor
              if (agent.turnInGroup != weight._1) {
                val distanceToAgent = distanceStats(group).head
                  .distancesPerMember(agent.turnInGroup)(weight._1) + Double.MinPositiveValue
                weight._1 -> weight._2 / (distanceToAgent + ConfigUtil.LAMBDA)
              }
              else
                weight._1 -> weight._2
              })
      ).toMap
    ).toMap


    agentData.map(
      agent => AgentParams(
        agent.phenome,
        agent.group: String,
        agent.turnInGroup,
        agent.groupMembers,
        updatedWeights(agent.group)(agent.turnInGroup),
        agent.knownGroupPatterns,
        agent.maslowianParams
      )
    )
  }
}
