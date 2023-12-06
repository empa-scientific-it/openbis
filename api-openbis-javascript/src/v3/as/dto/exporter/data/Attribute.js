/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

define(["stjs", "as/dto/common/Enum"], function (stjs, Enum) {
  var Attribute = function() {
    Enum.call(this, [
      "ARCHIVING_STATUS",
      "AUTO_GENERATE_CODES",
      "AUTO_GENERATE_CODE",
      "CHILDREN",
      "CODE",
      "DESCRIPTION",
      "DISALLOW_DELETION",
      "EXPERIMENT",
      "GENERATE_CODES",
      "GENERATED_CODE_PREFIX",
      "IDENTIFIER",
      "LABEL",
      "MAIN_DATA_SET_PATH",
      "MAIN_DATA_SET_PATTERN",
      "MODIFICATION_DATE",
      "MODIFIER",
      "ONTOLOGY_ID",
      "ONTOLOGY_VERSION",
      "ONTOLOGY_ANNOTATION_ID",
      "PARENTS",
      "PERM_ID",
      "PRESENT_IN_ARCHIVE",
      "PROJECT",
      "REGISTRATION_DATE",
      "REGISTRATOR",
      "SIZE",
      "SAMPLE",
      "SPACE",
      "STORAGE_CONFIRMATION",
      "UNIQUE_SUBCODES",
      "URL_TEMPLATE",
      "VALIDATION_SCRIPT",
      "VERSION"
    ]);
  }

  stjs.extend(
    Attribute,
    Enum,
    [Enum],
    function (constructor, prototype) {
    },
    {}
  );

  return new Attribute();
});