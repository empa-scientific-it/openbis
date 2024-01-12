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
  var ExportablePermId = function(exportableKind, permId) {
    this.exportableKind = exportableKind;
    this.permId = permId;
  }

  stjs.extend(
    ExportablePermId,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.exporter.data.ExportablePermId";

      constructor.serialVersionUID = 1;
      prototype.exportableKind = null;
      prototype.permId = null;

      prototype.getExportableKind = function() {
        return this.exportableKind;
      };

      prototype.setExportableKind = function(exportableKind) {
        this.exportableKind = exportableKind;
      };

      prototype.getPermId = function() {
        return this.permId;
      };

      prototype.setPermId = function(permId) {
        this.permId = permId;
      };

      prototype.hashCode = function() {
        return ((this.getPermId() == null) ? 0 : this.getPermId().hashCode());
      };

      prototype.equals = function(obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
          return false;
        }
        var that = obj;

        if (this.exportableKind != that.exportableKind)
        {
            return false;
        }
        return this.permId.equals(that.permId);
      };
    },
    {
      exportableKind: "ExportableKind",
      permId: "ObjectPermId"
    }
  );

  return ExportablePermId;
});