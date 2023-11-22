#   Copyright ETH 2023 Zürich, Scientific IT Services
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

from pybis import Openbis
import abc
import time
import json
import base64
import numpy as np
import sys
from urllib.parse import urljoin, urlparse


SERVICE_NAME = "imaging"

def get_instance(url="http://localhost:8888/openbis"):
    base_url = url
    # base_url = "http://localhost:8888/openbis"
    # base_url = "https://alaskowski:8443/openbis"
    # base_url = "https://openbis-sis-ci-sprint.ethz.ch/"
    openbis_instance = Openbis(
        url=base_url,
        verify_certificates=False,
        allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True
    )
    token = openbis_instance.login('admin', 'changeit')
    print(f'Connected to {base_url} -> token: {token}')
    return openbis_instance


def execute_custom_dss_service(openbis, code, parameters):
    service_id = {
        "@type": "dss.dto.service.id.CustomDssServiceCode",
        "permId": code
    }
    options = {
        "@type": "dss.dto.service.CustomDSSServiceExecutionOptions",
        "parameters": parameters
    }
    request = {
        "method": "executeCustomDSSService",
        "params": [
            openbis.token,
            service_id,
            options
        ],
    }
    full_url = urljoin(openbis._get_dss_url(), openbis.dss_v3)
    return openbis._post_request_full_url(full_url, request)


class AbstractImagingRequest(metaclass=abc.ABCMeta):

    @abc.abstractmethod
    def _validate_data(self):
        return

    def __str__(self):
        return json.dumps(self.__dict__)


