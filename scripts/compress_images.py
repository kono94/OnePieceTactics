import os
from PIL import Image

def compress_png(directory):
    print(f"Scanning directory: {directory}")
    for filename in os.listdir(directory):
        if filename.endswith(".png"):
            filepath = os.path.join(directory, filename)
            filesize_kb = os.path.getsize(filepath) / 1024

            print(f"Compressing {filename} ({filesize_kb:.1f} KB)...")
            try:
                img = Image.open(filepath)

                # Convert to palette mode (P) for massive reduction in pixel art/cel-shaded art
                # This reduces bit depth to 8-bit (256 colors max) which is perfect for these icons
                if img.mode != 'P':
                    img = img.convert('P', palette=Image.ADAPTIVE, colors=256)

                # Save with optimization
                img.save(filepath, "PNG", optimize=True)

                new_size_kb = os.path.getsize(filepath) / 1024
                print(f"  -> Optimized: {new_size_kb:.1f} KB")
            except Exception as e:
                print(f"  -> Error compressing {filename}: {e}")

if __name__ == "__main__":
    target_dir = "/Users/jan/Projects/OnePieceTactics-1/frontend/public/assets/units/pokemon/"
    compress_png(target_dir)
