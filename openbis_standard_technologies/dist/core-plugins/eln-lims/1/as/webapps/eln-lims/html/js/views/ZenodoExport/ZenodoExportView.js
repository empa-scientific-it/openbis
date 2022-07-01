/*
 * Copyright 2011 ETH Zuerich, CISD
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

function ZenodoExportView(exportController, exportModel) {
    this.repaint = function(views) {
        var $header = views.header;
        var $container = views.content;

        var $form = $("<div>");
        var $formColumn = $("<form>", {
            'name': 'zenodoExportForm',
            'role': 'form',
            'action': 'javascript:void(0);',
            'onsubmit': 'mainController.currentView.exportSelected();'
        });
        $form.append($formColumn);

        var $infoBox1 = FormUtil.getInfoBox('You can select any parts of the accessible openBIS structure to export:', [
            'If you select a tree node and do not expand it, everything below this node will be exported by default.',
            'To export selectively only parts of a tree, open the nodes and select what to export.'
        ]);
        $infoBox1.css('border', 'none');
        $container.append($infoBox1);

        var $infoBox2 = FormUtil.getInfoBox('Publication time constraint', [
            'After the resource has been exported it should be published in Zenodo UI within 2 hours.',
            'Otherwise, the publication metadata will not be registered in openBIS.'
        ]);
        $infoBox2.css('border', 'none');
        $container.append($infoBox2);

        var $tree = $('<div>', { 'id' : 'exportsTree' });
        $formColumn.append($('<br>'));
        $formColumn.append(FormUtil.getBox().append($tree));

        $container.append($form);

        exportModel.tree = TreeUtil.getCompleteTree($tree);

        var $formTitle = $('<h2>').append('Zenodo Export Builder');
        $header.append($formTitle);

        this.paintTitleTextBox($container);
        this.paintGroupCheckboxes($container);

        exportModel.tableModel = this._tableModel;

        var $exportButton = $('<input>', { 'type': 'submit', 'class': 'btn btn-primary', 'value': 'Export Selected',
            'onClick': '$("form[name=\'zenodoExportForm\']").submit()'});
        $header.append($exportButton);
    };

    this.paintTitleTextBox = function ($container) {
        this.$titleTextBox = FormUtil.getTextInputField('zenodo-submission-title', 'Submission title', true);
        var titleTextBoxFormGroup = FormUtil.getFieldForComponentWithLabel(this.$titleTextBox, 'Submission Title', null, true);
        titleTextBoxFormGroup.css('width', '50%');
        $container.append(titleTextBoxFormGroup);
    };

    this.paintGroupCheckboxes = function ($container) {
        var fieldset = this._getFieldset($container);
        var titleLabel = $("<label>", {"type": "label", "id": "zenodo-groups", "class": "control-label"})
            .text("Groups (*)");
        fieldset.append(titleLabel);
        this._tableModel = this._getTableModel();
        fieldset.append(this._getTable(this._tableModel));
    }

    this._getTableModel = function() {
        var tableModel = this._getBaseTableModel();
        tableModel.fullWidth = false;
        // define columns
        tableModel.columns = [{ label : "Group"}, { label : "included"}];
        tableModel.rowBuilders = {
            "Group" : function(rowData) {
                return $("<span>").text(rowData.label);
            },
            "included" : function(rowData) {
                var $checkbox = $("<input>", { type : "checkbox", name : "cb" });
                if (rowData.enabled) {
                    $checkbox.attr("checked", true);
                }
                return $checkbox;
            }
        };

        // add data
        var groups = SettingsManagerUtils.getGroups();
        if (groups.length > 1) {
            for (var i = 0; i < groups.length; i++) {
                var group = groups[i];
                tableModel.addRow({
                    label: group,
                    enabled: false
                });
            }
        }
        return tableModel;
    }

    this._getBaseTableModel = function() {
        var tableModel = {};
        tableModel.columns = []; // array of elements with label and optional width
        tableModel.rowBuilders = {}; // key (column name); value (function to build widget)
        tableModel.rows = []; // array of maps with key (column name); value (widget)
        tableModel.fullWidth = true; // table is drawn using the full width if true
        tableModel.valuesTransformer = function(values) { return values }; // optional transformer
        tableModel.getValues = (function() {
            var values = [];
            for (var i of Object.keys(tableModel.rows)) {
                var row = tableModel.rows[i];
                var rowValues = {};
                for (var column of tableModel.columns) {
                    var $widget = row[column.label];
                    if ($widget.is("span")) {
                        rowValues[column.label] = $widget.text();
                    } else if ($widget.is("input") && $widget.attr("type") === "checkbox") {
                        rowValues[column.label] = $widget.is(":checked");
                    }
                }
                values.push(rowValues);
            }
            return tableModel.valuesTransformer(values);
        }).bind(this);
        tableModel.addRow = function(rowData) {
            var rowWidgets = {};
            for (var column of tableModel.columns) {
                var rowBuilder = tableModel.rowBuilders[column.label];
                rowWidgets[column.label] = rowBuilder(rowData);
            }
            tableModel.rows.push(rowWidgets);
            return rowWidgets;
        };
        return tableModel;
    }

    this._getFieldset = function($container) {
        var $fieldsetOwner = $("<div>");
        var $fieldset = $("<div>");
        $fieldsetOwner.append($fieldset);
        $container.append($fieldsetOwner);
        return $fieldset;
    }

    this._getTable = function(tableModel) {
        var $table = $("<table>", { class : "table borderless table-compact" });
        if (tableModel.fullWidth != true) {
            $table.css("width", "initial");
        }
        // head
        var $thead = $("<thead>");
        var $trHead = $("<tr>");
        if (tableModel.rowExtraBuilder) {
            $trHead.append($("<th>").css("width", "30px"));
        }
        for (var column of tableModel.columns) {
            var $th = $("<th>").css("vertical-align", "middle").text(column.label);
            if (column.width) {
                $th.css("width", column.width);
            }
            $trHead.append($th);
        }
        $thead.append($trHead);
        $table.append($thead);
        // body
        var $tbody = $("<tbody>");
        for (var i of Object.keys(tableModel.rows)) {
            var row = tableModel.rows[i];
            this._addRow($tbody, tableModel, row);
        }
        $table.append($tbody);
        return $table
    }

    this._addRow = function($tbody, tableModel, tableModelRow) {
        var $tr = $("<tr>");
        $tbody.append($tr);

        for (var column of tableModel.columns) {
            var $td = $("<td>");
            $tr.append($td);
            var $widget = tableModelRow[column.label];
            $td.append($widget);
        }
    }

}