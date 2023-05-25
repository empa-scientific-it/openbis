import AndroidChrome192 from '@src/img/android-chrome-192x192.png?url'
import AndroidChrome512 from '@src/img/android-chrome-512x512.png?url'

const config = {
  name: 'openBIS Admin UI',
  short_name: 'Admin UI',
  icons: [
    {
      src: AndroidChrome192,
      sizes: '192x192',
      type: 'image/png'
    },
    {
      src: AndroidChrome512,
      sizes: '512x512',
      type: 'image/png'
    }
  ],
  theme_color: '#ffffff',
  background_color: '#ffffff',
  display: 'standalone'
}

export default 'data:application/manifest+json;base64,' +
  Buffer.from(JSON.stringify(config)).toString('base64')
