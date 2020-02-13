# kafka2s3 overview
This project provides a Spark structured streaming job to sink Kafka to S3.
- The data are read from Kafka topic with Spark structured streaming.
- The supported format of the input messages is JSON 
- The data are read in a type safe way. The messages are casted to custom case class defined in the config file.
- The write to S3 is triggered through a configurable period defined in the config file.
- The data are partitioned by the customer columns defined in the config file. For the timestamp column, a custom pattern could be specified for the partitioning.


## Build the jar
`git clone`
`sbt assembly`

## Create the config file
`kafka-url`: list of Kafka brokers.
`kafka-topic`: the name of the input Kafka topic.
`case-class-name`: your customer case class matching the JSON schema of the messages you want to read.
`batch-interval`: the buffering period before to trigger the write to S3
`output-path`: the output path. This could be hdfs folder `fs:///<yourfolder>` or S3 bucket `s3a://<your bucket>`. Notice that for S3 sink, you must use s3a protocol.
`output-format`: the format of the output files. You can use any format supported by Spark (parquet, csv, JSON).
`partition-columns`: the partitioning of the output files. The recommanded partition is the timestamp one (if you have timestamp in your data). Exalple of the supported time patterns:
- `YYYYMMddHH`: hourly
- `YYYYMMdd`: daily
- `YYYYMM`: monthly

## Run 
Set AWS access key and secret
Set the job argument (path to the config file)
