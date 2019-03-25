import {all} from 'redux-saga/effects'
import app from './app.js'
import api from './api.js'
import browser from './browser/browser.js'

export default function* root() {
  yield all([
    api(),
    app(),
    browser()
  ])
}
