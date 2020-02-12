package com.connector.kafka2s3

import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions.{col, from_json}
import scala.reflect.runtime.universe._

object KafkaReader {

  /** Read a message from Kafka as a String.
    *
    * @param spark Spark session
    * @param kafkaURL Broker url list
    * @param topic input topic
    * @return Dataset of String
    */
  def readAsString(spark: SparkSession, kafkaURL: String, topic: String): Dataset[String] = {

    import spark.implicits._

    val dfKafkaIn = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaURL)
      .option("subscribe", topic)
      .option("failOnDataLoss", false)
      .load()

    dfKafkaIn
      .select(col("value").cast(StringType) as "value")
      .as[String]

  }

  /** Read the JSON messages from KAFKA and cast them to a custom case class.
    *
    * @param spark Spark session
    * @param kafkaURL Broker url list
    * @param topic input topic
    * @param arg
    * @tparam T TypeTag of the case class matching with the json message
    * @return Dataset of case class T
    */
  def readTyped[T: TypeTag](spark: SparkSession, kafkaURL: String, topic: String)(
    implicit arg: Encoder[T]
  ): Dataset[T] = {
    val dataType = ScalaReflection.schemaFor[T].dataType.asInstanceOf[StructType]
    val df = this
      .readAsString(spark, kafkaURL, topic)
      .select(from_json(col("value"), dataType).alias("value"))
      .select("value.*")
    df.as[T]
  }

}