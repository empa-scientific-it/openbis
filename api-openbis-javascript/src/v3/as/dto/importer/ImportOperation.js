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

define(["stjs", "as/dto/common/operation/IOperation"],
  function (stjs, IOperation) {
    var ImportOperation = function(importData, importOptions) {
      this.importData = importData;
      this.importOptions = importOptions;
    }

    stjs.extend(
      ImportOperation,
      IOperation,
      [IOperation],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.ImportOperation";

        constructor.serialVersionUID = 1;
        prototype.importData = null;
        prototype.importOptions = null;

        prototype.getMessage = function() {
          return "ImportOperation";
        };

        prototype.getImportData = function() {
          return this.importData;
        };

        prototype.getImportOptions = function() {
          return this.importOptions;
        };
      },
      {
        importData: "IImportData",
        importOptions: "ImportOptions"
      }
    );

    return ImportOperation;
  });