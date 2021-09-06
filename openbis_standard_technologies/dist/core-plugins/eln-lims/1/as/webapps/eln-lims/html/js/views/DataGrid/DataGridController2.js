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
  heightPercentage,
  loadDataDynamic
) {
  if (!configKey) {
    window.alert(
      "[TO-DELETE] Empty configKey during the table init, this should never happen, tell the developers."
    );
  }

  var _this = this;

  this.id = configKey;
  this.header = title;

  var columns = [];
  columns = columns.concat(columnsFirst);
  columns = columns.concat(columnsLast);
  this.columns = columns.map(function (column) {
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
          value = column.render(params.row);
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

  this.rows = [];
  this.totalCount = 0;

  this.init = function ($container, extraOptions) {
    if (loadDataDynamic) {
      var GridElement = React.createElement(
        window.NgUiGrid.default.ThemeProvider,
        {},
        React.createElement(window.NgUiGrid.default.Grid, {
          id: _this.id,
          header: _this.header,
          columns: _this.columns,
          rows: _this.rows,
          totalCount: _this.totalCount,
          load: function () {
            return new Promise(function (resolve) {
              loadData(function (data) {
                resolve();
                _this.rows = data.objects;
                _this.totalCount = data.totalCount;
                _this.init($container, extraOptions);
              }, options);
            });
          },
        })
      );
      ReactDOM.render(GridElement, $container.get(0));
    } else {
      loadData(function (data) {
        var GridElement = React.createElement(
          window.NgUiGrid.default.ThemeProvider,
          {},
          React.createElement(window.NgUiGrid.default.Grid, {
            id: _this.id,
            header: _this.header,
            columns: _this.columns,
            rows: data.map(function (row, index) {
              return Object.assign(
                {
                  id: index,
                },
                row
              );
            }),
          })
        );

        ReactDOM.render(GridElement, $container.get(0));
      });
    }
  };

  this.refreshHeight = function () {};

  this.refresh = function () {};
}
