# Repair Swapper

Mod **exclusivo de cliente** para Fabric que cambia temporalmente a la mano secundaria los objetos dañados con **Reparación (Mending)** cuando recoges experiencia.

Este repositorio es un fork del proyecto original de [Tobi1Craft](https://github.com/tobi1craft/repair-swapper). El objetivo de este fork es mantener el código al día con las versiones recientes de Minecraft y corregir errores. La versión configurada actualmente es **Minecraft 26.2**.

## Características

- Se activa automáticamente cuando se acercan orbes de experiencia, antes de recogerlos, o manualmente con la tecla **R**.
- Solo se activa mientras tengas una espada en la mano principal (configurable: armas cuerpo a cuerpo o cualquier objeto); si la guardas, Repair Swapper se desactiva.
- Cambia los objetos dañados con Reparación únicamente a la mano secundaria, dejando libre la espada.
- Busca objetos dañados con Reparación en el inventario y prioriza el que tenga menos durabilidad restante (o menor porcentaje). Mantiene cada objeto hasta repararlo del todo.
- Nunca quita un **Tótem de la Inmortalidad** de la mano secundaria.
- Muestra el porcentaje de durabilidad del objeto en reparación sobre la ranura de la mano secundaria y avisa con un sonido cuando todo está reparado.
- Si mueves los objetos a mano (por ejemplo, con la tecla F), no los desordena: deja el inventario como está.
- Permite configurar cuánto tiempo permanece activo el cambio y excluir objetos concretos.
- Funciona en el cliente; no requiere instalar el mod en el servidor.

## Requisitos

- Minecraft **26.2**.
- Fabric Loader y Fabric API.
- Java **25**.
- MidnightLib está incluida dentro del mod y no hace falta instalarla por separado.

## Instalación

1. Instala Fabric Loader para Minecraft 26.2.
2. Instala Fabric API en la carpeta `mods`.
3. Descarga el archivo JAR desde [Releases](https://github.com/Bernard2806/repair-swapper/releases) y colócalo en la misma carpeta.
4. Inicia Minecraft con el perfil de Fabric.

## Uso y configuración

- Pulsa **R** (tecla predeterminada) para activar o desactivar Repair Swapper. La tecla no lo alterna si hay una pantalla o el chat abiertos.
- Puedes cambiar la tecla en **Opciones → Controles → Asignación de teclas → Repair Swapper**.
- La activación automática al recoger experiencia está habilitada por defecto.
- La configuración está disponible desde [Mod Menu](https://modrinth.com/mod/modmenu) (opcional) y permite ajustar:
  - la activación automática y el alcance de detección de orbes (0 para activarlo solo al recoger un orbe);
  - la demora antes de desactivar la función, en segundos (3 por defecto). El temporizador se pausa mientras estás agachado y un valor de 0 desactiva el reinicio automático;
  - el objeto necesario en la mano principal, la prioridad de reparación, la protección del tótem, la desactivación al terminar, el indicador en pantalla, los sonidos y la lista de objetos excluidos.
- Si desactivas Repair Swapper con la tecla, no se reactiva solo hasta que no queden orbes cerca.
- Al abrir el menú de pausa, la mano secundaria vuelve a su estado original para que salir del mundo sea seguro.
- Los avisos aparecen en verde al activar, en rojo al desactivar y en celeste cuando todo está reparado.

## Compatibilidad con servidores

Al ser un mod de cliente, puede usarse al conectarse a servidores. Sin embargo, el cambio de objetos mediante acciones de inventario puede ser marcado por algunos anticheats. El uso está sujeto a las reglas del servidor.

## Créditos

- **Tobi1Craft**, creador del proyecto original [Repair Swapper](https://github.com/tobi1craft/repair-swapper).
- **Suedfruchtchen**, colaborador del proyecto original.

Este fork conserva los créditos del proyecto original y se centra en mantenerlo actualizado y corregir errores.

## Reportar errores

Si encuentras un problema relacionado con este fork, abre un [issue](https://github.com/Bernard2806/repair-swapper/issues) e incluye la versión de Minecraft, Fabric Loader, los pasos para reproducirlo y el registro del error si está disponible.

## Licencia

Este proyecto se distribuye bajo la licencia [MIT](LICENSE).
