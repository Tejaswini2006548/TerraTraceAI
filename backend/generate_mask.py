import onnxruntime as ort
from PIL import Image
import numpy as np

model_path = "models/segmentation/terratrace-segmentation.onnx"
image_path = "../data/drone.jpg"

session = ort.InferenceSession(
    model_path,
    providers=["CPUExecutionProvider"]
)

image = Image.open(
    image_path
).convert("RGB")

original_size = image.size

image_resized = image.resize(
    (512, 512)
)

array = np.asarray(
    image_resized
).astype(
    np.float32
) / 255.0

tensor = np.transpose(
    array,
    (2, 0, 1)
)

tensor = np.expand_dims(
    tensor,
    axis=0
)

input_name = session.get_inputs()[0].name

output = session.run(
    None,
    {
        input_name: tensor
    }
)

logits = output[0]

prediction = np.argmax(
    logits,
    axis=1
)[0]

prediction_image = Image.fromarray(
    prediction.astype(np.uint8)
)

prediction_image = prediction_image.resize(
    original_size,
    Image.Resampling.NEAREST
)

prediction_array = np.asarray(
    prediction_image
)

mask = np.zeros(
    (original_size[1], original_size[0]),
    dtype=np.uint8
)

mask[prediction_array == 1] = 255

mask_image = Image.fromarray(
    mask
)

mask_image.save(
    "building_mask.png"
)

original_array = np.asarray(
    image
)

overlay = np.zeros_like(
    original_array
)

overlay[prediction_array == 1] = [
    255,
    0,
    0
]

blended = (
    original_array.astype(np.float32) * 0.65
    + overlay.astype(np.float32) * 0.35
)

blended = np.clip(
    blended,
    0,
    255
).astype(np.uint8)

overlay_image = Image.fromarray(
    blended
)

overlay_image.save(
    "building_overlay.png"
)

print("Segmentation completed")
print("Original image size:", original_size)
print("Prediction size:", prediction.shape)
print("Unique classes:", np.unique(prediction))
print("Building mask: building_mask.png")
print("Overlay image: building_overlay.png")