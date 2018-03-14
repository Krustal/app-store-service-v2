name := "app-store-service-v2"

version := "2.0.1"

scalaVersion := "2.12.4"

// This is because CassandraUnit fails in `sbt clean test` without this
parallelExecution in Test := false

resolvers += "bintray-pagerduty-oss-maven" at "https://dl.bintray.com/pagerduty/oss-maven"

libraryDependencies ++= {
  val akkaV = "2.5.6"
  val akkaHttpV = "10.0.10"
  val scalaTestV = "3.0.5"
  Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0" excludeAll (ExclusionRule(organization = "org.slf4j")),
    "org.json4s" %% "json4s-native" % "3.5.3",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.17",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.getsentry.raven" % "raven-logback" % "7.8.6",
    "com.indeed" % "java-dogstatsd-client" % "2.0.12",
    "com.pauldijou" %% "jwt-core" % "0.12.1",

    "org.cassandraunit" % "cassandra-unit" % "2.2.2.1" excludeAll (ExclusionRule(organization = "org.slf4j")),
    "org.scalatest" %% "scalatest" % scalaTestV % Test,
    "org.mockito" % "mockito-core" % "2.7.22" % Test
  )
}


imageNames in docker := Seq(ImageName(s"${organization.value}/app-store-service-v2:latest"))

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("anapsix/alpine-java:8")

    add(artifact, artifactTargetPath)

    entryPoint("java", "-jar", artifactTargetPath)
  }
}
