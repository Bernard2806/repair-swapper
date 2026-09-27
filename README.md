# Repair Swapper

Mod **exclusivo de cliente** para Fabric que cambia temporalmente a la mano correspondiente los objetos dañados con **Reparación (Mending)** cuando recoges experiencia.

Este repositorio es un fork del proyecto original de [Tobi1Craft](https://github.com/tobi1craft/repair-swapper). El objetivo de este fork es mantener el código al día con las versiones recientes de Minecraft y corregir errores. La versión configurada actualmente es **Minecraft 1.21.1**.

## Características

- Se activa automáticamente al recoger orbes de experiencia o manualmente con la tecla **R**.
- Busca objetos dañados con Reparación en el inventario y prioriza el que tenga menos durabilidad restante.
- Permite elegir la mano principal o la secundaria como destino.
- Permite configurar cuánto tiempo permanece activo el cambio.
- Funciona en el cliente; no requiere instalar el mod en el servidor.

## Requisitos

- Minecraft **1.21.1**.
- Fabric Loader y Fabric API.
- Java **21**.
- MidnightLib está incluida dentro del mod y no hace falta instalarla por separado.

## Instalación

1. Instala Fabric Loader para Minecraft 1.21.1.
2. Instala Fabric API en la carpeta `mods`.
3. Descarga el archivo JAR desde [Releases](https://github.com/Bernard2806/repair-swapper/releases) y colócalo en la misma carpeta.
4. Inicia Minecraft con el perfil de Fabric.

## Uso y configuración

- Pulsa **R** para activar o desactivar Repair Swapper.
- La activación automática al recoger experiencia está habilitada por defecto.
- En la configuración puedes seleccionar la mano principal o secundaria y ajustar la demora antes de desactivar la función.
- La demora predeterminada es de 60 ticks. Un valor de 0 desactiva el reinicio automático.

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
