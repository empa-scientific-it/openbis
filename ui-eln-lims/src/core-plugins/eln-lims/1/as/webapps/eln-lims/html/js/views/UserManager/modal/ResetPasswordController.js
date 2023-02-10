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

function ResetPasswordController(userId) {
	this._resetPasswordModel = new ResetPasswordModel(userId);
	this._resetPasswordView = new ResetPasswordView(this, this._resetPasswordModel);
	
	this.resetPassword = function() {
		var _this = this;
		
		if(_this._resetPasswordModel.password === _this._resetPasswordModel.passwordRepeat) {
			mainController.serverFacade.registerUserPassword(
					_this._resetPasswordModel.userId,
					_this._resetPasswordModel.password,
					function(isRegistered) {
						if(isRegistered) {
							Util.showSuccess("Password has been reset on the file authentication service.", function() {
								Util.unblockUI();
							});
						} else {
							Util.showError("Password can't be reset.");
						}
					});
		} else {
			Util.showUserError("Passwords are not equal.", function() {}, true);
		}
	}
	
	this.init = function() {
		this._resetPasswordView.repaint();
	}
	
}