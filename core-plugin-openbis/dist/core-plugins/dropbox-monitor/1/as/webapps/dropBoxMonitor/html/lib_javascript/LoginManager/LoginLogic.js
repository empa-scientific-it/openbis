define([], function() {
  "use strict";
  var LoginLogic = function(callbackFunction, openbis) {
    this.openbis = openbis;
    this.callbackFunction = callbackFunction;
  };

  LoginLogic.prototype.setUserName = function(userName) {
    this.userName = userName;
  };

  LoginLogic.prototype.setPassword = function(password) {
    this.password = password;
  };

  LoginLogic.prototype.login = function() {
    this.openbis.login(this.userName, this.password, this.callbackFunction);
  };

  LoginLogic.prototype.checkIfSessionActive = function(sessionActive,continueLogin) {
    this.openbis.checkIfSessionActive(sessionActive, continueLogin);
  };

  return LoginLogic;
});
