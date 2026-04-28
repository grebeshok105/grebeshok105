---
name: art-source
description: Use when adding textures, sounds, particles, or any visual/audio assets to the mod — check art-source/ first for existing material before creating new.
---

# Art Source Library

Проект содержит папку `art-source/` на корне репа — это **склад сырых ассетов**, куда пользователь кидает всё, что может пригодиться (текстурки, звуки, модели, FX-спрайты, референсы с других модов).

## Когда использовать

Перед тем как рисовать новую текстуру или искать звук в интернете — **всегда сначала смотреть в `art-source/`**. Вероятно нужное уже там.

## Что там сейчас

- **`rezero-fx-textures.zip`** — 52 MB архив:
  - `FX + TEXTURES REZERO/minecraft/textures/...` — оверлеи брони, блоки
  - `FX + TEXTURES REZERO/powerborne/...` — модели Mjolnir, Thor helmet, shield straps, animations
  - `FX + TEXTURES REZERO/soulsweapons/textures/particle/...` — partico-спрайты (purple_flame, soul_spark, sun_particle, и т.д.)
  - `FX + TEXTURES REZERO/ldlib/textures/...` — мелкие UI-текстуры

## Как использовать

```bash
# Посмотреть содержимое не распаковывая:
unzip -l art-source/rezero-fx-textures.zip | grep -i <ключевое-слово>

# Извлечь конкретный файл:
unzip -p art-source/rezero-fx-textures.zip "FX + TEXTURES REZERO/.../some.png" > /tmp/some.png

# Если нужен полный просмотр — распаковать во временную папку:
unzip -q art-source/rezero-fx-textures.zip -d /tmp/art-source/
```

После выбора ассета — **скопировать и переименовать** в `src/main/resources/assets/superheroes/textures/...` с правильным путём под нужный namespace. Не ссылаться на файл прямо из `art-source/` — рантайм-ресурсы должны быть в `src/main/resources/`.

## Форматы

- **Текстуры**: PNG, power-of-two (16, 32, 64, 128, 256). Альфа-канал для прозрачности.
- **Звуки**: OGG Vorbis. Если пришёл MP3 — конвертировать:
  ```bash
  ffmpeg -i input.mp3 -c:a libvorbis -qscale:a 5 output.ogg
  ```
- **Модели**: JSON-based (Blockbench export) или `.bbmodel` (для редактирования, конвертируем в JSON перед use).

## Когда пользователь кидает новый ассет

1. Скачать / принять файл.
2. Положить в `art-source/` (или в подпапку `art-source/inbox/` если много).
3. Если нужно в runtime — скопировать в `src/main/resources/assets/superheroes/...`
4. Закоммитить и то, и другое (art-source — как source-of-truth, runtime — как реально используемое).

## Правила

- `art-source/` **коммитится в git**. Не игнорить.
- Суммарный размер под контролем — если > 500 МБ, переходить на git-lfs или внешнее хранилище.
- Если ассет пришёл из чужого мода/пака — рядом класть `source.txt` или `LICENSE.md` со ссылкой.
