import numpy
from PIL import Image
import io
import base64
import sys
import json
import os
import sys
from spmpy_terry import spm   # <--- class spm defines objects of type spm with their attributes and class functions
import spmpy_terry as spmpy   # <--- spmpy has other methods

import matplotlib.pyplot as plt
# %matplotlib inline


class NumpyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, numpy.ndarray):
            return obj.tolist()
        return json.JSONEncoder.default(self, obj)

def load_image(path):
    return spm(path)


def get_lock_in(img):
    param_name = 'lock-in>lock-in status'
    param = img.get_param(param_name)
    return param


def get_channel(img, channel_name = 'z'):
    # channel_name = 'z'
    channel = img.get_channel(channel_name)
    return channel

print("SYS.ARGV:" + str(sys.argv))
file = sys.argv[1]
image_params = json.loads(sys.argv[2])
params = json.loads(sys.argv[3])
meta_data = json.loads(sys.argv[4])
format = sys.argv[5]


folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])


def generate_random_image(height, width):
    imarray = numpy.random.rand(height,width,3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format='PNG')
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return encoded


def get_sxm_image(channel_name, x_axis, y_axis, scaling, color_scale, colormap, colormap_scaling):
    img = load_image(file_path)
    img_byte_arr = io.BytesIO()

    log = False
    if scaling == 'logarithmic':
        log = True
    fig = img.plot(show=False, show_params=False, channel=channel_name, log=log, cmap=colormap,
                   color_scale=color_scale, x_axis=x_axis, y_axis=y_axis, colormap_scaling=colormap_scaling)
    plt.savefig(img_byte_arr, format=format)

    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    print_params = img.print_params(show=False).split('\n')
    print_params = {x: y for x, y in (s.split(':') for s in print_params)}
    print_params = json.dumps(print_params)
    print(f'PARAMS={print_params}')
    header = json.dumps(img.header, cls=NumpyEncoder)
    print(f'HEADER={header}')

    return encoded

def sxm_mode(parameters):
    channel = parameters['channel']
    x_axis = [float(x) for x in parameters['x-axis']]
    y_axis = [float(x) for x in parameters['y-axis']]
    color_scale = [float(x) for x in parameters['color-scale']]
    colormap = parameters['colormap']
    scaling = parameters['scaling']
    colormap_scaling = False
    if "colormap_scaling" in parameters:
        colormap_scaling = parameters['colormap_scaling'].upper() == "TRUE"
    print(f'{get_sxm_image(channel, x_axis, y_axis, scaling, color_scale, colormap, colormap_scaling)}')

print(params)
if 'mode' in params:
    if params['mode'] == '3':
        print(f'{generate_random_image(256, 256)}')
    elif params['mode'] == '5':
        sxm_mode(params)
else:
    sxm_mode(params)