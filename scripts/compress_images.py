"""Compress 512x512 PNG assets in one directory without resizing them."""

import argparse
from io import BytesIO
from pathlib import Path

ICON_SIZE = (512, 512)


def _png_files(directory):
    return sorted(
        path
        for path in directory.iterdir()
        if path.is_file() and path.suffix.lower() == ".png"
    )


def _format_bytes(size):
    return f"{size / 1024:.1f} KiB"


def compress_png(path):
    """Compress one PNG and return its before/after byte counts."""
    try:
        from PIL import Image
    except ModuleNotFoundError as error:
        raise RuntimeError("Pillow is required to compress PNG files") from error

    before = path.stat().st_size
    with Image.open(path) as image:
        if image.size != ICON_SIZE:
            raise ValueError(
                f"expected {ICON_SIZE[0]}x{ICON_SIZE[1]}, found "
                f"{image.size[0]}x{image.size[1]}"
            )

        converted = image
        if image.mode != "P":
            converted = image.convert("P", palette=Image.ADAPTIVE, colors=256)

        output = BytesIO()
        converted.save(output, "PNG", optimize=True)
        compressed = output.getvalue()

    with Image.open(BytesIO(compressed)) as check:
        if check.size != ICON_SIZE:
            raise ValueError(
                f"compression changed dimensions to "
                f"{check.size[0]}x{check.size[1]}"
            )
        check.load()

    path.write_bytes(compressed)
    return before, len(compressed)


def compress_directory(directory):
    png_paths = _png_files(directory)
    before_total = sum(path.stat().st_size for path in png_paths)
    after_total = before_total
    failures = 0

    print(f"Scanning directory: {directory}")
    print(f"PNG files: {len(png_paths)}")
    print(f"Before total: {_format_bytes(before_total)}")

    for path in png_paths:
        try:
            before, after = compress_png(path)
            after_total += after - before
            print(
                f"Compressed {path.name}: {_format_bytes(before)} => "
                f"{_format_bytes(after)}"
            )
        except Exception as error:
            failures += 1
            print(f"Error compressing {path.name}: {error}")

    print(f"After total: {_format_bytes(after_total)}")
    print(f"Total saved: {_format_bytes(before_total - after_total)}")
    print(f"Failures: {failures}")
    return failures


def _parse_args():
    parser = argparse.ArgumentParser(
        description="Compress 512x512 PNG files in one directory."
    )
    parser.add_argument("directory", type=Path, help="directory containing PNG files")
    return parser.parse_args()


def main():
    args = _parse_args()
    directory = args.directory.expanduser().resolve()
    if not directory.is_dir():
        raise SystemExit(f"Error: directory is not a directory: {directory}")

    return compress_directory(directory)


if __name__ == "__main__":
    raise SystemExit(main())
