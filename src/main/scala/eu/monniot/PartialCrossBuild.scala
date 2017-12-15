package eu.monniot

import sbt.Keys.{crossScalaVersions, scalaVersion}
import sbt.internal.CommandStrings.SwitchCommand
import sbt.complete.Parser
import sbt.librarymanagement.CrossVersion
import sbt.{Command, Extracted, Help, Project, ProjectRef, ReExposed, State}

import scala.language.postfixOps

object PartialCrossBuild {

  def aggregate(state: State): Seq[ProjectRef] = {
    val x = Project.extract(state)
    x.currentProject.aggregate
  }

  def crossVersions(state: State, proj: ProjectRef): Seq[String] = {
    val x = Project.extract(state)
    import x._

    crossScalaVersions in proj get structure.data getOrElse {
      // reading scalaVersion is a one-time deal
      (scalaVersion in proj get structure.data).toSeq
    }
  }

  /**
    * Parse the given command into either an aggregate command or a command for a project
    */
  private def parseCommand(command: String): Either[String, (String, String)] = {
    import sbt.internal.util.complete.DefaultParsers._
    val parser = (OpOrID <~ charClass(_ == '/', "/")) ~ any.* map {
      case project ~ cmd => (project, cmd.mkString)
    }
    Parser.parse(command, parser).left.map(_ => command)
  }

  def switchBack(x: Extracted): List[String] =
    scalaVersion in x.currentRef get x.structure.data map (SwitchCommand + " " + _) toList

  def runWithCommand(commandName: String): Command =
    Command.arb(ReExposed.requireSession(runWithParser(commandName)), runWithHelp(commandName))(runWithCommandImpl)

  def runWithCommandImpl(state: State, args: (String, String)): State = {
    val (version, command) = args

    val switchBackCommands = switchBack(Project.extract(state))

    parseCommand(command) match {
      case Right(_) =>
        // A project is specified, run as is
        command :: switchBackCommands ::: state
      case Left(_) =>

        // No project specified, only run for the projects that are compatible
        val projVersions = aggregate(state) map { proj =>
          proj -> crossVersions(state, proj)
        } toList

        // Targeted scala binary version
        val binaryVersion = CrossVersion.binaryScalaVersion(version)

        val commands = (SwitchCommand + " " + version + " -v") +:
          projVersions.collect {
            case (project, versions)
              if versions.exists(v => CrossVersion.binaryScalaVersion(v) == binaryVersion) =>
              project.project + "/" + command
          }

        commands ::: switchBackCommands ::: state
    }

  }

  private def runWithParser(commandName: String)(state: State): Parser[(String, String)] = {
    import ReExposed.defaultParsers._
    def versionAndCommand(spacePresent: Boolean) = {
      val x = Project.extract(state)
      import x._
      val knownVersions = crossVersions(state, currentRef)
      val version = token(StringBasic.examples(knownVersions: _*))
      val spacedVersion = if (spacePresent) version else version & spacedFirst(commandName)
      val command = token(Space ~> matched(state.combinedParser))
      spacedVersion ~ command
    }

    def spacedFirst(name: String) = opOrIDSpaced(name) ~ any.+

    token(commandName ~> OptSpace) flatMap { sp => versionAndCommand(sp.nonEmpty) }
  }

  private def runWithHelp(commandName: String) = Help.more(commandName,
    s"""$commandName <scala-version> <command>
       |  Runs the command with the Scala version.
       |
       |  Switch the build to <scalaVersion>, then runs the given command.
       |  If the command is for a single project, just executes that project,
       |  otherwise it aggregates all the projects that are binary compatible with the given
       |  scala version and executes those.
       |
       |  After running the command, it leaves the scala version back how it was.
    """.stripMargin)

}
