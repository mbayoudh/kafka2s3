organization := "com.connector"
name := "kafka2s3"
version := "1.0.0"

scalaVersion := "2.11.12"
val sparkVersion = "2.4.4"
val hadoopVersion = "2.9.2"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused-import",
  "-Ywarn-unused"
)

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.6.7",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.7",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.6.7.1"
)

excludeDependencies ++= Seq(
  // Exclude spark.usused to avoid conflicts during Fat jar assembly (SPARK-3812)
  ExclusionRule("org.spark-project.spark", "unused")
)

libraryDependencies ++= Seq(
  "org.apache.spark"           %% "spark-sql"                       % sparkVersion % Provided exclude ("org.apache.hadoop", "hadoop-client"),
  "org.apache.hadoop"          % "hadoop-client"                    % hadoopVersion % Provided,
  "org.apache.hadoop"          % "hadoop-aws"                       % hadoopVersion
    exclude ("org.apache.hadoop", "hadoop-common")
    exclude ("commons-beanutils", "commons-beanutils"),
  "org.apache.spark"           %% "spark-sql-kafka-0-10" % sparkVersion,
  "com.typesafe"               % "config"                % "1.3.3",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.2"
)

parallelExecution in Test := false
