define([], function() {
  "use strict";
  var OpenbisCommunicationFacade = function(openbis) {
    this.openbis = openbis;
    this.DSS = "";
  };

  OpenbisCommunicationFacade.prototype.login = function(username, password, registeredListener) {
    var objectSelfReference = this;
    this.openbis.login(username, password, function(data) {
      if (data.result === null) {
        alert("Login or password incorrect");
        return;
      } else {
        objectSelfReference.setDSS(function() {
          registeredListener(data);
        });
      }
    });
  };

  OpenbisCommunicationFacade.prototype.logout = function(registeredListener) {
    this.openbis.logout(function() {
      registeredListener();
    });
  };

  OpenbisCommunicationFacade.prototype.checkIfSessionActive = function(registeredListener, continueLogin) {
    var objectSelfReference = this;
    this.openbis.restoreSession();
    this.openbis.isSessionActive(function(data) {
      if (data.result) {
        objectSelfReference.setDSS(function() {
          registeredListener(data);
        });
      } else
        continueLogin();
    });
  };

  OpenbisCommunicationFacade.prototype.getDetailDropboxInfo = function(parameters, callback) {
    this.openbis.createReportFromAggregationService(this.DSS, "dropboxReporter", parameters, callback);
  };

  OpenbisCommunicationFacade.prototype.getSimpleDropboxInfo = function(callback) {
    var parameters = {};
    this.openbis.createReportFromAggregationService(this.DSS, "dropboxReporter", parameters, function(data) {
      if (data.error === undefined) {
        callback(data);
      } else {
        console.error(data);
      }
    });
  };

  OpenbisCommunicationFacade.prototype.setDSS = function(callback) {
    var objectSelfReference = this;
    this.openbis.listDataStores(function(data) {
      if (data.error === undefined) {
        objectSelfReference.DSS = data.result[0].code;
        callback();
      } else {
        console.error(data);
      }
    });
  };

  OpenbisCommunicationFacade.prototype.useSession = function (sessionId) {
    this.openbis.useSession(sessionId);
  };



  return OpenbisCommunicationFacade;
});
