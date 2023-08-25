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
  var ExportResult = function () {
  };

  stjs.extend(
    ExportResult,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.exporter.ExportResult";

      constructor.serialVersionUID = 1;
      prototype.downloadURL = null;
      prototype.warnings = null;

      prototype.getDownloadURL = function() {
        return this.downloadURL;
      };

      prototype.setDownloadURL = function(downloadURL) {
        this.downloadURL = downloadURL;
      };

      prototype.getWarnings = function() {
        return this.warnings;
      };

      prototype.setWarnings = function(warnings) {
        this.warnings = warnings;
      };
    },
    {
      downloadURL: "String",
      warnings: {
        name: "Collection",
        arguments: ["String"]
      }
    }
  );
  return ExportResult;
});