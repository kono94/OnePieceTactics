import sys
import os
from PIL import Image

def split_quadrants(image_path, output_dir=None):
    if not os.path.exists(image_path):
        print(f"Error: {image_path} does not exist.")
        return

    img = Image.open(image_path)
    width, height = img.size
    
    # Calculate quadrant dimensions
    q_width = width // 2
    q_height = height // 2
    
    # Define quadrants: (left, top, right, bottom)
    quadrants = [
        (0, 0, q_width, q_height),          # Top-left
        (q_width, 0, width, q_height),      # Top-right
        (0, q_height, q_width, height),      # Bottom-left
        (q_width, q_height, width, height)   # Bottom-right
    ]
    
    base_name = os.path.splitext(os.path.basename(image_path))[0]
    if output_dir is None:
        output_dir = os.path.dirname(image_path)
    
    output_files = []
    for i, box in enumerate(quadrants):
        quad_img = img.crop(box)
        output_name = f"{base_name}_q{i+1}.png"
        output_path = os.path.join(output_dir, output_name)
        quad_img.save(output_path)
        output_files.append(output_path)
        print(f"Saved: {output_path}")
    
    return output_files

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 quadrant_cutter.py <image_path> [output_dir]")
    else:
        path = sys.argv[1]
        out = sys.argv[2] if len(sys.argv) > 2 else None
        split_quadrants(path, out)
