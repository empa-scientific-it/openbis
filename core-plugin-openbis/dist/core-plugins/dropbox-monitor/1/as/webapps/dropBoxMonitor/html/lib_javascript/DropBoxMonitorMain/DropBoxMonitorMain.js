define([], function() {
  "use strict";
  var DropBoxMonitorMain = function(communicationFacade, container) {
    this.communicationFacade = communicationFacade;
    this.htmlContainer = container;
    this.isOpenbisEmbeded = false;
  };

  DropBoxMonitorMain.prototype.run = function() {
    var objectSelfReference = this;
    $(this.htmlContainer).empty();

    var webAppContext = new openbisWebAppContext();
    var sessionId = webAppContext.getSessionId();

    if (sessionId !== null) {
      this.isOpenbisEmbeded = true;
      this.communicationFacade.useSession(sessionId);
      this.communicationFacade.setDSS(function() {
        objectSelfReference.loginCallBack();
      });
    } else {
      require(["lib_javascript/LoginManager/LoginManager"], function(LoginManager) {
        var loginService = new LoginManager(objectSelfReference.communicationFacade,
          function(data) {
            objectSelfReference.loginCallBack(data);
          }, objectSelfReference.htmlContainer);
        loginService.serviceRun();
      });
    }
  };

  DropBoxMonitorMain.prototype.loginCallBack = function() {
    var objectSelfReference = this;
    $(this.htmlContainer).empty();
    require(["lib_javascript/DropBoxMonitor/DropBoxMonitorManager"],
      function(DropBoxMonitorManager) {
        var dropBoxMonitorService =
          new DropBoxMonitorManager(objectSelfReference.communicationFacade,
            function() {
              objectSelfReference.logoutCallBack();
            }, objectSelfReference.htmlContainer);
        dropBoxMonitorService.serviceRun();
      });
  };

  DropBoxMonitorMain.prototype.logoutCallBack = function() {
    if (this.isOpenbisEmbeded) {
      alert("Can't login as embeded app");
    } else {
      this.communicationFacade.logout(function() {
        location.reload();
      });
    }
  };

  return DropBoxMonitorMain;
});
