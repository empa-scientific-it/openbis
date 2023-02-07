define([], function() {
  "use strict";
  var LoginView = function(loginLogic) {
    this.loginLogic = loginLogic;
  };

  LoginView.prototype.render = function(htmlContainer) {
    var objectSelfReference = this;
    var html;
    html = $(htmlContainer).append("<div></div>");

    $(html).load("lib_javascript/LoginManager/loginHtml.html", function() {
      html.find("#username").change(function() {
        objectSelfReference.loginLogic.setUserName($(this).val());
      });

      html.find("#password").change(function() {
        objectSelfReference.loginLogic.setPassword($(this).val());
      });

      html.find("#login-form").submit(function() {
        objectSelfReference.loginLogic.login();
      });

    });
  };

  return LoginView;
});
