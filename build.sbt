import sbt.Keys.libraryDependencies
import scala.language.postfixOps

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

Global / excludeLintKeys += test / fork
Global / excludeLintKeys += run / mainClass

val json4sVersion = "4.0.6"

lazy val dependencies = Seq(
  "org.json4s" %% "json4s-native" % json4sVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "nsg_translator",
    idePackagePrefix := Some("com.ngs_translator"),
    libraryDependencies ++= dependencies
  )

unmanagedBase := baseDirectory.value / "lib"

compileOrder := CompileOrder.JavaThenScala
test / fork := true
run / fork := true
run / javaOptions ++= Seq(
  "-Xms8G",
  "-Xmx100G",
  "-XX:+UseG1GC"
)

Compile / mainClass := Some("com.ngs_translator.Main")
run / mainClass := Some("com.ngs_translator.Main")

val jarName = "ngs_translator.jar"
assembly/assemblyJarName := jarName

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
