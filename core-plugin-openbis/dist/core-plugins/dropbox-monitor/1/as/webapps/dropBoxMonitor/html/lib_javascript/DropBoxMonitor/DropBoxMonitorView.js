define(["lib_javascript/DetailedInfoDialogBox/DetailedInfoDialogManager",
    "lib_javascript/DropBoxMonitor/DropBoxMonitorUtil"
  ],
  function(DetailedInfoDialogManager, DropBoxMonitorUtil) {
    "use strict";
    var DropBoxMonitorView = function(dropBoxLogic) {
      this.dropBoxLogic = dropBoxLogic;
      this.dropBoxUtil = new DropBoxMonitorUtil();
      this.detailedInfoDialogManager =
        new DetailedInfoDialogManager(this.dropBoxLogic.getCommunicationFacade());
    };

    DropBoxMonitorView.prototype.render = function(htmlContainer) {
      var objectSelfReference = this;
      this.dropBoxLogic.getSimpleDropboxInfo(function(data) {
        objectSelfReference.run(data, htmlContainer);
      });
    };

    DropBoxMonitorView.prototype.run = function(data, htmlContainer) {
      var page, header, content;
      console.log(data);

      page = this.createPage().appendTo(htmlContainer);
      header = this.createHeader().appendTo(page);
      content = this.createContent(data).appendTo(page);

      page.append("<i style='fa fa-frown-o'></i>");
      this.afterCreationUtils();
    };

    DropBoxMonitorView.prototype.afterCreationUtils = function (first_argument) {
      this.dropBoxUtil.tableCellWidthFix();
    };

    DropBoxMonitorView.prototype.createPage = function() {
      var page = $("<div></div>");
      $(page).addClass("container-fluid page");

      return page;
    };

    DropBoxMonitorView.prototype.createContent = function(data) {
      var objectSelfReference = this;
      if(data.result.columns[1].title === "Error"){
        console.error(data.result.rows[0][1].value);
        var errorDiv = $("<div></div>");
        errorDiv.addClass("alert alert-danger");
        errorDiv.css("margin-top", "5px");
        errorDiv.text(data.result.rows[0][1].value);
        return errorDiv;
      }

      var columnsLength = data.result.columns.length;
      var rowLength = data.result.rows.length;
      var content,
        presentationTable,
        tableHead,
        columnTitle,
        tableCell,
        tableBody,
        dropBoxName,
        dataCellsCount,
        tableRow,
        tableCellContent,
        i, j;

      content = $("<div></div>").attr("id", "tableContent");

      presentationTable = $("<table></table>").appendTo(content);
      presentationTable.attr("id", "presentationTable");
      presentationTable.addClass("presentationTable table tablesorter table-striped table-hover");

      tableHead = $("<thead></thead>").appendTo(presentationTable);
      // create Legend
      tableRow = $("<tr></tr>").appendTo(tableHead);
      for (i = 0; i < columnsLength; i++) {
        columnTitle = data.result.columns[i].title;
        tableCell = $("<th></th>").appendTo(tableRow);
        $(tableCell).text(columnTitle);
      }
      // fill data
      tableBody = $("<tbody></tbody>").appendTo(presentationTable);
      tableBody.attr("id", "tableBody");

      var formatMap = {
        0 : function(tableCellContent) { return tableCellContent; },
        1 : function(tableCellContent) { return objectSelfReference.dropBoxUtil.iconifyLastStatus(tableCellContent); },
        2 : function(tableCellContent) { return tableCellContent; },
        3 : function(tableCellContent) { return objectSelfReference.dropBoxUtil.formatDate(tableCellContent); },
        4 : function(tableCellContent) { return objectSelfReference.dropBoxUtil.formatDate(tableCellContent); },
        5 : function(tableCellContent) { return objectSelfReference.dropBoxUtil.formatDate(tableCellContent); },
      };

      for (i = 0; i < rowLength; i++) {
        dropBoxName = data.result.rows[i][0].value;
        dataCellsCount = data.result.rows[i].length;

        tableRow = $("<tr></tr>").appendTo(tableBody);
        tableRow.attr("id", "rowFor" + dropBoxName);
        tableRow.attr("dropBoxName", dropBoxName);

        for (j = 0; j < dataCellsCount; j++) {
          tableCellContent = data.result.rows[i][j].value;
          tableCell = $("<td></td>").appendTo(tableRow);
          tableCell.attr("data", tableCellContent);
          tableCell.append(formatMap[j](tableCellContent));
        }

        tableRow.click(function() { // is there a way to create this eventhandlers not within a loop?
          var dropBoxName = $(this).attr("dropBoxName");
          objectSelfReference.detailedInfoDialogManager.serviceRun(dropBoxName);
        });
      }

      this.dropBoxUtil.tablesorterInit(presentationTable);
      this.dropBoxUtil.sortableInit(tableBody);

      return content;
    };

    DropBoxMonitorView.prototype.createHeader = function() {
      var objectSelfReference = this;
      var header, logoutButton, refreshButton;

      header = $("<div></div>").attr("id", "headerContent");
      header.className = "page-header nav navbar page-header-override"; //navbar-fixed-top MAYBE NOPE
      header.append("<h1>Dropbox state report</h1>");

      logoutButton = $("<button></button>").appendTo(header);
      logoutButton.addClass("btn btn-default header-button").text("Logout");
      logoutButton.click(function() {
        objectSelfReference.dropBoxLogic.logout();
      });

      refreshButton = $("<button></button>").appendTo(header);
      refreshButton.addClass("btn btn-default header-button").text("Refresh");
      refreshButton.click("click", function() {
        var refreshButtonReference = this;
        var spinner = $("<i></i>");
        spinner.addClass("fa fa-spinner fa-spin");
        spinner.appendTo($(this));
        $(this).attr("disabled", "disabled");
        objectSelfReference.dropBoxLogic.getSimpleDropboxInfo(function(data) {
          $(refreshButtonReference).attr("disabled", false);
          spinner.remove();
          var content = objectSelfReference.createContent(data);
          $("#tableContent").remove();
          content.appendTo($(".page"));
          objectSelfReference.afterCreationUtils();
        });
      });

      header.append("<hr>");

      return header;
    };

    return DropBoxMonitorView;
  });
