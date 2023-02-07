/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function CommentsView(commentsController, commentsModel) {
	this._commentsController = commentsController;
	this._commentsModel = commentsModel;
	this.commentsContainer = $("<div>");
	this.commentsAddButton = $("<div>");
	
	this.repaint = function($container) {
		$container.empty();
		$container.append(this.commentsContainer);
		this.commentsContainer.append($("<legend>", { "style" : "border-bottom-style:none; font-weight: bold;"} ).text("Comments Log"));
		$container.append(this.commentsAddButton);
		var comments = this._commentsController.getComments();
		for(var i = 0; i < comments.length; i++) {
			this.addCommentWidgetFromXML(comments[i]);
		}
		this.addAddButton();
	}
	
	this.addCommentWidgetFromXML = function(commentXMLNode) {
		var dateValue = commentXMLNode.attributes["date"].value;
		var userId = commentXMLNode.attributes["person"].value;
		var value = "";
		if(commentXMLNode.firstChild !== null) {
			value = commentXMLNode.firstChild.nodeValue;
		}
		
		this.addCommentWidget(dateValue, userId, value);
	}
	
	this.addCommentWidget = function(dateValue, userId, value) {
		var _this = this;
		var $buttonDelete = null;
		if(this._commentsModel.mode !== FormMode.VIEW) {
			$buttonDelete = $("<a>", {"class" : "btn btn-default"});
			$buttonDelete.append($("<span>", { "class" : "glyphicon glyphicon-minus-sign"}));
		}
		var date = Util.getFormatedDate(new Date(parseInt(dateValue)));
		
		var deleteEnabled = false;
		var $sufixComponent = null;
		if(deleteEnabled) {
			$sufixComponent = $buttonDelete;
		}
		var commentWidget = FormUtil.getFieldForLabelWithText(date + " (" + userId + ")", value, null, $sufixComponent, 
				{ 
					"background-color" : "lightblue",
					"padding-left" : "18px",
					"border-radius" : "4px"
				});
		
		if(this._commentsModel.mode !== FormMode.VIEW) {
			$buttonDelete.click(function() {
				_this._commentsController.deleteComment(dateValue);
				commentWidget.remove();
			});
		}
		
		this.commentsContainer.append(commentWidget);
	}
	
	this.addAddButton = function() {
		if(this._commentsModel.mode !== FormMode.VIEW) {
			var _this = this;
			var commentNumber = 0;
			var $buttonPlusOne = $("<a>", {"class" : "btn btn-default", "id" : "add-comment-btn"});
			$buttonPlusOne.append($("<span>", { "class" : "glyphicon glyphicon-plus-sign"}));
			$buttonPlusOne.click(function() {
				_this.addNewComment(commentNumber);
				commentNumber++;
			});
			this.commentsAddButton.append(FormUtil.getFieldForComponentWithLabel($buttonPlusOne, null));
		}
	}
	
	this.addNewComment = function(commentNumber) {
		var $textBox = FormUtil._getTextBox("comment-" + commentNumber + "-box", null, false);
		var $textBoxGroup = FormUtil.getFieldForComponentWithLabel($textBox, null, null);
		var $saveButton = FormUtil.getButtonWithText("Add Comment", null, null, "save-comment-" + commentNumber + "-btn");
		var $saveButtonGroup = FormUtil.getFieldForComponentWithLabel($saveButton, null, null);
		
		this.commentsContainer.append($textBoxGroup);
		this.commentsContainer.append($saveButtonGroup);
		
		var _this = this;
		$saveButton.click(function() {
			//Save Value
			value = $textBox.val();
			//value = html.sanitize(value);
		    value = DOMPurify.sanitize(value);
			_this._commentsController.addNewComment(value);
			//Remove New Comment Box
			$textBoxGroup.remove();
			$saveButtonGroup.remove();
		});
	}
}