import React from 'react'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import DatePickerProvider from '@src/js/components/common/date/DatePickerProvider.jsx'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'

export default class GridWrapper extends React.PureComponent {
  render() {
    return (
      <ThemeProvider>
        <ErrorBoundary>
          <DatePickerProvider>
            <Grid {...this.props} />
          </DatePickerProvider>
        </ErrorBoundary>
      </ThemeProvider>
    )
  }
}
