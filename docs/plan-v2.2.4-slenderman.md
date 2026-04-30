# План v2.2.4-pre — Slenderman (A-tier hero)

> Статус: **черновик плана**, ещё не подтверждён пользователем по боёвке.
> Источник эскиза: PR #41 (`devin/1777553078-slenderman-rip`) — содержит art-source с моделью/звуками/анимациями из чужого Forge-мода как reference.

## 1. Концепция

**Slenderman** — психологический stalker A-тира, доминирует через **полевой контроль страхом**, а не через DPS. Своеобразная анти-теза Хоумлендеру: тот «вижу всех сквозь стены и убиваю с дистанции», Slender — «никто не видит меня уверенно, и я наказываю за то что смотрят».

Тематически: SCP/creepypasta horror + game-feel из Slender: The Eight Pages.

## 2. Подтверждённые пользователем правки

1. **Модель**: enderman-like 3D (тощий, длинные руки, тёмный костюм), но **перекрашенный** — пользователь пришлёт собственный скин/перекрас. База — `slenderman.geo.json` из ripped мода (см. art-source).
2. **Dodge-механика**: как у эндермена с дождём (телепорт от воды) — но **от ВСЕХ ударов**. С шансом дотягиваемым до 100% для дальнобойных и понижением для ближнего боя в зависимости от условий.
3. **VFX база** — из `art-source/slenderman-mod-rip/` (TV-static overlay, jumpscare, звуки), плюс наши кастомные partikly из `rezero-fx-textures.zip` (DARK_STAR, BLACK_FLAME для тематики).

## 3. Стат-лист (sub-stats)

| Параметр | Значение | Vs ванильный игрок |
|---|---|---|
| HP | +20 (макс 40) | +20 |
| Armor | +5 | +5 |
| Speed | +25% | +25% |
| Step Height | 1.0 (вместо 0.6) | +0.4 |
| Knockback resist | 0.5 | +0.5 |
| Reach (melee) | +1.5 блока (длинные руки) | +1.5 |
| Damage из пустоты | 0 (телепорт-выход) | immune |
| Урон по нему от лука/сnapshow/projectile | base × 0.0 (полный dodge до cap) | -100% capped |
| Базовый melee damage | 6 (от tendrils) | +5 |

Drawback (см. п.7): глобальный sanity-penalty при прямом наблюдении.

## 4. Абилки

### 4.1 Passive: **Static Aura** (pre-built core mechanic)
Любой враг (Player + Mob по тегу `monster`) в **LoS-конусе** R=20, угол 90°, у которого Slenderman попал в `viewport` (raytrace без блок-перекрытия) — получает stack `slenderman_static`:
- 0–4 stacks: предупреждающий шумок (`slenderman_warning.ogg`).
- 5–9 stacks: visual TV-static оверлей — `static_far.png`, accompanied by `slender_static.ogg` quiet loop.
- 10–14 stacks: `static_mid.png` + Nausea I.
- 15–19 stacks: `static_close.png` + Nausea II + Slowness I.
- 20+ stacks: tentacle-strike **8 dmg** (`HOMELANDER_*` style custom DamageType `slenderman_static`) + сброс stack-ов.

Stack-и **накапливаются** пока Slender в LoS, **затухают** -1/s когда не в LoS. Реализация: `SlendermanStaticController` (server-side per-player tick).

### 4.2 Z (slot 1): **Blink** — `BlinkAbility`
Мгновенный телепорт на 8 блоков по направлению взгляда, проходящий сквозь любой блок (как enderman teleport), **сбрасывает агонию мобов на 2с** (mobs `setLastHurtByMob(null)`).

- Cooldown: 4s.
- Energy: 30 ED (cheap).
- VFX: spawn `ENDER_TP` + `DARK_STAR` particles на старте/прибытии.
- SFX: `slenderman_active.ogg` (один из вариаций).
- Если телепорт-точка в lava/void → fallback в безопасный блок в 2 блока radius.

### 4.3 X (slot 2): **Tendrils** — `TendrilsAbility`
4 невидимых руки выстреливают вперёд из спины игрока, R=4, конус 120°. Каждая рука бьёт `tendril_strike` 5 dmg + knockback-вверх 0.5.

- Cooldown: 8s.
- Energy: 50 ED.
- Custom damage type `slenderman_tendril` (death message «<player> was claimed by Slenderman’s grasp»).
- VFX: 4 trail-линии тёмных partikley (`BLACK_FLAME`).
- SFX: `slenderman_attack.ogg`.

