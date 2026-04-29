---
name: datagen
description: Use when running data generation for blockstates, models, recipes, loot tables, or tags via DataProviders.
---

# Data Generation

Этот мод использует Fabric DataGen API.

## Команда

```bash
cd /home/ubuntu/repos/mymodhero
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew runDatagen --no-daemon
```

## Куда попадают результаты
- `src/generated/resources/` — auto-generated. Не править руками.
- При `build` они автоматически попадут в jar (через source set `generated` в `build.gradle`)

## Когда нужен datagen
- Добавил новый блок/предмет → сгенерировать blockstate / model / loot table через `FabricBlockLootTableProvider` / `FabricModelProvider`
- Добавил тег → `FabricTagProvider`
- Рецепты — через `FabricRecipeProvider`

## Где провайдеры в репо
Если есть `src/main/java/com/example/superheroes/datagen/` — добавлять провайдеры туда. Если нет — создать структуру + регистрацию в `<ModId>DataGenerator` (entrypoint `fabric-datagen` в `fabric.mod.json`).

## Workflow
1. Добавить/изменить DataProvider
2. Запустить `./gradlew runDatagen`
3. Проверить diff в `src/generated/resources/`
4. Закоммитить и сгенерированное (Fabric ожидает их в репо)
