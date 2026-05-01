# Reinhard van Astrea — план нового героя

План для добавления героя **Reinhard van Astrea** из Re:Zero вместе с его мечом. Цель — сначала зафиксировать дизайн и список файлов, потом реализовывать отдельным PR без смешивания с v3.0 user-fixes.

## 0. Уточнения к пользователю

Нужно подтвердить перед реализацией:

1. **Предмет трансформации**: `Reinhard Insignia / Pendant` или трансформация прямо через меч?
2. **Меч**: отдельный item в инвентаре или авто-выдача/визуальный меч только пока игрок в форме Reinhard?
3. **Баланс**: лорно-OP Reinhard или баланс под текущих v3.0 героев?

Дефолтные допущения плана, если пользователь не уточнит: трансформация через `reinhard_pendant`, меч отдельным item `dragon_sword_reid`, баланс — высокий S-tier, но без бессмертия и без ваншотов по боссам.

## 1. Концепт героя

### ID / название

- Hero ID: `superheroes:reinhard`
- Display EN: `Reinhard van Astrea`
- Display RU: `Рейнхард ван Астрея`
- Основной ресурс: `Energy` как stamina/Divine Protection reserve.
- Mana: `0`.

### Фантазия геймплея

Reinhard — быстрый swordmaster с высоким burst-уроном, защитными Divine Protection proc-ами и ультимативным ударом мечом Reid. Он не летает, но двигается быстрее обычного игрока, игнорирует падение и хорошо переживает burst-атаки.

## 2. Ассеты из `art-source/rezero-fx-textures.zip`

В архиве уже есть подходящие Re:Zero ассеты:

- Skin/texture Reinhard:
  - `FX + TEXTURES REZERO/rezeromc/textures/entities/reinhard-van-astrea-re-zero-on-planetminecraft-com.png`
  - дубликат: `FX + TEXTURES REZERO/rezeromc/textures/reinhard-van-astrea-re-zero-on-planetminecraft-com.png`
- Sword / Reid assets:
  - `FX + TEXTURES REZERO/rezeromc/textures/item/dragonsword.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/item/dragonswordreidnew1.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/item/reidstick.png`
- Transformation item:
  - `FX + TEXTURES REZERO/rezeromc/textures/item/reinhardpendant.png`
- VFX particles:
  - `FX + TEXTURES REZERO/rezeromc/textures/particle/swordexplosion.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/particle/sword_explosion_1.png` … `sword_explosion_6.png`
- UI refs:
  - `FX + TEXTURES REZERO/rezeromc/textures/screens/rezeromcbuttonswordsmanship.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/screens/rezeromcbuttondivineprotection.png`

Runtime copy targets:

- `src/main/resources/assets/superheroes/textures/entity/hero/reinhard.png`
- `src/main/resources/assets/superheroes/textures/item/reinhard_pendant.png`
- `src/main/resources/assets/superheroes/textures/item/dragon_sword_reid.png`
- `src/main/resources/assets/superheroes/textures/particle/reinhard_sword_explosion*.png`

## 3. Hero stats/passives

### `ReinhardHero`

Файл: `src/main/java/com/example/superheroes/hero/ReinhardHero.java`

Поля:

- `ID = ModId.of("reinhard")`
- `SKIN = ModId.of("textures/entity/hero/reinhard.png")`
- `HeroTheme.REINHARD` или локальный `THEME` в классе, если не трогаем `HeroTheme`.

Базовые значения:

- Energy max: `300`
- Energy regen: `1.6/tick`
- Mana max: `0`
- Dimensions: vanilla player `0.6 x 1.8`

Атрибуты в `HeroAttributes`:

- Armor: `18`
- Armor toughness: `8`
- Attack damage: `8`
- Attack speed: `1.4`
- Movement speed: `+35% base`
- Max health: `+20`
- Knockback resistance: `0.6`
- Step height: `+0.5`

Пассивные эффекты при трансформации:

- `DAMAGE_RESISTANCE I`
- `MOVEMENT_SPEED I` только если атрибутов не хватает визуально; иначе не дублировать.
- Fall damage cancelled.

## 4. Меч Reid / Dragon Sword

### Предмет

Файл: `src/main/java/com/example/superheroes/item/DragonSwordReidItem.java`

Регистрация:

- `ModItems.DRAGON_SWORD_REID`
- model: `assets/superheroes/models/item/dragon_sword_reid.json`
- texture: `assets/superheroes/textures/item/dragon_sword_reid.png`
- lang:
  - EN: `Dragon Sword Reid`
  - RU: `Драконий меч Рейд`

Поведение:

- `stacksTo(1)`, `fireResistant()`, `rarity(EPIC)`, durability `2500`.
- Если держит Reinhard: повышенный melee damage / встроенный sweep через ability-контроллер.
- Если держит не-Reinhard: обычный сильный меч без hero abilities или с сильно урезанным уроном.

Важно: не ломать `HeroEquipmentLock`; если текущий lock запрещает предметы не-броню, проверить, что меч можно держать в форме героя.

## 5. Transformation item

Файл: `src/main/java/com/example/superheroes/item/ReinhardPendantItem.java`

- Extends `TransformationItem`.
- `super(ReinhardHero.ID, properties)`.
- Tooltip в стиле текущих hero items.
- Texture: `reinhard_pendant.png`.
- Добавить в `ModItemGroups`.

Опционально после подтверждения пользователя: при трансформации автоматически выдавать `DRAGON_SWORD_REID`, если у игрока его нет. Это лучше делать отдельным контроллером или в `HeroTransformService` только если пользователь подтвердит, потому что `HeroTransformService` — центральный файл.

## 6. Abilities

### 6.1 Sword Saint Dash

