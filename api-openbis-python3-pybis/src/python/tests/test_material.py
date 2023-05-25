#   Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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


def create_array_properties(openbis, code_prefix):
    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_INTEGER',
        label='integer array',
        description='integer array property',
        dataType='ARRAY_INTEGER',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_REAL',
        label='real array',
        description='real array property',
        dataType='ARRAY_REAL',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_STRING',
        label='string array',
        description='string array property',
        dataType='ARRAY_STRING',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_TIMESTAMP',
        label='timestamp array',
        description='timestamp array property',
        dataType='ARRAY_TIMESTAMP',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_JSON',
        label='json',
        description='json type property',
        dataType='JSON',
    )
    pt.save()


def test_material_array_properties_disabled(openbis_instance):

    create_array_properties(openbis_instance, "MATERIAL")

    material_code = 'TEST_ARRAY_METARIAL'
    material_type = openbis_instance.new_material_type(
        material_code,
        description=None,
        validationPlugin=None,
    )
    material_type.save()

    material_type.assign_property('$NAME')

    try:
        material_type.assign_property('MATERIAL_ARRAY_INTEGER')
    except ValueError as error:
        assert str(error).startswith('Wrong property type')

    try:
        material_type.assign_property('MATERIAL_ARRAY_REAL')
    except ValueError as error:
        assert str(error).startswith('Wrong property type')

    try:
        material_type.assign_property('MATERIAL_ARRAY_STRING')
    except ValueError as error:
        assert str(error).startswith('Wrong property type')

    try:
        material_type.assign_property('MATERIAL_ARRAY_TIMESTAMP')
    except ValueError as error:
        assert str(error).startswith('Wrong property type')

    try:
        material_type.assign_property('MATERIAL_JSON')
    except ValueError as error:
        assert str(error).startswith('Wrong property type')
