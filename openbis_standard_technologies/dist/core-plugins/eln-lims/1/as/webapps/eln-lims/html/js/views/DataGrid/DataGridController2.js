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

function DataGridController2(
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
  this.gridId = configKey;
  this.header = title;
  this.rows = [];
  this.totalCount = 0;

  this.init = function (session, $container, extraOptions) {
    ReactDOM.unmountComponentAtNode($container.get(0));
    ReactDOM.render(
      React.createElement(window.NgUiGrid.default.Loading, {
        loading: true,
      }),
      $container.get(0)
    );
    this._init(session, $container, extraOptions);
  };

  this._init = function (session, $container, extraOptions) {
    var GridElement = React.createElement(
      window.NgUiGrid.default.ThemeProvider,
      {},
      React.createElement(window.NgUiGrid.default.Grid, {
        settingsId: {
          webAppId: "ELN-LIMS",
          gridId: _this.gridId,
        },
        controllerRef: function (controller) {
          _this.controller = controller;
        },
        header: _this.header,
        loadSettings: _this._loadSettings,
        loadColumns: _this._loadColumns,
        loadRows: _this._loadRows,
        onSettingsChange: _this._onSettingsChange,
        onSelectedRowChange: _this._onSelectedRowChange,
      })
    );

    ReactDOM.render(GridElement, $container.get(0));
  };

  this._loadSettings = function () {
    return new Promise(function (resolve) {
      mainController.serverFacade.getSetting(configKey, function (settingsStr) {
        var settingsObj = null;
        if (settingsStr) {
          try {
            settingsObj = JSON.parse(settingsStr);
          } catch (e) {
            console.log(
              "[WARNING] Could not parse grid settings",
              configKey,
              settingsStr,
              e
            );
          }
        }
        resolve(settingsObj);
      });
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
    columns = columns
      .filter(function (column) {
        return column.property;
      })
      .map(function (column) {
        return {
          label: column.label,
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
              return column.filter(params.value, params.filter);
            } else {
              return params.defaultMatches(params.value, params.filter);
            }
          },
          compareValue: function (params) {
            if (column.sort) {
              return column.sort(
                params.value1,
                params.value2,
                params.sortDirection === "asc"
              );
            } else {
              return params.defaultCompare(params.value1, params.value2);
            }
          },
          sortable: column.sortable,
          filterable: column.filterable,
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
    };

    function assignRowIds(rows) {
      return rows.map(function (row, index) {
        return Object.assign(
          {
            id: index,
          },
          row
        );
      });
    }

    return new Promise(function (resolve) {
      loadData(function (data) {
        let dynamic = data.totalCount !== null && data.totalCount !== undefined;

        if (dynamic) {
          resolve({
            rows: assignRowIds(data.objects),
            totalCount: data.totalCount,
          });
        } else {
          resolve(assignRowIds(data));
        }
      }, options);
    });
  };

  this._onSettingsChange = function (settingsObj) {
    let settingsStr = JSON.stringify(settingsObj);
    mainController.serverFacade.setSetting(configKey, settingsStr);
  };

  this._onSelectedRowChange = function (selectedRow) {
    if (rowClickEventHandler) {
      rowClickEventHandler(selectedRow);
    }
  };

  this.refreshHeight = function () {};

  this.refresh = function () {};
}
