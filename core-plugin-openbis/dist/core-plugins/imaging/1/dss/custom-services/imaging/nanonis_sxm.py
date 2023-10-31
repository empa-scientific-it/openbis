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


file = sys.argv[1]
params = json.loads(sys.argv[2])
meta_data = json.loads(sys.argv[3])


folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])
# print(file_path)


def generate_random_image(height, width):
    imarray = numpy.random.rand(height,width,3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format='PNG')
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return encoded


def get_sxm_image():
    img = load_image(file_path)
    channel = get_channel(img, 'z')
    img_byte_arr = io.BytesIO()

    # im = plt.imshow(channel[0])
    # plt.savefig(img_byte_arr, format="png")
    fig = img.plot(show=False, show_params=False)
    fig.frameon=False
    # plt.title(None)
    plt.axis('off')
    plt.savefig(img_byte_arr, format="png", bbox_inches='tight')

    # print(img.header)
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    print_params = img.print_params(show=False).split('\n')
    print_params = {x: y for x, y in (s.split(':') for s in print_params)}
    print_params = json.dumps(print_params)
    print(f'PARAMS={print_params}')
    header = json.dumps(img.header, cls=NumpyEncoder)
    print(f'HEADER={header}')

    return encoded


def get_sxm_image2():
    img_orig = load_image(file_path)
    channel = get_channel(img_orig, 'z')[0]

    min_val = numpy.min(channel)
    max_val = numpy.max(channel)
    scaled_data = (channel - min_val) / (max_val - min_val)
    img = Image.fromarray(numpy.uint8(scaled_data * 255), 'L')

    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='PNG')
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)

    print_params = img_orig.print_params(show=False).split('\n')
    print_params = {x: y for x, y in (s.split(':') for s in print_params)}
    print_params = json.dumps(print_params)
    print(f'PARAMS={print_params}')
    header = json.dumps(img_orig.header, cls=NumpyEncoder)
    print(f'HEADER={header}')

    return encoded

print(params)
if params['mode'] == '1':
    # print(f'IMG={get_sxm_image()}')
    print(f'{get_sxm_image()}')
elif params['mode'] == '2':
    # print(f'IMG={get_sxm_image2()}')
    print(f'{get_sxm_image2()}')
elif params['mode'] == '3':
    # print(f'IMG={generate_random_image(320, 320)}')
    print(f'{generate_random_image(256, 256)}')