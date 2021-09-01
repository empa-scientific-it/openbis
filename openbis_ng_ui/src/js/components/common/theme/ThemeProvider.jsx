import React from 'react'
import { MuiThemeProvider, createTheme } from '@material-ui/core/styles'
import indigo from '@material-ui/core/colors/indigo'
import lightBlue from '@material-ui/core/colors/lightBlue'

const config = {
  typography: {
    useNextVariants: true,
    label: {
      fontSize: '0.7rem',
      color: '#0000008a'
    },
    sourceCode: {
      fontFamily: '"Fira code", "Fira Mono", monospace'
    }
  },
  palette: {
    primary: {
      main: indigo[700]
    },
    secondary: {
      main: lightBlue[600]
    },
    info: {
      main: lightBlue[600]
    },
    warning: {
      main: '#ff9609'
    },
    hint: {
      main: '#bdbdbd'
    },
    background: {
      primary: '#ebebeb',
      secondary: '#f5f5f5',
      field: '#e8e8e8'
    },
    border: {
      primary: '#dbdbdb',
      secondary: '#ebebeb',
      field: '#878787'
    }
  }
}

const theme = createTheme(config)

class ThemeProvider extends React.Component {
  render() {
    return (
      <MuiThemeProvider theme={theme}>{this.props.children}</MuiThemeProvider>
    )
  }
}

export default ThemeProvider
export { config }
