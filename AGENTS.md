# AGENTS.md — Superheroes Mod

Гайд для Devin / других AI-агентов. Читается **в начале каждой сессии автоматически**. Назначение — за 2-3 минуты дать карту проекта: что есть, где лежит, как добавлять, чего НЕ трогать.

---

## TL;DR (если спешишь)

- **Что это**: Fabric-мод 1.21 на Java 21. Тематика — супергерои с трансформацией, кастомным HUD, способностями, ресурсами, VFX.
- **Текущее состояние**: 3 героя реализованы (Homelander, Iron Man, Regulus), 1 в design (`docs/design/hero-sung-jinwoo.md`). Текущая версия: см. `gradle.properties` `mod_version`.
- **Mod ID**: `superheroes` · **Java package**: `com.example.superheroes`
- **Build**: `./gradlew build --no-daemon -x test` (CI **не ждать**, см. ниже)
- **Branching**: `devin/$(date +%s)-<short-name>`
- **Релизы**: GitHub releases с jar-ом из `build/libs/superheroes-X.X.X.jar` (см. skill `release-mod`)
- **PR title**: `vX.X.X: <kind>: <summary>` или `feat(scope): ...` / `fix(scope): ...`. Description **на русском**.
- **Перед любым кодом**: прочитать `.agents/skills/base-rules/SKILL.md` (что НЕЛЬЗЯ).

---

## Quick map: куда идти за чем

| Хочешь… | Файл/папка |
|---|---|
| Добавить героя | `src/main/java/com/example/superheroes/hero/` + регистрация в `Heroes.java` + `HeroAttributes.java` + skin texture + suit-item |
| Добавить способность | `src/main/java/com/example/superheroes/ability/<Name>Ability.java` + `AbilityIds.java` + регистрация в `AbilityRegistry.init()` + добавить в `Hero.getAbilities()` |
| Контроллер для tick-логики (madness, reactor, totem...) | `src/main/java/com/example/superheroes/effect/<Name>Controller.java` + `init()` зов в `SuperheroesMod.onInitialize()` |
| Mixin (server-side) | `src/main/java/com/example/superheroes/mixin/` + `superheroes.mixins.json` |
| Mixin (client-side) | `src/client/java/com/example/superheroes/client/mixin/` + `superheroes.client.mixins.json` |
| HUD / overlay | `src/client/java/com/example/superheroes/client/hud/<Name>Hud.java` + регистрация в `SuperheroesClient.onInitializeClient()` (`HudRenderCallback`) |
| Render / partial / лазер | `src/client/java/com/example/superheroes/client/render/` |
| Сетевой пакет | `src/main/java/com/example/superheroes/network/<Name>Payload.java` + регистрация в `ModNetworking.init()`, клиент-обработчик в `ClientNetworking.init()` |
| Предмет | `src/main/java/com/example/superheroes/item/<Name>Item.java` + регистрация в `ModItems.java` |
| Звук | `src/main/resources/assets/superheroes/sounds/...ogg` + `sounds.json` + `ModSounds.java` |
| Текстура | `src/main/resources/assets/superheroes/textures/...png` (раньше глянь `art-source/`) |
| Локализация | `src/main/resources/assets/superheroes/lang/{en_us,ru_ru}.json` (всегда обе) |
| Дизайн / план фичи | `docs/design/<topic>.md` |
| Сырой ассет от автора | `art-source/` (см. skill `art-source`) |

---

## Идентичность проекта

| Параметр | Значение |
|---|---|
| Mod ID | `superheroes` |
| Display Name | `Superheroes Mod` |
| Java package | `com.example.superheroes` |
| Main entrypoint | `com.example.superheroes.SuperheroesMod` |
| Client entrypoint | `com.example.superheroes.client.SuperheroesClient` |
| Datagen entrypoint | `com.example.superheroes.datagen.SuperheroesDataGenerator` |
| Minecraft | `1.21` |
| Fabric Loader | `>=0.19.2` |
| Fabric API | `0.102.0+1.21` |
| Java | 21 (на VM `/home/ubuntu/jdk-21.0.2`) |
| Mappings | Mojang (Yarn НЕ используем) |
| Loom | `1.16-SNAPSHOT` |
| License | CC0-1.0 |

