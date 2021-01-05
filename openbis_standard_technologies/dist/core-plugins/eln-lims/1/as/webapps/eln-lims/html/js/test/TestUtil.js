var TestUtil = new function() {
    this.realSaveAs = null;
    this.savedFiles = new Map();

	this.login = function(username, password) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("username"))
                     .then(() => e.write("username", username))
                     .then(() => e.write("password", password))
                     .then(() => e.click("login-button"))
                     .then(() => resolve())
                     .catch((error) => reject(error));
        });
    }

    this.reportToJenkins = function(id, msg) {
        if ($.cookie("report-to-jenkins") == "true") {
            var testName = "test" + id + "event";
            var event = jQuery.Event(testName);
            event.msg = msg;
            window.parent.$("#eln-frame").trigger(event);
        }
    }

    this.reportErrorToJenkins = function(id, msg) {
        if ($.cookie("report-to-jenkins") == "true") {
            // If one test is broken, then all tests must be failed.
            // If you need to add a new test, make sure that it will fail.

            for(let i = id; i < TestProtocol.getTestCount(); i++) {
                var event = jQuery.Event("test" + i + "event");
                if (i == id) {
                    event.msg = msg;
                } else {
                    event.msg = "Test " + id + " failed.";
                }
                window.parent.$("#eln-frame").trigger(event);
            }
        }
    }

    this.reportError = function(id, error, reject) {
        TestUtil.reportErrorToJenkins(id, error);
        reject(error);
    }

    this.testPassed = function(id) {
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id +" passed";
            TestUtil.reportToJenkins(id, msg);
            console.log("%c" + msg, "color: green");
            resolve();
        });
    }

    this.testNotExist = function(id) {
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id +" is not exist";
            TestUtil.reportToJenkins(id, msg);
            console.log("%c" + msg, "color: grey");
            resolve();
        });
    }

    this.testLocally = function(id) {
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id + " should be tested locally";
            TestUtil.reportToJenkins(id, msg);
            console.log("%c" + msg, "color: blue");
            resolve();
        });
    }

    this.allTestsPassed = function() {
        return new Promise(function executor(resolve, reject) {
            if ($.cookie("report-to-jenkins") != "true") {
                alert("Tests passed!");
            }
            resolve();
        });
    }

    this.setCookies = function(key, value) {
        return new Promise(function executor(resolve, reject) {
            $.cookie(key, value);
            resolve();
        });
    }

    this.deleteCookies = function(key) {
        return new Promise(function executor(resolve, reject) {
            $.removeCookie(key);
            resolve();
        });
    }

    this.verifyInventory  = function(testId, ids) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            chain = Promise.resolve();
            for (let i = 0; i < ids.length; i++) {
                chain = chain.then(() => e.waitForId(ids[i]));
            }
            chain.then(() => resolve()).catch(error => TestUtil.reportError(testId, error, reject));
        });
    }

    this.setFile = function(name, url, mimeType) {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
                _this.fetchBytes(url, function(file) {
                file.name = name;
                mainController.currentView.typeAndFileController.setFile(file);
                resolve();
            });
        });
    }

    this.checkFileEquality = function(name, url, replacer) {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            _this.fetchBytes(url, function(file) {
                _this.blob2Text(file, function(textFile) {
                    downloadedFile = _this.savedFiles[name];
                    downloadedFile = replacer(downloadedFile);

                    _this.savedFiles.delete(name);

                    if (textFile === downloadedFile) {
                        resolve();
                    } else {
                        throw "File " + name + " is not correct!";
                    }
                });
            });
        });
    }

    this.blob2Text = function(blob, action) {
        // get text from blob
        let fileReader = new FileReader();

        fileReader.readAsText(blob);

        fileReader.onload = function(event) {
            action(fileReader.result);
        };
    }

    this.idReplacer = function(text) {
        text = text.replaceAll( new RegExp('"/openbis/openbis/file-service/eln-lims/[A-Za-z0-9/-]+"'),
                                           '"/openbis/openbis/file-service/eln-lims/identifier"');

        return text;
    }

    this.dateReplacer = function(text) {
        text = text.replaceAll( new RegExp('Date: [0-9-]+ [0-9:]+'),
                                           'Date: YYYY-MM-DD HH:MM:SS');

        return text;
    }

    this.fetchBytes = function(url, action) {
	    var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.responseType = 'blob';

        xhr.onload = function(e) {
            if (this.status == 200) {
                // get binary data as a response
                action(this.response);
            }
        };

        xhr.send();
    }

    this.ckeditorSetData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            editor.setData(data);
            resolve();
        });
    }

    this.ckeditorTestData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            try {
                editor = CKEditorManager.getEditorById(id);
                var realData = editor.getData();

                if (realData === data) {
                    resolve();
                } else {
                    throw "CKEditor #" + elementId + " should be equal " + data;
                }
            } catch (error) {
                reject(error);
            }
        });
    }

    this.ckeditorDropFile = function(id, fileName, url) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            TestUtil.fetchBytes(url, function(file) {
                editor = CKEditorManager.getEditorById(id);

                file.name = fileName;

                editor.model.enqueueChange( 'default', () => {
                    editor.execute( 'imageUpload', { file: [file] } );
                } );
                resolve();
            });
        });
    }

    this.overloadSaveAs = function() {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            if (_this.realSaveAs === null) {
                _this.realSaveAs = saveAs;
            }

            saveAs = function(blob, name, no_auto_bom) {
                _this.blob2Text(blob, function(textFile) {
                    _this.savedFiles[name] = textFile;
                });
            }
            resolve();
        });
    }

    this.returnRealSaveAs = function() {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            saveAs = _this.realSaveAs;
            resolve();
        });
    }
}