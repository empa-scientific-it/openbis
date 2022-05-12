# Required software for the build
# brew install git
# brew install npm
# brew install yarn

# Example Build Environment
# git clone https://github.com/ckeditor/ckeditor5-build-inline.git
# cd ckeditor5-build-inline

# Use as reference for dependency versions: https://github.com/ckeditor/ckeditor5/blob/master/package.json

# Build commands

npm install --save "@ckeditor/ckeditor5-adapter-ckfinder@^18.0.0" \
	"@ckeditor/ckeditor5-basic-styles@^18.0.0" \
	"@ckeditor/ckeditor5-block-quote@^18.0.0" \
	"@ckeditor/ckeditor5-ckfinder@^18.0.0" \
	"@ckeditor/ckeditor5-core@^18.0.0" \
	"@ckeditor/ckeditor5-easy-image@^18.0.0" \
	"@ckeditor/ckeditor5-essentials@^18.0.0" \
	"@ckeditor/ckeditor5-heading@^18.0.0" \
	"@ckeditor/ckeditor5-image@^18.0.0" \
	"@ckeditor/ckeditor5-indent@^18.0.0" \
	"@ckeditor/ckeditor5-link@^18.0.0" \
	"@ckeditor/ckeditor5-list@^18.0.0" \
	"@ckeditor/ckeditor5-media-embed@^18.0.0" \
	"@ckeditor/ckeditor5-paragraph@^18.0.0" \
	"@ckeditor/ckeditor5-paste-from-office@^18.0.0" \
	"@ckeditor/ckeditor5-table@^18.0.0" \
	"@ckeditor/ckeditor5-theme-lark@^18.0.0" \
	'@ckeditor/ckeditor5-editor-inline@^18.0.0' \
	'@ckeditor/ckeditor5-alignment@^18.0.0' \
	'@ckeditor/ckeditor5-font@^18.0.0' \
	'@ckeditor/ckeditor5-highlight@^18.0.0' \
	'@ckeditor/ckeditor5-upload@^18.0.0' \
	'@ckeditor/ckeditor5-remove-format@^18.0.0' \
	'@ckeditor/ckeditor5-editor-decoupled@^18.0.0' \
	'@ckeditor/ckeditor5-special-characters@^18.0.0' \
	"eslint@^5.5.0" \
	"eslint-config-ckeditor5@^2.0.0" \
	"husky@^1.3.1" \
	"lint-staged@^7.0.0" \
	"postcss-loader@^3.0.0" \
	"raw-loader@^3.1.0" \
	"style-loader@^1.0.0" \
	"stylelint@^12.0.0" \
	"stylelint-config-ckeditor5@^1.0.3" \
	"terser-webpack-plugin@^2.2.1" \
	"webpack@^4.39.1" \
	"webpack-cli" \
	"@ckeditor/ckeditor5-dev-utils@^12.0.5" \
	"@ckeditor/ckeditor5-dev-webpack-plugin@^8.0.5";


yarn run build