import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "gul-zoe-server"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

retrieveManaged := true

libraryDependencies += "org.ini4j" % "ini4j" % "0.5.2"

libraryDependencies += "org.clapper" %% "argot" % "1.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.2"
