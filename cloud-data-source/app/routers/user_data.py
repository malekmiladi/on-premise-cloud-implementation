from fastapi import APIRouter, Response
from ..db.databaseConnection import db
from ..helpers.yamlDumper import YamlDumper
import yaml
import uuid

router = APIRouter(
    prefix="/instance",
)


@router.get("/{instanceId}/user-data")
def get_instance_user_data(instance_id: str):
    user_data_query = {
        "selector": {
            "_id": {
                "$eq": uuid.UUID(instance_id).hex
            }
        },
        "fields": [
            "user-data"
        ]
    }
    user_data_result = list(db.find(user_data_query))
    user_data = user_data_result[0]["user-data"]

    return Response(
        content="#cloud-config\n" + yaml.dump(
            user_data,
            indent=2,
            Dumper=YamlDumper
        ),
        media_type="text/plain"
    )
