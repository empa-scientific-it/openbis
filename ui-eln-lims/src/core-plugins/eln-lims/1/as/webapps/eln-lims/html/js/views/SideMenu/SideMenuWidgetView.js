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
 * Creates an instance of SideMenuWidgetView.
 *
 * @constructor
 * @this {SideMenuWidgetView}
 */
function SideMenuWidgetView(sideMenuWidgetController, sideMenuWidgetModel) {
    this._sideMenuWidgetController = sideMenuWidgetController
    this._sideMenuWidgetModel = sideMenuWidgetModel

    var MIN_LENGTH = 3

    this.repaint = function ($container) {
        this._$container = $container
        var $widget = $("<div>", { id: "sideMenuTopContainer" })
        $widget.css("height", "100%")
        $widget.css("display", "flex")
        $widget.css("flex-direction", "column")
        //
        // Fix Header
        //
        var $header = $("<div>", { id: "sideMenuHeader" })
        $header.css("background-color", "rgb(248, 248, 248)")
        $header.css("padding", "10px")
        var searchDomains = profile.getSearchDomains()

        var searchFunction = function () {
            var searchText = $("#search").val()
            if (searchText.length < MIN_LENGTH) {
                Util.showInfo(
                    "The minimum length for a global text search is " + MIN_LENGTH + " characters.",
                    function () {},
                    true
                )
                return false
            }

            var domainIndex = $("#search").attr("domain-index")
            var searchDomain = null
            var searchDomainLabel = null

            if (domainIndex) {
                searchDomain = profile.getSearchDomains()[domainIndex].name
                searchDomainLabel = profile.getSearchDomains()[domainIndex].label
            } else {
                searchDomain = profile.getSearchDomains()[0].name
                searchDomainLabel = profile.getSearchDomains()[0].label
            }

            var argsMap = {
                searchText: searchText,
                searchDomain: searchDomain,
                searchDomainLabel: searchDomainLabel,
            }
            var argsMapStr = JSON.stringify(argsMap)

            mainController.changeView("showSearchPage", argsMapStr)
        }

        var dropDownSearch = null
        if (searchDomains.length > 0) {
            //Prefix function
            var selectedFunction = function (selectedSearchDomain, domainIndex) {
                return function () {
                    var $search = $("#search")
                    $search.attr("placeholder", selectedSearchDomain.label + " Search")
                    $search.attr("domain-index", domainIndex)
                }
            }

            //Dropdown elements
            var dropDownComponents = []
            for (var i = 0; i < searchDomains.length; i++) {
                dropDownComponents.push({
                    href: selectedFunction(searchDomains[i], i),
                    title: searchDomains[i].label,
                    id: searchDomains[i].name,
                })
            }

            dropDownSearch = FormUtil.getDropDownToogleWithSelectedFeedback(
                null,
                dropDownComponents,
                true,
                searchFunction
            )
            dropDownSearch.change()
        }

        var searchElement = $("<input>", {
            id: "search",
            type: "text",
            class: "form-control search-query",
            placeholder: "Global Search",
        })
        searchElement.keypress(function (e) {
            var key = e.which
            var onFocus = searchElement.is(":focus")
            var searchString = searchElement.val()
            if (
                key == 13 && // the enter key code
                onFocus && // ensure is focused
                searchString.length >= MIN_LENGTH
            ) {
                // min search length of 3 characters
                searchFunction()
                return false
            } else if (key == 13 && onFocus && searchString.length < MIN_LENGTH) {
                Util.showInfo(
                    "The minimum length for a global text search is " + MIN_LENGTH + " characters.",
                    function () {},
                    true
                )
                return false
            }
        })
        searchElement.css({ display: "inline" })
        searchElement.css({ "padding-top": "2px" })
        searchElement.css({ "margin-left": "2px" })
        searchElement.css({ "margin-right": "2px" })

        var icon = mainController.loggedInAnonymously ? "glyphicon-log-in" : "glyphicon-off"
        var logoutButton = FormUtil.getButtonWithIcon(
            icon,
            function () {
                $("body").addClass("bodyLogin")
                sessionStorage.setItem("forceNormalLogin", mainController.loggedInAnonymously)
                mainController.loggedInAnonymously = false
                mainController.serverFacade.logout()
            },
            null,
            null,
            "logoutBtn"
        )

        var barcodeReaderBtn = FormUtil.getButtonWithIcon(
            "glyphicon-barcode",
            function() {
                BarcodeUtil.readBarcodeFromCamera();
            },
            null,
            null,
            "barcodeReaderBtn"
        )

        var $searchForm = $("<form>", { onsubmit: "return false;" })
            .append(logoutButton)
            .append(searchElement)
            .append(dropDownSearch);

        if(profile.mainMenu.showBarcodes) {
            $searchForm.append(barcodeReaderBtn);
        }

        $searchForm.css("width", "100%")
        $searchForm.css("display", "flex")

        $header.append($searchForm)

        var $body = $("<div>", { id: "sideMenuBody" })
        $body.css("overflow-y", "auto")
        $body.css("flex", "1 1 auto")

        LayoutManager.addResizeEventHandler(function () {
            if (LayoutManager.FOUND_SIZE === LayoutManager.MOBILE_SIZE) {
                $body.css("-webkit-overflow-scrolling", "auto")
            } else {
                $body.css("-webkit-overflow-scrolling", "touch")
            }
        })

        $widget.append($header).append($body)
        $container.empty()
        $container.css("height", "100%")
        $container.append($widget)

        //
        // Print Menu
        //
        this._sideMenuWidgetModel.menuDOMBody = $body
        this.repaintTreeMenuDinamic()
    }

    this._renderDOMNode = function (params) {
        var { node, container } = params

        var $node = $("<div>").addClass("browser-node")

        if (node.icon || node.iconUrl) {
            var $icon = null

            if (node.iconUrl) {
                $icon = $("<img/>").attr("src", node.iconUrl)
            } else {
                $icon = $("<span/>")
            }

            if (node.icon) {
                $icon.addClass(node.icon)
            }

            $("<div/>").addClass("browser-node-icon").append($icon).appendTo($node)
        }

        var text = null

        if (node.text !== null && node.text !== undefined) {
            text = node.text
        }

        if (node.view && node.object) {
            var menuId = JSON.stringify(node.object)
            var href = Util.getURLFor(menuId, node.view, node.viewData)
            var $link = $("<a>", {
                href: href,
                class: "browser-compatible-javascript-link browser-compatible-javascript-link-tree",
            }).text(text)
            $("<div/>").append($link).addClass("browser-node-text").appendTo($node)
        } else {
            $("<div/>").text(text).addClass("browser-node-text").appendTo($node)
        }

        $(container).empty().append($node)
    }

    this.repaintTreeMenuDinamic = function () {
        var _this = this

        this._sideMenuWidgetModel.menuDOMBody.empty().css("border-top", "1px solid #dbdbdb")

        var BrowserElement = React.createElement(
            window.NgComponents.default.ThemeProvider,
            {},
            React.createElement(window.NgComponents.default.Browser, {
                controller: _this._sideMenuWidgetController._browserController,
                renderDOMNode: _this._renderDOMNode,
                styles: {
                    nodes: "sideMenuNodes",
                },
            })
        )

        ReactDOM.render(BrowserElement, this._sideMenuWidgetModel.menuDOMBody.get(0))
    }
}

var Images = {};
Images.decodeArrayBuffer = function(buffer, onLoad) {
    var mime;
    var a = new Uint8Array(buffer);
    var nb = a.length;
    if (nb < 4)
        return null;
    var b0 = a[0];
    var b1 = a[1];
    var b2 = a[2];
    var b3 = a[3];
    if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47)
        mime = 'image/png';
    else if (b0 == 0xff && b1 == 0xd8)
        mime = 'image/jpeg';
    else if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46)
        mime = 'image/gif';
    else
        return null;
    var binary = "";
    for (var i = 0; i < nb; i++)
        binary += String.fromCharCode(a[i]);
    var base64 = window.btoa(binary);
    var image = new Image();
    image.onload = onLoad;
    image.src = 'data:' + mime + ';base64,' + base64;
    image.Uint8Array = a;
    return image;
}