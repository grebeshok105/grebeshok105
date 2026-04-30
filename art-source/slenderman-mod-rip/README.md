# Slenderman Mod Rip — Reference Material

Source: `slenderman-1.1.10-forge-1.20.1.jar` (MCreator-generated, GeckoLib-based).
Mod author: hqrvester. License: not declared (assumed All-Rights-Reserved).

## ⚠️ Usage Notice

These assets are **reference material only**. They are NOT bundled into the
shipped Superheroes mod. Re-use any of these (textures, sounds, geometry)
ONLY after confirming licensing with the original author, OR replace them
with originals before any release.

## What's here

- `assets/slenderman/geo/*.geo.json` — GeckoLib bedrock-format 3D models
  (slenderman, entity_303, the_boilded_one_reimagined, victim_ghost)
- `assets/slenderman/animations/*.animation.json` — bedrock animations
- `assets/slenderman/textures/entities/slenderman.png` — main 3D-model texture
- `assets/slenderman/textures/screens/{static_close,static_mid,static_far,slenderman_jumpscare}.png`
  — screen overlays (TV-static effect at different distances + jumpscare frame)
- `assets/slenderman/textures/mob_effect/*` — status effect icons
- `assets/slenderman/sounds/*.ogg` — 17 sound files:
  - `slenderman_active`, `slenderman_angry`, `slenderman_attack`, `slenderman_death`
  - `slenderman_hunt`, `slenderman_hurt`, `slenderman_living`, `slenderman_spawn`
  - `slenderman_victory`, `slenderman_warning`, `slenderman_jumpscare`
  - `slender_static` (TV-static loop)
  - `bush_movement_{1,2,3}` (ambient stalker sounds)
  - `page_grab`, `victim_ghost_*`
- `data/slenderman/*` — recipes, advancements, structures, biome (NOT used in our mod)

## How we use this for the Superheroes Slenderman hero

1. **Geometry**: port `slenderman.geo.json` + `slenderman.animation.json` to GeckoLib
   on Fabric 1.21 (need to add `geckolib-fabric` dependency). Recolor the texture
   to be **enderman-like** (purple eyes, dark suit) per user request.
2. **Static screen overlay**: copy `screens/static_*.png` into our HUD pipeline
   (`SlenderStaticHud.render`), tied to LoS proximity to the Slenderman hero.
3. **Sounds**: import the OGG files into `assets/superheroes/sounds/slenderman/`,
   register in `ModSounds.java`. `slenderman_static` becomes the looping ULT
   field-effect sound; `slenderman_warning` plays when staring at the hero.
4. **Status effect textures**: re-use `slender_static_*.png` for our debuff icons
   (TV-static intensity = stack count).

The actual `.class` Java files from the source mod are NOT included — this is
reference for art assets only. All combat logic will be reimplemented from
scratch in `com.example.superheroes` package on Fabric.
