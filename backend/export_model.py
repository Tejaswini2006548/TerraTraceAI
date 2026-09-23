import torch
from transformers import SegformerConfig, SegformerForSemanticSegmentation
from huggingface_hub import hf_hub_download
from pathlib import Path

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
    image_size=512
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

model.load_state_dict(
    state_dict,
    strict=True
)

model.eval()

dummy_input = torch.randn(
    1,
    3,
    512,
    512
)

output_directory = Path(
    "models/segmentation"
)

output_directory.mkdir(
    parents=True,
    exist_ok=True
)

output_path = (
    output_directory /
    "terratrace-segmentation.onnx"
)

torch.onnx.export(
    model,
    dummy_input,
    str(output_path),
    input_names=["pixel_values"],
    output_names=["logits"],
    opset_version=17,
    do_constant_folding=True
)

print("ONNX export completed")
print("Model:", output_path)
print("Input shape: (1, 3, 512, 512)")
print("Output shape: (1, 5, 128, 128)")