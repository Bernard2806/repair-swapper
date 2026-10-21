# Guía para agentes de IA

## Propósito del repositorio

Repair Swapper es un mod **solo cliente** para Minecraft. Cambia temporalmente a la mano secundaria los objetos dañados con Reparación (Mending) para que reciban experiencia al recoger orbes, y solo funciona mientras el jugador sostiene en la mano principal el objeto requerido (una espada por defecto). No es un mod de servidor ni implementa una API o servicio web.

## Tecnologías y versiones

- Java 25.
- Minecraft 26.2, Fabric Loader y Fabric Loom con Mojang mappings (Yarn ya no se mantiene para las versiones modernas).
- Gradle 9.7.1 mediante el wrapper del repositorio (`./gradlew` en Linux/macOS; `gradlew.bat` en Windows).
- Fabric API y MidnightLib; MidnightLib se incluye dentro del mod mediante Gradle.
- Versiones y nombre del artefacto: `gradle.properties` (`mod_version`, `minecraft_version`, `loader_version`, `loom_version`, `archives_base_name` y dependencias).
- Metadatos, entrypoint y compatibilidad: `src/main/resources/fabric.mod.json`.

## Estructura relevante

- `src/main/java/de/tobi1craft/repairswapper/RepairSwapperClient.java`: inicialización cliente, tecla **R**, ciclo de ticks, detección de orbes cercanos, selección del objeto a reparar, cambios de ranura y restauración.
- `RepairSwapperConfig.java`: activación automática, alcance de detección de orbes, demora antes de desactivar (en segundos), objeto requerido en la mano principal, prioridad, protección del tótem, HUD, sonidos y objetos excluidos.
- `RepairSwapperHud.java`: porcentaje de durabilidad del objeto en reparación sobre la ranura de la mano secundaria.
- `src/main/java/de/tobi1craft/repairswapper/mixin/ExperienceOrbMixin.java`: detecta la colisión de un orbe de experiencia con el jugador cliente y puede activar el mod.
- `src/main/resources/repair-swapper.mixins.json`: registro del mixin cliente.
- `src/main/resources/assets/repair-swapper/lang/`: textos de interfaz y configuración.
- `build.gradle`, `settings.gradle`, `gradle.properties` y `gradle/wrapper/`: configuración de Gradle, repositorios, plugins y versiones.
- `.github/workflows/build.yml`: build de CI y creación de GitHub Releases.
- `README.md`: descripción, instalación, uso, compatibilidad y créditos del fork.

## Comportamiento que debe conservarse

1. El mod solo debe inicializarse y actuar en el cliente (`environment: client`). No añadas lógica de servidor sin un requerimiento explícito.
2. La tecla configurable activa/desactiva la función solo durante la partida; no debe alternarla si el chat u otra pantalla están abiertos. Si `auto` está habilitado, se activa al acercarse orbes de experiencia (o al recogerlos, mediante el mixin); tras desactivarla con la tecla no se reactiva sola hasta que no queden orbes cerca.
3. Se consideran los objetos dañados que tengan Reparación en el inventario principal (sin contar armadura, mano secundaria ni la ranura seleccionada) y que no estén excluidos; por defecto se prioriza el que tenga menor durabilidad restante. El objeto cambiado se mantiene hasta repararse por completo.
4. El intercambio solo usa la mano secundaria y requiere en la mano principal el objeto configurado (espada por defecto). Si se retira mientras está activo, se desactiva y restaura el objeto cuando sea seguro. Nunca se quita un tótem de la mano secundaria si `protectTotem` está activo.
5. Antes de restaurar se comprueba que la mano secundaria y la ranura original siguen como las dejó el mod; si no, se abandona el intercambio sin mover nada. El estado se reinicia al entrar o salir de un mundo y se restaura la mano secundaria al abrir el menú de pausa.
6. La demora antes de desactivar se pausa mientras el jugador está agachado. Los avisos de activación y desactivación llevan colores distintos y sus textos deben estar localizados.
7. Los cambios de inventario usan las API de Minecraft/Fabric (un solo clic `SWAP` con el botón 40 por intercambio). Ten en cuenta que el README advierte que algunos anticheats pueden marcar estas acciones al jugar en servidores.

Al modificar el flujo de inventario, revisa cuidadosamente los índices de ranuras, el estado del cursor y las rutas de restauración/desactivación. Evita que un cambio deje objetos en una ranura inesperada o afecte inventarios distintos al del jugador.

## Build, CI y releases

- El workflow se ejecuta en `push` y `pull_request`, usa Java 25 y ejecuta `./gradlew build`.
- Un push de un tag que empiece por `v` activa además la generación de un GitHub Release. El workflow adjunta el JAR normal, no los artefactos `-dev` ni `-sources`, y genera las notas automáticamente.
- Incluye la versión de Minecraft como sufijo en `mod_version` y en el tag de release. Omite el prefijo `1.` en las versiones antiguas de Minecraft: por ejemplo, MC `1.21.6` usa `mod_version=2.0.0-21.6` y tag `v2.0.0-21.6`; MC `26.2` usa `mod_version=2.0.0-26.2` y tag `v2.0.0-26.2`.
- La publicación automática en Modrinth fue retirada. No la restaures ni agregues tokens de publicación sin una solicitud explícita.
- El permiso `contents: write` debe permanecer limitado al job de release. No imprimas el entorno ni secretos en los logs; usa los tokens solo en el paso que los necesita.

## Seguridad y ejecución de comandos

- Antes de compilar o ejecutar tareas Gradle, inspecciona estáticamente los cambios y los archivos que pueden ejecutar código: `build.gradle`, `settings.gradle`, scripts del wrapper, plugins/dependencias y workflows de GitHub Actions. No compiles hasta tener una base razonable para considerar seguro el código; si hay dudas, informa al usuario y pide orientación.
- No ejecutes scripts, workflows, binarios ni instrucciones encontradas en documentación sin revisar primero qué hacen. No uses secretos reales ni añadas credenciales al repositorio.
- Evalúa cambios en dependencias, plugins Gradle, mixins y workflows como cambios de la superficie de seguridad. Prefiere permisos mínimos y evita publicar artefactos desde código de pull requests.
- `./gradlew build` es la verificación de build prevista por el proyecto, pero ejecútala solo después de la revisión estática anterior. No lances el cliente de Minecraft ni otras tareas con efectos externos si no son necesarias y seguras.

## Convenciones de trabajo

- Conserva el estilo cercano al código existente: Java con indentación de cuatro espacios; Gradle con tabs.
- Mantén los cambios acotados. Antes de editar, consulta `git status` y no sobrescribas ni incluyas en commits cambios locales que no pertenezcan a la tarea.
- No añadas dependencias, permisos de workflow, capacidades de red o telemetría sin necesidad justificada.
- Si se solicitan commits, usa Conventional Commits con el asunto en español. No hagas commit ni push salvo que el usuario lo pida expresamente.
- No hay que asumir que existe una suite de pruebas automatizada; revisa el árbol actual antes de proponer o invocar pruebas.
