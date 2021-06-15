import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import store from '@src/js/store/store.js'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import DatePickerProvider from '@src/js/components/common/date/DatePickerProvider.jsx'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'

const render = () => {
  let App = require('./components/App.jsx').default

  ReactDOM.render(
    <Provider store={store}>
      <ThemeProvider>
        <ErrorBoundary>
          <DatePickerProvider>
            <App />
          </DatePickerProvider>
        </ErrorBoundary>
      </ThemeProvider>
    </Provider>,
    document.getElementById('app')
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
}

render()
