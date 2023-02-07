define(["lib_javascript/LoginManager/LoginView",
"lib_javascript/LoginManager/LoginLogic"], function(LoginView, LoginLogic) {
  "use strict";
  var LoginManager = function(communicationFacade, loginListener, container) {
    this.communicationFacade = communicationFacade;
    this.loginListener = loginListener;
    this.htmlContainer = container;
    this.logic = new LoginLogic(loginListener, communicationFacade);
    this.view = new LoginView(this.logic);
    var cssLink =
      $("<link rel='stylesheet' type='text/css' href='css/loginView.css'>");
    $("head").append(cssLink);

  };

  LoginManager.prototype.serviceRun = function() {
    var objectSelfReference = this;
    this.logic.checkIfSessionActive(this.loginListener, function() {
      objectSelfReference.loginPage();
    });
  };

  LoginManager.prototype.loginPage = function() {
    this.view.render(this.htmlContainer);
  };

  return LoginManager;
});