### 4.4 C (slot 3): **Phase Stalk** — `PhaseStalkAbility` (toggle)
Активный режим (длится пока не выключишь / не закончится энергия / не получишь crit):
- **Polный invisibility** (как Potion + spectral) пока стоишь.
- При движении — следы `DARK_STAR` partikley (только сервер видит — клиент видит чуть) подсвечивают позицию, делают перемещение читаемым для опытного.
- Ускоряет нарост Static-stacks у врагов в LoS **×4**.
- Drains 5 ED/s. На 0 ED — авто-выключение + actionbar warning.
- Сбрасывается на любой получаемый damage > 6.
- SFX (loop): `slenderman_living.ogg`.

### 4.5 V (ULT, slot 4): **The Static Field** — `StaticFieldAbility`
**Зона полевого контроля 12 секунд**, R=18 блоков от точки активации.

Что происходит для врагов в зоне:
- TV-static overlay (`static_close.png`) накладывается на их HUD (S2C packet).
- Nausea III + Blindness + Slowness II.
- Каждые 2с — random tendril-strike (5 dmg) откуда-то сбоку.
- Видимость стрелы/projectile-ов в зоне обнуляется (clientside particle hide).
- Звук `slender_static.ogg` loops.

Что происходит для Slenderman в зоне:
- **Полная invisibility** (даже с пузырём от eat / damage).
- Static-stacks накапливаются ×8 (вместо ×1).
- Регенерация I.
- ED-cost обычных абилок −50%.

Cooldown ULT: **180s** (3 минуты) — agressively long, чтобы не было spammable.
Energy: **350 ED** (нужен почти полный бар).

### 4.6 G (Super Jump): нет — заменён на стандартный jump

## 5. Dodge-механика (фишка из user-правки)

Аналог enderman-water-teleport, но триггер — **любой incoming damage**.

Реализация: mixin в `LivingEntity#hurt(DamageSource, float)` *(или Fabric `ServerLivingEntityEvents.ALLOW_DAMAGE`)*. Когда target — игрок-Slender:

1. Roll dodge chance:
   - **Projectile / arrow / trident**: 95% (почти всегда уклоняется).
   - **Magic / indirect (potion / wither star)**: 70%.
   - **Melee (другой игрок ударил мечом)**: 35%.
   - **Explosion / AOE**: 50%.
   - **In-fire / lava / drowning / fall**: 100% (лимит — нечего dodgeать).
   - **Self-damage / void**: 0% (нельзя dodgeать).
2. Если roll успешный:
   - Damage cancelled.
   - Slender телепортируется на **6–10 блоков** в случайное безопасное место в радиусе 12.
   - Spawns `ENDER_TP` particles в обоих позициях.
   - Plays `ENDERMAN_TELEPORT.ogg` тише + `slenderman_warning.ogg`.
   - **Cooldown 1s** между dodge-ами (нельзя через rapid-fire полностью обнулить урон от лука; после 1s можно снова).
3. Если cooldown активен — урон проходит как обычно.

Tunables в `SlendermanDodgeConfig` (статический класс) — для будущей балансировки.

**Анти-чит**: dodge ТОЛЬКО на тика (server authoritative), результат отправляется S2C с teleport packet.

## 6. Custom Damage Types

| ID | Death message (en) | Scaling | Effects |
|---|---|---|---|
| `slenderman_static` | "<victim> drowned in static" | NEVER | bypass armor 50% |
| `slenderman_tendril` | "<victim> was claimed by Slenderman’s grasp" | NEVER | bypass armor 100% |
| `slenderman_field` | "<victim> lost themselves in the static field" | NEVER | wither-style |

RELATED_GROUP: все три — одна группа для адаптации Doomsday.

## 7. Drawback / risk-mehanika

**Sanity Cost**:
- При прямом взгляде (LoS conic 30°, R<10) на любого ВРАГА — Slenderman теряет 0.5 ED/s.
- Если Slender смотрит прямо в глаза цели > 4 секунды подряд (mutual eye contact) — получает **«Exposed»** debuff: 4с весь dodge-chance × 0.3 (упал до 30% от base).
- ULT нельзя кастовать пока есть Exposed.

Это балансирует «вижу всех насквозь» Хоумлендера: Slender ОБЯЗАН играть сбоку, не лицом к лицу.

