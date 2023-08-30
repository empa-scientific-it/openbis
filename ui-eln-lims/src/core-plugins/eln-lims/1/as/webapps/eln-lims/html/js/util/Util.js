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

/**
 * Utility class Util, created as anonimous, it contains utility methods mainly to show messages.
 *
 * Contains methods used for common tasks.
 */
var Util = new function() {
	
    this.reloadApplication = function(popupMessage) {
        alert(popupMessage);
        window.location.reload();
    }

    this.download = function(content, mimeType, isBinary, filename) {
        var link = document.createElement('a');
        link.href = "data:" + mimeType + ";" + (isBinary ? "base64," : ",") + content;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

    }

	//
	// Methods to block user input
	//
	this.blockUINoMessage = function() {
		this.unblockUI();
		$('#navbar').block({ message: '', css: { width: '0px' } });
		$.blockUI({ message: '', css: { width: '0px' } });
	}
	
	this.blockUIConfirm = function(message, okAction, cancelAction) {
		var $messageWithOKAndCancel = $("<div>").append(message);
		
		var $ok = FormUtil.getButtonWithText("Accept", okAction);
		
		var $cancel = FormUtil.getButtonWithText("Cancel", function() {
			if(cancelAction) {
				cancelAction();
			}
			Util.unblockUI();
		});
		
		$messageWithOKAndCancel.append($("<br>")).append($ok).append("&nbsp;").append($cancel);
		this.blockUI($messageWithOKAndCancel, {
			'text-align' : 'left'
		});
	}
	
	this.showDropdownAndBlockUI = function(id, $dropdown, help) {
        var html = "";
        if(help) {
            html = "<div class='glyphicon glyphicon-info-sign'></div><span> " + help + "</span></br></br>";
        }
        html += $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='" + id + "Cancel'>Cancel</a>";
		Util.blockUI(html);
		$("#" + id).select2({ width: '100%', theme: "bootstrap" });
	}
	
	this.blockUI = function(message, extraCSS, disabledFadeAnimation, onBlock) {
		this.unblockUI();
		
		var css = { 
					'border': 'none', 
					'padding': '10px',
					'-webkit-border-radius': '6px 6px 6px 6px', 
					'-moz-border-radius': '6px 6px 6px 6px', 
					'border-radius' : '6px 6px 6px 6px',
					'box-shadow' : '0 1px 10px rgba(0, 0, 0, 0.1)',
					'cursor' : 'default'
		};
		
		if(extraCSS) {
			for(extraCSSProperty in extraCSS) {
				var extraCSSValue = extraCSS[extraCSSProperty];
				css[extraCSSProperty] = extraCSSValue;
			}
		}
		
		$('#navbar').block({ message: '', css: { width: '0px' } });
		var params = { css : css };
		if (message) {
			params.message = message;
		} else {
			params.message = '<img src="./img/busy.gif" />';
		}
		if (disabledFadeAnimation) {
			params.fadeIn = 0;
			params.fadeOut = 0;
		}
		if (onBlock) {
			params.onBlock = onBlock;
		}
		$.blockUI(params);
	}
	
	//
	// Methods to allow user input
	//
	this.unblockUI = function(callback) {
		$('#navbar').unblock();
		$.unblockUI({ 
			onUnblock: function() {
				window.setTimeout(function() { //Enable after all possible enable/disable events happen
					if (callback) {
						callback();
					}
				}, 150);
			}
		});
	}
	
	//
	// Methods to show messages as pop ups
	//
	this.showStacktraceAsError = function(stacktrace) {
		var isUserFailureException = 	stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") === 0;
		var isAuthorizationException = 	stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.AuthorizationFailureException") === 0;
		var isNestedUserFailureException = stacktrace.lastIndexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") > 0;

		var startIndex = null;
		var endIndex = null;
		if(isUserFailureException) {
			startIndex = "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
			endIndex = stacktrace.indexOf("at ch.systemsx");
		} else if(isAuthorizationException) {
			startIndex = "ch.systemsx.cisd.common.exceptions.AuthorizationFailureException".length + 2;
			endIndex = stacktrace.indexOf("at ch.systemsx");
		} else if(isNestedUserFailureException) {
            startIndex = stacktrace.lastIndexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") + "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
            endIndex = stacktrace.length;
        } else {
			startIndex = 0;
			endIndex = stacktrace.length;
		}
		var errorMessage = stacktrace.substring(startIndex, endIndex).trim();
		Util.showError(errorMessage, function() {Util.unblockUI();}, undefined, isUserFailureException || isAuthorizationException);
	}
	
	this.showWarning = function(text, okCallback, notUnblockOnAccept) {
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '20%',
				'background-color' : '#fcf8e3',
				'border-color' : '#faebcc',
				'color' : '#8a6d3b',
				'overflow' : 'auto'
		};
		
		var bootstrapWarning = "<strong>Warning!</strong></br></br>" + text;
		Util.blockUI(bootstrapWarning + "<br><br><br> <a class='btn btn-primary' id='warningAccept'>Accept</a> <a class='btn btn-default' id='warningCancel'>Cancel</a>", css);
		
		$("#warningAccept").on("click", function(event) {
			okCallback();
			if(!notUnblockOnAccept) {
			    Util.unblockUI();
			}
		});
		
		$("#warningCancel").on("click", function(event) { 
			Util.unblockUI();
		});
	}
	
	this.showUserError = function(message, andCallback, noBlock) {
		this.showError(message, andCallback, noBlock, true, false, true);
	}
	
	this.showFailedServerCallError = function(error) {
		var msg = error["message"]
		this.showError("Call failed to server: " + (msg ? msg : JSON.stringify(error)));
	}
	
	this.showError = function(message, andCallback, noBlock, isUserError, isEnvironmentError, disableReport) {
		var userErrorWarning = "";
		if(isUserError) {
			userErrorWarning = "<b>This error looks like a user error:</b>" + "<br>";
		}

		var $dismissButton = $("<a>", { id : 'jNotifyDismiss', class : 'btn btn-default'}).append('Dismiss');
        var $withHTMLToShow = $("<div>", {style : 'width:100%;'})
                                    .append($("<textarea>", { style : 'background: transparent; border: none; width:100%;', rows : '1'}).append(DOMPurify.sanitize(message)))
                                    .append($("<br>"))
                                    .append($dismissButton);

		if(!noBlock) {
			this.blockUINoMessage();
		}
		
		var localReference = this;
		var popUp = jError(
				$withHTMLToShow,
				{
				  autoHide : false,
				  clickOverlay : false,
				  MinWidth : '80%',
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();} else { localReference.unblockUI();}},
				  onCompleted : function(){ }
		});
		
		$dismissButton.click(function(e) {
			popUp._close();
		});
	}
	
	this.showSuccess = function(message, andCallback, forceAutoHide) {
		var localReference = this;
		jSuccess(
				DOMPurify.sanitize(message),
				{
				  autoHide : true,
				  clickOverlay : true,
				  MinWidth : '80%',
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
				  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();} else { }},
				  onCompleted : function(){ }
		});
	}
	
	this.showInfo = function(message, andCallback, noBlock, buttonLabel) {
		
		if(!noBlock) {
			this.blockUINoMessage();
		}
		if (!buttonLabel) {
			var buttonLabel = "Dismiss";
		}

		var $dismissButton = $("<a>", { id : 'jNotifyDismiss', class : 'btn btn-default'}).append(buttonLabel);
		var $withHTMLToShow = $("<span>").append(DOMPurify.sanitize(message)).append($("<br>")).append($dismissButton);

		var localReference = this;
		var popUp = jNotify(
				$withHTMLToShow,
				{
				  autoHide : false,
				  clickOverlay : false,
				  MinWidth : '80%',
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();} else { localReference.unblockUI();}},
				  onCompleted : function(){ }
		});
		
		$dismissButton.click(function(e) {
			popUp._close();
		});
	}

	this.mapValuesToList = function(map) {
		var list = [];
		for(e in map) {
			list.push(map[e]);
		}
		return list;
	}
	
	this.getDirectLinkWindows = function(protocol, config, path) {
		var hostName = window.location.hostname;
		var suffix = config.UNCsuffix;
		if(!suffix) {
			suffix = ""; 
		}
		var port = config.port;
	
		return "\\\\" + hostName + "\\" + (new String(suffix + path).replace(new RegExp("/", 'g'),"\\"));
	}
	
	this.getDirectLinkUnix = function(protocol, config, path) {
		var hostName = window.location.hostname;
		var suffix = config.UNCsuffix;
		if(!suffix) {
			suffix = ""; 
		}
		var port = config.port;
	
		return protocol + "://" + hostName + ":" + port + "/" + suffix + path;
	}
	
	this.showDirectLink = function(path) {
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '80%',
				'left' : '10%',
				'right' : '10%',
				'overflow' : 'hidden'
		};
		
		var isWindows = window.navigator.userAgent.toLowerCase().indexOf("windows") > -1;
		var sftpLink = null;
		
		if(isWindows) {
			if(profile.sftpFileServer) {
				sftpLink = this.getDirectLinkUnix("sftp", profile.sftpFileServer, path);
			}
		} else {
			if(profile.sftpFileServer) {
				sftpLink = this.getDirectLinkUnix("sftp", profile.sftpFileServer, path);
			}
		}
		
		var $close = $("<div>", { style : "float:right;" })
						.append($("<a>", { class : 'btn btn-default' }).append($("<span>", { class : 'glyphicon glyphicon-remove' }))).click(function() {
							Util.unblockUI();
		});
		
		var $window = $("<div>").append($close).append($("<h1>").append("Direct Links"));
		$window.append("To access the folder though the network you have the next options:").append($("<br>"));
		$window.css("margin-bottom", "10px");
		$window.css("margin-left", "10px");
		$window.css("margin-right", "10px");
		
		if(isWindows) {
			if(profile.sftpFileServer) {
				$window.append("<b>SFTP Link: </b>").append($("<br>")).append($("<a>", { "href" : sftpLink, "target" : "_blank"}).append(sftpLink)).append($("<br>"));
				$window.append("NOTE: The SFTP link can be opened with your favourite SFTP application, we recomend ").append($("<a>", { "href" : "https://cyberduck.io/", "target" : "_blank"}).append("Cyberduck")).append(".");
			}
		} else {
			if(profile.sftpFileServer) {
				$window.append($("<b>SFTP Link: </b>")).append($("<a>", { "href" : sftpLink, "target" : "_blank"}).append(sftpLink)).append($("<br>"));
			}
			
			$window.append("NOTE: Directly clicking on the links will open them with the default application. ").append("For SFTP we recomend ").append($("<a>", { "href" : "https://cyberduck.io/", "target" : "_blank"}).append("Cyberduck")).append(".");
		}
		
		Util.blockUI($window, css);
	}
	
	//HACK: This method is intended to be used by naughty SVG images that don't provide a correct with/height and don't resize correctly
	this.loadSVGImage = function(imageURL, containerWidth, containerHeight, callback, isSEQSVG) {
		d3.xml(imageURL, "image/svg+xml", 
				function(xml) {
					var importedNode = document.importNode(xml.documentElement, true);
					var d3Node = d3.select(importedNode);
					
					var imageWidth = d3Node.style("width");
					var imageHeight = d3Node.style("height");
					if((imageWidth === "auto" || imageHeight === "auto") || //Firefox some times
						(imageWidth === "" || imageHeight === "")) { //Safari and Chrome under any case
						imageWidth = containerWidth;
						imageHeight = containerHeight;
					} else if(imageWidth.indexOf("px") !== -1 || imageHeight.indexOf("px") !== -1) { //Firefox some times
						imageWidth = parseFloat(imageWidth.substring(0,imageWidth.length - 2));
						imageHeight = parseFloat(imageHeight.substring(0,imageHeight.length - 2));
					}
					
					if(containerWidth < imageWidth) {
						var newImageWidth = containerWidth;
						var newImageHeight = imageHeight * newImageWidth / imageWidth;
						
						imageWidth = newImageWidth;
						imageHeight = newImageHeight;
					}
					
					if(containerHeight < imageHeight) {
						var newImageHeight = containerHeight;
						var newImageWidth = imageWidth * newImageHeight / imageHeight;
						
						imageWidth = newImageWidth;
						imageHeight = newImageHeight;
					}
					
					if(isSEQSVG) {
						var size = (containerWidth > containerHeight)?containerHeight:containerWidth;
						d3Node.attr("width", size)
							.attr("height", size)
							.attr("viewBox", 0 + " " + 0 + " " + (size * 1.1) + " " + (size * 1.1));
					} else {
						d3Node.attr("width", imageWidth)
							.attr("height", imageHeight)
							.attr("viewBox", "0 0 " + imageWidth + " " + imageHeight);
					}
					
					
					callback($(importedNode));
		});
	}
	
	this.showImage = function(imageURL, isSEQSVG) {
		
		var showImageB = function($image) {
			var $imageWrapper = $("<div>", {"style" : "margin:10px"});
			$imageWrapper.append($image);
			
			var imageHTML = $imageWrapper[0].outerHTML;
						
			Util.blockUINoMessage();
			var popUp = jNotifyImage("<div style='text-align:right;'><a id='jNotifyDismiss' class='btn btn-default'><span class='glyphicon glyphicon-remove'></span></a></div>" + imageHTML,
					{
					  autoHide : false,
					  clickOverlay : false,
					  // MinWidth : '80%',
					  TimeShown : 2000,
					  ShowTimeEffect : 200,
					  HideTimeEffect : 200,
					  LongTrip :20,
					  HorizontalPosition : 'center',
					  VerticalPosition : 'center',
					  ShowOverlay : false,
			   		  ColorOverlay : '#000',
					  OpacityOverlay : 0.3,
					  onClosed : function(){ Util.unblockUI(); },
					  onCompleted : function(){ }
					});
			$("#jNotifyDismiss").click(function(e) {
				popUp._close();
			});
		};
		
		var containerWidth = $(window).width()*0.85;
		var containerHeight = $(window).height()*0.85;
		
		if(imageURL.toLowerCase().indexOf(".svg?sessionid") !== -1) {
			this.loadSVGImage(imageURL, containerWidth, containerHeight, showImageB, isSEQSVG);
			return;
		}
		
		var $image = $("<img>", {"src" : imageURL});
		$image.load((function() {
			var imageSize = this.getImageSize(containerWidth, containerHeight, $image[0].width, $image[0].height);
			$image.attr("width", imageSize.width);
			$image.attr("height", imageSize.height);
			showImageB($image);
		}).bind(this));
	}

	this.getImageSize = function(containerWidth, containerHeight, imageWidth, imageHeight) {
		
		if(containerWidth < imageWidth) {
			var newImageWidth = containerWidth;
			var newImageHeight = imageHeight * newImageWidth / imageWidth;
			
			imageWidth = newImageWidth;
			imageHeight = newImageHeight;
		}
		
		if(containerHeight < imageHeight) {
			var newImageHeight = containerHeight;
			var newImageWidth = imageWidth * newImageHeight / imageHeight;
			
			imageWidth = newImageWidth;
			imageHeight = newImageHeight;
		}

		return {width : imageWidth, height : imageHeight};		
	}

	//
	// Date Formating
	//
	this.parseDate = function(dateAsString) {
		if(dateAsString) {
			var yearTimeAndOffset = dateAsString.split(" ");
			var yearParts = yearTimeAndOffset[0].split("-");
			if (yearTimeAndOffset.length == 1) {
				return new Date(yearParts[0],parseInt(yearParts[1])-1,yearParts[2]);
			}
			var timeParts = yearTimeAndOffset[1].split(":");
			return new Date(yearParts[0],parseInt(yearParts[1])-1,yearParts[2], timeParts[0], timeParts[1], timeParts[2]);
		}
		return null;
	}

	this.isDateValid = function(dateAsString, isDateOnly) {
        if (dateAsString) {
            var timeValueObject = Util.parseDate(dateAsString);

            if(timeValueObject.getFullYear() !== parseInt(dateAsString.substring(0,4))) {
                isValid = false;
                error = "Incorrect Date Format. Please follow the format " + (isDateOnly ? 'yyyy-MM-dd (YEAR-MONTH-DAY)' : 'yyyy-MM-dd HH:mm:ss (YEAR-MONTH-DAY : HOUR-MINUTE-SECOND)') + ".";
            } else {
                isValid = true;
            }

            return {
                isValid : isValid,
                error : error
            };
        }
        return { isValid : true}
	}
	
	this.getFormatedDate = function(date) {
		var day = date.getDate();
		if(day < 10) {
			day = "0" + day;
		}
		var month = date.getMonth()+1;
		if(month < 10) {
			month = "0" + month;
		}
		var year = date.getFullYear();
		var hour = date.getHours();
		if(hour < 10) {
			hour = "0" + hour;
		}
		var minute = date.getMinutes();
		if(minute < 10) {
			minute = "0" + minute;
		}
		var second = date.getSeconds();
		if(second < 10) {
			second = "0" + second;
		}
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
	}
	
	//
	// Other
	//
	this.getMapAsString = function(map, length) {
		var mapAsString = "";
		for(key in map) {
			if(mapAsString.length > 0) {
				mapAsString += " , ";
			}
			mapAsString += "<b>" + key + "</b> : " + map[key];
		}
		
		if(length && mapAsString.length > length) {
			mapAsString = mapAsString.substring(0, length) + " ...";
		}
		return mapAsString;
	}
	
	this.getEmptyIfNull = function(toCheck) {
		if(	toCheck === undefined ||
			toCheck === null ||
			toCheck === "�(undefined)") {
			return "";
		} else {
			return toCheck;
		}
	}
	
	this.replaceURLWithHTMLLinks = function(text) {
	    var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	    return text.replace(exp,"<a href='$1' target='_blank'>$1</a>"); 
	}
	
	this.queryString = function() {
		return function () {
			  // This function is anonymous, is executed immediately and 
			  // the return value is assigned to QueryString!
			  var query_string = {};
			  var query = window.location.search.substring(1);
			  var vars = query.split("&");
			  for (var i=0;i<vars.length;i++) {
			    var pair = vars[i].split("=");
			    	// If first entry with this name
			    if (typeof query_string[pair[0]] === "undefined") {
			      query_string[pair[0]] = pair[1];
			    	// If second entry with this name
			    } else if (typeof query_string[pair[0]] === "string") {
			      var arr = [ query_string[pair[0]], pair[1] ];
			      query_string[pair[0]] = arr;
			    	// If third or later entry with this name
			    } else {
			      query_string[pair[0]].push(pair[1]);
			    }
			  } 
			    return query_string;
		} ();
	}
	
	this.guid = function() {
		  var s4 = function() {
		    return Math.floor((1 + Math.random()) * 0x10000)
		               .toString(16)
		               .substring(1);
		  }
		  
		  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
		           s4() + '-' + s4() + s4() + s4();
	};
	
	this.getNameOrCode = function(data) {
		var name = data[profile.propertyReplacingCode];
		if (name) {
			return name;
		}
		if (data.code) {
			return data.code;
		}
		var identifierSegments = data.identifier.split('/');
		return identifierSegments[identifierSegments.length - 1];
	}

	this.getDisplayNameForEntity = function(entity) {
		var displayName = "";
		if(profile.propertyReplacingCode && 
			entity.properties && 
			entity.properties[profile.propertyReplacingCode]) {
			displayName = entity.properties[profile.propertyReplacingCode];
		} else if(entity["@type"] === "as.dto.project.Project" || entity["@type"] === "as.dto.space.Space") {
			displayName = this.getDisplayNameFromCode(entity.code);
		} else {
			displayName = entity.code;
		}
		return displayName;
	}
	
	this.getDisplayNameForEntity2 = function(entity) {
		var text = null;
		if(entity["@type"] === "as.dto.dataset.DataSet") {
			text = entity.permId.permId;
			if(profile.propertyReplacingCode && entity.properties && entity.properties[profile.propertyReplacingCode]) {
				text += " (" + entity.properties[profile.propertyReplacingCode] + ")";
			}
			if(entity.sample) {
				text += " " + ELNDictionary.Sample + " [" + Util.getDisplayNameForEntity2(entity.sample) + "]";
			}
			
			if(entity.experiment) {
				text += " " + ELNDictionary.getExperimentDualName() + " [" + Util.getDisplayNameForEntity2(entity.experiment) + "]";
			}
		} else {
			if(entity.identifier && entity.identifier.identifier) {
				text = entity.identifier.identifier;
			}
			if(!entity.identifier && entity.code) {
				text = Util.getDisplayNameFromCode(entity.code);
			}
			if(profile.propertyReplacingCode && entity.properties && entity.properties[profile.propertyReplacingCode]) {
				text += " (" + entity.properties[profile.propertyReplacingCode] + ")";
			}
		}
		return text;
	}
	
    this.getDisplayLabelFromCodeAndDescription = function(codeAndDescription) {
        var label = Util.getDisplayNameFromCode(codeAndDescription.code);
        var description = Util.getEmptyIfNull(codeAndDescription.description);
        if (description !== "") {
            label += " (" + description + ")";
        }
        return label;
    }
	
	this.getDisplayNameFromCode = function(openBISCode) {
		var normalizedCodeParts = openBISCode.toLowerCase().split('_');
		var displayName = "";
		for(var i = 0; i < normalizedCodeParts.length; i++) {
			if(i > 0) {
				displayName += " ";
			}
			displayName += normalizedCodeParts[i].capitalizeFirstLetter();
		}
		return displayName;
	}
	
	this.getStoragePositionDisplayName = function(sample) {
		var storageData = sample.properties;
		var storagePropertyGroup = profile.getStoragePropertyGroup();
							
		var codeProperty = storageData[storagePropertyGroup.nameProperty];
		if(!codeProperty) {
			codeProperty = "NoCode";
		}
		var rowProperty = storageData[storagePropertyGroup.rowProperty];
		if(!rowProperty) {
			rowProperty = "NoRow";
		}
		var colProperty = storageData[storagePropertyGroup.columnProperty];
		if(!colProperty) {
			colProperty = "NoCol";
		}
		var boxProperty = storageData[storagePropertyGroup.boxProperty];
		if(!boxProperty) {
			boxProperty = "NoBox";
		}
		var positionProperty = storageData[storagePropertyGroup.positionProperty];
		if(!positionProperty) {
			positionProperty = "NoPos";
		}
		var displayName = codeProperty + " [ " + rowProperty + " , " + colProperty + " ] " + boxProperty + " - " + positionProperty;
		return displayName;
	}
	//
	// Grid related function
	//
	var alphabet = [null,'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
	this.getLetterForNumber = function(number) { //TODO Generate big numbers
		return alphabet[number];
	}
	
	this.getNumberFromLetter = function(letter) { //TODO Generate big numbers
		for(var i = 0; i < alphabet.length; i++) {
			if(letter === alphabet[i]) {
				return i;
			}
		}
		return null;
	}
	
	this.getXYfromLetterNumberCombination = function(label) {
		var parts = label.match(/[a-zA-Z]+|[0-9]+/g);
		var row = this.getNumberFromLetter(parts[0]);
		var column = parseInt(parts[1]);
		return [row, column];
	}
	
	this.manageError = function(err) {
	    var errorString = null;
	    if(err.stack) {
	        errorString = err.stack.toString();
	    } else {
	        errorString = err.toString();
	    }
		Util.showError(errorString);
	}
	
	//
	// URL Utils
	//
	this.getURLFor = function(menuId, view, argsForView) {
		var viewData = null;
		if((typeof arg) !== "string") {
			viewData = JSON.stringify(argsForView);
		} else {
		    viewData = argsForView;
		}
		return window.location.href.split("?")[0] + "?menuUniqueId=" +  menuId+ "&viewName=" + view + "&viewData=" +
				viewData;
	}
	
	//
	// TSV Export
	//
	this.downloadTSV = function(arrayOfRowArrays, fileName) {
		for(var rIdx = 0; rIdx < arrayOfRowArrays.length; rIdx++) {
			for(var cIdx = 0; cIdx < arrayOfRowArrays[rIdx].length; cIdx++) {
				var value = arrayOfRowArrays[rIdx][cIdx];
				if(!value) {
					value = "";
				}
				value = String(value).replace(/\r?\n|\r|\t/g, " "); //Remove carriage returns and tabs
				arrayOfRowArrays[rIdx][cIdx] = value;
			}
		}
		
		var tsv = $.tsv.formatRows(arrayOfRowArrays);
		var indexOfFirstLine = tsv.indexOf('\n');
		var tsvWithoutNumbers = tsv.substring(indexOfFirstLine + 1);
		var blob = new Blob([tsvWithoutNumbers], {type: 'text'});
		saveAs(blob, fileName);
	}
	
	this.downloadTextFile = function(content, fileName) {
		var contentEncoded = null;
		var out = null;
		var charType = null;
		
		contentEncoded = content;
		out = new Uint8Array(contentEncoded.length);
		for(var ii = 0,jj = contentEncoded.length; ii < jj; ++ii){
			out[ii] = contentEncoded.charCodeAt(ii);
		}
		charType = 'text/tsv;charset=UTF-8;';
		
		var blob = new Blob([out], {type: charType});
		saveAs(blob, fileName);
	}
	
	this.mergeObj = function(obj1, obj2) {
        var obj3 = {};
        for (var attrname in obj1) { obj3[attrname] = obj1[attrname]; }
        for (var attrname in obj2) { obj3[attrname] = obj2[attrname]; }
        return obj3;
    };
    
	//
	// Components Resize events
	//
	this.dragContainerFunc = function(e) {
	    var menu = $('#sideMenu');
	    var drag = $('#dragContainer');
	    var main = $('#mainContainer');
	    var mouseX = e.pageX;
	    var windowWidth = window.outerWidth;
	    var mainWidth = windowWidth - mouseX;
	    menu.css("width", mouseX + "px");
	    main.css("width", mainWidth + "px");
	    main.css("left", mouseX);
	    drag.css("left", mouseX);
    };
    
    this.elementEndsWithArrayElement = function(element, elementsToEndWith) {
    		for(var aIdx = 0; aIdx < elementsToEndWith.length; aIdx++) {
    		    var elementToEndWith = elementsToEndWith[aIdx];
    			if((typeof elementToEndWith === 'string' || elementToEndWith instanceof String) && element.endsWith(elementToEndWith)) {
    				return true;
    			}
    		}
    		return false;
	}

	this.isInPage = function(node) {
      return (node === document.body) ? false : document.body.contains(node);
    };

    this.onIsInPage = function(node, action) {
        var _polling = function() {
            if(Util.isInPage(node)) {
                action();
            } else {
                setTimeout(_polling, 50);
            }
        }

        setTimeout(_polling, 50);
    };

    this.isMapEmpty = function(map) {
        return Object.entries(map).length === 0 && map.constructor === Object;
    }
    
    this.requestArchiving = function(dataSets, callback) {
        if (dataSets.length === 0) {
            Util.showInfo("No datasets selected, nothing will be done.", callback);
            return;
        }

        var archivingRequested = false;
        for(var aIdx = 0; aIdx < dataSets.length; aIdx++) {
            var dataSet = dataSets[aIdx];
            archivingRequested = archivingRequested || dataSet.archivingRequested || (dataSet.physicalData && dataSet.physicalData.archivingRequested);
        }

        if(archivingRequested) {
            Util.showInfo("Some selected datasets are already queued for archiving, please unselect them before making the request, nothing will be done now.", callback);
            return;
        }

        var $window = $('<form>', { 'action' : 'javascript:void(0);' });
        $window.submit(function() {
            require([ "as/dto/dataset/update/DataSetUpdate", "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/update/PhysicalDataUpdate"],
                    function(DataSetUpdate, DataSetPermId, PhysicalDataUpdate) {
                        var updates = dataSets.map(function(dataSet) {
                            var update = new DataSetUpdate();
                            var permId = dataSet.permId.permId ? dataSet.permId.permId : dataSet.permId;
                            update.setDataSetId(new DataSetPermId(permId));
                            var physicalDataUpdate = new PhysicalDataUpdate();
                            physicalDataUpdate.setArchivingRequested(true);
                            update.setPhysicalData(physicalDataUpdate);
                            return update;
                        });
                        Util.blockUI();
                        mainController.openbisV3.updateDataSets(updates).done(function(result) {
                            Util.unblockUI();
                            Util.showSuccess("Archiving requested successfully", callback);
                        }).fail(function(result) {
                            Util.unblockUI();
                            Util.showFailedServerCallError(result);
                            callback();
                        });
                    });
            });

        $window.append($('<legend>').append('Request archiving'));

        var description = dataSets.length === 1 ? "data set" : dataSets.length + " data sets";
        var warning = "Your " + description + " will be queued for archiving " +
                "and will only be archived when the minimum size" +
                " is reached from this or other archiving requests.";
        var $warning = $('<p>').text(warning);
        $window.append($warning);

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
        var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
        $btnCancel.click(function() {
            Util.unblockUI();
        });

        $window.append($btnAccept).append('&nbsp;').append($btnCancel);

        var css = {
                'text-align' : 'left',
                'top' : '15%',
                'width' : '70%',
                'left' : '15%',
                'right' : '20%',
                'overflow' : 'hidden',
                'background' : '#ffffbf'
        };

        Util.blockUI($window, css);
    }
}



			
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function (prefix) {
    return this.slice(0, prefix.length) == prefix;
};

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

Array.prototype.uniqueOBISEntity = function() {
    var a = this.concat();
    for(var i=0; i<a.length; ++i) {
        for(var j=i+1; j<a.length; ++j) {
            if(a[i].identifier === a[j].identifier)
                a.splice(j--, 1);
        }
    }

    return a;
};

Array.prototype.unique = function() {
    var a = this.concat();
    for(var i=0; i<a.length; ++i) {
        for(var j=i+1; j<a.length; ++j) {
            if(a[i] === a[j])
                a.splice(j--, 1);
        }
    }

    return a;
};