`fabric.mod.json` — `src/main/resources/fabric.mod.json`. Mixin configs: `superheroes.mixins.json` (server+common), `superheroes.client.mixins.json`.

---

## Архитектура — общий поток

```
Item (HomelanderSuit/IronManSuit/RegulusSuit)
  ↓ ПКМ
HeroTransformService.transform(player, heroId)
  ↓
HeroData attachment (currentHero, energy, mana, bindings, activeAbilities)
  ↓ применяет
Hero.applyPassives() → атрибуты + permanent эффекты + размеры тела
  ↓ при активации (Z/X/C/V или radial)
ActivateAbilityC2SPayload → AbilityRouter.activate() → Ability.onActivate()
  ↓
ResourceController.tryConsume() (Energy/Mana с авто-fallback)
  ↓
Tick-логика в effect/*Controller.java (server-side)
  ↓
S2C payload → Client (ClientHeroState / ClientMadnessState / ClientReactorState)
  ↓
HUD render (HudRenderCallback) + Render layer (HeroSkinLayer / BeamRenderer)
```

Ключевые точки:
- **Attachment** (Fabric `AttachmentRegistry`) — персистентность через смерть/респавн (`copyOnDeath()`).
- **Mixin** правит ровно vanilla-поведение, остальное — события Fabric API.
- **Server-authoritative**: всё про урон/ресурсы/состояние решает сервер. Клиент рисует.
- **Networking**: типизированные `CustomPayload`-ы с `StreamCodec` (1.21 way), регистрация в `ModNetworking.init()`.

---

## Hero System

`Hero` — interface (`src/main/java/.../hero/Hero.java`). Реализации в `hero/`:

### `HomelanderHero` (id `superheroes:homelander`)

| Параметр | Значение |
|---|---|
| Energy max | 100 |
| Energy regen | 0.5/tick (10/s) |
| Mana max | 100 |
| HP boost | +20 (`HOMELANDER_HP`) |
| Armor | +50 (`HOMELANDER_ARMOR`) |
| Toughness | +6 |
| Attack damage | +6 |
| Speed | +20% |
| Knockback resistance | 1.0 (immune) |
| Permanent effects | Regeneration I, Fire Resistance, **Resistance I** |
| Cancels fall damage | yes |
| Abilities | `flight`, `eye_lasers`, `x_ray` |
| Default binding | x_ray → MANA, остальные → ENERGY |

### `IronManHero` (id `superheroes:iron_man`)

| Параметр | Значение |
|---|---|
| Energy max | (см. файл — реактор-driven) |
| Mana max | (см. файл) |
| Armor | +25 |
| Toughness | +4 |
| Attack damage | +4 |
| Speed | +10% |
| Knockback resistance | 0.6 |
| Abilities | `iron_man_flight`, `supersonic`, `repulsor`, `box_esp`, `unibeam` |
| Особенности | ECС-реактор-предмет в инвентаре (`IronManReactorTracker`), auto-eject на низком HP (`IronManAutoEjectController`), Unibeam — заскриптованный 2-фазный ультимат (`UnibeamController`) |

### `RegulusHero` (id `superheroes:regulus`)

| Параметр | Значение |
|---|---|
| Energy max | 1000 |
| Energy regen | 2.0/tick (40/s) |
| Mana max | (см. файл) |
| Базовый armor | (см. `REGULUS_ARMOR`) |
| Madness armor | +(см. `REGULUS_MADNESS_ARMOR` — даётся ТОЛЬКО в безумии) |
| Madness HP | + (даётся ТОЛЬКО в безумии) |
| Madness damage | + (даётся ТОЛЬКО в безумии) |
| Abilities | `lion_heart`, `mania_of_greed`, `lion_roar`, `counter_strike` |

