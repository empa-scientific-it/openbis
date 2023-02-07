define([], function() {
  "use strict";
  var DropBoxMonitorUtil = function() {

  };

  DropBoxMonitorUtil.prototype.formatDate = function(tableCellContent) {
    if (tableCellContent !== "") {
      var dateJson = JSON.parse(tableCellContent);
      var days = dateJson.days;
      var hours = dateJson.hours;
      var minutes = dateJson.minutes;
      var seconds = dateJson.seconds;

      if (days >= 2) {
        days = days + " days, ";
      } else if (days == 1) {
        days = days + " day, ";
      } else {
        days = "";
      }
      if (hours === 0) {
        hours = "";
      } else {
        hours = hours + "h ";
      }
      if (minutes === 0) {
        minutes = "";
      } else {
        minutes = minutes + "m ";
      }
      if (seconds === 0) {
        seconds = "";
      } else {
        seconds = seconds + "s ";
      }
      tableCellContent = days + hours + minutes + seconds + " ago";
    }
    return tableCellContent;
  };

  DropBoxMonitorUtil.prototype.iconifyLastStatus = function(item) {
    var icon;
    if (item === "0") {
      icon = $("<i></i>");
      icon.addClass("fa fa-smile-o fa-lg one");
    } else if (item === "1") {
      icon = $("<i></i>");
      icon.addClass("fa fa-frown-o fa-lg zero");
    } else {
      icon = "";
    }
    return icon;
  };

  DropBoxMonitorUtil.prototype.tableCellWidthFix = function() {
    $('td').each(function() {
      $(this).css('width', $(this).width() + 'px');
    });
  };

  DropBoxMonitorUtil.prototype.tablesorterInit = function(table) {
    $(table).tablesorter({
      cssAsc: "headerSortUp",
      cssDesc: "headerSortDown",
      cssHeader: "header",
      sortList: [
        [3, 0]
      ],
      textExtraction: {
        1: function(node) {
          var data = $(node).attr("data");
          if (data !== "") {
            return data;
          } else {
            return "~";
          }
        },
        3: dateSortExtraction,
        4: dateSortExtraction,
        5: dateSortExtraction
      }
    });
  };

  var dateSortExtraction = function(node) {
    var data = $(node).attr("data");
    if (data !== "") {
      data = JSON.parse(data);
      return zeroPad(data.days, 10) + zeroPad(data.hours, 2) + zeroPad(data.minutes, 2) +
        zeroPad(data.seconds, 2);
    } else {
      return "~";
    }
  };

  var zeroPad = function(num, amount) {
    var zero = amount - num.toString().length + 1;
    return new Array(+(zero > 0 && zero)).join("0") + num;
  };

  DropBoxMonitorUtil.prototype.sortableInit = function(tableBody) {
    $(tableBody).sortable();
  };

  return DropBoxMonitorUtil;
});
