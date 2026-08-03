# Palworld validation checklist

Palworld is a provider-backed game mode built on the shared engine and frontend. Its current architecture is documented
by ownership in the [backend context](../backend/BACKEND_CONTEXT.md#6-modes-and-data-loading) and
[frontend context](../frontend/FRONTEND_CONTEXT.md#9-modes-assets-and-styling). This document contains only the
mode-specific validation and release checks.

## Data and preview gates

The backend data validation and frontend gallery tests jointly check:

- 55 purchasable Pal lines;
- a `12/13/11/12/7` cost distribution;
- a `23/16/16` role distribution;
- nine elemental traits;
- 15 augments;
- one root ability per Pal, for 55 root abilities total;
- 55 attack previews and 55 ability previews.

`PalworldDataValidationTest` owns the roster, trait, augment, affinity, schema, and ability-data assertions. The frontend
`palworldUltimateGalleryRoster` tests own preview completeness and stable animation identity.

Run the focused backend checks from the repository root:

```bash
cd backend
mvn -Dtest=PalworldDataValidationTest,DamageResolverTest,DefaultAbilityCasterTest test
```

Then run the full backend suite:

```bash
cd backend
mvn test
```

## Asset gates

The portrait set must contain exactly 55 expected, decodable 512×512 PNG files and no unexpected filenames.

Validate the source assets from the repository root:

```bash
python3 scripts/validate_palworld_assets.py frontend/public/assets/units/palworld
```

When source portraits change, run the repository image compression tool and validate the result again:

```bash
python3 scripts/compress_images.py frontend/public/assets/units/palworld
python3 scripts/validate_palworld_assets.py frontend/public/assets/units/palworld
```

## Frontend gates

Run the frontend tests, lint checks, and production build:

```bash
cd frontend
npm test
npm run lint
npm run build
```

These checks cover mode metadata, asset and animation registry completeness, and live Palworld animation lookup in
addition to the shared frontend suite.

## Release gate

Before releasing a Palworld change:

1. Run the full backend and frontend suites above.
2. Validate the final portrait set.
3. Run the desired opt-in balance and role simulation profiles described in the
   [backend test strategy](../backend/BACKEND_CONTEXT.md#11-test-strategy).
4. Smoke-test the production container with Palworld selected in the lobby.
5. Confirm the in-app changelog describes commit-worthy behavior or balance changes.
