---
name: project-profile
description: Use when working in this repo — provides core mod identifiers, versions, and architectural decisions for the Superheroes Fabric mod.
triggers: ["model"]
---

# Project Profile — Superheroes Mod

## Идентификаторы
- Mod ID: `superheroes`
- Display Name: `Superheroes Mod`
- Java package: `com.example.superheroes`
- Main class: `com.example.superheroes.SuperheroesMod`

## Версии
- Minecraft: `1.21`
- Loader: Fabric (`fabric-loader 0.19.2`, `fabric-api 0.102.0+1.21`)
- Java: 21 (на VM лежит в `/home/ubuntu/jdk-21.0.2`)
- Mappings: Mojang (Yarn не используется)
- Loom: `1.16-SNAPSHOT`

## Цель мода
Высококачественный VFX-мод про супергероев. Система трансформации через item меняет модель/хитбокс/атрибуты игрока. Кастомный HUD, физика, кастомный круговой выбор способностей.

Запланированные герои: Homelander → Iron Man → Batman → Captain America → Doomsday. Реализованы Homelander и Iron Man.

## Главные системы
- **Hero / HeroData / HeroTransformService** — лайфцикл трансформации (HeroData.copyOnDeath)
- **Ability + AbilityRegistry + AbilityIds** — модульные способности (toggle / one-shot)
- **ResourceController** — двойной ресурс (Energy / Mana) с авто-fallback
- **RadialMenuHud** — real-time круговое меню (без паузы)
- **ResourceBarHud + HeroTheme** — HUD-цвета меняются под героя
- **BeamRenderer / ShockwaveUtil** — кастомный VFX
- **UnibeamController** — Phase 2 Iron Man: charge → 2s beam → stun
- **HeroLandingTracker** — приземление с шоквейвом
- **HeroEquipmentLock** — броня/элитра запрещены пока `hasHero`

## Чего быть НЕ должно
- Лазеры **не** разрушают блоки (Унибим — исключение, специально)
- Полёт не наносит урон от столкновений с блоками
- Меню способностей не паузит игру
- Ванильный glowing для просвечивания не используется — у Iron Man своя BoxESP
- Дефолтные звуки молнии (lightning_bolt_thunder) НЕ заменяются — кастомные миксуются с ванильными

## Запрещённые без явного разрешения файлы
Если пользователь не сказал явно — НЕ модифицировать:
- `Hero.java` (интерфейс, ломает все реализации)
- `HeroData.java` (изменения требуют миграции attachment)
- `ResourceController.java` (центральная балансировка ресурсов)
- `*Hud.java` (HUD-рендер)
- `HeroTheme.java`
- `LivingEntityFallDamageMixin.java`
- `HeroLandingTracker.java` (исключение: добавить `if (UnibeamController.isBusy(player)) skip` — разрешено)

Эти файлы в чёрном списке менялись только с явного «можешь залазить, разрешаю».

## Известные паттерны при добавлении
- Новая способность: append в `AbilityIds`/`AbilityRegistry`, `IronManHero.getAbilities()` или `HomelanderHero.getAbilities()`, новый файл в `ability/<Name>Ability.java`, контроллер в `effect/<Name>Controller.java`
- Новый звук: `assets/superheroes/sounds/<group>/<file>.ogg` (Vorbis q5) + `sounds.json` + `ModSounds.register("group.name")`
- Новая локализация: `lang/en_us.json` + `lang/ru_ru.json` (всегда обе)