Ключевая механика — **Madness** (см. `RegulusMadnessController`, 481 строка). Триггерится Lion Heart + Evangelion-чтение, даёт временные бафы, контратаку (`counter_strike`), магнит, специфический VFX (зачерчивание экрана, Zalgo-глифы, кровавый дождь).

`HeroAttributes.java` — единый файл для всех `AttributeModifierSet`-ов, см. там константы `*_ARMOR`, `*_DAMAGE`, `*_HP` и т.д.

`Heroes.java` — registry. Регистрация в статике + `init()` зов из `SuperheroesMod.onInitialize()`.

---

## Ability System

`Ability` — interface (`ability/Ability.java`). Все способности в `ability/`. Регистрация — `AbilityRegistry.init()` (вызывается из main-init).

| ID | Toggle? | costOnActivate | costPerTick (per-sec) | Тип / описание |
|---|---|---|---|---|
| `flight` | T | 0 | 0.5 (10/s) | Homelander полёт |
| `eye_lasers` | T | 2.5 | 0.75 (15/s) | Homelander лазеры из глаз |
| `x_ray` | T | 0 | 0.2 (4/s) | Homelander просвечивание (только местным игроком) |
| `iron_man_flight` | T | 0 | 0 | Iron Man полёт (тратит Reactor через `IronManReactorTracker`) |
| `supersonic` | T | 0 | 6.0 (120/s) | Iron Man супер-скорость |
| `repulsor` | A | 200 | 0 | Iron Man выстрел из ладони |
| `box_esp` | T | 0 | 1.2 (24/s) | Iron Man подсветка боксов мобов |
| `unibeam` | A | (gate) | 0 | Iron Man ульт — `UnibeamController` ведёт 2 фазы (charge → 2s beam) |
| `lion_heart` | T | 0 | 10 (200/s) | Regulus вход в безумие (drain) |
| `mania_of_greed` | T | 0 | 5 (100/s) | Regulus магнит (`RegulusGreedController` тащит entities в радиусе) |
| `lion_roar` | A | 150 | 0 | Regulus AoE-knockback |
| `counter_strike` | A | 200 | 0 | Regulus телепорт + слэм по последнему damager-у (см. `RegulusMadnessController`) |

Тип в HUD: `T` toggle, `A` active, `P` passive (пассивы у героя считаются по `AbilityDescriptions.HERO_PASSIVE_COUNT`).

Cooldowns: `AbilityCooldowns` (UUID → ability → tick deadline). Magnet 25s, counter 30s, post-counter energy lock 15s (`EnergyLocks`).

**Бинды**: игрок переключает `Energy/Mana` для каждой способности через radial menu (R) или `BindingsScreen` (B). Дефолт берётся из `Hero.getDefaultBinding(abilityId)`.

---

## Resource System

`resource/`:
- `ResourceKind` — enum `ENERGY` / `MANA`.
- `ResourceController` — `tryConsume(player, abilityId, amount)`. Сначала пробует bound resource, если не хватает — fallback. Server-authoritative. Авто-tick регена.
- `EnergyLocks` — мапа `UUID → unlock_tick`, блокирует energy-cost способности после контратаки.

Mana пополняется только предметами:
- `MilkBottleItem` — Homelander
- `UraniumIsotopeItem` — Iron Man (если по дизайну)
- `EvangelionItem` — Regulus (триггерит safe-чтение → madness)

---

## Effect Controllers (server-side, tick-based)

В `effect/` — каждый ставится на server-tick через `init()`-зов в `SuperheroesMod.onInitialize()`:

