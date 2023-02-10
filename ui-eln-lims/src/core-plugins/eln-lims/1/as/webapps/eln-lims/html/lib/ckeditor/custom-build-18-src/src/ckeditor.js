/**
 * @license Copyright (c) 2003-2019, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or https://ckeditor.com/legal/ckeditor-oss-license
 */

// The editor creator to use.


// This is the source for custom build.
// Checkout inline editor here - https://github.com/ckeditor/ckeditor5-build-inline
// Copy this file to the path ckeditor5-build-inline/src/ckeditor.js
// You can read about the custom build at this link - shttps://ckeditor.com/docs/ckeditor5/latest/builds/guides/development/custom-builds.html

import InlineEditorBase from '@ckeditor/ckeditor5-editor-inline/src/inlineeditor';
import DecoupledEditorBase from '@ckeditor/ckeditor5-editor-decoupled/src/decouplededitor';

import Essentials from '@ckeditor/ckeditor5-essentials/src/essentials';
import Alignment from '@ckeditor/ckeditor5-alignment/src/alignment';
import Font from '@ckeditor/ckeditor5-font/src/font';
import Highlight from '@ckeditor/ckeditor5-highlight/src/highlight';
import SimpleUploadAdapter from '@ckeditor/ckeditor5-upload/src/adapters/simpleuploadadapter';
import Bold from '@ckeditor/ckeditor5-basic-styles/src/bold';
import Italic from '@ckeditor/ckeditor5-basic-styles/src/italic';
import Strikethrough from '@ckeditor/ckeditor5-basic-styles/src/strikethrough';
import Underline from '@ckeditor/ckeditor5-basic-styles/src/underline';
import Subscript from '@ckeditor/ckeditor5-basic-styles/src/subscript';
import Superscript from '@ckeditor/ckeditor5-basic-styles/src/superscript';
import BlockQuote from '@ckeditor/ckeditor5-block-quote/src/blockquote';
import CKFinder from '@ckeditor/ckeditor5-ckfinder/src/ckfinder';
import EasyImage from '@ckeditor/ckeditor5-easy-image/src/easyimage';
import Heading from '@ckeditor/ckeditor5-heading/src/heading';
import Image from '@ckeditor/ckeditor5-image/src/image';
import ImageCaption from '@ckeditor/ckeditor5-image/src/imagecaption';
import ImageStyle from '@ckeditor/ckeditor5-image/src/imagestyle';
import ImageToolbar from '@ckeditor/ckeditor5-image/src/imagetoolbar';
import ImageUpload from '@ckeditor/ckeditor5-image/src/imageupload';
import ImageResize from '@ckeditor/ckeditor5-image/src/imageresize';
import Link from '@ckeditor/ckeditor5-link/src/link';
import List from '@ckeditor/ckeditor5-list/src/list';
import MediaEmbed from '@ckeditor/ckeditor5-media-embed/src/mediaembed';
import Paragraph from '@ckeditor/ckeditor5-paragraph/src/paragraph';
import PasteFromOffice from '@ckeditor/ckeditor5-paste-from-office/src/pastefromoffice';
import Table from '@ckeditor/ckeditor5-table/src/table';
import TableToolbar from '@ckeditor/ckeditor5-table/src/tabletoolbar';
import TableProperties from '@ckeditor/ckeditor5-table/src/tableproperties';
import TableCellProperties from '@ckeditor/ckeditor5-table/src/tablecellproperties';
import RemoveFormat from '@ckeditor/ckeditor5-remove-format/src/removeformat';
import SpecialCharacters from '@ckeditor/ckeditor5-special-characters/src/specialcharacters';
import SpecialCharactersEssentials from '@ckeditor/ckeditor5-special-characters/src/specialcharactersessentials';

class InlineEditor extends InlineEditorBase {}
class DecoupledEditor extends DecoupledEditorBase {}

const plugins = [Essentials,
                 	Alignment,
                 	Font,
                 	Highlight,
                 	SimpleUploadAdapter,
                 	Bold,
                 	Italic,
                 	Strikethrough,
                 	Underline,
                 	Subscript,
                 	Superscript,
                 	BlockQuote,
                 	CKFinder,
                 	EasyImage,
                 	Heading,
                 	Image,
                 	ImageCaption,
                 	ImageStyle,
                 	ImageToolbar,
                 	ImageUpload,
                 	ImageResize,
                 	Link,
                 	List,
                 	MediaEmbed,
                 	Paragraph,
                 	PasteFromOffice,
                 	Table,
                 	TableToolbar,
                 	TableProperties,
                 	TableCellProperties,
                 	RemoveFormat,
                 	SpecialCharacters,
                 	SpecialCharactersEssentials];

// Plugins to include in the build.
InlineEditor.builtinPlugins = plugins;
DecoupledEditor.builtinPlugins = plugins;

// Editor configuration.
const config = {
               	toolbar: {
               		viewportTopOffset : 156,
               		items: [
               			'heading',
               			'|',
               			'fontSize',
               			'fontFamily',
               			'fontColor',
               			'fontBackgroundColor',
               			'|',
               			'bold',
               			'italic',
               			'underline',
               			'strikethrough',
               			'subscript',
               			'Superscript',
               			'highlight',
               			'|',
               			'specialCharacters',
               			'|',
               			'alignment',
               			'|',
               			'numberedList',
               			'bulletedList',
               			'|',
               			'link',
               			'blockquote',
               			'imageUpload',
               			'insertTable',
               			'|',
               			'undo',
               			'redo'
               		]
               	},
               	fontFamily: {
               		options: [
               			'default',
               			'Arial, Helvetica, sans-serif',
               			'Courier New, Courier, monospace',
               			'Georgia, serif',
               			'Lucida Sans Unicode, Lucida Grande, sans-serif',
               			'Tahoma, Geneva, sans-serif',
               			'Times New Roman, Times, serif',
               			'Trebuchet MS, Helvetica, sans-serif',
               			'Verdana, Geneva, sans-serif',
               			'Calibri, sans-serif',
               			'Arial Unicode MS, sans-serif',
               			'Comic Sans MS/Comic Sans MS, cursive;'
               		]
               	},
               	fontSize: {
               		options: [
               			9,
               			9.5,
               			10,
               			10.5,
               			11,
               			11.5,
               			12,
               			12.5,
               			13,
               			13.5,
               			'default',
               			14,
               			15,
               			16,
               			17,
               			18,
               			19,
               			20,
               			21,
               			22,
               			23,
               			24,
               			25
               		]
               	},
               	image: {
               		styles: [
               			'full',
               			'alignLeft',
               			'alignRight'
               		],
               		toolbar: [
               			'imageStyle:alignLeft',
               			'imageStyle:full',
               			'imageStyle:alignRight',
               			'|',
               			'imageTextAlternative'
               		]
               	},
               	table: {
               		contentToolbar: [
               			'tableColumn',
               			'tableRow',
               			'mergeTableCells',
                        'tableProperties',
                        'tableCellProperties'
               		]
               	},
               	// This value must be kept in sync with the language defined in webpack.config.js.
               	language: 'en'
               };

InlineEditor.defaultConfig = config;
DecoupledEditor.defaultConfig = config;

export default {
    InlineEditor, DecoupledEditor
};