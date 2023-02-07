var CKEditorManager = new function() {

    var editors = new Map();

    this.addEditor = function(id, editor) {
        this.SpecialCharactersGreekExtended(editor);
        editors.set(id, editor);
    }

    this.getEditorById = function(id) {
        return editors.get(id);
    }

    this.getMaximized = function() {
        var ckMaximized = false;
        for (let editor of editors.values()) {
            var commands = editor.commands;
            if(commands && commands.maximize && commands.maximize.state == 1) {
                ckMaximized = true;
            }
        }
        return ckMaximized;
    }

	this.destroy = function() {
	    for (let editor of editors.values()) {
	        editor.destroy().catch( error => {console.log( error );});
	    }
	    editors.clear();
	}

	this.SpecialCharactersGreekExtended = function(editor) {
	    editor.plugins.get( 'SpecialCharacters' ).addItems( 'Greek', [
            { title: 'Alpha lowercase'      , character: 'α' },
            { title: 'Beta lowercase'       , character: 'β' },
            { title: 'Gamma lowercase'      , character: 'γ' },
            { title: 'Delta lowercase'      , character: 'δ' },
            { title: 'Epsilon lowercase'    , character: 'ε' },
            { title: 'Zeta lowercase'       , character: 'ζ' },
            { title: 'Eta lowercase'        , character: 'η' },
            { title: 'Theta lowercase'      , character: 'θ' },
            { title: 'Iota lowercase'       , character: 'ι' },
            { title: 'Kappa lowercase'      , character: 'κ' },
            { title: 'Lambda lowercase'     , character: 'λ' },
            { title: 'Mu lowercase'         , character: 'μ' },
            { title: 'Nu lowercase'         , character: 'ν' },
            { title: 'Xi lowercase'         , character: 'ξ' },
            { title: 'Omicron lowercase'    , character: 'ο' },
            { title: 'Pi lowercase'         , character: 'π' },
            { title: 'Rho lowercase'        , character: 'ρ' },
            { title: 'Sigma lowercase'      , character: 'σ' },
            { title: 'Sigma lowercase (final position)' , character: 'ς' },
            { title: 'Tau lowercase'        , character: 'τ' },
            { title: 'Upsilon lowercase'    , character: 'υ' },
            { title: 'Phi lowercase'        , character: 'φ' },
            { title: 'Chi lowercase'        , character: 'χ' },
            { title: 'Psi lowercase'        , character: 'ψ' },
            { title: 'Omega lowercase'      , character: 'ω' },

            { title: 'Alpha uppercase'      , character: 'Α' },
            { title: 'Beta uppercase'       , character: 'Β' },
            { title: 'Gamma uppercase'      , character: 'Γ' },
            { title: 'Delta uppercase'      , character: 'Δ' },
            { title: 'Epsilon uppercase'    , character: 'Ε' },
            { title: 'Zeta uppercase'       , character: 'Ζ' },
            { title: 'Eta uppercase'        , character: 'Η' },
            { title: 'Theta uppercase'      , character: 'Θ' },
            { title: 'Iota uppercase'       , character: 'Ι' },
            { title: 'Kappa uppercase'      , character: 'Κ' },
            { title: 'Lambda uppercase'     , character: 'Λ' },
            { title: 'Mu uppercase'         , character: 'Μ' },
            { title: 'Nu uppercase'         , character: 'Ν' },
            { title: 'Xi uppercase'         , character: 'Ξ' },
            { title: 'Omicron uppercase'    , character: 'Ο' },
            { title: 'Pi uppercase'         , character: 'Π' },
            { title: 'Rho uppercase'        , character: 'Ρ' },
            { title: 'Sigma uppercase'      , character: 'Σ' },
            { title: 'Tau uppercase'        , character: 'Τ' },
            { title: 'Upsilon uppercase'    , character: 'Υ' },
            { title: 'Phi uppercase'        , character: 'Φ' },
            { title: 'Chi uppercase'        , character: 'Χ' },
            { title: 'Psi uppercase'        , character: 'Ψ' },
            { title: 'Omega uppercase'      , character: 'Ω' }
        ]);
	}

    this.imagePattern = "<img src=";
    this.servicePattern = "/openbis/openbis/file-service/eln-lims/";
	this.isDataValid = function(data) {
	    var domain = window.location.hostname;
	    var offset = 0;
	    while(offset < data.length) {
	        var indexOf = data.indexOf(this.imagePattern, offset);
	        if(indexOf > 0) {
	            // 1. Move offset
	            offset = indexOf + this.imagePattern.length + 1;
	            // 2. Check that image src
	            var dataCheckEnd = offset + this.servicePattern.length;
	            var dataCheck = data.substring(offset, dataCheckEnd);
	            var startsWith = this.servicePattern === dataCheck;
	            if(!startsWith) {
                    return false;
	            }
	        } else {
	            return true;
	        }
	    }
	    return true;
	}
}