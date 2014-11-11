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
		$container.append(this.commentsAddButton);
		var commentsXML = this._commentsModel.getComments();
		var xmlDoc = new DOMParser().parseFromString(commentsXML, 'text/xml');
		var comments = xmlDoc.getElementsByTagName("commentEntry");
		for(var i = 0; i < comments.length; i++) {
			this.addCommentWidget(comments[i]);
		}
		this.addAddButton();
	}
	
	this.addCommentWidget = function(commentXMLNode) {
		var commentWidget = null;
		if(this._commentsModel.mode === FormMode.VIEW) {
			var date = new Date(parseInt(commentXMLNode.attributes["date"].value) * 1000);
			var userId = commentXMLNode.attributes["person"].value;
			var value = "";
			if(commentXMLNode.firstChild !== null) {
				value = commentXMLNode.firstChild.nodeValue;
			}
			
			commentWidget = FormUtil.getFieldForLabelWithText(date + " " + userId, value, null);
		} else {
			
		}
		this.commentsContainer.append(commentWidget);
	}
	
	this.addAddButton = function() {
		
	}
}