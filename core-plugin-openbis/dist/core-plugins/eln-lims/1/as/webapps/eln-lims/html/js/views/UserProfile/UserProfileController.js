/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function UserProfileController(mainController, mode) {
	this._mainController = mainController;
	this._userProfileModel = new UserProfileModel(mode);
	this._userProfileView = new UserProfileView(this, this._userProfileModel);
    this._zenodoApiTokenKey = this._mainController.zenodoApiTokenKey;

	this.init = function(views) {
        this._userProfileView.repaint(views);
	}

	this.resetPassword = function() {
		var userId = this._mainController.serverFacade.getUserId();
		var resetPasswordController = new ResetPasswordController(userId);
		resetPasswordController.init();
	}

	this.getUserInformation = function(callback) {
		var userId = this._mainController.serverFacade.getUserId();

		this._mainController.serverFacade.getSessionInformation(function(sessionInfo) {
			var userInformation = {
				firstName : sessionInfo.person.firstName,
				lastName : sessionInfo.person.lastName,
				email : sessionInfo.person.email,
			};
			callback(userInformation);
		});
	}

	this.save = function(userInformation) {
		var errors = this._validate(userInformation);
		if (errors.length > 0) {
			Util.showError(FormUtil._getSanitizedErrorString("Validation Error:", errors));
			return;
		}
		var userId = this._mainController.serverFacade.getUserId();

		this.setSettingValue(this._zenodoApiTokenKey, userInformation.zenodoToken);
		this._mainController.serverFacade.updateUserInformation(userId, userInformation, (function(ok) {
			if (ok) {
				if(this.isFileAuthentication()) {
					Util.showInfo("Profile saved. You will be logged out automatically in order to reload the profile data upon login.", 
							(function() {
								this._mainController.serverFacade.logout();
							}).bind(this),
							false, "OK");
				} else {
				    mainController.changeView("showUserProfilePage");
				}
			}
		}).bind(this));
	}

	this._validate = function(userInformation) {
		var errors = [];
		var fields = ["firstName", "lastName", "email"];
		for (var field of fields) {
			if (userInformation[field] == null || userInformation[field].length === 0) {
				errors.push(field + " can not be empty.");
			}
		}
		return errors;
	}

	this.isFileAuthentication = function() {
		return this._mainController.profile.isFileAuthenticationService &&
				this._mainController.profile.isFileAuthenticationUser;
	}

	this.getSettingValue = function (key, callback) {
		this._mainController.serverFacade.getSetting(key, callback);
	};

	this.setSettingValue = function (key, value) {
		this._mainController.serverFacade.setSetting(key, value);
	};

}