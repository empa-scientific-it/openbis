import React from 'react'
import { MuiThemeProvider, createTheme } from '@material-ui/core/styles'
import { config } from '@src/js/components/common/theme/ThemeProvider.jsx'

const theme = createTheme({
  ...config,
  props: {
    MuiCollapse: {
      timeout: 0
    }
  },
  transitions: {
    create: () => 'none'
  }
})

export default class ThemeProvider extends React.Component {
  render() {
    return (
      <MuiThemeProvider theme={theme}>{this.props.children}</MuiThemeProvider>
    )
  }
}
