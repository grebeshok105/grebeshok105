---
name: build-mod
description: Use when user asks to build the mod, compile, check for errors, or produce a .jar. Run before opening a PR.
---

# Build Mod

## Пререквизит — JDK 21
Loom требует Java 21. На VM лежит готовая дистрибуция в `/home/ubuntu/jdk-21.0.2`.

```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH
java -version  # должно быть 21.x
```

Если `/home/ubuntu/jdk-21.0.2` не существует (после wipe `/tmp`):
```bash
mkdir -p /home/ubuntu/jdk-21.0.2
cd /tmp && wget -q https://download.oracle.com/java/21/archive/jdk-21.0.2_linux-x64_bin.tar.gz \
  && tar -xzf jdk-21.0.2_linux-x64_bin.tar.gz --strip-components=1 -C /home/ubuntu/jdk-21.0.2
```

## Команды

```bash
cd /home/ubuntu/repos/mymodhero
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH

./gradlew compileJava --no-daemon          # быстрая проверка ошибок компиляции
./gradlew build --no-daemon -x test        # полная сборка без тестов (CI обычно так)
./gradlew build --no-daemon                # с тестами (тестов в репо пока нет)
```

`--no-daemon` — потому что Gradle daemon съедает RAM на VM и иногда даёт race conditions.

## Артефакты
После успешного `build`:
- `build/libs/superheroes-<version>.jar` — release jar
- `build/libs/superheroes-<version>-sources.jar` — sources

Версия берётся из `gradle.properties: mod_version=...`.

## Типовые ошибки
- `Unsupported Java version` → не настроил JAVA_HOME (см. выше)
- `Could not resolve net.fabricmc.fabric-api:fabric-api` → нет интернета или VPN. Запустить с `--refresh-dependencies` после сети
- `error: cannot find symbol` от Mojang mappings → метод/класс переименовался в 1.21+, используй research-tools skill для поиска правильного имени в декомпилированном jar
