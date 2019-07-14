name := "toy-engine"

version := "0.1.0"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.23",
  
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  
  //"org.springframework.boot" % "spring-boot" % "2.1.3.RELEASE",

//  "com.typesafe.play" %% "play" % "2.7.0",
//  "org.apache.cassandra" % "cassandra-all" % "3.11.3"
)