## 8. Сборка ассетов

### 8.1 Звуки → `assets/superheroes/sounds/slenderman/`

Из `art-source/slenderman-mod-rip/assets/slenderman/sounds/` импортируем:
- `slenderman_active.ogg` → blink активация
- `slenderman_attack.ogg` → tendrils
- `slenderman_warning.ogg` → static-aura warning
- `slenderman_hunt.ogg` → phase-stalk loop
- `slender_static.ogg` → ULT field loop
- `slenderman_jumpscare.ogg` → static-stacks 20+ tentacle-strike
- `slenderman_hurt.ogg` → take damage (после dodge fail)
- `slenderman_death.ogg` → детрансформация
- `bush_movement_1/2/3.ogg` → ambient stalker (рандом во время phase-stalk)

Регистрация в `ModSounds.java` с весами для рандома.

### 8.2 3D-модель

**ВАЖНО**: модель пользователь пришлёт сам. Рип содержит `slenderman.geo.json` + 4 анимации (idle/walk/sprint/attack) — оставляем как **reference**, не используем напрямую.

Когда пользователь пришлёт `.geo.json` + текстуру + (опционально) `.animation.json`:

- Подключить **GeckoLib 4.x для Fabric 1.21** (`software.bernie.geckolib:geckolib-fabric-1.21`).
- Создать `SlendermanGeoModel implements GeoModel<SlendermanRenderState>`.
- `SlendermanArmorRenderer` — overlay поверх player armor layer.
- Текстура enderman-like: пурпурные глаза светятся при phase-stalk (custom shader или emissive overlay).

Если пользователь пришлёт **java модель** (BlockBenchModel-style ModelPart) — port напрямую, без GeckoLib.

### 8.3 HUD overlays

