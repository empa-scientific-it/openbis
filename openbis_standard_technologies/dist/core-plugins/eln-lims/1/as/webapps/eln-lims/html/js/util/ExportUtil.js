var ExportUtil = new function() {

    this.paintGroupCheckboxes = function ($container, labelId) {
        var fieldset = this._getFieldset($container);
        var titleLabel = $("<label>", {"type": "label", "id": labelId, "class": "control-label"})
            .text("Groups (*)");
        fieldset.append(titleLabel);
        this.getTableModel();
        fieldset.append(this._getTable(this._tableModel));
    }

    this.getTableModel = function() {
        if (!this._tableModel) {
            this._tableModel = this._createTableModel();
        }
        return this._tableModel;
    }

    this._createTableModel = function() {
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
