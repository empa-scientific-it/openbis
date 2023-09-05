/** Default owner/source */
const owner = "demo-sample";
const source = "";
const HTTP_SERVER_URI = "/data-store-server";

/// The datastoreServer we use for our data
// datastoreServer = new datastore('https://localhost:8443/openbis', 'https://localhost:8444/datastore_server');
datastoreServer = new DataStoreServer('http://localhost:8085', HTTP_SERVER_URI);



/** Creates open button for reading the file from dss */
function createOpenButton(row, filePath, fileSize) {
	readButton = document.createElement("button");
			readButton.innerText = "open";
			readButton.dataset.filepath = filePath;
			readButton.dataset.filesize = fileSize;
			readButton.onclick = (function() { 
				datastoreServer.read(owner, this.dataset.filepath, 0, this.dataset.filesize, (responseData => {
						var win = window.open("", "_blank");
						var doc = win.document;
						doc.open("text/html");
						doc.write(responseData);
						doc.close();
					}));
			});
			row.appendChild(readButton);
}

/** creates button for deleteing a file from the dss */
function createDeleteButton(row, filePath) {
	deleteButton = document.createElement("button");
	deleteButton.innerText = "delete";
	deleteButton.dataset.filepath = filePath;
	deleteButton.onclick = (function() { 
		if (confirm("Do you want to delete "+filePath+" ?") == true) {
			datastoreServer.delete(owner, this.dataset.filepath, (responseData => {
					showEntries();
				}));
		}
	});
	row.appendChild(deleteButton);
}

/** recursive implementation of DFS algorithm to traverse the tree and accumulate results in order of traversal */
function dfs(tree, map, path, acc) {
	for(const [key, val] of Object.entries(tree).sort(([a1,a2],[b1,b2]) => a1>b1 ? 1:-1)) {
		var newPath = path + '/' + key;
		if(newPath in map) {
			acc.push(map[newPath]);
		}
		dfs(val, map, newPath, acc);
	}
	return acc;
}

/** function to generate the table of file information */
function generateTable(files) {

	/** converts array into tree object for sorting files by path */
	const toTree = (paths) => {
		const tree = {};
		for (const path of paths) {
			if (path) {
				let node = tree;
				const parts = path[1]['path'].split("/");
				for (const part of parts) {
					if (part) {
						node = node[part] ?? (node[part] = {});
					}
				}
			}
		}
		return tree;
	};

	var tree = toTree(files);
	map = {};
	files.forEach(file => map[file[1]['path']] = file[1]);
	files = dfs(tree, map, '', []);
	files.forEach(file => {
		file.owner = owner;
		file.path = file.path.slice(owner.length + 1);
	});
	

	// column names for the list of files
	const columns = ["name", "owner", "path", "directory", "size", "creationTime", "lastAccessTime", "lastModifiedTime"];
	const mytable = document.getElementById("html-data-table");

	let headRow = document.createElement("tr");
	columns.forEach(header => {
		let cell = document.createElement("th");
		cell.innerText = header;
		headRow.appendChild(cell);
	});
	cell = document.createElement("th");
	cell.innerText = "actions";
	headRow.appendChild(cell);
	mytable.appendChild(headRow);

	// create rows in table together with open and delete buttons
	for (let i in files) {
		let file = files[i];

		let newRow = document.createElement("tr");
		columns.forEach(column => {
			let cell = document.createElement("th");
			cell.innerText = file[column];
			newRow.appendChild(cell);
		});
		if (file['directory'] !== true) {
			createOpenButton(newRow, file['path'], file['size']);
		}
		createDeleteButton(newRow, file['path']);
		mytable.appendChild(newRow);
	}

  }
  

/**
 * Display the files returned by the server
 */
function displayReturnedFiles(data)
{
	if (data.error) {
		console.log(data.error);
		alert("Could not retrieve data.");
		return;
	}
	
	var results = data.result[1];

	// Restrict the display to 50 samples
	// results = results.splice(0, 50);

	generateTable(results);
}

/**
 * Request file list from the server and show them in the Page.
 */
function showEntries()
{
	let table = document.getElementById("html-data-table");
	while (table.firstChild) {
		table.firstChild.remove()
	}
	datastoreServer.list(owner, source, "true", displayReturnedFiles);
}


/** validation for write */
function isWriteValid() {
	return document.getElementById("fpath").value != '' && document.getElementById("foffset").value != '';
}

/** validation for copy */
function isCopyValid() {
	return document.getElementById("copy-from-path").value != '' && document.getElementById("copy-to-path").value != '';
}

/** validation for move */
function isMoveValid() {
	return document.getElementById("move-from-path").value != '' && document.getElementById('move-to-path').value != '';
}


function enterApp(data)
{
	if(data == null){
		alert("Login or password incorrect");
		document.getElementById("username").focus();
		return;
	}

	document.getElementById("login-form-div").style.display = "none";
	document.getElementById("main").style.display = "block";
	document.getElementById("openbis-logo").style.height = "30px";

	showEntries();
}


window.onload = function() {
	new dssClientLoginPage(datastoreServer, enterApp).configure();

	document.getElementById("list-button").onclick = function() {
		showEntries();
	}

	document.getElementById("write-submit").onclick = function() { 
		if(isWriteValid()) {
			datastoreServer.write(owner, 
				document.getElementById("fpath").value.trim(),
				parseInt(document.getElementById("foffset").value.trim()), 
				document.getElementById("write-text").value.trim(),
				(_ => {
				showEntries();
			}));
		}
	};

	document.getElementById("copy-submit").onclick = function() { 
		if(isCopyValid()) {
			datastoreServer.copy(owner, document.getElementById("copy-from-path").value.trim(), owner, document.getElementById("copy-to-path").value.trim(),
			 (_ => {
				showEntries();
			}));
		}
		
	};

	document.getElementById("move-submit").onclick = function() { 
		if(isMoveValid()) {
			datastoreServer.move(owner, 
				document.getElementById("move-from-path").value.trim(), 
				owner, 
				document.getElementById("move-to-path").value.trim(), 
				(_ => {
				showEntries();
			}));
		}
	};

	document.getElementById("create-submit").onclick = function() {
		datastoreServer.create(owner, document.getElementById("create-path").value.trim(),
			document.getElementById("create-directory").checked,
			(_ => {
				showEntries();
			}));
	};

}

