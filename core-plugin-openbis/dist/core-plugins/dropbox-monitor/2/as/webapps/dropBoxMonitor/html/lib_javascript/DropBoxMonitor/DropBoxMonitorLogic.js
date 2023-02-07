define([], function() {
  "use strict";
  var DropBoxMonitorLogic = function(logoutCallback, communicationFacade) {
    this.communicationFacade = communicationFacade;
    this.logoutCallback = logoutCallback;
  };

  DropBoxMonitorLogic.prototype.getSimpleDropboxInfo = function(callback) {
    this.communicationFacade.getSimpleDropboxInfo(callback);
  };

  DropBoxMonitorLogic.prototype.getDetailDropboxInfo = function(getInfoAbout, callback) {
    var parameters = {
      dropboxName: getInfoAbout
    };
    this.communicationFacade.getDetailDropboxInfo(parameters, callback);
  };

  DropBoxMonitorLogic.prototype.logout = function() {
    this.logoutCallback();
  };

  DropBoxMonitorLogic.prototype.getCommunicationFacade = function() {
    return this.communicationFacade;
  };

  return DropBoxMonitorLogic;
});
