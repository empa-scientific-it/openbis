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

import unittest
import imaging.imaging as imaging
from unittest.mock import MagicMock
import json

IMAGING_CONFIG_PROP_NAME = "$IMAGING_DATA_CONFIG".lower()

PROPER_CONFIG = {
    "config": {
        "adaptor": "some.adaptor.class.MyAdaptorClass",
        "version": 1.0,
        "speeds": [1, 2, 5],
        "resolutions": ["150dpi", "300dpi"],
        "playable": True,
        "exports": [{
            "label": "Archive",
            "type": "Dropdown",
            "values": ["zip", "tar.gz"],
            "multiselect": False,
            "metaData": {}
        }],
        "inputs": [{
            "label": "Option 1",
            "section": "section",
            "type": "Dropdown",
            "values": ["a", "b", "c"],
            "multiselect": True,
            "playable": False,
            "metaData": {}
        }, {
            "label": "Option 2",
            "section": "section",
            "type": "Slider",
            "unit": "cm",
            "range": ["1", "2", "0.1"],
            "multiselect": False,
            "playable": True,
            "speeds": [1, 2, 5],
            "metaData": {}
        }, {
            "label": "Option 2",
            "section": "section",
            "type": "Range",
            "multiselect": False,
            "playable": False,
            "visibility": [{
                "label": "Option 1",
                "values": ["a", "b"],
                "range": ["1", "2", "0.1"],
                "unit": "mm"
            }, {
                "label": "Option 1",
                "values": ["c"],
                "range": ["0", "100", "1"],
                "unit": "px"
            }],
            "metaData": {}
        }],
        "metadata": {}
    },
    "images": [{
        "previews": [{
            "config": {},
            "format": "png",
            "bytes": "base64_encoded_bytes",
            "show": False,
            "metadata": {}
        }],
        "config": {},
        "metadata": {}
    }]
}


class ImagingTestCase(unittest.TestCase):

    def setUp(self):
        self.dataset_mock = MagicMock()
        self.set_dataset_config(PROPER_CONFIG)

        self.openbis_mock = MagicMock()
        self.openbis_mock.get_dataset.return_value = self.dataset_mock

        self.imaging_control = imaging.ImagingControl(self.openbis_mock)

    def set_dataset_config(self, config):
        json_config = json.dumps(config)
        self.dataset_mock.props = {IMAGING_CONFIG_PROP_NAME: json_config}

    def test_get_property_config(self):
        self.imaging_control.get_property_config('some_perm_id')







if __name__ == '__main__':
    unittest.main()
