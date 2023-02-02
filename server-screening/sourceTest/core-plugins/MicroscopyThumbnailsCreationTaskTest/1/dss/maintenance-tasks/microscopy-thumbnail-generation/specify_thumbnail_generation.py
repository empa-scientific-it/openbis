from ch.systemsx.cisd.openbis.dss.etl.dto.api.impl import MaximumIntensityProjectionGenerationAlgorithm
from sets import Set

def _get_series_num():
    series_numbers = Set()
    for image_info in image_data_set_structure.getImages():
        series_numbers.add(image_info.tryGetSeriesNumber())
    return series_numbers.pop()

def process(transaction, parameters, tableBuilder):
    seriesNum = _get_series_num()
    if int(seriesNum) % 2 == 0:
        image_config.setImageGenerationAlgorithm(
                MaximumIntensityProjectionGenerationAlgorithm(
                    "MICROSCOPY_IMG_THUMBNAIL", 256, 128, "thumbnail.png"))
    image_config.setGenerateThumbnails(True)
