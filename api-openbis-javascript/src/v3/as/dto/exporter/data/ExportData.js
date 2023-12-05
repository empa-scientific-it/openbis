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

define(["stjs"], function (stjs) {
  var ExportData = function(permIds, fields) {
    this.permIds = permIds;
    this.fields = fields;
  }

  stjs.extend(
    ExportData,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.exporter.data.ExportData";

      constructor.serialVersionUID = 1;
      prototype.permIds = null;
      prototype.fields = null;

      prototype.getPermIds = function() {
        return this.permIds;
      };

      prototype.setPermIds = function(permIds) {
        this.permIds = permIds;
      };

      prototype.getFields = function() {
        return this.fields;
      };

      prototype.setFields = function(fields) {
        this.fields = fields;
      };
    },
    {
      permIds: {
        name: "List",
        arguments: ["ExportablePermId"]
      },
      fields: "IExportableFields"
    }
  );

  return ExportData;
});