- `static_close.png` / `static_mid.png` / `static_far.png` → `SlenderStaticHud` (для ВРАГОВ Slender'а).
- `slenderman_jumpscare.png` → 0.5s flash при достижении 20 stacks.
- VFX toggle: при LEGACY mode (см. v2.2.3-pre F8 settings) — overlays отключаются, оставляется только vanilla Nausea-shader.

### 8.4 Status effect icons

`mob_effect/slender_static_close.png` (etc.) → используем для иконок наших custom MobEffect-ов:
- `STATIC_BUILDUP` (level 1–4 = far, 5–9 = mid, 10+ = close).

## 9. Файлы и архитектура (preview)

### Новые
```
src/main/java/com/example/superheroes/
  hero/SlendermanHero.java                       — extends BaseHero, applyPassives/removePassives
  ability/BlinkAbility.java                       — Z slot
  ability/TendrilsAbility.java                    — X slot
  ability/PhaseStalkAbility.java                  — C slot toggle
  ability/StaticFieldAbility.java                 — V (ULT) slot
  effect/SlendermanStaticController.java          — server tick: LoS scan, stack accumulation
  effect/SlenderDodgeMixin.java (mixin)           — LivingEntity#hurt → dodge-roll
  effect/SlendermanFieldZone.java                 — active ULT zone tracking
  effect/StaticBuildupEffect.java                 — MobEffect (для иконки и стак-уровня)
  damage/ModDamageTypes.java                       — добавить SLENDERMAN_STATIC/TENDRIL/FIELD
  network/SlendermanFieldS2CPayload.java          — S2C: «ты вошёл в Static Field»
  network/SlendermanStaticS2CPayload.java         — S2C: «обнови overlay уровень X»

src/client/java/com/example/superheroes/client/
  hud/SlenderStaticHud.java                       — рендер static overlays
  hud/SlenderJumpscareHud.java                    — flash на 20 stacks
  render/SlendermanGeoModel.java                  — (после получения модели от user)
  render/SlendermanArmorRenderer.java             — GeckoLib renderer
```

### Модифицируемые
- `damage/ModDamageTypes.java` + datagen — +3 damage types.
- `sound/ModSounds.java` + `sounds.json` — +9 sound entries.
- `HeroRegistry.java` — register Slenderman.
- `lang/en_us.json` + `ru_ru.json` — abilities + effects + damage messages.
- `superheroes.mixins.json` — добавить SlenderDodgeMixin.
- `build.gradle` — добавить GeckoLib dependency (если идём по этому пути).

## 10. Доминирование над другими героями

| Vs | Преимущество Slender | Контр-стратегия противника |
|---|---|---|
| Хоумлендер (A) | Static Field обнуляет visibility lasers; dodge 95% от ranged | Heat Vision AOE может прорвать static; close-range melee попадает |
| Iron Man (B) | Dodge от unibeam (ranged), static-aura оглушает HUD | Box-ESP всё ещё работает в static-field частично |
| Goku (B) | Dodge от Камехамеха (ranged) | Голый melee SS-форма прорывается |
| Captain (B) | Slender медленнее но stealth + длинная рука | Vibranium shield reduces dodge-chance? (TBD) |
| Naruto (B) | Phase-stalk vs clones — одинаково невидимы; но Slender видит сквозь иллюзии | Rasengan close-range пробивает |
| Sung Jinwoo (B) | Static-Field мешает summon миньонов (visibility-зависимо) | Жуткие миньоны игнорят static |
| Doomsday (S) | Static Field фрустрирует tier-evolution; tendril-strike не адаптируется сразу | Doomsday адаптируется через 15 hp, но 4 tendril-strike = 20 dmg → быстро |
| Регулус (S) | Phase-stalk обходит magnet-pull; Slender может уйти из madness-counter | Madness-counter всё ещё ловит при contact; Регулус — главный анти-Slender |

**Вывод**: Slender жёстко наказывает **ranged-классы** и теряется vs Регулуса (его main counter). Это и есть A-tier баланс — есть конкретный hard-counter, но доминирует в широкой сетке.

## 11. Sequential implementation order

1. **GeckoLib integration** — добавить либу, тестовый куб-mob.
2. **HeroRegistry + SlendermanHero (skeleton)** — passive stats + transform/detransform.
3. **Custom damage types** — 3 типа + локализация + datagen.
4. **SlendermanStaticController** — LoS scanner + stack accumulator (server) + S2C.
5. **SlenderStaticHud** — клиентский overlay по stack-level.
6. **BlinkAbility + TendrilsAbility** — простые activation abilities.
7. **SlenderDodgeMixin** — главная фишка, тщательное тестирование.
8. **PhaseStalkAbility** — toggle с energy drain + invisibility.
9. **StaticFieldAbility (ULT)** — zone tracking + S2C field-state + Slender-bonus inside.
10. **Sounds + SFX wiring** — после работающей механики.
11. **3D-модель user** — порт в GeckoLib после получения файла от пользователя.
12. **Lang + integration testing**.

## 12. Test-plan checklist

- [ ] Static-stacks накапливаются → overlay меняется на close→mid→far по shadow distance.
- [ ] При 20+ stacks — tentacle-strike 8 dmg попадает.
- [ ] Blink телепортирует через стену; не работает если нет места приземления.
- [ ] Tendrils бьют 4 цели в конусе.
- [ ] Phase-stalk: invisible пока стоишь, particles при движении, выкл при damage > 6.
- [ ] ULT: 12с зона, враг в зоне получает Nausea III + Blindness, Slender внутри полностью невидим.
- [ ] Dodge: лук пускает 10 стрел → ~9-10 dodgeятся. Меч → ~3-4 dodgeятся из 10.
- [ ] Eye-contact > 4с → Exposed debuff, dodge упал до 30%.
- [ ] Multiplayer: каждый игрок-противник видит свой own static overlay (не mass-effect).
- [ ] Doomsday адаптируется к 1 типу tendril → к остальным двум damage types сразу.

## 13. Открытые вопросы (подтвердить с user)

- **Q1**: Подтвердить numerical values dodge chance (95/70/35/50) — устроит или баланс жёстче?
- **Q2**: Нужны ли «Pages» (как в Slender: TEP) — дополнительная mehanika, собирать 8 страниц для permanent buff? Или это уже overload и оставляем 4 ability + ULT + passive?
- **Q3**: Sanity-cost (0.5 ED/s при прямом LoS) — устроит или жёстче?
- **Q4**: GeckoLib OK как dependency? Это +3MB к jar size.
- **Q5**: Хочешь ли биом «Slender Forest» как в оригинальном моде (deep trees, fog, slender natural spawn)? Это огромный scope — отдельный план.
- **Q6**: Иконки абилок в HUD — сразу или отложить (как у tier 7 в v2.2.3)?

После ответов — финализирую план и создаю отдельную задачу-PR на v2.2.4-pre.
