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

define(["stjs", "as/dto/common/operation/IOperationResult"],
  function (stjs, IOperationResult) {
    var ExportOperationResult = function(exportResult) {
      this.exportResult = exportResult;
    }

    stjs.extend(
      ExportOperationResult,
      IOperationResult,
      [IOperationResult],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.exporter.ExportOperationResult";

        constructor.serialVersionUID = 1;

        prototype.exportResult = null;

        prototype.getMessage = function() {
          return "ExportOperationResult";
        };

        prototype.getExportResult = function() {
          return this.exportResult;
        }
      },
      {}
    );

    return ExportOperationResult;
  });