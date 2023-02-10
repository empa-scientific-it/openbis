# Required software for the build
# brew install git
# brew install npm
# brew install yarn

# Example Build Environment
# git clone https://github.com/ckeditor/ckeditor5-build-inline.git
# cd ckeditor5-build-inline

# Use as reference for dependency versions: https://github.com/ckeditor/ckeditor5/blob/master/package.json

# Build commands

npm install --save "@ckeditor/ckeditor5-adapter-ckfinder@^34.0.0" \
                       "@ckeditor/ckeditor5-basic-styles@^34.0.0" \
                       "@ckeditor/ckeditor5-block-quote@^34.0.0" \
                       "@ckeditor/ckeditor5-ckfinder@^34.0.0" \
                       "@ckeditor/ckeditor5-cloud-services@^34.0.0" \
                       "@ckeditor/ckeditor5-easy-image@^34.0.0" \
                       "@ckeditor/ckeditor5-editor-inline@^34.0.0" \
                       "@ckeditor/ckeditor5-essentials@^34.0.0" \
                       "@ckeditor/ckeditor5-heading@^34.0.0" \
                       "@ckeditor/ckeditor5-image@^34.0.0" \
                       "@ckeditor/ckeditor5-indent@^34.0.0" \
                       "@ckeditor/ckeditor5-link@^34.0.0" \
                       "@ckeditor/ckeditor5-list@^34.0.0" \
                       "@ckeditor/ckeditor5-media-embed@^34.0.0" \
                       "@ckeditor/ckeditor5-paragraph@^34.0.0" \
                       "@ckeditor/ckeditor5-paste-from-office@^34.0.0" \
                       "@ckeditor/ckeditor5-table@^34.0.0" \
                       "@ckeditor/ckeditor5-typing@^34.0.0" \
                       "@ckeditor/ckeditor5-core@^34.0.0" \
                       "@ckeditor/ckeditor5-dev-utils@^28.0.1" \
                       "@ckeditor/ckeditor5-dev-webpack-plugin@^28.0.1" \
                       "@ckeditor/ckeditor5-theme-lark@^34.0.0" \
                       "@ckeditor/ckeditor5-editor-decoupled@^34.0.0" \
                       "@ckeditor/ckeditor5-alignment@^34.0.0" \
                       "@ckeditor/ckeditor5-font@^34.0.0" \
                       "@ckeditor/ckeditor5-highlight@^34.0.0" \
                       "@ckeditor/ckeditor5-remove-format@^34.0.0" \
                       "@ckeditor/ckeditor5-special-characters@^34.0.0" \
                       "@ckeditor/ckeditor5-core@^34.0.0" \
                       "@ckeditor/ckeditor5-dev-utils@^30.0.0" \
                       "@ckeditor/ckeditor5-dev-webpack-plugin@^30.0.0" \
                       "@ckeditor/ckeditor5-theme-lark@^30.0.0" \
                       "css-loader@^5.2.7" \
                       "postcss-loader@^4.3.0" \
                       "raw-loader@^4.0.1" \
                       "style-loader@^2.0.0" \
                       "terser-webpack-plugin@^4.2.3" \
                       "webpack@^5.58.1" \
                       "webpack-cli@^4.9.0"

yarn run build