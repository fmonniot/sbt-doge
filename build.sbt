sbtPlugin := true

name := "sbt-partial-cross-build"

organization := "eu.monniot"

version := "0.1.0-SNAPSHOT"

description := "sbt plugin to aggregate across crossScalaVerions for muilti-project builds"

licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT"))

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

publishMavenStyle := false

bintrayRepository := "sbt-plugins"

// As the original plugin didn't had a license, I can't choose one like this
bintrayOmitLicense := true

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false
