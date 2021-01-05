var EventUtil = new function() {

    var DEFAULT_TIMEOUT = 15000;
    var DEFAULT_TIMEOUT_STEP = 1000;

	this.click = function(elementId, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.trigger('click');
                resolve();
            } catch(error) {
                reject(error);
            }
	    });
	};

	this.change = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.val(value).change();
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.changeSelect2 = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.select2();
                element.focus();
                element.val(value);
                element.select2().trigger('change');
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.triggerSelectSelect2 = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.val(value);
                element.select2().trigger('select2:select');
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.checked = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.prop('checked', value);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.equalTo = function(elementId, value, isEqual, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if (isEqual && (element.html() === value || element.val() === value) ||
                    !isEqual && (element.html() != value && element.val() != value)) {
                    resolve();
                } else {
                    throw "Element #" + elementId + " should" + (isEqual ? "" : " not") + " be equal " + value;
                }
            } catch(error) {
                reject(error);
            }
        });
    };

	this.write = function(elementId, text, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                $(element).val(text);
                for (var i = 0; i < text.length; i++) {
                    $(element).trigger('keydown', {which: text.charCodeAt(i)});
                    $(element).trigger('keyup', {which: text.charCodeAt(i)});
                }
                resolve();
            } catch(error) {
                reject(error);
            }
	    });
	};

	this.keypress = function(elementId, key, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                var e = $.Event('keypress');
                e.which = key;
                $(element).trigger(e);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
	}

	this.verifyExistence = function(elementId, isExist, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        var elementExistence = $("#" + elementId).length > 0;
	        if (elementExistence == isExist) {
                resolve();
	        } else {
	            if (ignoreError) {
	                resolve();
                } else {
                    throw "Element " + (isExist ? "not" : "") + " found: #" + elementId;
                }
	        }
	    });
	}

    this.waitForId = function(elementId, ignoreError, timeout) {
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, ignoreError, resolve);

                if($("#" + elementId).length <= 0) {
                    setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.waitForClass = function(className, ignoreError, timeout) {
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(className, timeout, ignoreError, resolve);

                if($("." + className).length <= 0) {
                    setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.waitForStyle = function(elementId, styleName, styleValue, ignoreError, timeout) {
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, ignoreError, resolve);

                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if (element[0].style[styleName] === styleValue) {
                    resolve();
                } else {
                    setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
                }
            } catch(error) {
                reject(error);
            }
        });
    }

    this.waitForFill = function(elementId, ignoreError, timeout) {
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, ignoreError, resolve);

                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if(element.html().length <= 0 && element.val().length <= 0) {
                    setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.waitForCkeditor = function(elementId, data, timeout) {
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, false, resolve);
                editor = CKEditorManager.getEditorById(elementId);

                if(editor === undefined) {
                    setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    }

    this.checkTimeout = function(elementId, timeout, ignoreError, resolve) {
        if (!timeout) {
            timeout = DEFAULT_TIMEOUT;
        }
        timeout -= DEFAULT_TIMEOUT_STEP;

        if (timeout <= 0) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element '" + elementId + "' is not exist.";
            }
        }
        return timeout;
    }

    this.sleep = function(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }

    this.dragAndDrop = function(dragId, dropId, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var dragElement = EventUtil.getElement(dragId, ignoreError, resolve).draggable();
                var dropElement = EventUtil.getElement(dropId, ignoreError, resolve).droppable();

                var dt = new DataTransfer();

                var dragStartEvent = jQuery.Event("dragstart");
                dragStartEvent.originalEvent = jQuery.Event("mousedown");
                dragStartEvent.originalEvent.dataTransfer = dt;

                dropEvent = jQuery.Event("drop");
                dropEvent.originalEvent = jQuery.Event("DragEvent");
                dropEvent.originalEvent.dataTransfer = dt;

                dragElement.trigger(dragStartEvent);
                dropElement.trigger(dropEvent);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.dropFile = function(fileName, url, dropId, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var dropElement = EventUtil.getElement(dropId, ignoreError, resolve).droppable();

                TestUtil.fetchBytes(url, function(file) {
                    file.name = fileName;

                    var dt = { files: [file] };

                    dropEvent = jQuery.Event("drop");
                    dropEvent.originalEvent = jQuery.Event("DragEvent");
                    dropEvent.originalEvent.dataTransfer = dt;
                    dropElement.trigger(dropEvent);
                    resolve();
                });
            } catch(error) {
                reject(error);
            }
        });
    };

    this.getElement = function(elementId, ignoreError, resolve) {
        var element = $( "#" + elementId );
        if(!element) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element not found: #" + elementId;
            }
        }
        return element;
    }
}