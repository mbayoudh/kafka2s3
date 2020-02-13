# kafka2s3 overview
This project provides a Spark structured streaming job to sink Kafka to S3.
- The data are read from Kafka topic with Spark structured streaming.
- The supported format of the input messages is JSON 
- The data are read in a type safe way. The messages are casted to custom case class defined in the config file.
- The write to S3 is triggered through a configurable period defined in the config file.
- The data are partitioned by the customer columns defined in the config file. For the timestamp column, a custom pattern could be specified for the partitioning.


## Build the jar
- `git clone git@github.com:mbayoudh/kafka2s3.git`
- `sbt assembly`

## Create the config file from the sample
`cp conf/sample-default.conf connector.conf`

## Edit the config file and set the config 
`vim connector.conf`

Define the config:
- `kafka-url`: list of Kafka brokers.
- `kafka-topic`: the name of the input Kafka topic.
- `case-class-name`: your customer case class matching the JSON schema of the messages you want to read.
- `batch-interval`: the buffering period before to trigger the write to S3
- `output-path`: the output path. This could be hdfs folder `fs:///<yourfolder>` or S3 bucket `s3a://<your bucket>`. Notice that for S3 sink, you must use s3a protocol.
- `output-format`: the format of the output files. You can use any format supported by Spark (parquet, csv, JSON).
- `partition-columns`: the partitioning of the output files. The recommanded partition is the timestamp one (if you have timestamp in your data). Exalple of the supported time patterns:
  - `YYYYMMddHH`: hourly
  - `YYYYMMdd`: daily
  - `YYYYMM`: monthly

## Run 
Set AWS access key and secret Set the job argument (path to the config file).
- `export AWS_ACCESS_KEY_ID=<your AWS access key>`
- `export AWS_SECRET_ACCESS_KEY=<your AWS secret access key>`

Run the job with spark-submit (Spark 2.4.4 compiled with hadoop 2.9.2):

`spark-submit --class com.connector.kafka2s3.ConnectorApp target/scala-2.11/kafka2s3-assembly-1.0.0.jar connector.conf`
