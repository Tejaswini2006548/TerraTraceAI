import torch
from transformers import SegformerConfig, SegformerForSemanticSegmentation
from huggingface_hub import hf_hub_download
from PIL import Image
import numpy as np

repo_id = "yantrikaran-innovations/segformer-b5-andhra-landuse"

checkpoint_path = hf_hub_download(
    repo_id=repo_id,
    filename="best_model.pth"
)

config = SegformerConfig(
    num_channels=3,
    num_encoder_blocks=4,
    depths=[3, 6, 40, 3],
    sr_ratios=[8, 4, 2, 1],
    hidden_sizes=[64, 128, 320, 512],
    patch_sizes=[7, 3, 3, 3],
    strides=[4, 2, 2, 2],
    num_attention_heads=[1, 2, 5, 8],
    mlp_ratios=[4, 4, 4, 4],
    hidden_act="gelu",
    hidden_dropout_prob=0.0,
    attention_probs_dropout_prob=0.0,
    classifier_dropout_prob=0.1,
    initializer_range=0.02,
    drop_path_rate=0.1,
    layer_norm_eps=1e-6,
    decoder_hidden_size=768,
    num_labels=5,
    image_size=512,
    id2label={
        0: "Background",
        1: "Buildings",
        2: "Road",
        3: "Water Body",
        4: "Open Plot"
    },
    label2id={
        "Background": 0,
        "Buildings": 1,
        "Road": 2,
        "Water Body": 3,
        "Open Plot": 4
    }
)

model = SegformerForSemanticSegmentation(config)

checkpoint = torch.load(
    checkpoint_path,
    map_location="cpu",
    weights_only=True
)
state_dict = {
    key.removeprefix("model."): value
    for key, value in checkpoint["model_state_dict"].items()
}
result = model.load_state_dict(
    state_dict,
    strict=True
)

model.eval()

image = Image.open(
    "D:/TerraTraceAI/TerraTraceAI/data/drone.jpg"
).convert("RGB")

image = image.resize((512, 512))

array = np.asarray(image).astype(np.float32) / 255.0

tensor = torch.from_numpy(
    array.transpose(2, 0, 1)
).unsqueeze(0)

with torch.no_grad():
    output = model(pixel_values=tensor)

print("Model loaded successfully")
print("Missing keys:", result.missing_keys)
print("Unexpected keys:", result.unexpected_keys)
print("Input shape:", tuple(tensor.shape))
print("Output shape:", tuple(output.logits.shape))
print("Predicted mask shape:", tuple(output.logits.argmax(dim=1).shape))