"""Validate the manually supplied Palworld portrait crops."""

import argparse
from pathlib import Path

ICON_SIZE = (512, 512)
EXPECTED_PORTRAIT_FILENAMES = (
    "lamball_v1.png",
    "cattiva_v1.png",
    "chikipi_v1.png",
    "foxparks_v1.png",
    "lifmunk_v1.png",
    "pengullet_v1.png",
    "daedream_v1.png",
    "depresso_v1.png",
    "gumoss_v1.png",
    "vixy_v1.png",
    "sparkit_v1.png",
    "tanzee_v1.png",
    "fuack_v1.png",
    "tocotoco_v1.png",
    "direhowl_v1.png",
    "celaray_v1.png",
    "dumud_v1.png",
    "dazzi_v1.png",
    "flambelle_v1.png",
    "mimog_v1.png",
    "cremis_v1.png",
    "melpaca_v1.png",
    "galeclaw_v1.png",
    "lovander_v1.png",
    "hoodle_v1.png",
    "chillet_v1.png",
    "penking_v1.png",
    "katress_v1.png",
    "lunaris_v1.png",
    "quivern_v1.png",
    "petallia_v1.png",
    "mossanda_v1.png",
    "grizzbolt_v1.png",
    "tarantriss_v1.png",
    "relaxaurus_v1.png",
    "tetroise_v1.png",
    "anubis_v1.png",
    "shadowbeak_v1.png",
    "lyleen_v1.png",
    "orserk_v1.png",
    "selyne_v1.png",
    "jormuntide-ignis_v1.png",
    "bellanoir_v1.png",
    "aegidron_v1.png",
    "renjishi_v1.png",
    "silvance_v1.png",
    "dandilord_v1.png",
    "shaolong_v1.png",
    "jetragon_v1.png",
    "frostallion_v1.png",
    "paladius_v1.png",
    "necromus_v1.png",
    "neptilius_v1.png",
    "xenolord_v1.png",
    "panthalus_v1.png",
)
EXPECTED_PORTRAIT_NAMES = frozenset(EXPECTED_PORTRAIT_FILENAMES)


def _png_files(directory):
    return sorted(
        path
        for path in directory.iterdir()
        if path.is_file() and path.suffix.lower() == ".png"
    )


def _validate_png(path):
    try:
        from PIL import Image
    except ModuleNotFoundError as error:
        raise RuntimeError("Pillow is required to validate PNG files") from error

    try:
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            if image.size != ICON_SIZE:
                return (
                    f"{path.name}: expected {ICON_SIZE[0]}x{ICON_SIZE[1]}, "
                    f"found {image.size[0]}x{image.size[1]}"
                )
            image.load()
    except Exception as error:
        return f"{path.name}: cannot decode PNG ({error})"
    return None


def validate_directory(directory):
    png_paths = _png_files(directory)
    found_names = {path.name for path in png_paths}
    errors = []

    errors.extend(
        f"missing expected portrait: {name}"
        for name in EXPECTED_PORTRAIT_FILENAMES
        if name not in found_names
    )
    errors.extend(
        f"unexpected PNG: {name}"
        for name in sorted(found_names - EXPECTED_PORTRAIT_NAMES)
    )
    for path in png_paths:
        if path.name not in EXPECTED_PORTRAIT_NAMES:
            continue
        problem = _validate_png(path)
        if problem is not None:
            errors.append(problem)
    return errors


def _parse_args():
    parser = argparse.ArgumentParser(
        description="Validate the expected 55 Palworld portrait PNG files."
    )
    parser.add_argument("directory", type=Path, help="directory containing portrait PNGs")
    return parser.parse_args()


def main():
    args = _parse_args()
    directory = args.directory.expanduser().resolve()
    if not directory.is_dir():
        raise SystemExit(f"Error: directory is not a directory: {directory}")

    try:
        errors = validate_directory(directory)
    except RuntimeError as error:
        raise SystemExit(f"Error: {error}") from error

    if errors:
        print(f"Palworld portrait validation failed ({len(errors)} issue(s)):")
        for error in errors:
            print(f"- {error}")
        return 1

    print(
        f"Validated {len(EXPECTED_PORTRAIT_FILENAMES)} Palworld portraits: "
        "all are decodable 512x512 PNGs."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
