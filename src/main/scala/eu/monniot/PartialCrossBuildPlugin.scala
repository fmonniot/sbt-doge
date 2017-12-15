package eu.monniot

import sbt.Keys.commands
import sbt.{AutoPlugin, Def}


object PartialCrossBuildPlugin extends AutoPlugin {

  override def requires = empty

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    commands ~= (PartialCrossBuild.runWithCommand("+++") +: _)
  )
}