class ImagingDataSetPreview(AbstractImagingRequest):
    config: dict
    format: str
    bytes: str | None
    show: bool
    metadata: dict

    def __init__(self, config, preview_format, metadata=None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetPreview"
        self.bytes = None
        self.format = preview_format
        self.config = config if config is not None else dict()
        self.metadata = metadata if metadata is not None else dict()
        self._validate_data()

    def _validate_data(self):
        assert self.format is not None, "Format can not be null"

    def save_to_file(self, file_path):
        assert self.bytes is not None, "There is no image information!"
        img_data = bytearray(self.bytes, encoding='utf-8')
        with open(file_path, "wb") as fh:
            fh.write(base64.decodebytes(img_data))


class ImagingDataSetExport(AbstractImagingRequest):
    config: dict
    metadata: dict

    def __init__(self, config, metadata=None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetExport"
        self.config = config if config is not None else dict()
        self.metadata = metadata if metadata is not None else dict()
        self._validate_data()

    def _validate_data(self):
        assert self.config is not None, "Config can not be null"
        required_keys = {"include", "archive-format", "image-format", "resolution"}
        for key in required_keys:
            assert key in self.config and self.config[key] is not None, \
                f"export->config->{key}: Must not be None!"


class ImagingDataSetMultiExport(AbstractImagingRequest):
    permId: str
    index: int
    config: dict
    metadata: dict

    def __init__(self, permId, index, config, metadata=None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetMultiExport"
        self.permId = permId
        self.index = index
        self.config = config
        self.metadata = metadata if metadata is not None else dict()
        self._validate_data()

    def _validate_data(self):
        assert self.permId is not None, "PermId can not be null"
        assert self.index is not None, "Index can not be null"
        assert self.config is not None, "Config can not be null"
        required_keys = {"include", "archive-format", "image-format", "resolution"}
        for key in required_keys:
            assert key in self.config and self.config[key] is not None, \
                f"export->config->{key}: Must not be None!"


class ImagingDataSetControlVisibility:
    label: str
    values: list[str]
    range: list[str]
    unit: str

    def __init__(self):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetControlVisibility"


class ImagingDataSetControl:
    label: str
    section: str
    type: str
    values: list[str]
    unit: str
    range: list[str]
    multiselect: bool
    playable: bool | None
    speeds: list[int]
    visibility: list[ImagingDataSetControlVisibility]
    metaData: dict

    def __init__(self):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetControl"


class ImagingDataSetConfig:
    adaptor: str
    version: float
    speeds: list[int]
    resolutions: list[str]
    playable: bool
    exports: list[ImagingDataSetControl]
    inputs: list[ImagingDataSetControl]
    metadata: dict

    def __init__(self):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetConfig"


class ImagingDataSetImage:
    previews: list[ImagingDataSetPreview]
    config: dict
    metadata: dict

    def __init__(self, config=None, previews=None, metadata=None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetImage"
        self.config = config if config is not None else dict()
        self.previews = previews if previews is not None else []
        self.metadata = metadata if metadata is not None else dict()

    def add_preview(self, preview):
        self.previews += [preview]


class ImagingDataSetPropertyConfig:
    config: ImagingDataSetConfig
    images: list[ImagingDataSetImage]

    def __init__(self, config: ImagingDataSetConfig, images: list[ImagingDataSetImage]):
        assert config is not None, "Config must not be None!"
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetPropertyConfig"
        self.config = config
        self.images = images if images is not None else []

    def add_image(self, image):
        self.images += [image]


def get_preview(perm_id: str, index: int, preview: ImagingDataSetPreview) -> ImagingDataSetPreview:
    parameters = {
        "type": "preview",
        "permId": perm_id,
        "index": index,
        "error": None,
        "preview": preview.__dict__
    }
    service_response = execute_custom_dss_service(o, SERVICE_NAME, parameters)
    if service_response['error'] is None:
        preview.__dict__ = service_response["preview"]
        return preview
    else:
        raise ValueError(service_response['error'])


def get_export(perm_id: str, index: int, export: ImagingDataSetExport) -> str:
    parameters = {
        "type": "export",
        "permId": perm_id,
        "index": index,
        "error": None,
        "url": None,
        "export": export.__dict__
    }
    service_response = execute_custom_dss_service(o, SERVICE_NAME, parameters)
    if service_response['error'] is None:
        return service_response['url']
    else:
        raise ValueError(service_response['error'])


def get_multi_export(exports: list[ImagingDataSetMultiExport]) -> str:
    parameters = {
        "type": "multi-export",
        "error": None,
        "url": None,
        "exports": [export.__dict__ for export in exports]
    }
    service_response = execute_custom_dss_service(o, SERVICE_NAME, parameters)
    if service_response['error'] is None:
        return service_response['url']
    else:
        raise ValueError(service_response['error'])


o = get_instance()

config_sxm_preview = {
    "channel": "z", # usually one of these: ['z', 'I', 'dIdV', 'dIdV_Y']
    "x-axis": [0.0, 3.0], # file dependent
    "y-axis": [0.0, 3.0], # file dependent
    "color-scale": [-700.0, 700.0], # file dependend
    "colormap": "cividis", # [gray, YlOrBr, viridis, cividis, inferno, rainbow, Spectral, RdBu, RdGy]
    "scaling": "linear", # ['linear', 'logarithmic']
    # "mode": 3 # uncomment this if you want to generate random pixel image generation
}

# imaging_preview = ImagingDataSetPreview(preview_format="png", config=config_sxm)
# response = get_preview('20231110130838616-26', 0, imaging_preview)
# print(response)

config_export = {
    "include": ['image', 'raw data'],
    "image-format": 'original',
    "archive-format": "zip",
    "resolution": "original"
}
# imaging_export = ImagingDataSetExport(config_export)
# export_response = get_export('20231110130838616-26', 0, imaging_export)
# print(export_response)


# imaging_export1 = ImagingDataSetMultiExport('20231110130838616-26', 0, config_export)
# imaging_export2 = ImagingDataSetMultiExport('20231110134813653-27', 0, config_export)
# multi_export_response = get_multi_export([imaging_export1, imaging_export2])
# print(multi_export_response)






