/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

function DataGridController(
  title,
  columnsFirst,
  columnsLast,
  columnsDynamicFunc,
  loadData,
  rowClickEventHandler,
  showAllColumns,
  configKey,
  isMultiselectable,
  heightPercentage
) {
  if (!configKey) {
    window.alert(
      "[TO-DELETE] Empty configKey during the table init, this should never happen, tell the developers."
    );
  }

  var _this = this;

  this.init = function ($container, extraOptions) {
    let $element = $("<div>");

    ReactDOM.render(
      React.createElement(window.NgUiGrid.default.Loading, {
        loading: true,
      }),
      $element.get(0)
    );

    this._init($element, extraOptions);

    $container.empty().append($element);
  };

  this._init = function ($container, extraOptions) {
    var GridElement = React.createElement(
      window.NgUiGrid.default.ThemeProvider,
      {},
      React.createElement(window.NgUiGrid.default.Grid, {
        controllerRef: function (controller) {
          _this.controller = controller;
        },
        header: title,
        loadSettings: _this._loadSettings,
        loadColumns: _this._loadColumns,
        loadRows: _this._loadRows,
        onSettingsChange: _this._onSettingsChange,
        onRowClick: rowClickEventHandler,
        selectable: false,
        multiselectable: isMultiselectable,
        actions: _this._actions(extraOptions),
      })
    );

    ReactDOM.render(GridElement, $container.get(0));
  };

  this._loadSettings = function () {
    return new Promise(function (resolve) {
      mainController.serverFacade.getSetting(
        configKey,
        function (elnGridSettingsStr) {
          var gridSettingsObj = null;

          if (elnGridSettingsStr) {
            try {
              var elnGridSettingsObj = JSON.parse(elnGridSettingsStr);

              gridSettingsObj = {
                pageSize: elnGridSettingsObj.pageSize,
                sort: elnGridSettingsObj.sort
                  ? elnGridSettingsObj.sort.sortProperty
                  : null,
                sortDirection: elnGridSettingsObj.sort
                  ? elnGridSettingsObj.sort.sortDirection
                  : null,
                columnsVisibility: elnGridSettingsObj.columns,
                columnsSorting: elnGridSettingsObj.columnsSorting,
                exportOptions: elnGridSettingsObj.exportOptions,
              };
            } catch (e) {
              console.log(
                "[WARNING] Could not parse grid settings",
                configKey,
                elnGridSettingsStr,
                e
              );
            }
          }
          resolve(gridSettingsObj);
        }
      );
    });
  };

  this._loadColumns = function (objects) {
    var columns = [];
    columns = columns.concat(columnsFirst);

    if (columnsDynamicFunc) {
      var dynamicColumns = columnsDynamicFunc(objects);
      if (dynamicColumns !== null && dynamicColumns.length > 0) {
        columns = columns.concat(dynamicColumns);
      }
    }

    columns = columns.concat(columnsLast);
    columns = columns.filter(function (column) {
      return column.property;
    });
    columns = columns.map(function (column, index) {
      return {
        label: React.createElement("span", {
          dangerouslySetInnerHTML: {
            __html: column.label,
          },
        }),
        name: column.property,
        getValue: function (params) {
          return params.row[column.property];
        },
        renderDOMValue: function (params) {
          var maxLineLength = 200;
          var value = null;

          if (column.render) {
            let grid = {
              lastReceivedData: {
                objects: _this.controller.getRows(),
                totalCount: _this.controller.getTotalCount(),
              },
              lastUsedOptions: {
                pageIndex: _this.controller.getPage(),
                pageSize: _this.controller.getPageSize(),
                sortProperty: _this.controller.getSort(),
                sortDirection: _this.controller.getSortDirection(),
                search:
                  Object.keys(_this.controller.getFilters()).length > 0
                    ? Object.values(_this.controller.getFilters()).join(" ")
                    : null,
                searchMap: _this.controller.getFilters(),
              },
            };

            value = column.render(params.row, grid);
          } else {
            value = params.value;
          }

          //2. Sanitize
          var value = FormUtil.sanitizeRichHTMLText(value);

          //3. Shorten
          var finalValue = null;
          if (value && value.length > maxLineLength) {
            finalValue = value.substring(0, maxLineLength) + "...";
          } else {
            finalValue = value;
          }

          //4. Tooltip
          if (value !== finalValue) {
            finalValue = $("<div>").html(finalValue);
            finalValue.tooltipster({
              content: $("<span>").html(value),
            });
          }

          $(params.container).empty();
          $(params.container).append(finalValue);
        },
        matchesValue: function (params) {
          if (column.filter) {
            return column.filter(params.row, params.filter);
          } else {
            return params.defaultMatches(params.value, params.filter);
          }
        },
        compareValue: function (params) {
          if (column.sort) {
            return column.sort(params.row1, params.row2, true);
          } else {
            return params.defaultCompare(params.value1, params.value2);
          }
        },
        sortable: column.sortable,
        filterable: column.filterable,
        visible:
          !column.hide &&
          (showAllColumns ||
            column.showByDefault ||
            column.canNotBeHidden ||
            index < 3 ||
            index === columns.length - 1),
        configurable: !column.hide && !column.canNotBeHidden,
        exportable: column.isExportable,
      };
    });

    return columns;
  };

  this._loadRows = function (params) {
    var options = {
      pageIndex: params.page,
      pageSize: params.pageSize,
      sortProperty: params.sort,
      sortDirection: params.sortDirection,
      search:
        Object.keys(params.filters).length > 0
          ? Object.values(params.filters).join(" ")
          : null,
      searchMap: params.filters,
    };

    function checkRowIds(rows) {
      for (var i = 0; i < rows.length; i++) {
        var rowId = rows[i].id;
        if (rowId === null || rowId === undefined) {
          console.error("Row id was null", rows[id]);
          throw new Error("Row id was null");
        }
      }
    }

    return new Promise(function (resolve) {
      loadData(function (data) {
        let dynamic = data.totalCount !== null && data.totalCount !== undefined;

        if (dynamic) {
          checkRowIds(data.objects);
          resolve({
            rows: data.objects,
            totalCount: data.totalCount,
          });
        } else {
          checkRowIds(data);
          resolve(data);
        }
      }, options);
    });
  };

  this._actions = function (extraOptions) {
    if (!extraOptions) {
      return [];
    }

    return extraOptions.map(function (extraOption) {
      return {
        label: extraOption.name,
        execute: function (params) {
          let selectedObjects = Object.values(params.multiselectedRows).map(
            function (selectedRow) {
              return selectedRow.data.$object;
            }
          );
          extraOption.action(selectedObjects);
        },
      };
    });
  };

  this._onSettingsChange = function (gridSettingsObj) {
    let elnGridSettingsObj = {
      pageSize: gridSettingsObj.pageSize,
      sort: {
        sortProperty: gridSettingsObj.sort,
        sortDirection: gridSettingsObj.sortDirection,
      },
      columns: gridSettingsObj.columnsVisibility,
      columnsSorting: gridSettingsObj.columnsSorting,
      exportOptions: gridSettingsObj.exportOptions,
    };

    let elnGridSettingsStr = JSON.stringify(elnGridSettingsObj);
    mainController.serverFacade.setSetting(configKey, elnGridSettingsStr);
  };

  this.refreshHeight = function () {};

  this.refresh = function () {
    if (_this.controller) {
      _this.controller.load();
    }
  };
}
