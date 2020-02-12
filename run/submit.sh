#!/bin/bash
export AWS_ACCESS_KEY_ID=<your AWS access key>
export AWS_SECRET_ACCESS_KEY=<your AWS secret access key>
spark-submit --class com.connector.kafka2s3.ConnectorApp target/scala-2.11/kafka2s3-assembly-1.0.0.jar