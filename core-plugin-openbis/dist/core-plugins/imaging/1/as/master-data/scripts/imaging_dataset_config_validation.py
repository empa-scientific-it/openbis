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

import json


def assert_control(control_config):
    if not control_config:
        return True, ''
    obligatory_tags = ['@type', 'label', 'type']
    for tag in obligatory_tags:
        if tag not in control_config:
            return False, tag+': is missing!'
        if control_config[tag] is None:
            return False, tag+': can not be empty!'

    list_tags = ['values', 'range', 'speeds', 'visibility']
    for list_tag in list_tags:
        if list_tag in control_config and control_config[list_tag] is not None and not isinstance(control_config[list_tag], list):
            return False, list_tag+': must be a list or null!'

    boolean_tags = ['playable', 'multiselect']
    for boolean_tag in boolean_tags:
        if boolean_tag in control_config and control_config[boolean_tag] is not None and not isinstance(control_config[boolean_tag], bool):
            return False, boolean_tag+': must be a boolean or empty!'

    if 'visibility' in control_config and control_config['visibility'] is not None:
        visibility = control_config['visibility']
        for vis in visibility:
            tags = ['label', 'values']
            all_tags = tags + ['range', 'unit']
            for tag in ['label', 'values']:
                if tag not in vis:
                    return False, 'visibility->'+tag+': is missing!'
                if vis[tag] is None:
                    return False, 'visibility->'+tag+': can not be empty!'

            for tag in ['values', 'range']:
                if tag in vis and vis[tag] is not None and not isinstance(vis[tag], list):
                    return False, 'visibility->'+tag+': must be a list!'

    if 'metadata' in control_config and control_config['metadata'] is not None and not isinstance(control_config['metadata'], dict):
        return False, '->metadata: must be a dictionary or null!'

    return True, ''


def assert_config(json_config):
    if 'config' in json_config:
        config = json_config['config']
        obligatory_tags = ['@type', 'adaptor', 'version', 'playable', 'exports', 'inputs']
        for tag in obligatory_tags:
            if tag not in config:
                return False, 'config->' + tag + ': is missing!'
            if config[tag] is None:
                return False, 'config->' + tag + ': can not be empty!'

        if config['adaptor'].strip() == '':
            return False, 'config->adaptor: can not be blank!'

        list_tags = ['speeds', 'resolutions', 'exports', 'inputs']
        for list_tag in list_tags:
            if list_tag in config and config[list_tag] is not None and not isinstance(config[list_tag], list):
                return False, '\''+list_tag+'\' must be a list or null!'

        if not isinstance(config['playable'], bool):
            return False, 'config->playable: must be a boolean!'

        for control in config['exports']:
            result, err = assert_control(control)
            if not result:
                return result, 'config->exports->' + err

        for control in config['inputs']:
            result, err = assert_control(control)
            if not result:
                return result, 'config->inputs->' + err

        if 'metadata' in config and config['metadata'] is not None and not isinstance(config['metadata'], dict):
            return False, 'config->metadata: must be a dictionary or null!'
    else:
        return False, 'Missing \'config\' tag in configuration!'
    return True, ''


def assert_preview(preview_config):
    obligatory_tags = ['@type', 'format', 'show']
    for tag in obligatory_tags:
        if tag not in config:
            return False, tag + ': is missing!'
        if config[tag] is None:
            return False, tag + ': can not be empty!'

    if not isinstance(preview_config['show'], bool):
        return False, 'show: must be boolean!'

    if not isinstance(preview_config['format'], str):
        return False, 'format: must be string!'

    if 'bytes' in preview_config and preview_config['bytes'] is not None and not isinstance(preview_config['bytes'], str):
        return False, 'bytes: must be a base64 encoded string or null!'

    if 'metadata' in preview_config and preview_config['metadata'] is not None and not isinstance(preview_config['metadata'], dict):
        return False, 'metadata: must be a dictionary or null!'

    if 'config' in preview_config and preview_config['config'] is not None and not isinstance(preview_config['config'], dict):
        return False, 'config: must be a dictionary or null!'

    return True, ''


def assert_images(json_config):
    if 'images' in json_config:
        images = json_config['images']
        if images is None:
            return False, '\'images\' tag can not be null!'
        if not isinstance(images, list):
            return False, '\'images\' tag must be a list!'

        for image in images:
            obligatory_tags = ['@type']
            for tag in obligatory_tags:
                if tag not in image:
                    return False, 'images->' + tag + ': missing tag!'
                if image[tag] is None:
                    return False, 'images->' + tag + ': can not be empty!'

            if 'metadata' in image and image['metadata'] is not None and not isinstance(image['metadata'], dict):
                return False, 'images->metadata: must be a dictionary or null!'

            if 'previews' in image and image['previews'] is not None and not isinstance(image['previews'], list):
                return False, 'images->previews: must be a list or null!'

            for preview in image['previews']:
                res, err = assert_preview(preview)
                if not res:
                    return result, 'images->previews->' + err

    else:
        return False, 'Missing \'images\' tag in configuration!'
    return True, ''


def get_rendered_property(entity, property):
    properties = entity.externalDataPE().getProperties()
    for prop in properties:
        etpt = prop.getEntityTypePropertyType()
        pt = etpt.getPropertyType()
        code = pt.getCode()
        if code == property:
            return prop.tryGetUntypedValue()
    return None


def validate(entity, is_new):
    imaging_dataset_config = get_rendered_property(entity, "$IMAGING_DATA_CONFIG")
    if imaging_dataset_config is None or imaging_dataset_config == "":
        return "Imaging dataset config can not be empty!"
    elif "test_validation_failure" in imaging_dataset_config:
        return "Imaging dataset config validation failure!"
    else:

        try:
            config = json.loads(imaging_dataset_config)
        except Exception as e:
            return "Could not parse JSON: " + e

        result, err = assert_config(config)
        if not result:
            return err
