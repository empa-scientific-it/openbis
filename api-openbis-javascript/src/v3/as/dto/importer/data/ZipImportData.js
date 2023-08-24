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

define(["stjs", "as/dto/importer/data/IImportData"],
  function (stjs, IImportData) {
    var ZipImportData = function() {
    }

    stjs.extend(
      ZipImportData,
      IImportData,
      [IImportData],
      function (constructor, prototype) {
        prototype["@type"] = "as.dto.importer.data.ZipImportData";

        constructor.serialVersionUID = 1;
        prototype.format = null;
        prototype.file = null;

        prototype.getFormat = function() {
          return this.format;
        };

        prototype.setFormat = function(format) {
          this.format = format;
        };

        prototype.getFile = function() {
          return this.file;
        };

        prototype.setFile = function(file) {
          this.file = file;
        };
      },
      {
        format: "ImportFormat",
        file: "byte[]"
      }
    );

    return ZipImportData;
  });