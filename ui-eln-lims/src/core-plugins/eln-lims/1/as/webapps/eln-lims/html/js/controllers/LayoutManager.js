//This layout manager can potentially be used outside the ELN
// Requires Jquery, Jquery UI and Bootstrap

var LayoutManager = {
	FOUND_SIZE : undefined,
	DESKTOP_SIZE : 1024,
	TABLET_SIZE : 768,
	MOBILE_SIZE : 0,
	MIN_HEADER_HEIGHT : 120,
	MAX_FIRST_COLUMN_WIDTH : 350,
	MAX_THIRD_COLUMN_WIDTH : 350,
	body : null,
	mainContainer : null,
	firstColumn : null,
	secondColumn : null,
	secondColumnHeader : null,
	secondColumnContent : null,
	secondColumnContentResize : function() {
		var width = $( window ).width();
		if (width > LayoutManager.TABLET_SIZE) {
			LayoutManager.secondColumnContent.css({
				height : $( window ).height() - LayoutManager.secondColumnHeader.outerHeight()
			});
		}
	},
	thirdColumn : null,
	isResizingColumn : false,
	isLoadingView : false,
    settings: null,
    fullScreenFlag : false,
	_init : function(isFirstTime) {
		var _this = this;

		if(this.body === null) {
			this.body = $(document.body);
			this.body.css({
				"overflow" : "hidden"
			});
		}

		if(this.mainContainer === null) {
			this.mainContainer = $("#mainContainer");
		}

		if(isFirstTime) {
			if(this.firstColumn !== null && (this.FOUND_SIZE === this.MOBILE_SIZE)) {
				this.firstColumn.resizable("destroy");
				this.firstColumn.children().detach();
				this.firstColumn.remove();
				this.firstColumn = null;
			}

			if(this.secondColumn !== null) {
				this.secondColumn.resizable("destroy");
				this.secondColumn.children().detach();
				this.secondColumn.remove();
				this.secondColumn = null;

				this.secondColumnHeader = null;
				this.secondColumnContent = null;
			}

			if(this.thirdColumn !== null) {
				this.thirdColumn.children().detach();
				this.thirdColumn.remove();
				this.thirdColumn = null;
			}
		}

		if(this.firstColumn == null) {
			this.firstColumn = $("<div>");
			this.firstColumn.css({
				"display" : "none",
				"overflow" : "visible", //To show the dropdowns
				"padding" : "0",
				"float" : "left"
			});
		}

		if(this.secondColumn == null) {
			this.secondColumn = $("<div>");
			this.secondColumn.css({
				"display" : "none",
				"overflow-x": "hidden",
				"overflow-y": "hidden",
				"padding" : "0",
				"float" : "left"
			});
			this.secondColumnHeader = $("<div>");
			this.secondColumnHeader.css({
				'display' : "none",
				'overflow': "visible" //To show the dropdowns
			});
			this.secondColumnContent = $("<div>");
			this.secondColumnContent.css({
				display : "none",
				"overflow-x" : "auto",
				"overflow-y" : "auto"
			});

			this.secondColumn.append(this.secondColumnHeader);
			this.secondColumn.append(this.secondColumnContent);
			$(this.secondColumnHeader).on( "DOMNodeInserted", this.secondColumnContentResize);
			$(this.secondColumnHeader).on( "DOMNodeRemoved", this.secondColumnContentResize);
		}

		if(this.thirdColumn == null) {
			this.thirdColumn = $("<div>");
			this.thirdColumn.css({
				"display" : "none",
				"overflow-x" : "hidden",
				"overflow-y" : "auto",
				"padding" : "0",
				"float" : "left"
			});
		}

		if (isFirstTime) {
			//Attach created components
			if($("#sideMenuTopContainer").length !== 1) {
			    this.mainContainer.append(this.firstColumn);
			}
			this.mainContainer.append(this.secondColumn).append(this.thirdColumn);

			//
			// Columns drag functionality
			//

			// Only usable in Desktop and tablet

			// Moving to the right +x
			// Add size to first column +x
			// Remove size from second column -1 * +x

			// Moving to the left -x
			// Remove size to the first column -x
			// Add size to the second column -1 * -x
			if($("#sideMenuTopContainer").length !== 1) {
			this.firstColumn.resizable({
				handles : 'e',
				ghost : true,
				start : function(event, ui) {
					_this.isResizingColumn = true;
				},
				stop : function(event, ui) {
					var widthChange = ui.size.width - ui.originalSize.width;
					_this.secondColumn.css('width', _this.secondColumn.width() + (-1 * widthChange) - 1);
					_this.isResizingColumn = false;
					_this._saveSettings()
				}
			});
            }

			// Only usable in Desktop mode

			// Moving to the right +x
			// Add size to the second column +x
			// Remove size from the third column -1 * +x

			// Moving to the left -x
			// Remove size from the second column -x
			// Add size to the third column -1 * -x
			this.secondColumn.resizable({
				handles : 'e',
				ghost : true,
				start : function(event, ui) {
					_this.isResizingColumn = true;
				},
				stop : function(event, ui) {
					var widthChange = ui.size.width - ui.originalSize.width;
					_this.thirdColumn.css('width', _this.thirdColumn.width() + (-1 * widthChange) - 1);
					_this.isResizingColumn = false;
					_this._saveSettings()
				}
			});
		}
	},
	_setDesktopLayout : function(view, isFirstTime) {
        var _this = this

        var settings = _this._loadSettings()
        var width = $( window ).width();
        var height = $( window ).height();
        var headerHeight = 0;

        var firstColumnWidth = settings.firstColumnWidth;
        if(!firstColumnWidth){
            firstColumnWidth = width * 0.25
            if(firstColumnWidth > LayoutManager.MAX_FIRST_COLUMN_WIDTH) {
                firstColumnWidth = LayoutManager.MAX_FIRST_COLUMN_WIDTH;
            }
        }

        if(isFirstTime) {
            _this.firstColumn.append(view.menu);
        }

        _this.firstColumn.css({
                "display" : "block",
                "height" : height,
                "width" : firstColumnWidth
        });

        mainController.serverFacade.getSetting("eln-layout-first-column-width", function (widthStr) {
            _this.firstColumn
        })

        var thirdColumnWidth = settings.thirdColumnWidth;
        if(!thirdColumnWidth){
            thirdColumnWidth = (width - _this.firstColumn.width()) * 0.34 - 1
            if(thirdColumnWidth > LayoutManager.MAX_THIRD_COLUMN_WIDTH) {
                thirdColumnWidth = LayoutManager.MAX_THIRD_COLUMN_WIDTH;
            }
        }

        var secondColumWidth;
        if (view.auxContent) {
            secondColumWidth = width - _this.firstColumn.width() - thirdColumnWidth - 1;
        } else {
            secondColumWidth = width - _this.firstColumn.width() - 1;
        }

        _this.secondColumn.css({
            "display" : "block",
            "width" : secondColumWidth
        });


        if (view.header) {
            headerHeight = _this.MIN_HEADER_HEIGHT;
            if(isFirstTime) {
                _this.secondColumnHeader.append(view.header);
            }
            _this.secondColumnHeader.css({
                display : "block",
                "min-height" : headerHeight,
                "height" : "auto"
            });
        } else {
            _this.secondColumnHeader.css({ display : "none" });
        }

        if (view.content) {
            _this.secondColumnContent.css({
                display : "block",
                height : height - headerHeight
            });

            if(isFirstTime) {
                _this.secondColumnContent.append(view.content);
            }
        } else {
            _this.secondColumnContent.css({ display : "none" });
        }


        if (view.auxContent) {
            _this.thirdColumn.css({
                "display" : "block",
                "height" : height,
                "width" : thirdColumnWidth
            });

            if(isFirstTime) {
                _this.thirdColumn.append(view.auxContent);
            }
        } else {
            _this.thirdColumn.css({
                "display" : "none",
                "width" : "0%"
            });
        }
    },
    _setTabletLayout : function(view, isFirstTime) {
        var _this = this

        var settings = _this._loadSettings()
        var width = $( window ).width();
        var height = $( window ).height();
        var headerHeight = 0;

        var firstColumnWidth = settings.firstColumnWidth;
        if(!firstColumnWidth){
            firstColumnWidth = width * 0.25;
            if(firstColumnWidth > LayoutManager.MAX_FIRST_COLUMN_WIDTH) {
                firstColumnWidth = LayoutManager.MAX_FIRST_COLUMN_WIDTH;
            }
        }

        if($("#sideMenuTopContainer").length !== 1) {
            _this.firstColumn.append(view.menu);
        }

        _this.firstColumn.css({
                display : "block",
                height : height,
                "width" : firstColumnWidth
        });

        _this.secondColumn.css({
            display : "block",
            "width" : width - _this.firstColumn.width() - 1
        });

        if (view.header) {
            headerHeight = _this.MIN_HEADER_HEIGHT;
            if(isFirstTime) {
                _this.secondColumnHeader.append(view.header);
            }
            _this.secondColumnHeader.css({
                display : "block",
                "min-height" : headerHeight,
                "height" : "auto"
            });
        } else {
            _this.secondColumnHeader.css({ display : "none" });
        }

        if (view.content) {
            _this.secondColumnContent.css({
                display : "block",
                height : height - headerHeight
            });

            if(isFirstTime) {
                _this.secondColumnContent.append(view.content);
            }
        } else {
            _this.secondColumnContent.css({ display : "none" });
        }


        if (view.auxContent) {
            if(isFirstTime) {
                _this.secondColumnContent.append(view.auxContent);
            }
        }
        _this.thirdColumn.css({ display : "none" });
    },
	_setMobileLayout : function(view, isFirstTime) {
		var width = $( window ).width();
		var height = $( window ).height();

		//
		// Set screen size
		//
		this.firstColumn.css({
			display : "block",
			height : height,
			"overflow-y" : "auto",
			"width" : "100%"
		});
		this.secondColumn.css({ display : "none" });
		this.thirdColumn.css({ display : "none" });

		//
		// Attach available views
		//
		if (view.menu) {
			if(isFirstTime) {
				this.firstColumn.append(view.menu);
			}
		}

		if (view.header) {
			view.header.css({
				"min-height" : this.MIN_HEADER_HEIGHT,
				"height" : "auto"
			});

			if(isFirstTime) {
				this.firstColumn.append(view.header);
			}
		}

		if(view.content) {
			if(isFirstTime) {
				this.firstColumn.append(view.content);
			}
		}

		if (view.auxContent) {
			if(isFirstTime) {
				this.firstColumn.append(view.auxContent);
			}
		}
	},
    canReload : function() {
        // Don't reload when CKEditor is maximized
        var ckMaximized = CKEditorManager.getMaximized();

        return  this.isResizingColumn === false &&
                this.isLoadingView === false &&
                !ckMaximized;
    },
	getContentWidth : function() {
		var width = $( window ).width();
		if (width > this.DESKTOP_SIZE) {
			return this.secondColumn.width();
		} else if (width > this.TABLET_SIZE) {
			return this.secondColumn.width();
		} else if (width > this.MOBILE_SIZE) {
			return this.firstColumn.width();
		} else {
			alert("Layout manager unable to know the layout, this should never happen.");
		}
	},
    getExpectedContentHeight : function() {
        return $(window).height() - LayoutManager.secondColumnHeader.outerHeight();
    },
    getExpectedContentWidth : function() {
        return LayoutManager.secondColumnHeader.outerWidth();
    },
    isMobile : function() {
        var width = $( window ).width();
        if (width > this.TABLET_SIZE) {
            return false;
        } else {
            return true;
        }
    },
	fullScreen : function() {
		var width = $( window ).width();
		if (width > this.DESKTOP_SIZE) {
			this.firstColumn.hide();
			this.thirdColumn.hide();
			this.secondColumn.width(width);
		} else if (width > this.TABLET_SIZE) {
			this.firstColumn.hide();
			this.secondColumn.width(width);
		} else if (width > this.MOBILE_SIZE) {
			// No columns to hide, remove menu instead.
			$("#sideMenuTopContainer").parent().css({ "height" : "" });
			$("#sideMenuBody").css({ "display" : "none" });
			//
		} else {
			alert("Layout manager unable to go fullScreen, this should never happen.");
		}
		this.fullScreenFlag = true; // The flag is set until gets restored by pressing the back button on mobile or changing views on larger layouts.
	},
	restoreStandardSize : function() {
	    // Restore changes of mobile layout
	    if(this.isMobile()) {
            $("#sideMenuTopContainer").parent().css({ "height" : "100%" });
            $("#sideMenuBody").css({ "display" : "" });
        }
        //
		LayoutManager.resize(mainController.views, true);
		this.fullScreenFlag = false;
	},
	reloadView : function(view, forceFirstTime) {
		var _this = this;
		this.isLoadingView = true;

		var isFirstTime = this.mainContainer === null || forceFirstTime === true || forceFirstTime === undefined;

		// sideMenuBody scroll fix
		var firstColumnScroll = null;
		if(this.FOUND_SIZE >= this.TABLET_SIZE) {
			firstColumnScroll = $(".sideMenuNodes").scrollTop();
		}
		//

		var width = $( window ).width();
//		if (width > this.DESKTOP_SIZE) {
//			if (this.FOUND_SIZE !== this.DESKTOP_SIZE) {
//				isFirstTime = true;
//				this.FOUND_SIZE = this.DESKTOP_SIZE;
//			}
//		} else
		if (width > this.TABLET_SIZE) {
			if (this.FOUND_SIZE !== this.TABLET_SIZE) {
				isFirstTime = true;
				this.FOUND_SIZE = this.TABLET_SIZE;
			}
		} else if (width > this.MOBILE_SIZE) {
			if (this.FOUND_SIZE !== this.MOBILE_SIZE) {
				isFirstTime = true;
				this.FOUND_SIZE = this.MOBILE_SIZE;
			}
		}

		this._init(isFirstTime);
		if (this.FOUND_SIZE === this.DESKTOP_SIZE) {
			this._setDesktopLayout(view, isFirstTime);
			this.fullScreenFlag = false;
		} else if (this.FOUND_SIZE === this.TABLET_SIZE) {
			this._setTabletLayout(view, isFirstTime);
			this.fullScreenFlag = false;
		} else if (this.FOUND_SIZE === this.MOBILE_SIZE) {
			this._setMobileLayout(view, isFirstTime);
		}

		// sideMenuBody scroll fix
		if(this.FOUND_SIZE >= this.TABLET_SIZE && firstColumnScroll) {
			$(".sideMenuNodes").scrollTop(firstColumnScroll);
		}
		//

		this.triggerResizeEventHandlers();
		this.isLoadingView = false;
	},
	resizeEventHandlers : [],
	addResizeEventHandler : function(eventHandler) {
		this.resizeEventHandlers.push(eventHandler);
	},
	removeResizeEventHandler : function(eventHandler) {
		this.resizeEventHandlers = this.resizeEventHandlers.filter(function(el) {
			return el === eventHandler;
		});
	},
	triggerResizeEventHandlers : function() {
		for(var idx = 0; idx < this.resizeEventHandlers.length; idx++) {
			this.resizeEventHandlers[idx]();
		}
		this.secondColumnContentResize();
	},
	resize : function(view, forceFirstTime) {
		if(this.canReload()) {
			this.reloadView(view, forceFirstTime);
		}
	},
    _saveSettings(){
        var _this = this

        _this.settings = {
            firstColumnWidth : _this.firstColumn.width(),
            thirdColumnWidth : _this.thirdColumn.width()
        }

        mainController.serverFacade.setSetting('eln-layout', JSON.stringify(_this.settings))
    },
    _loadSettings(){
        var _this = this

        if(!_this.settings){
            mainController.serverFacade.getSetting('eln-layout', function(settingsStr){
                if(settingsStr){
                    _this.settings = JSON.parse(settingsStr)
                    if(mainController && mainController.views) {
                        LayoutManager.resize(mainController.views, true);
                    }
                }
            })
        }

        return _this.settings || {}
    }
}

$(window).resize(function() {
	if(mainController && mainController.views && mainController.sideMenu) {
		LayoutManager.resize(mainController.views, false);
	}
});