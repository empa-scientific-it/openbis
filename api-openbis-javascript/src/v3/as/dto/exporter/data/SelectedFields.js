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

define(["stjs", "as/dto/exporter/data/IExportableFields"], function (stjs, IExportableFields) {
  var SelectedFields = function() {
  }

  stjs.extend(
    SelectedFields,
    IExportableFields,
    [IExportableFields],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.exporter.data.SelectedFields";

      constructor.serialVersionUID = 1;
      prototype.attributes = null;
      prototype.properties = null;

      prototype.getAttributes = function() {
        return this.attributes;
      };

      prototype.setAttributes = function(attributes) {
        this.attributes = attributes;
      };

      prototype.getProperties = function() {
        return this.properties;
      };

      prototype.setProperties = function(properties) {
        this.properties = properties;
      };
    },
    {
      attributes: {
        name: "List",
        arguments: ["Attribute"]
      },
      properties: {
        name : "List",
        arguments : ["PropertyTypePermId"]
      }
    }
  );

  return SelectedFields;
});