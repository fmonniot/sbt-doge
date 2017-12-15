lazy val rootProj = (project in file("."))
  .aggregate(libProj, fooPlugin, abProject)


lazy val abProject = (project in file("ab-project"))
  .settings(
    name := "ab-project",
    scalaVersion := "2.11.1"
  )
  .dependsOn(libProj)

lazy val libProj = (project in file("lib"))
  .settings(
    name := "foo-lib",
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.11.1", "2.10.4")
  )

lazy val fooPlugin = (project in file("sbt-foo"))
  .settings(
    name := "sbt-foo",
    sbtPlugin := true,
    scalaVersion := "2.10.4"
  )
