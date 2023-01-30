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

function DrawingBoardsView(drawingBoardsController, drawingBoardsModel) {
	this._drawingBoardsController = drawingBoardsController;
	this._drawingBoardsModel = drawingBoardsModel;
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		
		$container.empty();
		var $wrapper = $('<form>', { 'id' : 'mainDataSetForm', 'role' : 'form'});
		var $title = $('<h2>').append('Drawing Board');
		$header.append($title);
		var $createNewBtn = FormUtil.getButtonWithText('New Drawing Board', function() {
			$container.empty();
			
			var containerWidth = $container.width();
			var containerHeight = $container.height() - 50;
			
			var $drawingboard = $("<div>", { "id" : "scratchboard", "style" : "width: " + containerWidth + "px; height: " + containerHeight + "px; padding: 10px;" });
			$container.append($drawingboard);
			
			//pass options and add custom controls to a board
			var customBoard = new DrawingBoard.Board('scratchboard', {
				background: "#ffffff",
				color: "#000",
				webStorage: false,
				size: 30,
				controls: [
					{ Size: { type: "dropdown" } },
					{ Navigation: { back: false, forward: false } },
					'DrawingMode',
					'Color',
					'Download'
				]
			});
		});
		$header.append($createNewBtn);
		$container.append($wrapper);
	}
}