| Controller | Назначение |
|---|---|
| `MadnessFlightController` | Принудительная отмена полёта при мадности (Регулус не может летать в madness) |
| `MadnessAftermathController` + `MadnessAftermathMobEffect` | После выхода из безумия — debuff |
| `UnibeamController` | 2-фазный ульт Iron Man, busy-flag (другие падения не считаются landing-ом) |
| `HeroLandingTracker` | Шоквейв при приземлении (сила = velocity), исключает Unibeam-busy |
| `HeroEquipmentLock` | Запрет armor-slot и elytra при `hasHero` |
| `IronManReactorTracker` | Регенерация энергии Iron Man пока реактор в инвентаре |
| `IronManAutoEjectController` | Авто-снять костюм Iron Man при критическом HP |
| `RegulusTotemController` | Resurrect-механика для Регулуса (если есть Evangelion) |
| `RegulusGreedController` | Магнит — притягивает entities в AABB |
| `RegulusMadnessController` | **Главный мозг Регулуса**: безумие, last-damager tracking (200-tick TTL), counter-strike (телепорт + 27 dmg + flight-strip), evangelion-чтение |
| `SuperJumpController` | Hulk-style прыжок (Регулус) на G-key |
| `AutoSaturationController` | Авто-кормит героя пока трансформирован (нет голода как механики) |

**Важно**: при добавлении нового controller-а — обязательно добавить `<Name>Controller.init()` в `SuperheroesMod.onInitialize()`, иначе он мёртвый.

`MobEffect`-ы регистрируются в `ModEffects.java` (Madness, MadnessAftermath, SuperheroWeakness).

---

## Mixins

### Server / common (`src/main/java/.../mixin/`)
- `LightningBoltAccessor` — доступ к private fields молнии
- `PlayerDimensionsMixin` — heros меняют размеры (`Hero.getDimensions(pose)`)
- `PlayerFlightPoseMixin` — поза игрока при полёте
- `LivingEntityFallDamageMixin` — отмена fall damage если `Hero.cancelsFallDamage()`
- `LivingEntityFallFlyingMixin` — управление elytra-flying state

### Client (`src/client/java/.../mixin/`)
- `PlayerRendererMixin` — patch render (масштаб, эффекты)
- `LocalPlayerFlightMixin` — локальный полёт (без задержки сервера)
- `CameraMixin` — позиция камеры при полёте/ульте
- `AbstractClientPlayerSkinMixin` — **форсит Classic (Steve / WIDE) модель** для всех игроков с активным героем (фикс v1.0.25 от skin-layer-mismatch)

`{config}.json` файлы — `superheroes.mixins.json` и `superheroes.client.mixins.json`. **Каждый новый mixin = добавить в JSON**, иначе не подхватится.

---

## Client architecture

### State (`src/client/java/com/example/superheroes/client/`)
- `ClientHeroState` — кешированный `HeroData` локального игрока (синхронизируется через `HeroDataSyncS2CPayload`)
- `ClientMadnessState` — состояние madness локального игрока
- `ClientReactorState` — состояние реактора Iron Man
- `RemoteHeroSkins` — `Map<UUID, HeroId>` для других игроков (нужно чтобы `AbstractClientPlayerSkinMixin` форсил их модель тоже)

### HUD (`client/hud/`)
| HUD | Что |
|---|---|
| `ResourceBarHud` | Энергия/мана внизу-слева (цвета из `HeroTheme`) |
| `RadialMenuHud` | Круговое меню R, real-time (мир не паузится) |
| `AbilitiesTooltipHud` | Тултипы способностей (toggle H) |
| `JarvisOverlayHud` | Iron Man HUD (рамка, индикаторы) |
| `MadnessHudOverlay` | Регулус: vignette, флэш, ~65 floating-symbols (Greek/kanji/runes/Zalgo/cuneiform) |
| `BloodRainHud` | Регулус: 4 формы капель (STREAM/BLOB/ZIGZAG/SMEAR), wobble, splat-marks |
| `LowResourceVignetteHud` | Винетка при low energy/mana |
| `ScreenFlashHud` | Один-кадровый флэш (urgency/событие) |
| `ReactorOverlayHud` | Iron Man реактор-индикатор |
| `SunWindupHud` | Charge-индикатор (Unibeam, EyeLasers windup) |

### Render (`client/render/`)
- `HeroSkinLayer` — `LivingEntityFeatureRenderer`, накладывает hero skin поверх ваниль-скина (forced Classic geometry)
- `LaserBeamRenderer`, `RepulsorBeamRenderer`, `BeamRenderer` — лазерные/балка-VFX
- `IronManEspRenderer` — рамки боксов мобов
- `LocalLaserOverlay` — локальный лазер из глаз (first-person)
- `lightning/SuperheroLightningRenderer` — кастомная отрисовка молний

