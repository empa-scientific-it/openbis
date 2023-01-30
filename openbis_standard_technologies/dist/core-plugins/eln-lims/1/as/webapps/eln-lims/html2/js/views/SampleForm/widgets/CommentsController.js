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

function CommentsController(entity, mode, form) {
	this._commentsModel = new CommentsModel(entity, mode, form);
	this._commentsView = new CommentsView(this, this._commentsModel);
	
	this.init = function($container) {
		this._commentsView.repaint($container);
	}
	
	this.isEmpty = function() {
		var comments = this.getComments();
		return comments === null || comments.length === 0;
	}
	
	this.getComments = function() {
		var commentsXML = this._commentsModel.getComments();
		var xmlDoc = new DOMParser().parseFromString(commentsXML, 'text/xml');
		var comments = xmlDoc.getElementsByTagName("commentEntry");
		return comments;
	}
	
	this.deleteComment = function(commentTimestampToDelete) {
		var commentsXML = this._commentsModel.getComments();
		var xmlDoc = new DOMParser().parseFromString(commentsXML, 'text/xml');
		var comments = xmlDoc.getElementsByTagName("commentEntry");
		for(var i = 0; i < comments.length; i++) {
			var commentNode = comments[i];
			var commentTimes = commentNode.attributes["date"].value;
			if(commentTimes === ""+commentTimestampToDelete) {
				commentNode.parentNode.removeChild(commentNode);
			}
		}
		
		var xmlDocAsString = (new XMLSerializer()).serializeToString(xmlDoc);
		this._commentsModel.setComments(xmlDocAsString)
	}
	
	this.addNewComment = function(newComment) {
		//New Data
		var timestamp = Math.round(new Date().getTime());
		var userId = mainController.serverFacade.getUserId();
		
		//Update Model
		var commentsXML = this._commentsModel.getComments();
		if(!commentsXML || commentsXML.indexOf("<root>") === -1) {
			commentsXML = "<root></root>";
		}
		var xmlDoc = new DOMParser().parseFromString(commentsXML, 'text/xml');
		var newCommentNode = xmlDoc.createElement("commentEntry");
		newCommentNode.setAttribute("date", timestamp);
		newCommentNode.setAttribute("person", userId);
		newCommentNode.appendChild(xmlDoc.createTextNode(newComment));
		var root = xmlDoc.getElementsByTagName("root")[0];
		root.appendChild(newCommentNode);
		
		var xmlDocAsString = (new XMLSerializer()).serializeToString(xmlDoc);
		this._commentsModel.setComments(xmlDocAsString)
		
		//Update UI
		this._commentsView.addCommentWidget(timestamp, userId, newComment);
	}
}