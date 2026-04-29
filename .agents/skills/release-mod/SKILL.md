---
name: release-mod
description: Use when user asks to release the mod, create a GitHub release, tag a version, or publish a build. Triggered by "релиз", "release", "тег".
---

# Release Mod

Создание GitHub-релиза с прикреплённым jar.

## Шаги

1. **Перейти на baseline и подтянуть свежий**
```bash
cd /home/ubuntu/repos/mymodhero
git checkout baseline && git pull origin baseline
```

2. **Поднять версию локально (НЕ коммитить)**
```bash
# редактируем gradle.properties: mod_version=<X>
# например 1.0.6, 1.0.7, или 1.0.7-suffix
```

3. **Собрать** (см. skill `build-mod`)
```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew build --no-daemon -x test
ls build/libs/superheroes-<X>.jar  # проверить что собрался
```

4. **Создать release**
```bash
gh release create v<X> build/libs/superheroes-<X>.jar \
  --target baseline \
  --title "v<X> — <короткое описание>" \
  --notes "<markdown notes>"
```

В notes писать на русском, перечислить что вошло (можно ссылками на смерженные PR).

5. **Откатить версию локально**
```bash
git checkout -- gradle.properties
```

`mod_version` в репо всегда `1.0.0` — версии существуют только как релиз-теги.

## Текущие релизы
- `v1.0.5-unibeam` — Phase 2 Унибим (PR #8)
- `v1.0.6` — авто-сброс при смерти, броня 20/25, fire resistance (PR #10)

## Naming
- `v1.0.X` для основных фич
- `v1.0.X-<suffix>` для тематических (`-unibeam`, `-armor`, etc.) — но обычно лучше просто `v1.0.X`
