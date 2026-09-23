import onnxruntime as ort
from PIL import Image
import numpy as np

model_path = "models/segmentation/terratrace-segmentation.onnx"
image_path = "../data/drone.jpg"

session = ort.InferenceSession(
    model_path,
    providers=["CPUExecutionProvider"]
)

image = Image.open(image_path).convert("RGB")

image = image.resize(
    (512, 512)
)

array = np.asarray(
    image
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
)

print("ONNX model loaded successfully")
print("Input name:", input_name)
print("Input shape:", tensor.shape)
print("Output shape:", logits.shape)
print("Prediction shape:", prediction.shape)
print("Unique classes:", np.unique(prediction))