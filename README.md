sbt-partial-cross-build
========

:bangbang: This is a fork of the original [sbt-doge plugin](https://github.com/sbt/sbt-doge) which only keep the strict cross build feature. Once [sbt#3698](https://github.com/sbt/sbt/issues/3698) have been fixed, this plugin will be retired as well. :bangbang:

`sbt-partial-cross-build` is a sbt plugin to aggregate across `crossScalaVersions` for multi-project builds, which I call partial cross building.

![sbt-doge](sbt-doge.png?raw=true)

Current implementation of `+` cross building operator does not take in account for the `crossScalaVersions` of the sub projects. Until that's fixed, here's an alternative implementation of it.

setup
-----

This is an auto plugin, so you need sbt 1.0+. Put this in `project/plugins.sbt`:

```scala
addSbtPlugin("eu.monniot" % "sbt-partial-cross-build" % "0.1.0-SNAPSHOT")
```

usage
-----

First, define a multi-project build with a root project aggregating some child projects:

```scala
lazy val rootProj = (project in file("."))
  .aggregate(libProj, fooPlugin, abProject)

lazy val libProj = (project in file("lib"))
  .settings(
    name := "foo-lib",
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.11.1", "2.10.4")
  )

lazy val abProject = (project in file("ab-project"))
  .settings(
    name := "ab-project",
    scalaVersion := "2.11.1"
  )
  .dependsOn(libProj)

lazy val fooPlugin = (project in file("sbt-foo"))
  .settings(
    name := "sbt-foo",
    sbtPlugin := true,
    scalaVersion := "2.10.4"
  )
```

Next run this from the root project:

```scala
> +++2.11.1 compile
```

sbt-partial-cross-build will break the above into the following commands and executes them:

```scala
> ++ 2.11.1 -v
> libProj/compile
> abProject/compile
```

Note that sbt-partial-cross-build is a triggered plugin and so didn't needs to be enable manually.
