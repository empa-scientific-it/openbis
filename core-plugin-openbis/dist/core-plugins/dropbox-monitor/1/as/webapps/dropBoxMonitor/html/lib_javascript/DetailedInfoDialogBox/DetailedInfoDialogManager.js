define(["lib_javascript/DetailedInfoDialogBox/DetailedInfoDialogView",
"lib_javascript/DetailedInfoDialogBox/DetailedInfoDialogLogic"],
function(DetailedInfoDialogView, DetailedInfoDialogLogic) {
    "use strict";
    var DropBoxMonitorManager = function(communicationFacade) {
        this.logic = new DetailedInfoDialogLogic(communicationFacade);
        this.ViewClass = DetailedInfoDialogView;
        this.viewMap = {};
        var cssLink = $("<link rel='stylesheet' type='text/css' href='css/detailedInfoDialogView.css'>");
        $("head").append(cssLink);
    };

    DropBoxMonitorManager.prototype.serviceRun = function(dropboxName) {
      if(dropboxName in this.viewMap) {
        this.viewMap[dropboxName].render(dropboxName);
      } else {
        this.viewMap[dropboxName] = new this.ViewClass(this.logic, dropboxName);
        this.viewMap[dropboxName].render();
      }
    };

    return DropBoxMonitorManager;
});