ID: `reinhard_sword_saint_dash`

Тип: one-shot dash slash.

Файл: `ability/ReinhardSwordSaintDashAbility.java`

Поведение:

- Cost: `55 energy`.
- Cooldown: `6s`.
- Игрок рывком движется вперёд на 8–10 блоков.
- Все LivingEntity в капсуле/линии получают `14–18` damage.
- Небольшой knockback по направлению рывка.
- Частицы `SWEEP_ATTACK`, `CRIT`, custom `reinhard_sword_explosion_*` если подключим particle provider.

### 6.2 Divine Protection

ID: `reinhard_divine_protection`

Тип: toggle или passive proc.

Рекомендация: passive controller, чтобы не занимать слот ability.

Файл: `effect/ReinhardDivineProtectionController.java`

Поведение:

- Когда Reinhard получает урон, раз в `20s` может сработать защита:
  - уменьшить incoming damage на 50–70% через Fabric damage event/mixin/controller pattern;
  - оттолкнуть атакующего;
  - дать короткий `ABSORPTION`/`REGENERATION`.
- Если проще без нового damage hook: tick-controller держит `DAMAGE_RESISTANCE`, а active ability даёт `ABSORPTION` на 8s.

### 6.3 Reid Draw / Dragon Sword Release

ID: `reinhard_reid_draw`

Тип: ultimate one-shot AoE cone.

Файл: `ability/ReinhardReidDrawAbility.java`

Поведение:

- Требует `DRAGON_SWORD_REID` в main/offhand.
- Cost: `160 energy`.
- Cooldown: `45s`.
- Перед ударом charge `20–30 ticks` с частицами вокруг меча.
- Конус перед игроком: range `12`, angle `60°`.
- Damage: `35–45`, cap по боссам/игрокам если нужно.
- Сильный knockback + flash/sound.
- Не разрушает блоки.

### 6.4 Astrea Counter

ID: `reinhard_astrea_counter`

Тип: короткий defensive parry.

Файл: `ability/ReinhardAstreaCounterAbility.java`

Поведение:

- Toggle/charge на `1.5s`.
- Если игрок получает melee damage в окне parry, damage cancel/reduce и ответный slash по атакующему.
- Cooldown: `12s`.

## 7. Damage types / datagen

Новые damage types:

- `reinhard_sword_dash`
- `reinhard_reid_draw`
- `reinhard_counter`

Файлы/правки:

- `damage/ModDamageTypes.java`
- `datagen/ModDamageTypeProvider.java`
- generated JSON в `src/main/generated/data/superheroes/damage_type/`
- lang death messages EN/RU.

## 8. Registration checklist

Код:

- `hero/ReinhardHero.java`
- `hero/Heroes.java` — добавить static field + `register(REINHARD)`.
- `hero/HeroAttributes.java` — добавить modifiers.
- `ability/AbilityIds.java` — добавить IDs.
- `ability/AbilityRegistry.java` — instantiate/register abilities.
- `effect/ReinhardDivineProtectionController.java` — init in `SuperheroesMod`.
- `item/ReinhardPendantItem.java`.
- `item/DragonSwordReidItem.java`.
- `item/ModItems.java`.
- `item/ModItemGroups.java`.

Assets/resources:

- `assets/superheroes/textures/entity/hero/reinhard.png`
- `assets/superheroes/textures/item/reinhard_pendant.png`
- `assets/superheroes/textures/item/dragon_sword_reid.png`
- `assets/superheroes/models/item/reinhard_pendant.json`
- `assets/superheroes/models/item/dragon_sword_reid.json`
- `assets/superheroes/lang/en_us.json`
- `assets/superheroes/lang/ru_ru.json`

Datagen:

- если item models генерируются через `ModItemModelProvider`, добавить туда pendant/sword вместо ручного JSON.
- выполнить `./gradlew runDatagen --no-daemon`.

## 9. Баланс v1

Рекомендуемый старт без OP-перекоса:

- Reinhard без меча: очень сильный melee герой, но ниже Doomsday по raw durability.
- Reinhard с Reid: высокий burst, cooldown-heavy.
- Нет постоянного creative-flight.
- Нет полного бессмертия.
- Divine Protection имеет cooldown и не спасает от void/kill commands.
- Ultimate не ломает блоки и не ваншотит боссов.

## 10. Риски

- Damage reduction/counter требует аккуратного hook-а в damage pipeline. Если нет готового Fabric event для нужной точки, лучше начинать с explicit active parry state и минимального mixin-а.
- Автовыдача меча при трансформации может конфликтовать с inventory/full inventory и death/drop правилами.
- Сторонние Re:Zero assets из `art-source` нужно сохранить с понятным source note, если ещё нет лицензии/описания.
- Если нужен 3D-меч GeckoLib/Blockbench, это отдельный scope; v1 можно делать vanilla item texture/model.

## 11. Минимальный первый PR реализации

Чтобы быстро получить playable героя:

1. Reinhard hero + attributes + skin.
2. Pendant transformation item.
3. Dragon Sword Reid item + texture.
4. 2 abilities: `Sword Saint Dash`, `Reid Draw`.
5. Lang EN/RU, models, item group.
6. Datagen + build.

`Divine Protection` и `Astrea Counter` можно делать вторым PR, если первый станет слишком большим.

## 12. Проверка

Команды:

```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew runDatagen --no-daemon
./gradlew build --no-daemon -x test
```

Manual smoke-test:

- Pendant transforms/untransforms Reinhard.
- Skin отображается.
- Sword appears in creative tab and has texture/model.
- Dash damages enemies in line and spends energy/cooldown.
- Reid Draw requires sword, charges, hits cone, spends energy/cooldown.
- Death/untransform removes passives cleanly.
