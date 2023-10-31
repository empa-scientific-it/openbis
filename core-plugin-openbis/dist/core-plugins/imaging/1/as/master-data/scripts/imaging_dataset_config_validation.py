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


def get_rendered_property(entity, property):
    value = entity.property(property)
    if value is not None:
        return value.renderedValue()


def validate(entity, is_new):
    imaging_dataset_config = get_rendered_property(entity, "$IMAGING_DATA_CONFIG")
    if imaging_dataset_config is None or imaging_dataset_config == "":
        return "Imaging dataset config can not be empty!"
    else:
        # TODO add deserialization and validation of particular fields
        pass
