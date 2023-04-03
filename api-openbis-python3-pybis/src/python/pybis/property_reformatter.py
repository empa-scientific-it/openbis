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
from datetime import datetime

import pandas as pd


def is_of_openbis_supported_date_format(value):
    is_supported = False
    for date_format in PropertyReformatter.SUPPORTED_DATETIME_FORMATS:
        try:
            datetime.strptime(value, date_format)
            is_supported = True
        except ValueError:
            pass
    return is_supported


class PropertyReformatter:
    """Helper class for reformatting of properties, is needed"""
    LONG_DATETIME_FORMAT = "%Y-%m-%d %H:%M:%S"

    SUPPORTED_DATETIME_FORMATS = ["%Y-%m-%d", "%y-%m-%d",  # ShortDateFormat
                                  "%Y-%m-%d %H:%M", "%y-%m-%d %H:%M",  # NormalDateFormat
                                  "%Y-%m-%d %H:%M:%S", "%y-%m-%d %H:%M:%S"]  # LongDateFormat

    def __init__(self, openbis_obj):
        self.openbis = openbis_obj

    def format(self, properties):
        if properties is None:
            raise ValueError('properties can not be None!')

        for key, value in properties.items():
            property_type = self.openbis.get_property_type(key)
            if property_type.dataType == 'TIMESTAMP':
                properties[key] = self._format_timestamp(value)
                
        return properties

    def _format_timestamp(self, value):
        if is_of_openbis_supported_date_format(value):
            return value
        timestamp = pd.to_datetime(value)
        result = timestamp.strftime(PropertyReformatter.LONG_DATETIME_FORMAT)
        print(
            f'WARNING: "{value}" is not of any OpenBis supported datetime formats. Reformatting to "{result}"')
        return result
