function DropboxLogsView(dropboxLogsController, dropboxLogsModel) {
    this._controller = dropboxLogsController;
    this._model = dropboxLogsModel;
    this._lastSelectedLogFile = null;

    this.repaint = function() {
        var _this = this;
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });
        $window.append($('<legend>').append("Logs for dropbox '" + this._model.dropboxName));
        var $main = $("<div>").css({
            "height": 400, 
            "width": "100%",
            "line-height": 1
        }).appendTo($window);
        var logFiles = this._model.logFiles;
        if (logFiles.length > 0) {
            this.createList($main, logFiles);
        } else {
            $main.append(FormUtil.getInfoText("No log files available."));
        }

        var $buttons = $("<div>").addClass("container-fluid").css({
            "padding": 0,
            "padding-top": 10
        }).appendTo($window);
        var $btnMore = $('<a>', { 'class' : 'btn btn-default', 'id' : 'moreBtn' }).append('More Logs');
        $btnMore.click(function() {
            _this._model.maxNumberOfLogs += 10;
            _this._controller.loadLogFiles();
        });
        $buttons.append($btnMore);
        var $btnRefresh = $('<a>', { 'class' : 'btn btn-default', 'id' : 'refreshBtn' }).append('Refresh');
        $btnRefresh.click(function() {
            _this._controller.loadLogFiles();
        });
        $buttons.append('&nbsp;').append($btnRefresh);
        var $btnClose = $('<a>', { 'class' : 'btn btn-default', 'id' : 'cancelBtn' }).append('Close');
        $btnClose.css("float", "right");
        $btnClose.click(function() {
            Util.unblockUI();
        });
        $buttons.append($btnClose);

        var css = {
                'text-align' : 'left',
                'top' : '15%',
                'width' : '70%',
                'height' : 500,
                'left' : '15%',
                'right' : '20%',
                'overflow' : 'hidden'
        };
        Util.blockUI($window, css);
    }

    this.createList = function($window, logFiles) {
        var _this = this;
        var $container = $("<div>").addClass("container-fluid").css("padding", 0).appendTo($window);
        var $filesListTable = $("<table>").appendTo($("<div>").css({
            "overflow": "scroll",
            "display": "inline-block",
            "float": "left",
            "height": 400,
            "width": "40%"
        }).appendTo($container));
        var $contentView = $("<div>").css({
            "height": 400,
            "width": "60%",
            "float": "right",
            "font-size": 14,
            "overflow": "auto",
            "cursor": "text",
            "display": "inline-block"
        }).appendTo($container);
        var $toBeSelected = null;
        var $lastSelected = null;
        logFiles.forEach(function(e) {
            var $row = $("<tr>").appendTo($filesListTable);
            $row.css({"border-bottom": "1px solid rgba(204, 204, 204, 0.2)", "margin-top": 5});
            var $cell = $("<td>").appendTo($row);
            $cell.css({"padding": 2, "padding-left": 0, "font-size": "14px", "cursor": "pointer"});
            $cell.css("color", e.status === "succeded" ? "green" : (e.status === "failed" ? "red" : "blue"));
            $cell.append(DropboxMonitorUtil.iconizeStatus(e.status));
            $cell.append(" " + e.logFile);
            var $content = $("<pre>").css("height", "98%").text(e.content);
            $cell.click(function() {
                _this._lastSelectedLogFile = e.logFile;
                if ($lastSelected) {
                    $lastSelected.css("font-weight", "");
                }
                $lastSelected = $cell;
                $cell.css("font-weight", "bold");
                $contentView.empty();
                $contentView.append($content);
            });
            if (_this._lastSelectedLogFile === e.logFile || $toBeSelected == null) {
                $toBeSelected = $cell;
            }
        });
        if ($toBeSelected) {
            $toBeSelected.click();
        }
    };
}