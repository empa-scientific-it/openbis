function DropboxMonitorView(dropboxMonitorController, dropboxMonitorModel) {
    this._dropboxMonitorController = dropboxMonitorController;
    this._dropboxMonitorModel = dropboxMonitorModel;

    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        $header.empty();
        $header.append($("<h1>").append("Dropbox Monitor"));
        var refreshBtn = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;", "id" : "refresh-btn"}).append("Refresh");
        refreshBtn.click(function() {
            _this._dropboxMonitorController.loadOverview(views);
        });
        $header.append(refreshBtn);

        var $container = views.content;
        $container.empty();
        
        var columns = [ {
            label : 'Dropbox Name',
            property : 'dropboxName',
            sortable : true
        } , {
            label : 'Last Status',
            property : 'lastStatus',
            render: function(data) {
                return DropboxMonitorUtil.iconizeStatus(data.lastStatus);
            },
            sortable : true
        }, {
            label : 'Current Status',
            property : 'currentStatus',
            sortable : true
        }, {
            label : 'Current Status Time',
            property : 'currentStatusTime',
            render: function(data) {
                return _this._renderTime(data.currentStatusTime);
            },
            sortable : true
        }, {
            label : 'Last Success',
            property : 'lastSucces',
            render: function(data) {
                return _this._renderTime(data.lastSucces);
            },
            sortable : true
        }, {
            label : 'Last Failure',
            property : 'lastFailure',
            render: function(data) {
                return _this._renderTime(data.lastFailure);
            },
            sortable : true
        }];
        var getDataList = function(callback) {
            var dataList = [];
            for(var idx = 0; idx < _this._dropboxMonitorModel.dropboxes.length; idx++) {
                var dropbox =  _this._dropboxMonitorModel.dropboxes[idx];
                dataList.push({
                    id: idx,
                    dropboxName: dropbox[0].value,
                    lastStatus : dropbox[1].value,
                    currentStatus : dropbox[2].value,
                    currentStatusTime : dropbox[3].value,
                    lastSucces : dropbox[4].value,
                    lastFailure : dropbox[5].value
                });
            }
            callback(dataList);
        }
        var rowClick = function(e) {
            _this._dropboxMonitorController.showLogsModal(e.data.dropboxName);
        }

        var dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, "DROPBOX_MONITOR_OVERVIEW", false, false, 90);
        dataGrid.setId("dropbox-monitor-overview-grid");
        var dataGridContainer = $("<div>").css("margin-top", "-10px").css("margin-left", "-10px");
        dataGrid.init(dataGridContainer);
        var $containerColumn = $("<form>", {
            'role' : "form", 
            "action" : "javascript:void(0);", 
            "onsubmit" : ""
        });
        $container.append($containerColumn);
        $containerColumn.append(dataGridContainer);
    };

    this._renderTime = function(timeAsJsonString) {
        if (timeAsJsonString === "") {
            return  "";
        }
        var time = JSON.parse(timeAsJsonString);
        var days = time.days;
        var hours = time.hours;
        var minutes = time.minutes;
        var seconds = time.seconds;

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
        return days + hours + minutes + seconds + " ago";
    };
}