### FX
- `fx/ScreenShakeManager` — управляет shake-стейтом (амплитуда + частота)

### Screens
- `screen/BindingsScreen` — UI для смены биндов Energy/Mana по способностям

### Input
- `ModKeys`: R (radial), B (bindings), H (toggle tooltips), G (super jump), Z/X/C/V (ability slots 1-4)

---

## Networking (1.21 typed payloads)

Регистрация в `ModNetworking.init()` (server) и `ClientNetworking.init()` (client). Ниже список:

### C2S (client → server)
- `ActivateAbilityC2SPayload` — игрок жмёт кнопку способности
- `DeactivateAbilityC2SPayload` — отпустил toggle
- `BindAbilityResourceC2SPayload` — поменял Energy↔Mana бинд
- `SuperJumpC2SPayload` — G-key super-jump

### S2C (server → client)
- `HeroDataSyncS2CPayload` — полная синхронизация `HeroData` (после трансформации, респавна)
- `ResourceUpdateS2CPayload` — текущие energy/mana значения (per-tick)
- `RemoteHeroSkinS2CPayload` — кому-то рядом сменился hero (для `RemoteHeroSkins` и skin-mixin-а)
- `MadnessSyncS2CPayload` / `MadnessVisualS2CPayload` — состояние безумия + триггер визуала
- `LaserFiredS2CPayload` — пуск лазера (Homelander/Iron Man)
- `RepulsorBlastS2CPayload` — пуск репульсора
- `ScreenShakeS2CPayload` — триггер shake
- `ReactorStateS2CPayload` — состояние реактора Iron Man

`StreamCodecs` — общие codec-ы для типов (UUID, Vec3, Optional...).

---

## Items

`item/`:
- **Suit-items**: `HomelanderSuitItem`, `IronManSuitItem`, `RegulusSuitItem` — `extends TransformationItem`. ПКМ → `HeroTransformService.transform()`. Каждый — EPIC, stack 1, fire resistant.
- **Resource-items**: `MilkBottleItem`, `UraniumIsotopeItem`, `EvangelionItem` — пополняют ману / триггерят madness.
- **Other**: `IronManReactorItem` (нужен в инвентаре для `iron_man_flight`), `UraniumDaggerItem` (anti-Homelander оружие), `CompoundVItem` (?).
- **Frames / UI**: `TooltipFrame`, `ModItemGroups`.

Регистрация — `ModItems.register(name, item)`. Item Group — `ModItemGroups.init()`.

---

## Damage / Particles / Sounds / Entities

| Файл | Что |
|---|---|
| `damage/ModDamageTypes.java` | Кастомные `DamageType`-ResourceKey (regulus_counter, unibeam, eye_lasers...) |
| `entity/ModEntities.java` | Кастомные entity (если есть, например снаряды) |
| `particle/ModParticles.java` | `TRANSFORM_SPARK`, `LASER_SPARK`, `REPULSOR_SPARK`, `UNIBEAM_SPARK` |
| `sound/ModSounds.java` | Регистрация SoundEvent-ов. Ассеты в `assets/superheroes/sounds/`. |
| `physics/ShockwaveUtil.java` | Helper для шоквейвов (radial knockback, dmg falloff) |

Vanilla-замены:
- `assets/minecraft/sounds.json` — переопределение vanilla lightning thunder (50/50 weight между двумя custom-OGG)

---

## Datagen (`src/main/java/.../datagen/`)

`SuperheroesDataGenerator` — entrypoint `fabric-datagen`.

Providers:
- `ModDamageTypeProvider` — JSON для damage types
- `ModDamageTypeTagProvider` — теги (вкл/выкл fall damage, fire scaling)
- `ModItemModelProvider` — модели предметов
- `ModRecipeProvider` — рецепты крафта

Запуск: `./gradlew runDatagen --no-daemon` → `src/main/generated/`.

См. skill `datagen` для подробностей.

