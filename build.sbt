name := "akka-epl"

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


libraryDependencies += "org.jsoup" % "jsoup" % "1.10.3"
libraryDependencies += "com.ning" % "async-http-client" % "1.9.40"
libraryDependencies += "com.typesafe.play" % "play-json_2.12" % "2.6.3"


libraryDependencies += "com.cloudant" % "cloudant-client" % "2.6.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.12" % "2.5.4"


