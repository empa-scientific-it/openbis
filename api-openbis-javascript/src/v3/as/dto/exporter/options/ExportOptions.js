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
  var SelectedFields = function(formats, xlsTextFormat, withReferredTypes, withImportCompatibility) {
    this.formats = formats;
    this.xlsTextFormat = xlsTextFormat;
    this.withReferredTypes = withReferredTypes;
    this.withImportCompatibility = withImportCompatibility;
  }

  stjs.extend(
    SelectedFields,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.exporter.options.SelectedFields";

      constructor.serialVersionUID = 1;
      prototype.formats = null;
      prototype.xlsTextFormat = null;
      prototype.withReferredTypes = null;
      prototype.withImportCompatibility = null;

      prototype.getFormats = function() {
        return this.formats;
      };

      prototype.getXlsTextFormat = function() {
        return this.xlsTextFormat;
      };

      prototype.isWithReferredTypes = function() {
        return this.withReferredTypes;
      };

      prototype.isWithImportCompatibility = function() {
        return this.withImportCompatibility;
      };
    },
    {}
  );

  return SelectedFields;
});