import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import DatePickerProvider from '@src/js/components/common/date/DatePickerProvider.jsx'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import V3API from '@srcV3Example/static/V3API.js'

async function useV3APIImported() {
  var facade = new V3API.openbis()
  await facade.login('admin', 'password')
  var searchResult = await facade.searchSpaces(
    new V3API.SpaceSearchCriteria(),
    new V3API.SpaceFetchOptions()
  )
  alert('Found spaces: ' + searchResult.getObjects().length)
}

const render = () => {
  let App = require('./components/App.jsx').default

  ReactDOM.render(
    <ThemeProvider>
      <ErrorBoundary>
        <DatePickerProvider>
          <button onClick={useV3APIImported}>Use V3 API</button>
          <App />
        </DatePickerProvider>
      </ErrorBoundary>
    </ThemeProvider>,
    document.getElementById('app')
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
}

render()
