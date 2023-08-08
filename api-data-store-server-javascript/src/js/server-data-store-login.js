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
 * @module datastore-login
 */


/**
 * Configure the login page to hide and show the login form and main content
 * as appropriate
 *
 * @param datastore The datastore facade object
 * @param onLogin The function to be called when login succeeds.
 * @function
 */

function dssClientLoginPage(datastore, onLogin)
{
	this.datastore = datastore;
	this.onLogin = onLogin;
}

dssClientLoginPage.prototype.configure = function(){
	var loginPage = this;
	
	document.getElementById("main").style.display = "none";
	
	var username = document.getElementById("username").value;
	if(username == null || username.length==0) {
		document.getElementById("username").focus();
	} else {
		document.getElementById("login-button").focus();
	}

	document.getElementById("logout-button").onclick = function() { 
		loginPage.datastore.logout(function(data) { 
			document.getElementById("login-form-div").style.display = "block";
			document.getElementById("main").style.display = "none";
			document.getElementById("openbis-logo").style.height = "100px";
			document.getElementById("username").focus();
		});
		
	};
	
	
	document.getElementById("login-form").onsubmit = function() {
		 loginPage.datastore.login(document.getElementById("username").value.trim(), 
		 document.getElementById("password").value.trim(), 
		 function(data) { 
			document.getElementById("username").value = '';
			document.getElementById("password").value = '';
			loginPage.onLogin(data); 
		})
	};
	
	loginPage.datastore.ifRestoredSessionActive(function(data) { loginPage.onLogin(data) });
	

	document.onkeydown=function(evt){
        var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
        if(keyCode == 13)
        {
			document.getElementById("login-form").submit();
        }
    }
	
}
