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
                getValue: function (params) {
                    return params.row.changes.author
                },
                showByDefault: true,
            },
            {
                label: "Changes",
                property: "changes",
                getValue: function (params) {
                    $element = _this._renderChanges(params.row)
                    return $element ? $element.text() : null
                },
                render: this._renderChanges,
                showByDefault: true,
            },
            {
                label: "Full Document",
                property: "fullDocument",
                getValue: function (params) {
                    $element = _this._renderFullDocument(params.row)
                    return $element ? $element.text() : null
                },
                render: this._renderFullDocument,
                showByDefault: true,
                sortable: false,
            },
            {
                label: "Timestamp",
                property: "timestamp",
                getValue: function (params) {
                    var timestamp = params.row.changes.timestamp
                    return Util.getFormatedDate(new Date(timestamp))
                },
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
            true
        )

        this._dataGrid.init(this._container, [
            {
                name: "Compare",
                action: function (selected) {
                    if (selected.length !== 2) {
                        alert("Please select 2 versions to compare")
                        return
                    }
                },
            },
        ])

        this._container.prepend($("<legend>").append("History"))
    }

    this._renderChanges = function (row) {
        var $container = $("<div>")

        var relations = row.changes.relations
        if (!_.isEmpty(relations)) {
            var $relations = $("<ul>")
            Object.keys(relations)
                .sort(function (r1, r2) {
                    var sortings = {
                        SPACE: 1,
                        PROJECT: 2,
                        EXPERIMENT: 3,
                        SAMPLE: 4,
                        DATA_SET: 5,
                        PARENT: 6,
                        CHILD: 7,
                        CONTAINER: 8,
                        COMPONENT: 9,
                    }
                    return sortings[r1] - sortings[r2]
                })
                .forEach(function (relationType) {
                    var relation = relations[relationType]
                    if (!_.isEmpty(relation.removed)) {
                        $("<li>")
                            .text(relationType + " removed: " + JSON.stringify(relation.removed))
                            .appendTo($relations)
                    }
                    if (!_.isEmpty(relation.added)) {
                        $("<li>")
                            .text(relationType + " added: " + JSON.stringify(relation.added))
                            .appendTo($relations)
                    }
                    if (relation.set !== undefined) {
                        $("<li>")
                            .text(relationType + ": " + relation.set)
                            .appendTo($relations)
                    }
                })
            $container.append("Relations:").append($relations)
        }

        var properties = row.changes.properties
        if (!_.isEmpty(properties)) {
            var $properties = $("<ul>")
            Object.keys(properties)
                .sort()
                .forEach(function (propertyName) {
                    var $property = $("<li>").text(propertyName + ": " + properties[propertyName])
                    $properties.append($property)
                })
            $container.append("Properties:").append($properties)
        }

        return $container
    }

    this._renderFullDocument = function (row) {
        var visible = false

        var $json = $("<pre>")
        $json.text(JSON.stringify(row.fullDocument, null, 4))
        $json.hide()

        var $showHide = $("<a>").text("show")
        $showHide.click(function () {
            if (visible) {
                $showHide.text("show")
                $json.hide(500)
            } else {
                $showHide.text("hide")
                $json.show(500)
            }
            visible = !visible
        })

        return $("<div>").append($showHide).append($json)
    }
}