---

## Команды (`/superheroes ...`)

`SuperheroesCommands.java`:
- `/superheroes hero <id>` — трансформировать в героя
- `/superheroes untransform` — снять трансформацию
- `/superheroes energy <amount>` — выдать energy
- `/superheroes mana <amount>` — выдать ману
- `/superheroes abilities info` — список способностей героя

---

## Build / Release

### Build
```bash
./gradlew build --no-daemon -x test
# jar: build/libs/superheroes-${mod_version}.jar
```

`mod_version` в `gradle.properties`. **Бампать локально, не коммитить.** После релиза — `git checkout -- gradle.properties`.

### Запуск Minecraft с модом
```bash
./gradlew runClient --no-daemon
./gradlew runServer --no-daemon
./gradlew runDatagen --no-daemon
```

### Release flow (см. skill `release-mod`)
1. Бамп `mod_version` локально
2. `./gradlew build --no-daemon -x test`
3. `gh release create vX.X.X build/libs/superheroes-X.X.X.jar --title "vX.X.X: <kind>" --notes "..."`
4. `git checkout -- gradle.properties`
5. (НЕ коммитить version-bump)

### CI
**Не ждать `git pr_checks`** в этом репо. Доверяем локальному build. Knowledge: `skip-ci-for-superheroes`.

---

## Design docs (in flight)

- `docs/design/hero-sung-jinwoo.md` — план 4-го героя (Sung Jinwoo, Solo Leveling). Саммонер с Shadow Charges. **Не реализован**, ждёт явного go-ahead.
- `docs/design/balance-homelander-buffs.md` — план буфов Хоумлендера в 4 фазы (laser dmg → Heat Sweep → Supersonic Shout → ...). **Не реализован**.

---

## Skills index (более узкие гайды)

`.agents/skills/<name>/SKILL.md`:

| Skill | Когда использовать |
|---|---|
| `project-profile` | Базовые ID/версии (короткая версия этого AGENTS.md) |
| `base-rules` | **ВСЕГДА в начале** — что НЕЛЬЗЯ делать |
| `build-mod` | Перед PR — собрать и проверить локально |
| `add-item` | Добавить новый предмет |
| `add-block` | Добавить блок (если когда-то понадобится) |
| `datagen` | Регенерация blockstates/моделей/тегов |
| `debug-crash` | Юзер прислал стектрейс / краш |
| `loader-gotchas` | Fabric/NeoForge/MC 1.21+ ловушки (mappings, registration order) |
| `minecraft-mod-dev` | Общая разработка модов (mod APIs, JEI, AE2) |
| `release-mod` | Релизить версию на GitHub |
| `publish-mod` | Публикация на CurseForge / Modrinth |
| `art-source` | Откуда брать ассеты, что лежит в `art-source/` |
| `research-tools` | Какие MCP-инструменты использовать (mcdev / context7 / github / fetch) |

`.windsurf/rules/` — те же правила в windsurf-формате (`always_on`-rules), часть дублируется.

---

## DO NOT (forbidden actions)

Жёсткие правила (см. `base-rules` SKILL):
1. **Не создавать `.md`-файлы (README, CHANGELOG, docs)** без явного запроса. Этот AGENTS.md — исключение, был запрошен.
2. **Не комментировать код** если не просили.
3. **Не делать косметических правок** (форматирование, импорты, переименования) — только по делу.
4. **Не использовать deprecated API** Minecraft / Fabric / Loom.
5. **Не коммитить `gradle.properties` с бампом версии** — откатывать после релиза.
6. **Не править файлы из чёрного списка** без явного «можешь, разрешаю»:
   - `Hero.java` (interface — ломает все impls)
   - `HeroData.java` (требует attachment-миграции)
   - `ResourceController.java` (центральная балансировка)
   - `*Hud.java` (HUD-рендер — есть собственный стиль)
   - `HeroTheme.java`
   - `LivingEntityFallDamageMixin.java`
   - `HeroLandingTracker.java` (исключение: можно добавить `if (UnibeamController.isBusy(player)) skip`)
