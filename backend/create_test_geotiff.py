import rasterio
import numpy as np
from PIL import Image
from rasterio.transform import from_origin

image = Image.open("../data/drone.jpg").convert("RGB")
data = np.array(image)

transform = from_origin(
    500000,
    1800000,
    0.5,
    0.5
)

with rasterio.open(
    "uploads/test_georeferenced.tif",
    "w",
    driver="GTiff",
    height=data.shape[0],
    width=data.shape[1],
    count=3,
    dtype=data.dtype,
    crs="EPSG:32644",
    transform=transform
) as dst:

    dst.write(data[:, :, 0], 1)
    dst.write(data[:, :, 1], 2)
    dst.write(data[:, :, 2], 3)