import uuid

from fastapi import APIRouter, Request

from ..db.databaseConnection import db

router = APIRouter(
    prefix="/instances",
)


@router.post("/instance")
async def instance_config(request: Request, instance_id: str):
    config = await request.json()
    config.update({"_id": uuid.UUID(instance_id).hex})
    db.save(config)

    return True
