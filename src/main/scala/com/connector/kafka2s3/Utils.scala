package com.connector.kafka2s3

import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, Dataset}
import org.joda.time.DateTime
import scala.reflect.api
import scala.reflect.runtime.universe._

object Utils {

  /** Method returning the TypeTag of a case class from the case class name provided as a String.
    *
    * @param name class name
    * @tparam A class type
    * @return TypeTag of the class
    */
  def stringToTypeTag[A](name: String): TypeTag[A] = {
    val c = Class.forName(name)
    val mirror = runtimeMirror(c.getClassLoader)
    val sym = mirror.staticClass(name)
    val tpe = sym.selfType
    TypeTag(
      mirror,
      new api.TypeCreator {
        def apply[U <: api.Universe with Singleton](m: api.Mirror[U]): U#Type =
          if (m eq mirror) tpe.asInstanceOf[U#Type]
          else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors")
      }
    )
  }

  /** Enrich a dataset with new columns (and handle the timestamp column if exists).
    *
    * @param ds dataset
    * @param columns columns to add.
    * @return enriched dataset.
    */
  def enrichDatasetWithNewColumns[T](ds: Dataset[T], columns: Seq[ColumnDefinition]): DataFrame = {

    val splitPartitionsColumns = columns.partition(_.isTimestamp)
    val timeColumn: Option[ColumnDefinition] = splitPartitionsColumns._1.headOption
    val otherColumns: Seq[ColumnDefinition] = splitPartitionsColumns._2

    val df: DataFrame =
      if (timeColumn.isDefined) {
        val timeColumnVal = timeColumn.get
        val timePattern: String = timeColumnVal.newName
        // UDF to format the timestamp in the timePattern format
        val getTimePartition = udf((s: Long) => new DateTime(s).toString(timePattern))
        // Add formatted time pattern
        ds.withColumn(timePattern, getTimePartition(col(timeColumnVal.name)))
      } else ds.toDF

    otherColumns.foldLeft(df)((df, columnDef) => df.withColumn(columnDef.newName, col(columnDef.name)))

  }

}

case class ColumnDefinition(name: String, newName: String, isTimestamp: Boolean)
