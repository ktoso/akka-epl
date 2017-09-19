name := "akka-epl"

version := "1.0"

scalaVersion := "2.12.2"

val akkaVersion = "2.5.3"
val akkaHttpVersion = "10.0.10"

libraryDependencies += "com.typesafe.akka" %% "akka-actor"   % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream"  % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http"    % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"


libraryDependencies += "com.ning" % "async-http-client" % "1.9.40" // no need for this, use Akka's HTTP client
libraryDependencies += "com.typesafe.play" % "play-json_2.12" % "2.6.3"

libraryDependencies += "org.jsoup" % "jsoup" % "1.10.3"

libraryDependencies += "com.cloudant" % "cloudant-client" % "2.6.2"

// testing dependencies ---
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
