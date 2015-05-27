name := """UDS-server"""

organization := "psyriccio"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.3.9",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.google.guava" % "guava" % "18.0",
  "io.spray" % "spray-can_2.11" % "1.3.2",
  "io.spray" % "spray-routing_2.11" % "1.3.2",
  "org.jodd" % "jodd-core" % "3.6.5"
)

packSettings

packMain := Map(name.value -> name.value.toString().toLowerCase().concat(".AppMain"))

packJvmOpts := Map(name.value -> Seq("-Xmx512m"))

packExtraClasspath := Map(name.value-> Seq("${PROG_HOME}/lib"))

packGenerateWindowsBatFile := true

packJarNameConvention := "default"

packExpandedClasspath := true

assemblyJarName in assembly := name.value + "-" + version.value + "-monolith.jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true, includeDependency = true)

assemblyMergeStrategy in assembly := {
    case "plugin.properties" => MergeStrategy.rename
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}

val reflect	= TaskKey[Seq[File]]("reflect")

val reflectPackage = SettingKey[String]("reflect-package")

val reflectClass = SettingKey[String]("reflect-class")

reflectPackage	:= "buildinfo"

reflectClass	:= "buildInfo"

reflect	<<= (sbt.Keys.sourceManaged, sbt.Keys.name, sbt.Keys.version, reflectPackage, reflectClass) map {
  (sourceManaged:File, name:String, version:String, reflectPackage:String, reflectClass:String)	=>
    val	file	= sourceManaged / reflectPackage / "buildInfo.scala"
    val code	=
        (
          if (reflectPackage.nonEmpty)	"package " + reflectPackage + "\n"
          else							""
        ) +
        "object " + reflectClass + " {\n" +
        "\tval name\t= \"" + name + "\"\n" +
        "\tval version\t= \"" + version + "\"\n" +
        "}\n"
    IO write (file, code)
    Seq(file)
}

sourceGenerators in Compile <+= reflect map identity

unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "res") }

unmanagedJars in Compile += file("../UDS-lib/dist/UDS-lib.jar")