define(["lib_javascript/DropBoxMonitor/DropBoxMonitorLogic", "lib_javascript/DropBoxMonitor/DropBoxMonitorView"], function(DropBoxMonitorLogic, DropBoxMonitorView) {
  "use strict";
  var DropBoxMonitorManager = function(communicationFacade, logoutListener, container) {
    this.communicationFacade = communicationFacade;
    this.logoutListener = logoutListener;
    this.htmlContainer = container;
    this.logic = new DropBoxMonitorLogic(logoutListener, communicationFacade);
    this.view = new DropBoxMonitorView(this.logic);
    var cssLink = $("<link rel='stylesheet' type='text/css' href='css/dropBoxMonitorView.css'>");
    $("head").append(cssLink);
  };

  DropBoxMonitorManager.prototype.serviceRun = function() {
    this.view.render(this.htmlContainer);
  };

  return DropBoxMonitorManager;
});
