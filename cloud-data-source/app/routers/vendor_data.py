from fastapi import APIRouter, Response
from ..db.databaseConnection import db
from ..helpers.yamlDumper import YamlDumper
import yaml
import uuid

router = APIRouter(
    prefix="/instance",
)


@router.get("/{instanceId}/vendor-data")
def get_instance_vendor_data(instance_id: str):
    vendor_data_query = {
        "selector": {
            "_id": {
                "$eq": uuid.UUID(instance_id).hex
            }
        },
        "fields": [
            "vendor-data"
        ]
    }
    vendor_data_result = list(db.find(vendor_data_query))
    vendor_data = vendor_data_result[0]["vendor-data"]

    return Response(
        content=yaml.dump(
            vendor_data,
            indent=2,
            Dumper=YamlDumper
        ),
        media_type="text/plain"
    )
