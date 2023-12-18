#
#
#  Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pathlib import Path
import json
import logging

app = FastAPI()

def load_schema(schema_path: Path):
    with open(schema_path, "r") as file:
        schema_data = json.load(file)
    return schema_data

logger = logging.getLogger(__name__)
@app.get("/jsonschema/{path:path}")
async def get_json_schema(path: str):
    schema_path = Path(f"schemas/{path}.json")
    logger.info(f"Loading schema {schema_path}")
    if not schema_path.is_file():
        raise HTTPException(status_code=404, detail=f"Schema {path} not found")

    schema_data = load_schema(schema_path)
    return JSONResponse(content=schema_data, media_type="application/json")
