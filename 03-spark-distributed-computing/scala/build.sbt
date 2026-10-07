ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "course"
ThisBuild / version := "1.0.0"

name := "spark-interview-course"
Compile / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.6" % Provided
