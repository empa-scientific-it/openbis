# Required software for the build
# brew install git
# brew install npm
# brew install yarn

# Example Build Environment
# git clone https://github.com/ckeditor/ckeditor5-build-inline.git
# cd ckeditor5-build-inline

# Use as reference for dependency versions: https://github.com/ckeditor/ckeditor5/blob/master/package.json

# Build commands

npm install --save "@ckeditor/ckeditor5-adapter-ckfinder@^32.0.0" \
                       "@ckeditor/ckeditor5-autoformat@^32.0.0" \
                       "@ckeditor/ckeditor5-basic-styles@^32.0.0" \
                       "@ckeditor/ckeditor5-block-quote@^32.0.0" \
                       "@ckeditor/ckeditor5-ckfinder@^32.0.0" \
                       "@ckeditor/ckeditor5-cloud-services@^32.0.0" \
                       "@ckeditor/ckeditor5-easy-image@^32.0.0" \
                       "@ckeditor/ckeditor5-editor-inline@^32.0.0" \
                       "@ckeditor/ckeditor5-essentials@^32.0.0" \
                       "@ckeditor/ckeditor5-heading@^32.0.0" \
                       "@ckeditor/ckeditor5-image@^32.0.0" \
                       "@ckeditor/ckeditor5-indent@^32.0.0" \
                       "@ckeditor/ckeditor5-link@^32.0.0" \
                       "@ckeditor/ckeditor5-list@^32.0.0" \
                       "@ckeditor/ckeditor5-media-embed@^32.0.0" \
                       "@ckeditor/ckeditor5-paragraph@^32.0.0" \
                       "@ckeditor/ckeditor5-paste-from-office@^32.0.0" \
                       "@ckeditor/ckeditor5-table@^32.0.0" \
                       "@ckeditor/ckeditor5-typing@^32.0.0" \
                       "@ckeditor/ckeditor5-core@^32.0.0" \
                       "@ckeditor/ckeditor5-dev-utils@^28.0.1" \
                       "@ckeditor/ckeditor5-dev-webpack-plugin@^28.0.1" \
                       "@ckeditor/ckeditor5-theme-lark@^32.0.0" \
                       "@ckeditor/ckeditor5-editor-decoupled@^32.0.0" \
                       "@ckeditor/ckeditor5-alignment@^32.0.0" \
                       "@ckeditor/ckeditor5-font@^32.0.0" \
                       "@ckeditor/ckeditor5-highlight@^32.0.0" \
                       "@ckeditor/ckeditor5-remove-format@^32.0.0" \
                       "@ckeditor/ckeditor5-special-characters@^32.0.0" \
                       "css-loader@^5.2.7" \
                       "postcss-loader@^4.3.0" \
                       "raw-loader@^4.0.1" \
                       "style-loader@^2.0.0" \
                       "terser-webpack-plugin@^4.2.3" \
                       "webpack@^5.58.1" \
                       "webpack-cli@^4.9.0";


yarn run build