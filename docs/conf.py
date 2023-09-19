#html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']
html_logo = "img/openBIS.png"
html_theme_options = {
        'logo_only': True,
        'display_version': False,
}
extensions = ['myst_parser']
source_suffix = ['.rst', '.md']
def setup(app):
    app.add_css_file('my_theme.css')
