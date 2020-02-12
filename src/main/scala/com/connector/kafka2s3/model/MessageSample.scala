package com.connector.kafka2s3.model

case class MessageSample(timestamp: Long, companyName: String, numberOfTransactions: Int)

/** Example of expected JSON Kafka messages:
  {"timestamp": 1581344284000, "companyName": "BigCompany1", "numberOfTransactions": 42}
  {"timestamp": 1581344384000, "companyName": "BigCompany1", "numberOfTransactions": 1}
  {"timestamp": 1581344294000, "companyName": "BigCompany2", "numberOfTransactions": 420}
*/