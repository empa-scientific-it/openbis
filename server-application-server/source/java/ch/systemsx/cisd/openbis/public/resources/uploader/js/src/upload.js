//-*- coding: utf-8 -*-
/*
Copyright 2012 Oliver Lau, Heise Zeitschriften Verlag

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/


var Uploader = new function () {
    var defaults = {
        upload_dir: "/uploaded",
        file_upload_url: "/uploader2/upload.php",
        form_upload_url: "/uploader2/form-upload.php",
        drop_area: "#filedrop",
        file_list: "#filelist",
        file_input: "#fileinput",
        upload_form: "#upload-form",
        file_input_button: "#fileinput-button",
        //file_list_clear_button: "#filelist-clear-button",
        oncomplete: function(file) { },
        ondelete: function(file) {},
        chunk_size: 100*1024,
        smart_mode: window.File && window.FileReader && window.XMLHttpRequest,
        main_title_container : "#session-workspace-uploader-main-title",
        uploads_title_container : "#session-workspace-uploader-uploads-title",
        main_title : "<h2>Session workspace upload</h2>",
        uploads_title : "<h2>Uploads</h2>"
    };


    var current_upload_id = 0;
    var current_form_id = 0;
    var progress = {};
    var form = {};
    var settings = defaults;


    function reset() {
        current_upload_id = 0;
        current_form_id = 0;
        progress = {};
        $(settings.file_list).removeClass("visible");
        //$(settings.file_list_clear_button).css("display", "none");
        setTimeout(function() {
            $(settings.file_list).empty();
        }, 256);
    }


    function async_exec(f, ms) {
        ms = ms || 100;
        setTimeout(f, ms);
    }


    this.uploadsInProgress = function() {
        return Object.keys(progress).length > 0;
    }


//    function clearFileList() {
//        if (this.uploadsInProgress()) {
//            $(".ready").addClass("fadeOut");
//            $(".bad").addClass("fadeOut");
//            $(".aborted").addClass("fadeOut");
//            setTimeout(function() { 
//                $(".ready").remove();
//                $(".bad").remove();
//                $(".aborted").remove();
//            }, 256);
//        }
//        else {
//            reset();
//        }
//    }


    function styleSize(n) {
        var prefixes = [ "KB", "MB", "GB" ];
        var prefix = "bytes";
        while (n > 10240 && prefixes.length > 0) {
            n /= 1024;
            prefix = prefixes.shift();
        }
        return Math.round(n) + "&nbsp;" + prefix;
    }


    function makeChunk(file, startByte, endByte) {
        var blob = undefined;
        if (file.slice)
            blob = file.slice(startByte, endByte);
        else if (file.webkitSlice)
            blob = file.webkitSlice(startByte, endByte);
        else if (file.mozSlice)
            blob = file.mozSlice(startByte, endByte);
        return blob;
    }


    function resumeUpload(id) {
        progress[id].pause = false;
        progress[id].abort = false;
        $("#play-button-" + id).remove();
        $("#pause-button").clone().attr("id", "pause-button-" + id)
            .appendTo("#action-bar-" + id)
            .click(function() {
                pauseUpload(id);
            });
        var startByte = progress[id].bytesSent;
        var endByte = startByte + settings.chunk_size;
        if (endByte > progress[id].file.size)
            endByte = progress[id].file.size;
        var blob = makeChunk(progress[id].file, startByte, endByte);
        uploadChunk(progress[id].file, blob, id, startByte, endByte);
    }


    function abortUpload(id) {
        progress[id].abort = true;
        progress[id].xhr.abort();
    }


    function showUploadingError(id) {
        alert("Uploading of \'" + progress[id].file.name + "\' failed");
    }
    

    function pauseUpload(id) {
        progress[id].pause = true;
        $("#pause-button-" + id).remove();
        $("#play-button").clone().attr("id", "play-button-" + id)
            .appendTo("#action-bar-" + id)
            .click(function() {
                resumeUpload(id);
            });
    }
    

    function uploadChunk(file, blob, id, startByte, endByte) {
        if (typeof progress[id] === "undefined" || progress[id].abort || progress[id].pause)
            return;
        var reader = new FileReader;
        reader.onload = function(e) {
            if (e.target.readyState == FileReader.DONE) {
                if (typeof progress[id] === "undefined")
                    return;
                var xhr = new XMLHttpRequest;
                progress[id].xhr = xhr;
                // pkupczyk: added sessionID
                xhr.open("POST", settings.file_upload_url +
                         "?filename=" + encodeURIComponent(file.name) +
                         "&id=" + id +
                         "&startByte=" + startByte +
                         "&endByte=" + endByte + 
                         "&sessionID=" + settings.sessionID,
                         true);
                xhr.setRequestHeader("Content-type", "multipart/form-data");
                xhr.onload = function(e) {
                    var d = JSON.parse(xhr.responseText);
                    if (typeof progress[d.id] === "undefined")
                        return;
                    if (d.status === "ok") {
                        progress[d.id].bytesSent += d.endByte - d.startByte;
                        var secs = 1e-3 * (Date.now() - progress[d.id].startTime);
                        if (progress[d.id].bytesSent < file.size) {
                            var percentage = 100 * progress[d.id].bytesSent / file.size;
                            $("#progressbar-" + d.id).css("width", percentage + "%");
                            $("#speed-" + d.id).html(styleSize(progress[d.id].bytesSent / secs) + "/s");
                            startByte = endByte;
                            endByte += settings.chunk_size;
                            if (endByte > file.size)
                                endByte = file.size;
                            var blob = makeChunk(file, startByte, endByte);
                            uploadChunk(file, blob, id, startByte, endByte);
                        }
                        else {
                            $("#progressbar-" + d.id).addClass("ready");
                            $("#progressbar-" + d.id).css("width", "100%");
                            $("#upload-" + d.id).addClass("ready");
                            $("#speed-" + d.id).html(styleSize(file.size / secs) + "/s");
                            // pkupczyk: changed download url
                            $("#filename-" + d.id).replaceWith("<a target=\"_blank\" " +
                                                               "href=\"" + settings.file_download_url + "?sessionID=" + settings.sessionID + "&filePath=" +
                                                               encodeURIComponent(d.filename) + "\">" + d.filename + "</a>"); 
                            $("#action-bar-" + d.id).remove();
                            delete progress[d.id];
                            settings.oncomplete(file);
                        }
                    }
                    else {
                        $("#progressbar-" + d.id).addClass("bad");
                        $("#upload-" + d.id).addClass("bad");
                        $("#speed-" + d.id).replaceWith("<strong>" + d.message + "</strong>");
                        $("#action-bar-" + d.id).remove();
                        delete progress[d.id];
                    }
                };
                xhr.onabort = function(e) {
                    $("#progressbar-" + id).addClass("aborted");
                    $("#upload-" + id).addClass("aborted");
                    $("#action-bar-" + id).remove();
                    showUploadingError(id);
                    delete progress[id];
                };
                xhr.onerror = function(e) {
                    $("#progressbar-" + id).addClass("bad");
                    $("#upload-" + id).addClass("bad");
                    $("#action-bar-" + id).remove();
                    showUploadingError(id);
                    delete progress[id];
                };
                xhr.send(e.target.result);
            }
        };
        reader.onerror = function(e) {
            switch (e.target.error.code) {
            case e.target.error.NOT_FOUND_ERR:
                alert("File not found.");
                break;
            case e.target.error.NOT_READABLE_ERR:
                alert("File not readable.");
                break;
            case e.target.error.ABORT_ERR:
                console.log("File reading aborted.");
                break;
            default:
                alert("An error occurred while accessing the file.");
                break;
            }
        };
        reader.onabort = function() {
            alert("Reading of the file aborted.");
        };
        reader.readAsArrayBuffer(blob);
    }


    function upload(file) {
        var id = current_upload_id;
        ++current_upload_id;
        $(settings.file_list)
            .append("<li class=\"upload\" id=\"upload-" + id + "\">" +
            		"<span id='delete-" + id + "' class='delete-button'>X</span> " +
                    "<span id=\"progress-" + id + "\" class=\"progressbar-container\">" +
                    "<span id=\"progressbar-" + id + "\" class=\"progressbar\"></span>" + 
                    "</span>" +
                    "<span id=\"action-bar-" + id + "\"></span> " +
                    "<span id=\"filename-" + id + "\">" + file.name + "</span>" +
                    " (" + styleSize(file.size) + ", " +
                    "<span id=\"speed-" + id + "\">? KB/s</span>)" +
                    "</li>");
        $("#delete-"+id).click(function() {
        	if(!progress[id]) {
        		var fileData = file;
            	$( "#upload-"+id).remove();
            	settings.ondelete(fileData);
        	} else {
        		alert("The upload is in progress, please wait.");
        	}
        });
        
        $("#upload-" + id).addClass("starting");
        if (settings.smart_mode) {
            $("#stop-button").clone().attr("id", "stop-button-" + id)
                .appendTo("#action-bar-" + id)
                .click(function() {
                    abortUpload(id);
                });
            $("#pause-button").clone().attr("id", "pause-button-" + id)
                .appendTo("#action-bar-" + id)
                .click(function() {
                    pauseUpload(id);
                });
            progress[id] = {
                file: file,
                startTime: Date.now(),
                bytesSent: 0,
                abort: false,
                pause: false,
                xhr: null
            };
            // 1. Chunk will be uploaded, further chunks will be uploaded via onload-Handler
            var lastByte = (file.size < settings.chunk_size)? file.size : settings.chunk_size;
            var blob = makeChunk(file, 0, lastByte);
            uploadChunk(file, blob, id, 0, lastByte);
        }
        else {
            $("#progressbar-" + id).css("width", "100%");
        }
    }


    function uploadFiles(files) {
        if (settings.singleFile && files.length > 1) {
            alert("You can drop only one file.");
            return false;
        }
        $(settings.file_list).addClass("visible");
        //$(settings.file_list_clear_button).css("display", "inline");
        if (typeof files === "object" && files.length > 0) {
            $.each(files, function() { upload(this); });
        }
    }


    function generateUploadForm() {
        var id = ++current_form_id;
        $("#iframe-container")
        	// pkupczyk: added sessionID
            .append("<iframe id=\"iframe-" + id + "\"" +
                    " name=\"iframe-" + id + "\"" +
                    "></iframe>" +
                    "<form action=\"" + settings.form_upload_url + "?sessionID=" + settings.sessionID + "\"" +
                    " target=\"iframe-" + id + "\"" +
                    " id=\"form-" + id + "\"" +
                    " enctype=\"multipart/form-data\"" +
                    " method=\"POST\">" +
                    "<input type=\"file\" " + (settings.singleFile ? "" : "multiple") + " name=\"files[]\"" +
                    " id=\"fileinput-" + id + "\" />" +
                    "</form>");
        $("#fileinput-" + id)
            .bind("change", function(event) {
                var files = event.target.files;
                uploadFiles(files);
                if (!settings.smart_mode) {
                    generateUploadForm();
                    event.target.form.submit();
                }
                event.preventDefault();
                return false;
            })
        $(settings.file_input_button)
            .unbind("click")
            .bind("click", function() {
                $("#fileinput-" + id).trigger("click");
            });
        $("#iframe-" + id).bind("load", { id: id }, function(event) {
            var id = event.data.id,
            iframe = document.getElementById("iframe-" + id).contentDocument;
            if (iframe.location.href !== "about:blank") {
                $(".progressbar").addClass("ready");
                $("#iframe-" + id).remove();
                $("#form-" + id).remove();
            }
        });
    }

    this.init = function(opts) {
            // Checks whether the browser can show SVG, if not PNGs are used
            var svgSupported = (function() {
                var svg;
                try {
                    svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                } catch (e) { console.log(e); }
                return typeof svg !== "undefined" ||
                    (navigator.userAgent.match(/(Safari|MSIE [5-9])/)
                     && !navigator.userAgent.match(/Chrome/));
            })();
            if (!svgSupported) {
                $("#upload-icon").replaceWith("<img id=\"upload-icon\" src=\"img/upload-icon.png\" width=\"200\" height=\"140\">");
                $("#play-button").replaceWith("<img id=\"play-button\" src=\"img/play-button.png\" width=\"12\" height=\"12\" class=\"mini-button\">");
                $("#stop-button").replaceWith("<img id=\"stop-button\" src=\"img/stop-button.png\" width=\"12\" height=\"12\" class=\"mini-button\">");
                $("#pause-button").replaceWith("<img id=\"pause-button\" src=\"img/pause-button.png\" width=\"12\" height=\"12\" class=\"mini-button\">");
            }
            // Site-spezifische Einstellungen aus Konfigurationsdatei lesen
            
            /* pkupczyk: we don't need this
             
            $.ajax("config.json", { async: false })
                .done(function(data) {
                    settings = $.extend({}, settings, data);
                })
                .error(function(jqXHR, textStatus, errorThrown) {
                    console.log([jqXHR, textStatus, errorThrown]);
                });
            */
            
            // Settings might be overwritten by init() parameters
            settings = $.extend({}, settings, opts);
            settings.smart_mode = settings.smart_mode && defaults.smart_mode;
            
            // pkupczyk: we do not provide listing of uploaded files yet
            //$("h2 > a").attr("href", settings.upload_dir);
            var fileOrFilesText = settings.singleFile ? "file" : "files";
            var buttonLabel = "Select " + fileOrFilesText + " to upload";
            if (settings.singleFile) {
                $("#fileinput-button").text(buttonLabel);
                $("#fileinput").removeAttr("multiple");
            }
            if (settings.smart_mode) {
                $("#filedrop-hint").html("Drag and drop the " + fileOrFilesText + " to upload here or click '" 
                        + buttonLabel + "' to upload' button.");
                $(settings.file_input)
                    .bind("change", function(event) {
                        uploadFiles(event.target.files);
                    });
                $(settings.file_input_button)
                    .click(function() {
                        $(settings.file_input).trigger("click");
                    });
                $(settings.drop_area).bind(
                    {
                        dragover: function(event) {
                            var e = event.originalEvent;
                            e.stopPropagation();
                            e.preventDefault();
                            e.dataTransfer.dropEffect = "copy";
                            $(settings.drop_area).addClass("over");
                        }
                        ,
                        dragleave: function(event) {
                            var e = event.originalEvent;
                            e.stopPropagation();
                            e.preventDefault();
                            $(settings.drop_area).removeClass("over");
                        }
                        ,
                        drop: function(event) {
                            var e = event.originalEvent;
                            e.stopPropagation();
                            e.preventDefault();
                            $(settings.drop_area).removeClass("over");
                            uploadFiles(e.dataTransfer.files);
                        }
                    }
                );
            }
            else { // fallback mode
                $("#filedrop-hint").html("Click '" + buttonLabel + "' button.");
                generateUploadForm();
            }
            //$(settings.file_list_clear_button).click(clearFileList);
            $("#filedrop-hint").append("<br/>Upload starts immediately after the file selection.");
            
        	if(settings.hideHint) {
        		$("#filedrop-hint").hide();
        		$("#session-workspace-uploader-uploads-title").hide();
        	}
        	
            //Setting titles
            $(settings.main_title_container).append(settings.main_title);
            $(settings.uploads_title_container).append(settings.uploads_title);
        }
}
