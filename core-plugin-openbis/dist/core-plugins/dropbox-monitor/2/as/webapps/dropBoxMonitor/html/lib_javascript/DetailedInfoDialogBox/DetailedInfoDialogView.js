define([], function() {
  "use strict";
  var DetailedInfoDialogView = function(DetailedInfoDialogLogic, dropboxName) {
    this.dialogLogic = DetailedInfoDialogLogic;
    this.fetchNLog = 10;
    this.dropboxName = dropboxName;
    this.dialogComponent = "";
    this.dialogId = "detailInfo" + this.dropboxName;
  };

  DetailedInfoDialogView.prototype.render = function() {
    var objectSelfReference = this;
    var dropBoxDialog = $("#" + this.dialogId);
    if (dropBoxDialog.dialog("isOpen") === true) {
      dropBoxDialog.dialog("moveToTop");
    } else {
      this.dialogLogic.getDetailDropboxInfo(this.dropboxName,
        function(data) {
          objectSelfReference.displayFetchedData(data);
        }, this.fetchNLog);
    }
  };

  DetailedInfoDialogView.prototype.displayFetchedData = function(data) {
    var objectSelfReference = this;
    var dropBoxInformation = data.result.rows[0][1].value;
    var infoDiv;

    infoDiv = $("<div></div>");
    infoDiv.addClass("container-fluid info-div");
    infoDiv.attr("id", this.dialogId);
    infoDiv.attr("title", this.dropboxName);

    if (dropBoxInformation === "null") {
      var textDiv = $("<div></div>");
      textDiv.addClass();
      textDiv.text("No information available");
      textDiv.css("text-align", "center");
      infoDiv.append(textDiv);
      this.dialogComponent = $(infoDiv).dialog({
      });
      return;
    }

    infoDiv.empty();

    this.prepareInnerDataContainers(dropBoxInformation, infoDiv);

    this.dialogComponent = $(infoDiv).dialog({
      height: $(window).height() * (3 / 4),
      width: $(window).width() * (3 / 4),
      maxHeight: $(window).height(),
      maxWidth: $(window).width(),
      minHeight: $(window).height() * (1 / 2),
      minWidth: $(window).width() * (1 / 2),
      open: function(event, ui) {
        $(this).find("td").first().click();
      },
      resize: function(event, ui) {
        $(this).filter(".info-div").css("width", "auto");
      },
      resizeStop: function(event, ui) {
        $(this).filter(".info-div").css("width", "auto");
      },
      beforeClose: function(event, ui) {
        $(this).remove();
        objectSelfReference.fetchNLog = 10;

      },
      buttons: [{
        text: 'More logs',
        class: 'btn btn-default more-logs-dialog',
        click: function() {
          objectSelfReference.fetchNLog += 10;
          objectSelfReference.refreshView();
        }
      }, {
        text: 'Refresh',
        class: 'btn btn-default refresh-dialog',
        click: function() {
          objectSelfReference.refreshView();
        }
      }]
    });

  };

  DetailedInfoDialogView.prototype.refreshView = function() {
    var objectSelfReference = this;
    var spinner = $("<i></i>");
    spinner.addClass("fa fa-spinner fa-spin");
    $("#"+this.dialogId).next(".ui-dialog-buttonpane").find(".more-logs-dialog").attr("disabled", true);
    $("#"+this.dialogId).next(".ui-dialog-buttonpane").find(".refresh-dialog").append(spinner);
    $("#"+this.dialogId).next(".ui-dialog-buttonpane").find(".refresh-dialog").attr("disabled", true);
    var boldObject = $("#"+this.dialogId).find("td").filter(".bold-text").text();
    this.dialogLogic.getDetailDropboxInfo(this.dropboxName,
      function(data) {
        $(objectSelfReference.dialogComponent[0].firstChild).empty();
        $(objectSelfReference.dialogComponent[0].lastChild).empty();
        var dropBoxInformation = data.result.rows[0][1].value;
        objectSelfReference.fillInnerDataContainers(dropBoxInformation,
          $(objectSelfReference.dialogComponent[0].firstChild),
          $(objectSelfReference.dialogComponent[0].lastChild));
          spinner.remove();
            $("#"+objectSelfReference.dialogId).next(".ui-dialog-buttonpane").find(".more-logs-dialog").attr("disabled", false);
            $("#"+objectSelfReference.dialogId).next(".ui-dialog-buttonpane").find(".refresh-dialog").attr("disabled", false);
            $("#"+objectSelfReference.dialogId).find("td:contains("+ boldObject +")").click();
      }, objectSelfReference.fetchNLog);
  };

  DetailedInfoDialogView.prototype.prepareInnerDataContainers = function(dropBoxInformation, infoDiv) {
    var filesList,
      fileContainer;

    filesList = $("<div></div>").appendTo(infoDiv);
    filesList.addClass("files-list");
    fileContainer = $("<div></div>").appendTo(infoDiv);
    fileContainer.addClass("files-container");

    this.fillInnerDataContainers(dropBoxInformation, filesList, fileContainer);
  };

  DetailedInfoDialogView.prototype.fillInnerDataContainers = function(dropBoxInformation, filesList, fileContainer) {
    var objectSelfReference = this;
    var infoHandleObjectList = [];
    var filesListTable;

    dropBoxInformation = JSON.parse(dropBoxInformation);
    $.each(dropBoxInformation, function(dropboxStatus, infoPack) {
      $.each(infoPack, function(index, item) {
        var infoHandleObject = objectSelfReference.createFilesListInformationCell(index, item, fileContainer, dropboxStatus);
        infoHandleObjectList.push(infoHandleObject);
      });
    });

    infoHandleObjectList.sort(function(a, b) {
      if ($(a[0]).text() < $(b[0]).text()) {
        return 1;
      }
      if ($(a[0]).text() > $(b[0]).text()) {
        return -1;
      }
      return 0;
    });

    filesListTable = $("<table></table>").appendTo(filesList);

    $.each(infoHandleObjectList, function(index, item) {
      var tableRow = $("<tr></tr>").appendTo(filesListTable);
      tableRow.addClass("files-list-row");
      item.appendTo(tableRow);
    });

  };

  DetailedInfoDialogView.prototype.createFilesListInformationCell = function(index, item, fileContainer, dropboxStatus) {
    var infoHandleObject,
      icon,
      infoDetailObject;

    infoHandleObject = $("<td></td>");
    infoHandleObject.addClass("files-list-cell");
    infoHandleObject.addClass("text " + dropboxStatus + "-text");
    icon = $("<i></i>").appendTo(infoHandleObject);

    if (dropboxStatus === "failed") {
      icon.addClass("fa fa-frown-o");
    }
    if (dropboxStatus === "succeded") {
      icon.addClass("fa fa-smile-o");
    } else {
      icon.addClass("fa fa-spinner");
    }
    infoHandleObject.append(" " + index);
    infoDetailObject = $("<pre></pre>");
    infoDetailObject.text(item);
    infoDetailObject.addClass("files");

    infoHandleObject.click(function() {
      $(this).closest("tbody").find(".bold-text").removeClass("bold-text");
      $(this).addClass("bold-text");
      $(fileContainer).empty();
      $(fileContainer).append(infoDetailObject);
    });
    return infoHandleObject;
  };

  return DetailedInfoDialogView;

});