7. **Не заменять vanilla lightning thunder напрямую** — у нас 50/50 mix через `assets/minecraft/sounds.json`.
8. **Лазеры не разрушают блоки** (Унибим — единственное исключение, специально).
9. **Не использовать ванильный glowing для просвечивания** — у Iron Man своя BoxESP.
10. **Не паузить мир в radial menu** — он специально real-time.
11. **Меню сетевых ивентов**: всё через `ModNetworking` payload-ы, **не использовать `PacketByteBuf`-стиль из старых версий**.

---

## Common Tasks (рецепты)

### Новая способность

1. ID в `AbilityIds.java`: `public static final ResourceLocation MY_ABILITY = ModId.of("my_ability");`
2. Файл `ability/MyAbility.java` implements `Ability`. Реализовать: `getId()`, `isToggle()`, `costOnActivate()`, `costPerTick()`, `onActivate(player)`, `onDeactivate(player)` (если toggle), `onTick(player)` (если нужно).
3. Регистрация: `AbilityRegistry.register(new MyAbility())` в `AbilityRegistry.init()`.
4. Добавить в `Hero.getAbilities()` нужного героя.
5. (Опц.) Tick-логика → новый `effect/MyController.java` + `init()` в `SuperheroesMod.onInitialize()`.
6. (Опц.) Кастомный VFX → S2C payload + client renderer / HUD.
7. Локализация: `lang/en_us.json` и `ru_ru.json`:
   - `ability.superheroes.my_ability` (название)
   - `ability.superheroes.my_ability.desc` (описание)
8. (Если нужно) описание в `AbilityDescriptions` если поведение нестандартное.

### Новый герой

1. ID + skin path в `<Name>Hero.java` (extends `Hero`).
2. `HeroAttributes.<NAME> = AttributeModifierSet.builder()...build()` + `ResourceLocation`-ы для каждого модификатора.
3. Skin texture в `assets/superheroes/textures/entity/hero/<name>.png` (Classic/Steve geometry, 64×64).
4. Suit-item в `item/<Name>SuitItem.java` extends `TransformationItem`.
5. Регистрация в `Heroes.init()`, `HeroAttributes`, `ModItems`.
6. (Опц.) Тема для HUD — `HeroTheme` запись.
7. Локализация название/описание.
8. Дизайн-док в `docs/design/hero-<name>.md` ДО кода.

### Новый предмет

См. skill `add-item`.

### Новый звук

1. OGG Vorbis (`ffmpeg -c:a libvorbis -qscale:a 5 in.* out.ogg`) в `assets/superheroes/sounds/<group>/<file>.ogg`. **Сырой файл — в `art-source/`**.
2. Запись в `assets/superheroes/sounds.json`:
   ```json
   "group.event": {
     "sounds": [{"name": "superheroes:group/file"}]
   }
   ```
3. Регистрация в `ModSounds.java`: `register("group.event")`.
4. Использовать: `ModSounds.GROUP_EVENT` в коде.

---

## Workflow для новой задачи

```
1. Прочитать AGENTS.md (этот файл) — карта.
2. Прочитать .agents/skills/base-rules/SKILL.md — что НЕЛЬЗЯ.
3. Найти ближайший SKILL под задачу (`.agents/skills/<name>/SKILL.md`).
4. Если задача нетривиальная — todo list.
5. Branch: devin/$(date +%s)-<short-name>.
6. Кодить.
7. ./gradlew build --no-daemon -x test (локально).
8. Commit + PR (template: feat/fix(scope): ..., description на русском).
9. CI **не ждём** (см. skip-ci знание).
10. Закрыть todo, сообщить пользователю PR-ссылку.
```

---

## Если что-то не сходится

- Этот файл может устаревать — если видишь расхождение с реальным кодом, **верь коду**, а потом обнови AGENTS.md одним PR.
- При сомнениях — спрашивай пользователя, не выдумывай поведение.
- Большие архитектурные решения (новые системы, переписывание core) — сначала дизайн-док в `docs/design/`, потом обсуждение, потом код.
