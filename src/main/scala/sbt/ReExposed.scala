package sbt

import sbt.complete.{DefaultParsers, Parser}


// Re exposed some sbt internal methods
object ReExposed {
  def requireSession[T](p: State => Parser[T]): State => Parser[T] =
    sbt.Cross.requireSession(p)


  val defaultParsers = DefaultParsers
}