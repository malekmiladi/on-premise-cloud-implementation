from fastapi import FastAPI
from .routers import user_data, vendor_data, meta_data, instance_config


app = FastAPI()
app.include_router(user_data.router)
app.include_router(meta_data.router)
app.include_router(vendor_data.router)
app.include_router(instance_config.router)


@app.get("/")
def read_root():
    return {"_service": "cloud-init-datasource", "_version": 1.0}
