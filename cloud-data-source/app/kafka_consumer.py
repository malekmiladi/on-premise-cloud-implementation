import json
import uuid

from confluent_kafka import Consumer

from db.databaseConnection import db


def main():
    conf = {'bootstrap.servers': '192.168.1.17:9092',
            'group.id': "clouddatasource-instance-config-save-requests"}
    topic = "clouddatasource-instance-config-save-requests"
    consumer = Consumer(conf)
    consumer.subscribe([topic])
    while True:
        try:
            msg = consumer.poll(1.0)
            if msg is None:
                continue
            print(f"*** Received message: {msg.value().decode('utf-8')}\n\n")
            json_message = json.loads(msg.value())
            instance_id = json_message["instanceId"]
            instance_config = json.loads(json_message["instanceConfig"])
            instance_config.update({"_id": uuid.UUID(instance_id).hex})
            db.save(instance_config)
        except KeyboardInterrupt:
            break
    consumer.close()


if __name__ == "__main__":
    main()
