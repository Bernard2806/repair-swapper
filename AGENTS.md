# Guía para agentes de IA

## Propósito del repositorio

Repair Swapper es un mod **solo cliente** para Minecraft. Cambia temporalmente a la mano principal o secundaria los objetos dañados con Reparación (Mending) para que reciban experiencia al recoger orbes. No es un mod de servidor ni implementa una API o servicio web.

## Tecnologías y versiones

- Java 21.
- Minecraft 1.21.1, Fabric Loader y Fabric Loom.
- Gradle 8.8 mediante el wrapper del repositorio (`./gradlew` en Linux/macOS; `gradlew.bat` en Windows).
- Fabric API y MidnightLib; MidnightLib se incluye dentro del mod mediante Gradle.
- Versiones y nombre del artefacto: `gradle.properties` (`mod_version`, `minecraft_version`, `archives_base_name` y dependencias).
- Metadatos, entrypoint y compatibilidad: `src/main/resources/fabric.mod.json`.

## Estructura relevante

- `src/main/java/de/tobi1craft/repairswapper/RepairSwapperClient.java`: inicialización cliente, tecla **R**, ciclo de ticks, selección del objeto con menor durabilidad restante y cambios de ranura.
- `RepairSwapperConfig.java`: configuración de activación automática, mano de destino y demora antes de desactivar (en ticks).
- `src/main/java/de/tobi1craft/repairswapper/mixin/ExperienceOrbMixin.java`: detecta la colisión de un orbe de experiencia con el jugador cliente y puede activar el mod.
- `src/main/resources/repair-swapper.mixins.json`: registro del mixin cliente.
- `src/main/resources/assets/repair-swapper/lang/`: textos de interfaz y configuración.
- `build.gradle`, `settings.gradle`, `gradle.properties` y `gradle/wrapper/`: configuración de Gradle, repositorios, plugins y versiones.
- `.github/workflows/build.yml`: build de CI y creación de GitHub Releases.
- `README.md`: descripción, instalación, uso, compatibilidad y créditos del fork.

## Comportamiento que debe conservarse

1. El mod solo debe inicializarse y actuar en el cliente (`environment: client`). No añadas lógica de servidor sin un requerimiento explícito.
2. La tecla R activa/desactiva la función. Al recoger experiencia, el mixin puede activarla si `auto` está habilitado.
3. Se consideran los objetos dañados que tengan Reparación; se prioriza el que tenga menor durabilidad restante.
4. El destino predeterminado es la mano secundaria. La configuración permite usar la mano principal y definir una demora de reinicio.
5. Los cambios de inventario usan las API de Minecraft/Fabric. Ten en cuenta que el README advierte que algunos anticheats pueden marcar estas acciones al jugar en servidores.

Al modificar el flujo de inventario, revisa cuidadosamente los índices de ranuras, el estado del cursor y las rutas de restauración/desactivación. Evita que un cambio deje objetos en una ranura inesperada o afecte inventarios distintos al del jugador.

## Build, CI y releases

- El workflow se ejecuta en `push` y `pull_request`, usa Java 21 y ejecuta `./gradlew build`.
- Un push de un tag que empiece por `v` activa además la generación de un GitHub Release. El workflow adjunta el JAR normal, no los artefactos `-dev` ni `-sources`, y genera las notas automáticamente.
- Mantén `mod_version` en `gradle.properties` coherente con el tag de release (por ejemplo, `mod_version=1.0.2` y tag `v1.0.2`).
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
