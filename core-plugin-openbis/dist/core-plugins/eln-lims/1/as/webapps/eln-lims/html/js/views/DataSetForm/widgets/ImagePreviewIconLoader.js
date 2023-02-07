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

/**
 * This loader takes fancytree nodes and replaces the file icons with previews.
 * This is done of the filesize if the image is not bigger than the max size 
 * configured.
 */
function ImagePreviewIconLoader() {

    this._queue = [];

    /**
     * @param node - fancytree node
     */
    this.loadImagePreviewIfNotAlreadyLoaded = function(node) {
        if (this._shouldLoadPreview(node)) {
            this._initLoading(node);
            this._addToQueue(node);
        }
    }

    this._shouldLoadPreview = function(node) {
        return ! node.data.previewLoading && ! node.data.previewLoaded && node.data.fileSize <= profile.datasetViewerMaxFilesizeForImagePreview;
    }

    this._addToQueue = function(node) {
        this._queue.push(node);
        if (this._queue.length == 1) { // no running loading process
            this._processNextQueueElement();
        }
    }

    this._processNextQueueElement = function() {
        if (this._queue.length > 0) {
            var node = this._queue[0];
            this._loadImagePreview({
                node : node,
                done : (function() {
                    this._queue.shift();
                    this._processNextQueueElement();                    
                }).bind(this)
            });
        }
    }

    this._initLoading = function(node) {

        node.data.previewLoading = true;

        var $span = $(node.span);

        var $imageContainer = $("<span>", { id : "image-container" }).css({
            "width" : profile.datasetViewerImagePreviewIconSize + "px",
            "height" : profile.datasetViewerImagePreviewIconSize * 1.2 + "px",
            "display" : "inline-block",
            "vertical-align" : "top",
        });

        var $spinner = $("<img>", { src : "./img/search-spinner.gif"}).css({
            "width" : profile.datasetViewerImagePreviewIconSize + "px",
            "height" : profile.datasetViewerImagePreviewIconSize + "px",
            "vertical-align" : "initial",
        });

        // align the rest of the span to be vertically centered in relation to the image
        var spanMarginTop = profile.datasetViewerImagePreviewIconSize * 0.6 - 8;
        $span.css({ "margin-top" : spanMarginTop + "px" });
        $imageContainer.css({ "margin-top" : -spanMarginTop + 3 + "px" });

        $imageContainer.append($spinner);
        $span.children(".fancytree-expander").after($imageContainer);
        $span.children(".glyphicon-file").remove();
    }

    /**
     * @param params.node - fancytree node
     * @param params.done - callback when loaded
     */
    this._loadImagePreview = function(params) {

        var $span = $(params.node.span);
        var $imageContainer = $span.children("#image-container");
        
        var $iconImg = $("<img>", { src : params.node.data.imageIconUrl }).css({
                "width" : profile.datasetViewerImagePreviewIconSize + "px",
                "height" : (profile.datasetViewerImagePreviewIconSize * 1.2) + "px",
                "margin-top" : "",
            });

        $iconImg.load(function() {
            var naturalWidth = $(this).prop("naturalWidth");
            var naturalHeight = $(this).prop("naturalHeight");            

            var imageSize = Util.getImageSize(
                    profile.datasetViewerImagePreviewIconSize, 
                    profile.datasetViewerImagePreviewIconSize * 1.2, 
                    naturalWidth,
                    naturalHeight);

            var imageMarginTop = (profile.datasetViewerImagePreviewIconSize * 1.2 - imageSize.height ) / 2;
            var imageMarginLeft = (profile.datasetViewerImagePreviewIconSize  - imageSize.width ) / 2;

            $iconImg.css({
                width : "" + imageSize.width + "px",
                height : "" + imageSize.height + "px",
                "margin-top" : imageMarginTop,
                "margin-left" : imageMarginLeft,
            });

            $imageContainer.empty();
            $imageContainer.append($iconImg);

            params.node.data.previewLoaded = true;
            params.node.data.previewLoading = false;
            params.done();
        });
    }

}
