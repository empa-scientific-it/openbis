/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function HistoryView(controller, model) {
    this._model = model
    this._controller = controller
    this._container = $("<div>")
    this._dataGrid

    this.repaint = function (views) {
        var _this = this

        var $containerColumn = $("<form>", {
            role: "form",
            action: "javascript:void(0);",
            onsubmit: "",
        })
        $containerColumn.append(this._container)
        views.content.append($containerColumn)

        views.header.append($("<h1>").append("History of " + Util.getDisplayNameForEntity(this._model.entity)))

        this._showHistory()
    }

    this._showHistory = function () {
        var _this = this

        var columns = []

        columns.push(
            {
                label: "Author",
                property: "author",
                showByDefault: true,
            },
            {
                label: "Type",
                property: "type",
                showByDefault: true,
            },
            {
                label: "Property Name",
                property: "propertyName",
                showByDefault: true,
            },
            {
                label: "Property Value",
                property: "propertyValue",
                showByDefault: true,
            },
            {
                label: "Relation Type",
                property: "relationType",
                showByDefault: true,
            },
            {
                label: "Related Object Id",
                property: "relatedObjectId",
                showByDefault: true,
            }
        )

        if (this._model.entity["@type"] === "as.dto.dataset.DataSet") {
            columns.push(
                {
                    label: "External Code",
                    property: "externalCode",
                    showByDefault: true,
                },
                {
                    label: "Path",
                    property: "path",
                    showByDefault: true,
                },
                {
                    label: "Git Commit Hash",
                    property: "gitCommitHash",
                },
                {
                    label: "Git Repository Id",
                    property: "gitRepositoryId",
                },
                {
                    label: "External DMS Id",
                    property: "externalDmsId",
                },
                {
                    label: "External DMS Code",
                    property: "externalDmsCode",
                },
                {
                    label: "External DMS Label",
                    property: "externalDmsLabel",
                },
                {
                    label: "External DMS Address",
                    property: "externalDmsAddress",
                }
            )
        }

        columns.push(
            {
                label: "Valid From",
                property: "validFrom",
                showByDefault: true,
            },
            {
                label: "Valid To",
                property: "validTo",
                showByDefault: true,
            }
        )

        var getDataList = function (callback) {
            var data = _this._model.getData()
            callback(data)
        }

        this._dataGrid = new DataGridController(
            null,
            columns,
            [],
            null,
            getDataList,
            null,
            false,
            this._model.entity["@type"] + "_HISTORY",
            false
        )
        this._dataGrid.init(this._container)
        this._container.prepend($("<legend>").append("History"))
    }
}
