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
import json
import base64
import requests
import os
from urllib.parse import urljoin


DEFAULT_SERVICE_NAME = "imaging"
IMAGING_CONFIG_PROP_NAME = "$IMAGING_DATA_CONFIG".lower()


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


class AbstractImagingClass(metaclass=abc.ABCMeta):
    def to_json(self):
        return json.dumps(self, default=lambda x: x.__dict__, sort_keys=True, indent=4)

    def __str__(self):
        return json.dumps(self.__dict__, default=lambda x: x.__dict__)

    def __repr__(self):
        return json.dumps(self.__dict__, default=lambda x: x.__dict__)


class AbstractImagingRequest(AbstractImagingClass, metaclass=abc.ABCMeta):
    @abc.abstractmethod
    def _validate_data(self):
        return


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

    @classmethod
    def from_dict(cls, data):
        if data is None:
            return None
        preview = cls(None, None, None)
        for prop in cls.__annotations__.keys():
            attribute = data.get(prop)
            preview.__dict__[prop] = attribute
        return preview


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


class ImagingDataSetControlVisibility(AbstractImagingClass):
    label: str
    values: list[str]
    range: list[str]
    unit: str

    def __init__(self, label: str, values: list[str], values_range: list[str], unit: str = None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetControlVisibility"
        self.label = label
        self.values = values
        self.range = values_range
        self.unit = unit

    @classmethod
    def from_dict(cls, data):
        if data is None:
            return None
        control = cls(None, None, None, None)
        for prop in cls.__annotations__.keys():
            attribute = data.get(prop)
            control.__dict__[prop] = attribute
        return control


class ImagingDataSetControl(AbstractImagingClass):
    label: str
    section: str
    type: str
    values: list[str]
    unit: str
    range: list[str]
    multiselect: bool
    playable: bool
    speeds: list[int]
    visibility: list[ImagingDataSetControlVisibility]
    metaData: dict

    def __init__(self, label: str, control_type: str, values: list[str] = None,
                 values_range: list[str] = None, multiselect: bool = None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetControl"
        self.label = label
        self.type = control_type
        if control_type.lower() in ["slider", "range"]:
            self.range = values_range
        elif control_type.lower() == "dropdown":
            self.values = values
            self.multiselect = multiselect

    @classmethod
    def from_dict(cls, data):
        if data is None:
            return None
        control = cls(None, "", None, None)
        for prop in cls.__annotations__.keys():
            attribute = data.get(prop)
            if prop == 'visibility' and attribute is not None:
                attribute = [ImagingDataSetControlVisibility.from_dict(visibility) for visibility in attribute]
            control.__dict__[prop] = attribute
        return control


class ImagingDataSetConfig(AbstractImagingClass):
    adaptor: str
    version: float
    speeds: list[int]
    resolutions: list[str]
    playable: bool
    exports: list[ImagingDataSetControl]
    inputs: list[ImagingDataSetControl]
    metadata: dict

    def __init__(self, adaptor: str, version: float, resolutions: list[str], playable: bool,
                 speeds: list[int] = None, exports: list[ImagingDataSetControl] = None,
                 inputs: list[ImagingDataSetControl] = None, metadata: dict = None):
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetConfig"
        self.adaptor = adaptor
        self.version = version
        self.resolutions = resolutions
        self.playable = playable
        if playable:
            self.speeds = speeds
        self.exports = exports
        self.inputs = inputs
        self.metadata = metadata

    @classmethod
    def from_dict(cls, data):
        if data is None:
            return None
        config = cls(None, None, None, None)
        for prop in cls.__annotations__.keys():
            attribute = data.get(prop)
            if prop in ['exports', 'inputs'] and attribute is not None:
                attribute = [ImagingDataSetControl.from_dict(control) for control in attribute]
            config.__dict__[prop] = attribute
        return config


class ImagingDataSetImage(AbstractImagingClass):
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

    @classmethod
    def from_dict(cls, data):
        if data is None:
            return None
        image = cls(None, None, None)
        for prop in cls.__annotations__.keys():
            attribute = data.get(prop)
            if prop == 'images' and attribute is not None:
                attribute = [ImagingDataSetPreview.from_dict(image) for image in attribute]
            image.__dict__[prop] = attribute
        return image


class ImagingDataSetPropertyConfig(AbstractImagingClass):
    config: ImagingDataSetConfig
    images: list[ImagingDataSetImage]

    def __init__(self, config: ImagingDataSetConfig, images: list[ImagingDataSetImage]):
        assert config is not None, "Config must not be None!"
        self.__dict__["@type"] = "dss.dto.imaging.ImagingDataSetPropertyConfig"
        self.config = config
        self.images = images if images is not None else []

    @classmethod
    def from_dict(cls, data: dict):
        assert data is not None and any(data), "There is no property config found!"
        config = ImagingDataSetConfig.from_dict(data.get('config'))
        attr = data.get('images')
        images = [ImagingDataSetImage.from_dict(image) for image in attr] if attr is not None else None
        return cls(config, images)

    def add_image(self, image: ImagingDataSetImage):
        if self.images is None:
            self.images = []
        self.images += [image]


class ImagingControl:

    def __init__(self, openbis_instance, service_name=DEFAULT_SERVICE_NAME):
        self._openbis = openbis_instance
        self._service_name = service_name

    def _execute_custom_dss_service(self, parameters):
        service_id = {
            "@type": "dss.dto.service.id.CustomDssServiceCode",
            "permId": self._service_name
        }
        options = {
            "@type": "dss.dto.service.CustomDSSServiceExecutionOptions",
            "parameters": parameters
        }
        request = {
            "method": "executeCustomDSSService",
            "params": [
                self._openbis.token,
                service_id,
                options
            ],
        }
        full_url = urljoin(self._openbis._get_dss_url(), self._openbis.dss_v3)
        return self._openbis._post_request_full_url(full_url, request)

    def make_preview(self, perm_id: str, index: int, preview: ImagingDataSetPreview) -> ImagingDataSetPreview:
        parameters = {
            "type": "preview",
            "permId": perm_id,
            "index": index,
            "error": None,
            "preview": preview.__dict__
        }
        service_response = self._execute_custom_dss_service(parameters)
        if service_response['error'] is None:
            preview.__dict__ = service_response["preview"]
            return preview
        else:
            raise ValueError(service_response['error'])

    def get_export_url(self, perm_id: str, export: ImagingDataSetExport, image_index: int = 0) -> str:
        parameters = {
            "type": "export",
            "permId": perm_id,
            "index": image_index,
            "error": None,
            "url": None,
            "export": export.__dict__
        }
        service_response = self._execute_custom_dss_service(parameters)
        if service_response['error'] is None:
            return service_response['url']
        else:
            raise ValueError(service_response['error'])

    def get_multi_export_url(self, exports: list[ImagingDataSetMultiExport]) -> str:
        parameters = {
            "type": "multi-export",
            "error": None,
            "url": None,
            "exports": [export.__dict__ for export in exports]
        }
        service_response = self._execute_custom_dss_service(parameters)
        if service_response['error'] is None:
            return service_response['url']
        else:
            raise ValueError(service_response['error'])

    def single_export_download(self, perm_id: str, export: ImagingDataSetExport, image_index: int = 0, directory_path=""):
        export_url = self.get_export_url(perm_id, export, image_index)
        self._download(export_url, directory_path)

    def multi_export_download(self, exports: list[ImagingDataSetMultiExport], directory_path=""):
        export_url = self.get_multi_export_url(exports)
        self._download(export_url, directory_path)

    def _download(self, url, directory_path=""):
        get_response = requests.get(url, stream=True)
        file_name = url.split("/")[-1]
        with open(os.path.join(directory_path, file_name), 'wb') as f:
            for chunk in get_response.iter_content(chunk_size=1024):
                if chunk:
                    f.write(chunk)

    def get_property_config(self, perm_id: str) -> ImagingDataSetPropertyConfig:
        dataset = self._openbis.get_dataset(perm_id)
        imaging_property = json.loads(dataset.props[IMAGING_CONFIG_PROP_NAME])
        return ImagingDataSetPropertyConfig.from_dict(imaging_property)

    def update_property_config(self, perm_id: str, config: ImagingDataSetPropertyConfig):
        dataset = self._openbis.get_dataset(perm_id)
        dataset.props[IMAGING_CONFIG_PROP_NAME] = config.to_json()
        dataset.save()


# o = get_instance()
#
#
# # imaging_preview = ImagingDataSetPreview(preview_format="png", config=config_sxm)
# # response = get_preview('20231110130838616-26', 0, imaging_preview)
# # print(response)
#
# config_export = {
#     "include": ['image', 'raw data'],
#     "image-format": 'original',
#     "archive-format": "zip",
#     "resolution": "original"
# }
# # imaging_export = ImagingDataSetExport(config_export)
# # export_response = get_export('20231110130838616-26', 0, imaging_export)
# # print(export_response)
#
#
# # imaging_export1 = ImagingDataSetMultiExport('20231110130838616-26', 0, config_export)
# # imaging_export2 = ImagingDataSetMultiExport('20231110134813653-27', 0, config_export)
# # multi_export_response = get_multi_export([imaging_export1, imaging_export2])
# # print(multi_export_response)
#
#
# # imaging_property_config = get_property_config('20231110134813653-27')
# # print(imaging_property_config.to_json())
#
# ic = ImagingControl(o)
# perm_id = '20231110130838616-26'
# pc = ic.get_property_config(perm_id)
#
#
#
# config_sxm_preview = {
#     "channel": "z", # usually one of these: ['z', 'I', 'dIdV', 'dIdV_Y']
#     "x-axis": [1.2, 3.0], # file dependent
#     "y-axis": [1.2, 3.0], # file dependent
#     "color-scale": [-700.0, 700.0], # file dependend
#     "colormap": "gray", # [gray, YlOrBr, viridis, cividis, inferno, rainbow, Spectral, RdBu, RdGy]
#     "scaling": "linear", # ['linear', 'logarithmic']
#     # "mode": 3 # uncomment this if you want to generate random pixel image generation
# }
#
# # imaging_preview = ImagingDataSetPreview(preview_format="png", config=config_sxm_preview)
# #
# # preview = ic.make_preview(perm_id, 0, imaging_preview)
# # pc.images[0].add_preview(preview)
# # ic.update_property_config(perm_id, pc)
# #
# # print(ic.get_property_config(perm_id))
#
#
#
# config_export = {
#     "include": ['image', 'raw data'],
#     "image-format": 'original',
#     "archive-format": "zip",
#     "resolution": "original"
# }
# imaging_export = ImagingDataSetExport(config_export)
# ic.single_export_download(perm_id, imaging_export, 0, '/home/alaskowski/PREMISE')





