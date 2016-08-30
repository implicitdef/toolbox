name := """toolbox"""

organization := "com.github.implicitdef"

version := "0.5.0"

scalaVersion := "2.11.7"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies += "com.typesafe.play" %% "play" % "2.5.6"
libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.5.6"

libraryDependencies += "me.lessis" %% "retry" % "0.2.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
