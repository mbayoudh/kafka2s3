package com.connector.kafka2s3

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, Encoders, SparkSession}
import scala.collection.JavaConverters._

object ConnectorApp extends App with StrictLogging {

  // Get path to conf file
  val confPath: String = if(args.nonEmpty) args(0) else "conf/example-default.conf"

  // Read and parse the config file
  val confFile: File = new File(confPath)
  val fileConfig: Config = ConfigFactory.parseFile(confFile)

  // Get the App configuration
  val appConf: Config = fileConfig.getConfig("app-conf")
  val appName = appConf.getString("app-name")
  val KafkaUrl = appConf.getString("kafka-url")
  val inputTopic = appConf.getString("kafka-topic")
  val className = appConf.getString("case-class-name")
  val batchInterval = appConf.getString("batch-interval")
  val checkpointPath = appConf.getString("checkpoint-path")
  val outputPath = appConf.getString("output-path")
  val outputFormat = appConf.getString("output-format")
  val partitionColumns: Seq[ColumnDefinition] = appConf
    .getConfigList("partition-columns")
    .asScala
    .map(
      c =>
        ColumnDefinition(
          c.getString("name"),
          c.getString("new-name"),
          c.getBoolean("is-timestamp-pattern")
        )
    )

  // Spark session
  val spark: SparkSession = SparkSession.builder
    .appName(appName)
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate

  logger.info(s"Connexion to Kafka broker: $KafkaUrl")
  logger.info(s"Reading from Kafka topic: $inputTopic")
  logger.info(s"Reading from Kafka topic: $inputTopic")
  logger.info(s"The batch period: $batchInterval")

  // Read data from Kafka and load it as a dataset of className
  val kafkaDataDs =
    KafkaReader
      .readTyped(spark, KafkaUrl, inputTopic)(
        Utils.stringToTypeTag(className),
        Encoders.product(Utils.stringToTypeTag(className))
      ).coalesce(1)

  // Enrich dataset with new columns created for partitioning
  val enrichedKafkaDataDf: DataFrame =
    Utils.enrichDatasetWithNewColumns(kafkaDataDs, partitionColumns)

  logger.info(s"Writing to S3 bucket or HDFS: $inputTopic")

  // Write the stream to S3 each batchInterval with partitioning
  enrichedKafkaDataDf.writeStream
    .partitionBy(partitionColumns.map(_.newName): _*)
    .trigger(Trigger.ProcessingTime(batchInterval))
    .outputMode("append")
    .format(outputFormat)
    .option("path", outputPath)
    .option("checkpointLocation", checkpointPath)
    .start()

  spark.streams.awaitAnyTermination()

}
