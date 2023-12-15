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

from ch.ethz.sis.openbis.generic.imagingapi.v3.dto import ImagingDataSetPropertyConfig
from java.io import ByteArrayInputStream
from com.fasterxml.jackson.databind.exc import UnrecognizedPropertyException
from ch.ethz.sis.openbis.generic.server.sharedapi.v3.json import GenericObjectMapper

def get_rendered_property(entity, property):
    properties = entity.externalDataPE().getProperties()
    for prop in properties:
        etpt = prop.getEntityTypePropertyType()
        pt = etpt.getPropertyType()
        code = pt.getCode()
        if code == property:
            return prop.tryGetUntypedValue()
    return None


def get_object_mapper():
    return GenericObjectMapper()


def validate_config(imagingDataSetConfig):
    assert imagingDataSetConfig.getAdaptor() is not None, "config->adaptor must not be null!"


def validate_preview(imagingDataSetPreview):
    assert imagingDataSetPreview.getFormat() is not None, "images->previews->format must not be null!"


def validate_image(imagingDataSetImage):
    assert imagingDataSetImage.getPreviews() is not None, "images->previews must not be null!"
    assert imagingDataSetImage.getPreviews().size() > 0, "images->previews must habe at least one preview!"

    for preview in imagingDataSetImage.getPreviews():
        validate_preview(preview)


def validate_property_config(imagingDataSetPropertyConfig):
    assert imagingDataSetPropertyConfig.getConfig() is not None, "Config must not be null!"
    assert imagingDataSetPropertyConfig.getImages() is not None, "Images must not be null!"

    validate_config(imagingDataSetPropertyConfig.getConfig())

    for image in imagingDataSetPropertyConfig.getImages():
        validate_image(image)


def validate(entity, is_new):
    imaging_dataset_config = get_rendered_property(entity, "$IMAGING_DATA_CONFIG")
    if imaging_dataset_config is None or imaging_dataset_config == "":
        return "Imaging dataset config can not be empty!"
    elif "test_validation_failure" in imaging_dataset_config:
        return "Imaging dataset config validation failure!"
    else:
        try:
            mapper = get_object_mapper()
            byte_array = imaging_dataset_config.encode('utf-8')
            bais = ByteArrayInputStream(byte_array)
            config_obj = mapper.readValue(bais, ImagingDataSetPropertyConfig)
            validate_property_config(config_obj)
        except UnrecognizedPropertyException as upe:
            return upe
        except Exception as e:
            return e
