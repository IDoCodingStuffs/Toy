package net.codingstuffs.abilene.simulation.generators.random

import net.codingstuffs.abilene.intake.parse.ConfigUtil

import scala.util.Random

object Central {
  final def GENERATOR = new Random(ConfigUtil.MAIN_GENERATOR_SEED)
}
