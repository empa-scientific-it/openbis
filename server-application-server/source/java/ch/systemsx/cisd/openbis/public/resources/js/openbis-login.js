/**
 * A module for configuring the login page to openBIS. It hides and shows
 * the login form and main content as necessary. It invokes a specified
 * function on successful login.
 *
 * This module assumes that the page follows the structure of our standard 
 * login page. This means that the Following elements are defined : 
 *
 *   div#login-form-div
 *     form#login-form
 *       input#username
 *       input#password
 *       button#login-button
 *   div#main
 *     button#logout-button
 * 
 * Assuming these elements are defined, this module configures their appeareance
 * and behavior.The div#main element is initially hidden until the user logs in.
 * Once logged in, the div#login-form-div element is hidden and the div#main 
 * element is made visible.
 *
 * @module openbis-login
 * @requires jquery
 */


/**
 * Configure the login page to hide and show the login form and main content
 * as appropriate
 *
 * @param openbis The openbis facade object
 * @param onLogin The function to be called when login succeeds.
 * @function
 */

function openbisLoginPage(openbis, onLogin)
{
	this.openbis = openbis;
	this.onLogin = onLogin;
}

openbisLoginPage.prototype.configure = function(){
	var loginPage = this;
	
	$('#main').hide();
	
	var username = $("#username").value;
	if(username == null || username.length==0) {
		$("#username").focus();
	} else {
		$("#login-button").focus();
	}
	
	$('#logout-button').click(function() { 
		loginPage.openbis.logout(function(data) { 
			$("#login-form-div").show();
			$("#main").hide();
			$("#username").focus();
		});
	});
	
	$('#login-form').submit(function() {
		 loginPage.openbis.login( $.trim($('#username').val()), $.trim($('#password').val()), function(data) { loginPage.onLogin(data) })
	});
	
	loginPage.openbis.ifRestoredSessionActive(function(data) { loginPage.onLogin(data) });
	
		// Make the ENTER key the default button
	$("login-form input").keypress(function (e) {
		if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
			$('button[type=submit].default').click();
			return false;
		} else {
			return true;
		}
	});
}