# OnePieceCollectr — Documentación Técnica

## Índice
1. [Visión general](#1-visión-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Base de datos](#4-base-de-datos)
5. [Arquitectura de la aplicación](#5-arquitectura-de-la-aplicación)
6. [Flujo de arranque](#6-flujo-de-arranque)
7. [Módulos y controladores](#7-módulos-y-controladores)
   - [App](#71-app)
   - [Login](#72-login)
   - [Principal](#73-principal)
   - [DashboardController](#74-dashboardcontroller)
   - [ColeccionController](#75-coleccioncontroller)
   - [MazosController](#76-mazoscontroller)
   - [CardDetailController](#77-carddetailcontroller)
   - [EventosController](#78-eventoscontroller)
   - [MarketController](#79-marketcontroller)
8. [Modelos de datos](#8-modelos-de-datos)
9. [Sistema de estilos (CSS)](#9-sistema-de-estilos-css)
10. [Reglas de negocio del TCG](#10-reglas-de-negocio-del-tcg)
11. [Flujos principales de usuario](#11-flujos-principales-de-usuario)
12. [Mini-proyecto: Importador de Cartas](#12-mini-proyecto-importador-de-cartas)

---

## 1. Visión general

OnePieceCollectr es una aplicación de escritorio JavaFX para gestionar colecciones del juego de cartas One Piece TCG. Permite al usuario:

- Registrarse e iniciar sesión con contraseña hasheada (BCrypt).
- Ver todas las cartas del juego (~3130) y marcar cuáles posee.
- Filtrar la colección por tipo, set, color, rareza, coste, contador y poder.
- Crear y editar mazos respetando las reglas oficiales del TCG (50 cartas, máx. 4 copias, 1 líder).
- Registrar eventos y torneos con fecha, lugar y estado de favorito.
- Exportar listas de cartas a CardTrader desde el módulo de Mercado.

---

## 2. Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| UI | JavaFX (FXML) | 17.0.2 |
| Base de datos | PostgreSQL (Supabase cloud) | — |
| Driver JDBC | org.postgresql | 42.7.2 |
| Hash de contraseñas | jBCrypt | 0.4 |
| Build | Maven | 3.x |
| Empaquetado | jpackage (vía maven) | — |

---

## 3. Estructura del proyecto

```
tfg/
├── pom.xml
└── src/main/
    ├── java/com/onepiececollectr/
    │   ├── App.java                    ← Punto de entrada JavaFX
    │   ├── Login.java                  ← Auth + conexión BD + log
    │   ├── Principal.java              ← Shell de navegación
    │   ├── DashboardController.java    ← Vista Home
    │   ├── ColeccionController.java    ← Vista Colección
    │   ├── MazosController.java        ← Vista Mazos
    │   ├── CardDetailController.java   ← Popup de gestión de copias
    │   ├── EventosController.java      ← Vista Eventos
    │   ├── MarketController.java       ← Vista Mercado
    │   ├── Carta.java                  ← Modelo carta
    │   ├── Deck.java                   ← Modelo mazo
    │   ├── Event.java                  ← Modelo evento
    │   ├── Mercado.java                ← Modelo publicación de mercado
    │   ├── Usuario.java                ← Modelo usuario de sesión
    │   ├── Coleccion.java              ← Modelo fila coleccion (BD)
    │   ├── Deck_carta.java             ← Modelo fila deck_carta (BD)
    │   └── Carta_set.java              ← Modelo relación carta-set
    └── resources/
        ├── view/
        │   ├── login.fxml
        │   ├── principal.fxml
        │   ├── dashboard.fxml
        │   ├── coleccion.fxml
        │   ├── mazos.fxml
        │   ├── deckDetail.fxml
        │   ├── cardPopup.fxml
        │   ├── eventos.fxml
        │   ├── mercado.fxml
        │   └── sidebar.css
        └── cards/
            └── background.png          ← Fondo del login
```

---

## 4. Base de datos

La BD está alojada en Supabase (PostgreSQL). La cadena de conexión y credenciales viven en `Login.java`.

### Tablas inferidas del código

#### `carta`
| Columna | Tipo | Notas |
|---|---|---|
| id_carta | VARCHAR | PK, formato `OP01-001` |
| nombre | VARCHAR | |
| tipo | VARCHAR | LIDER / PERSONAJE / EVENTO / STAGE |
| color | VARCHAR | RED, BLUE, GREEN, YELLOW, PURPLE, BLACK (o combinaciones: `RED/GREEN`) |
| rareza | VARCHAR | C, UC, R, SR, SEC, L, P, SP |
| imagen_url | VARCHAR | URL pública de la imagen |
| texto | VARCHAR | Habilidad de la carta (puede ser NULL) |
| coste | INTEGER | Puede ser NULL (líderes no tienen coste) |
| poder | INTEGER | Puede ser NULL |
| contador | INTEGER | Puede ser NULL |
| subtipos | VARCHAR | Puede ser NULL |
| atributo | VARCHAR | Puede ser NULL |

#### `usuario`
| Columna | Tipo | Notas |
|---|---|---|
| id_usuario | INTEGER | PK, autogenerado |
| nombre | VARCHAR | Único |
| password | VARCHAR | Hash BCrypt |

#### `coleccion`
| Columna | Tipo | Notas |
|---|---|---|
| id_usuario | INTEGER | FK → usuario |
| id_carta | VARCHAR | FK → carta |
| cantidad | INTEGER | |

Constraint: `ON CONFLICT DO NOTHING` → una carta solo puede estar una vez por usuario.

#### `deck`
| Columna | Tipo | Notas |
|---|---|---|
| id_deck | INTEGER | PK, autogenerado |
| id_usuario | INTEGER | FK → usuario |
| nombre | VARCHAR | |
| colores | VARCHAR | Colores en hex separados por coma: `#e74c3c,#3498db` |

#### `deck_carta`
| Columna | Tipo | Notas |
|---|---|---|
| id_deck | INTEGER | FK → deck |
| id_carta | VARCHAR | FK → carta |
| cantidad | INTEGER | |

Constraint: `ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad` → upsert.

#### `eventos`
| Columna | Tipo | Notas |
|---|---|---|
| id_usuario | INTEGER | FK → usuario |
| nombre | VARCHAR | |
| fecha | VARCHAR | Formato `dd/MM/yyyy` |
| lugar | VARCHAR | |
| favorito | BOOLEAN | |

---

## 5. Arquitectura de la aplicación

La aplicación sigue el patrón **MVC** implementado con FXML:

- **Model**: clases POJO en `com.onepiececollectr` (`Carta`, `Deck`, `Event`, `Mercado`, `Usuario`…).
- **View**: archivos `.fxml` en `src/main/resources/view/`.
- **Controller**: clases `*Controller.java` vinculadas a cada FXML mediante `fx:controller`.

### Patrón de navegación

La app usa un **shell único** (`principal.fxml` / `Principal.java`) con un `StackPane` central (`contentArea`). Cada sección de la app carga su FXML en ese `StackPane`, reemplazando el contenido anterior. No hay cambios de `Stage`, solo de escena interna.

```
Stage
└── Scene (login.fxml)
      ↓ login exitoso
└── Scene (principal.fxml)
      ├── VBox sidebar  (navegación)
      └── StackPane contentArea
            ← dashboard.fxml
            ← coleccion.fxml
            ← mazos.fxml / deckDetail.fxml
            ← eventos.fxml
            ← mercado.fxml
```

El método estático `Principal.mostrarVista(Parent)` permite a cualquier controlador cambiar el contenido del `contentArea` sin necesidad de tener una referencia al `Principal`.

### Caché de vistas

`Principal` mantiene un `Map<String, Parent> vistaCache` para no recargar el FXML en cada navegación. Excepciones: `coleccion.fxml` y `mazos.fxml` se eliminan de la caché antes de cargar para forzar recarga (ya que su estado depende del mazo seleccionado).

---

## 6. Flujo de arranque

```
App.main()
  └── Application.launch()
        └── App.start(Stage)
              ├── new Thread(App::cargarDatosGlobales).start()  ← en paralelo
              ├── FXMLLoader.load("/view/login.fxml")
              ├── stage.initStyle(UNDECORATED)                  ← sin barra nativa
              └── stage.show()
```

`cargarDatosGlobales()` carga todas las cartas de la BD en `App.todasLasCartas` (lista estática global) en un hilo secundario para no bloquear la UI mientras se muestra el login.

---

## 7. Módulos y controladores

### 7.1 App

**Archivo**: `App.java`  
**Rol**: Punto de entrada de JavaFX. Gestiona el estado global de cartas e imágenes.

| Elemento | Descripción |
|---|---|
| `todasLasCartas` | `List<Carta>` estática. Todas las cartas del juego cargadas en memoria al arrancar. |
| `imagenCache` | `Map<String, Image>` estática. Caché de imágenes para no descargar la misma URL dos veces. |
| `getImagen(url)` | Devuelve la imagen de la caché o la descarga asíncronamente (105×145px). |
| `cargarDatosGlobales()` | `SELECT * FROM carta` → rellena `todasLasCartas`. Se llama en hilo secundario al arrancar y también desde `ColeccionController` si la lista está vacía. |

---

### 7.2 Login

**Archivo**: `Login.java`  
**FXML**: `login.fxml`  
**Rol**: Autenticación de usuarios, gestión de la conexión BD, y sistema de log de la app.

#### Campos estáticos importantes

| Campo | Tipo | Descripción |
|---|---|---|
| `conexion` | `Connection` | Conexión JDBC singleton, compartida por toda la app. |
| `sesionUsuario` | `Usuario` | Usuario que ha iniciado sesión. Accedido globalmente. |

#### Métodos principales

| Método | Visibilidad | Descripción |
|---|---|---|
| `initialize()` | FXML | Establece conexión BD, aplica estilos a los controles de ventana, carga imagen de fondo. |
| `IniciarSesion()` | FXML | Lee usuario/contraseña del formulario, llama a `buscarUsuarioEnBD()`, si OK navega a principal. |
| `registrarNuevoUsuario()` | FXML | Inserta nuevo usuario con contraseña hasheada con BCrypt. |
| `buscarUsuarioEnBD(nom, pass)` | private | `SELECT` por nombre, verifica hash con `BCrypt.checkpw()`. Devuelve `Usuario` o null. |
| `getConexion()` | public static | Devuelve la conexión activa. Si está cerrada, la reabre. Punto de acceso único a la BD para toda la app. |
| `conectar()` | public static | Crea la conexión JDBC con la URL de Supabase. |
| `registrarEnLog(mensaje)` | public static | Escribe una línea timestampeada en `One_piece.log`. Usado por todos los controladores. |
| `iraPanrallaPrincipal()` | public | Carga `principal.fxml` en el mismo Stage, reemplazando la escena del login. |
| `minimizeWindow()` | FXML | Minimiza la ventana (botón −). |
| `maximizeWindow()` | FXML | Alterna maximizado (botón □). |
| `closeWindow()` | FXML | Cierra la aplicación (botón ✕). |
| `mostrarAlerta(titulo, msj)` | public | Muestra un `Alert` de tipo INFORMATION. |

#### Controles de ventana (login)
La ventana del login es `UNDECORATED` (sin barra nativa). Un `HBox winControls` con los botones −/□/✕ flota en `TOP_RIGHT` del `StackPane` raíz. El drag de ventana se activa arrastrando sobre ese `HBox`.

---

### 7.3 Principal

**Archivo**: `Principal.java`  
**FXML**: `principal.fxml`  
**Rol**: Shell de navegación. Contiene el sidebar y el área de contenido central.

#### Campos

| Campo | Descripción |
|---|---|
| `staticContentArea` | Referencia estática al `StackPane` central. Permite que otros controladores inyecten vistas. |
| `vistaCache` | `Map<String, Parent>` — caché de FXMLs ya cargados para evitar recargas. |
| `sidebarExpanded` | Estado del sidebar (visible/oculto). Arranca en `false` (sidebar colapsado). |
| `dragOffsetX/Y` | Offset para el drag de la ventana desde `windowControls`. |

#### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Aplica estilos a los botones de nav (dorado/hover dorado oscuro), añade `sidebar.css` a la escena, aplica estilos transparentes a los controles de ventana, registra handler de drag, carga dashboard y colapsa el sidebar. |
| `mostrarVista(Parent)` | **Estático.** Reemplaza el contenido de `contentArea`. Llamado desde cualquier controlador para cambiar de sección. |
| `loadVista(fxml)` | Carga un FXML usando `vistaCache`. Si ya está en caché no lo recarga. |
| `toggleSidebar()` | Alterna visibilidad del sidebar. Cuando está oculto, muestra `btnToggle` flotante. |
| `loadDashboard()` | Navega a `dashboard.fxml` (desde caché). |
| `loadColeccion()` | Limpia `mazoSeleccionado`, elimina `coleccion.fxml` de la caché, carga la vista y llama a `ColeccionController.instancia.refrescar()`. |
| `loadMazos()` | Elimina `mazos.fxml` de caché y recarga. |
| `loadMercado()` | Navega a `mercado.fxml` (desde caché). |
| `loadEventos()` | Navega a `eventos.fxml` (desde caché). |
| `minimizeWindow()` | Minimiza el Stage. |
| `maximizeWindow()` | Alterna maximizado del Stage. |
| `closeWindow()` | Cierra el Stage. |

#### Sidebar colapsable
El sidebar es un `VBox` con `visible/managed` alternados. Al colapsarse, aparece un botón flotante `btnToggle` (☰ dorado) en `TOP_LEFT` del `StackPane` raíz, implementado como overlay de `StackPane`.

#### Controles de ventana (principal)
Igual que en login: `HBox windowControls` con `alignment="TOP_RIGHT"` y `maxWidth="Infinity"`, botones transparentes icon-only (azul claro `#7fb3d3`). ✕ se pone rojo en hover. Drag mediante handlers `onMousePressed/Dragged` en el HBox.

---

### 7.4 DashboardController

**Archivo**: `DashboardController.java`  
**FXML**: `dashboard.fxml`  
**Rol**: Pantalla Home con estadísticas del usuario.

#### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Llama a `actualizarEstadisticas()` si hay usuario en sesión. |
| `actualizarEstadisticas(idUsuario)` | `SELECT COUNT(DISTINCT id_carta) FROM coleccion WHERE id_usuario = ?` para las cartas poseídas. Calcula porcentaje sobre `App.todasLasCartas.size()`. Lee `MazosController.getMisMazos().size()` y `EventosController.getListaEventos().size()` para los contadores de mazos y eventos. Muestra el próximo evento de la lista. |

#### Datos mostrados
- Total de cartas poseídas (de BD).
- Número de mazos (de memoria).
- Número de eventos (de memoria).
- Barra de progreso de la colección (`cartasPoseidas / totalCartasApp`).
- Próximo evento registrado.

---

### 7.5 ColeccionController

**Archivo**: `ColeccionController.java`  
**FXML**: `coleccion.fxml`  
**Rol**: Vista principal de la colección. Muestra todas las cartas con filtros, paginación virtual y gestión de posesión.

#### Constantes

| Constante | Valor | Descripción |
|---|---|---|
| `PAGE_SIZE` | 50 | Cartas por página en la paginación virtual. |
| `RAREZA_ORDEN` | `["C","UC","R","SR","SEC","L","P","SP"]` | Orden de rareza para el combo. |
| `ORDEN_COLECCION` | Comparator | Ordena por set (OP→EB→PRB→resto) y luego por número dentro del set. |
| `HEX_A_COLOR_BD` | Map | Mapea hex de color a alias válidos en BD (inglés y español). |

#### Estado interno

| Campo | Descripción |
|---|---|
| `instancia` | Referencia estática a la instancia activa. Permite refrescar desde `Principal`. |
| `idsPoseidos` | `Set<String>` con los `id_carta` que posee el usuario. Cargados desde BD en hilo secundario. |
| `cartasCargadas` | Lista local (copia ordenada de `App.todasLasCartas`). |
| `listaFiltradaActual` | Resultado del último filtrado. Base para la paginación. |
| `cartasMostradas` | Número de cartas actualmente renderizadas en el grid. |
| `ticketBusqueda` | Contador para cancelar renders obsoletos (evita race conditions entre búsquedas rápidas). |

#### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Rellena combos, registra listeners en todos los filtros y el scroll, arranca hilo secundario para cargar IDs poseídos. |
| `filtrarLocalmente(texto)` | Aplica todos los filtros activos sobre `cartasCargadas`. Resultado → `listaFiltradaActual`. Llama a `mostrarPrimeraPagina()`. |
| `obtenerColoresMazo(mazo)` | Convierte los hex del mazo a alias válidos para comparar con la columna `color` de la BD. |
| `mostrarPrimeraPagina()` | Incrementa `ticketBusqueda`, limpia el grid y renderiza las primeras `PAGE_SIZE` cartas. |
| `cargarMasCartas()` | Llamado al llegar al 85% del scroll. Añade las siguientes `PAGE_SIZE` cartas al grid sin limpiar. |
| `createCard(carta)` | Crea el nodo visual de una carta (ImageView + Label). Asigna handlers de click. |
| `actualizarEstiloCarta(card, carta)` | Aplica el borde/fondo correcto según estado: en mazo (verde), límite alcanzado (rojo/opaco), poseída (azul+dorado), no poseída (oscuro+opaco). |
| `puedeAnadirAlMazo(carta, mazo)` | Valida la regla del líder único. Devuelve false y muestra alerta si ya hay un líder distinto. |
| `abrirSelectorDeCopias(carta)` | Abre `cardPopup.fxml` como ventana modal. Al cerrar, recarga cartas del mazo y refresca la vista. |
| `registrarEnColeccion(idCarta)` | `INSERT INTO coleccion ... ON CONFLICT DO NOTHING`. Click izquierdo sin mazo activo. |
| `borrarDeMiColeccion(idCarta)` | `DELETE FROM coleccion WHERE id_usuario = ? AND id_carta = ?`. Click derecho. |
| `cargarIdsPoseidos()` | `SELECT id_carta FROM coleccion WHERE id_usuario = ?` → rellena `idsPoseidos`. |
| `poblarComboSet()` | Extrae sets únicos de `cartasCargadas`, los ordena y los mete en `comboSet`. |
| `poblarComboRareza()` | Extrae rarezas únicas y las ordena según `RAREZA_ORDEN`. |
| `toggleFiltros()` | Muestra/oculta el panel de filtros colapsable. |
| `limpiarFiltros()` | Resetea todos los combos y checkboxes a "Todos"/seleccionado. |
| `refrescar()` | Llamado desde `Principal.loadColeccion()` para refrescar la vista en el hilo de UI. |
| `actualizarContadorMazo()` | Muestra/oculta el label de conteo del mazo activo arriba a la derecha. |

#### Lógica de click sobre carta
- **Sin mazo activo + click izquierdo**: registra en colección.
- **Sin mazo activo + click derecho**: elimina de colección.
- **Con mazo activo + click izquierdo**: valida reglas y abre `cardPopup.fxml`.
- **Modo mercado**: selecciona/deselecciona para la lista de CardTrader.

#### Filtros disponibles
| Filtro | Fuente de datos |
|---|---|
| Búsqueda de texto | nombre, id_carta, texto, subtipos, atributo |
| Tipo | LIDER / PERSONAJE / EVENTO / STAGE |
| Colección (Set) | Prefijo del id_carta (OP01, OP02… EB01, PRB01…) |
| Color | RED, BLUE, GREEN, YELLOW, PURPLE, BLACK |
| Rareza | C, UC, R, SR, SEC, L, P, SP |
| Coste | 0–10 |
| Counter | 0, 1000, 2000 |
| Poder | 1000–13000 |
| Posesión | checkboxes Poseídas / No poseídas |

---

### 7.6 MazosController

**Archivo**: `MazosController.java`  
**FXMLs**: `mazos.fxml` (lista) y `deckDetail.fxml` (interior del mazo)  
**Rol**: Creación, listado, edición y visualización de mazos. Gestión de cartas dentro de un mazo.

#### Constantes

| Constante | Valor | Descripción |
|---|---|---|
| `MAX_COPIAS_LIDER` | 1 | Máximo de copias de una carta de tipo LIDER. |
| `MAX_COPIAS_NORMAL` | 4 | Máximo de copias de cartas normales. |
| `MAX_CARTAS_MAZO` | 51 | Tamaño máximo del mazo (50 cartas + 1 líder). |
| `TIPO_ORDEN` | `["LIDER","STAGE","EVENTO","PERSONAJE"]` | Orden de visualización en el grid del mazo. |

#### Estado estático

| Campo | Descripción |
|---|---|
| `misMazos` | `List<Deck>` estática. Todos los mazos del usuario cargados en memoria. |
| `mazoSeleccionado` | `Deck` actualmente abierto para edición. `null` si no hay ninguno. |
| `instancia` | Referencia estática a la instancia activa del controlador. |

#### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Carga mazos de BD si la lista está vacía, refresca la lista visual, limita selección de colores. |
| `cargarMazosDesdeBD()` | **Estático.** `SELECT * FROM deck WHERE id_usuario = ?`. Llamado al login y al recargar mazos. |
| `createDeck()` | Valida que haya al menos un color, crea el `Deck` en BD y lo añade a `misMazos`. |
| `refreshDeckList()` | Reconstruye el `VBox deckList` con un botón por mazo (gradiente de color) y botón 🗑 de borrado. |
| `borrarMazo(mazo)` | `DELETE FROM deck WHERE id_deck = ?`. Elimina de `misMazos` y refresca la lista. |
| `openDeck(deck)` | Pone el deck en `mazoSeleccionado` y carga `deckDetail.fxml` en `contentArea`. |
| `cargarCartasDelMazo(mazo)` | `SELECT c.*, dc.cantidad FROM carta JOIN deck_carta ...`. Por cada carta, la añade `cantidad` veces a `mazo.getCartas()` para que el `.size()` refleje el total real. |
| `renderDeck(mazo)` | Agrupa cartas por id, las ordena (LIDER→STAGE→EVENTO→PERSONAJE, con opciones de sub-orden por coste/nombre/color), y pinta el grid con `createMiniCardConMultiplicador()`. |
| `createMiniCardConMultiplicador(carta, deck, cantidad)` | Crea la miniatura de la carta en el detalle del mazo: imagen + badge "xN" (rojo si límite, azul si no) + nombre + botón ✕. |
| `cambiarCantidad(delta)` | Punto de entrada de los botones +1/-1/+4/-4 en `deckDetail`. Aplica todas las reglas (líder único, límite de copias, límite de mazo), calcula delta efectivo y llama a `guardarEnBD()`. |
| `guardarEnBD(cant)` | Si `cant == 0` → DELETE; si `cant > 0` → UPSERT en `deck_carta`. |
| `actualizarColoresMazoSegunLider(lider)` | Cuando se añade un líder, parsea su color y actualiza `colores` en BD y en memoria. Así el filtro de colección se adapta automáticamente. |
| `renombrarMazo(mazo, nuevoNombre)` | `UPDATE deck SET nombre = ?`. Activado con doble click sobre el nombre en `deckDetail`. |
| `eliminarMazoActual()` | Muestra diálogo de confirmación y borra el mazo actual desde `deckDetail`. |
| `exportarMazoPDF()` | Genera un archivo `.html` con la lista de cartas y lo abre en el navegador del sistema. |
| `limitarSeleccionColores()` | Listener en los 6 checkboxes de color: si se marcan más de 2, desmarca el último y muestra alerta. |
| `calculateGradient(deck)` | Devuelve un string de estilo JavaFX con gradiente lineal de los colores del mazo. |
| `getColorIcons(deck)` | Devuelve emojis de colores (🔴🔵🟢🟡🟣⚫) para mostrar en el botón del mazo. |

---

### 7.7 CardDetailController

**Archivo**: `CardDetailController.java`  
**FXML**: `cardPopup.fxml`  
**Rol**: Popup modal para gestionar el número de copias de una carta en el mazo activo.

#### Métodos principales

| Método | Descripción |
|---|---|
| `cargarDatos(carta)` | Carga imagen y nombre. Consulta BD para obtener cantidad actual en el mazo. |
| `handleMas()` | Añade 1 copia respetando límites. |
| `handleMas4()` | Añade hasta el máximo permitido (botón MAX). |
| `handleMenos()` | Quita 1 copia. |
| `handleMenos4()` | Quita todas las copias (pone a 0 → DELETE en BD). |
| `handleCerrar()` | Cierra el Stage del popup. |
| `calcularDeltaPermitido(deltaDeseado)` | Calcula el delta real respetando: (1) líder único, (2) límite de copias de la carta, (3) hueco restante en el mazo. Muestra alertas informativas si recorta el delta. |
| `guardarEnBD(idCarta, nuevaCantidad)` | Si `cantidad == 0` → DELETE; si `> 0` → UPSERT. Llama a `ejecutarUpdate()`. |
| `actualizarColoresMazoSegunLider(lider)` | Si se añade un líder multicolor, parsea sus colores y actualiza el mazo en BD y en `MazosController.instancia`. |
| `actualizarInterfaz()` | Actualiza el label `xN` y habilita/deshabilita los botones +/- según los límites. |
| `obtenerCantidadEnMazo(idCarta)` | `SELECT cantidad FROM deck_carta WHERE id_deck = ? AND id_carta = ?`. |

#### Lógica de límites
- Líderes: máximo 1 copia, y no puede haber dos líderes distintos en el mismo mazo.
- Cartas normales: máximo 4 copias.
- El mazo completo no puede superar `MAX_CARTAS_MAZO` (51). Si el hueco es menor que el delta pedido, se añade solo lo que cabe y se informa al usuario.

---

### 7.8 EventosController

**Archivo**: `EventosController.java`  
**FXML**: `eventos.fxml`  
**Rol**: Gestión de eventos y torneos (crear, editar, eliminar, marcar favoritos).

#### Estado estático

| Campo | Descripción |
|---|---|
| `listaEventos` | `List<Event>` estática. Compartida con `DashboardController` para mostrar el próximo evento. |

#### Métodos principales

| Método | Descripción |
|---|---|
| `initialize()` | Bloquea fechas pasadas en el `DatePicker`, añade listener de búsqueda, renderiza eventos. |
| `renderEvents()` | Reconstruye el `VBox eventList` con los eventos filtrados. Aplica colores según urgencia (rojo = pasado, amarillo = próximos 3 días, blanco = normal). |
| `createEventCard(event)` | Crea la tarjeta visual de un evento con botones Editar/Favorito/Eliminar. |
| `saveEvent()` | Si `selectedEvent != null` → UPDATE en BD y en memoria. Si no → INSERT en BD y añade a `listaEventos`. |
| `cargarEventosDesdeBD()` | `SELECT * FROM eventos WHERE id_usuario = ?`. Rellena `listaEventos`. |
| `prepararEdicion(event)` | Rellena el formulario con los datos del evento para edición. |
| `toggleFavorites()` | Alterna el filtro de solo favoritos. |

#### Colores de urgencia de eventos
- **Rojo** (`#ffe6e6`): fecha ya pasada.
- **Amarillo** (`#fff3cd`): en los próximos 3 días.
- **Blanco/gris**: más de 3 días.

---

### 7.9 MarketController

**Archivo**: `MarketController.java`  
**FXML**: `mercado.fxml`  
**Rol**: Herramienta de optimización de colección mediante integración con CardTrader.

#### Estado estático

| Campo | Descripción |
|---|---|
| `modoSeleccionMercado` | `boolean`. Cuando es `true`, los clicks en `ColeccionController` seleccionan cartas para la lista en lugar de añadirlas a la colección. |
| `listaParaOptimizar` | `List<Carta>` de cartas seleccionadas para exportar a CardTrader. |

#### Flujo de uso
1. El usuario pulsa "Seleccionar de mi colección" → `irASeleccionarDeColeccion()` activa `modoSeleccionMercado = true` y carga `coleccion.fxml`.
2. En la colección, los clicks añaden/quitan cartas de `listaParaOptimizar` (resaltadas con borde azul).
3. Al volver al mercado, `actualizarListaTexto()` muestra los `id_carta` en el `TextArea`.
4. "Abrir CardTrader" abre el navegador del sistema en la URL de CardTrader.

#### Métodos principales

| Método | Descripción |
|---|---|
| `irASeleccionarDeColeccion()` | Activa el modo selección y carga `coleccion.fxml`. |
| `actualizarListaTexto()` | Rellena el `TextArea` con los IDs de `listaParaOptimizar` separados por salto de línea. |
| `abrirCardTrader()` | Abre `https://www.cardtrader.com/wishlists/new` en el navegador. |
| `limpiarLista()` | Vacía `listaParaOptimizar` y limpia el `TextArea`. |
| `volverAlPrincipal()` | Desactiva `modoSeleccionMercado` y vuelve al dashboard. |

---

## 8. Modelos de datos

### Carta
Representa una carta del TCG. Todos los campos opcionales devuelven valores seguros (cadena vacía o 0) para evitar NPE.

```
id_carta   → String  (formato: "OP01-001")
nombre     → String
tipo       → String  (LIDER / PERSONAJE / EVENTO / STAGE)
color      → String  (puede ser multicolor: "RED/GREEN")
rareza     → String  (C, UC, R, SR, SEC, L, P, SP)
imagen_url → String
texto      → String? (habilidad)
coste      → Integer?
poder      → Integer?
contador   → Integer?
subtipos   → String?
atributo   → String?
```

### Deck
```
id_deck       → int
id_usuario    → int
nombre_deck   → String
colores       → List<String>  (hex: "#e74c3c")
cartas        → List<Carta>   (en memoria, poblada por cargarCartasDelMazo)
```
`getColoresComoString()` y `setColoresDesdeString()` convierten entre lista y CSV para BD.

### Usuario
```
id     → int
nombre → String
```
Singleton de sesión en `Login.sesionUsuario`.

### Event
```
name     → String
date     → String  ("dd/MM/yyyy")
location → String
favorite → boolean
```

### Mercado
```
id_publicacion → int
carta          → Carta
vendedor       → Usuario
precio         → double
estado         → String  ("Disponible"/"Vendido"/"Reservado")
```
El módulo de mercado está parcialmente implementado; la funcionalidad de compra/venta está comentada.

---

## 9. Sistema de estilos (CSS)

### Paleta de colores

| Uso | Hex |
|---|---|
| Fondo principal | `#0d1b2a` |
| Panel / card bg | `#1a2c42` |
| Borde / separador | `#2e4a6b` |
| Texto claro | `#c8dce8` |
| Acento azul | `#7fb3d3` |
| Acento azul hover | `#4a6fa5` |
| Dorado (sidebar/botones) | `#e8c96d` |
| Dorado oscuro hover | `#c9a84c` |
| Verde (en mazo) | `#27ae60` |
| Rojo (límite/error) | `#c0392b` |

### Estrategia de aplicación de estilos

JavaFX carga `modena.css` como stylesheet de usuario-agente con alta prioridad. Para garantizar que los estilos del proyecto se apliquen, se usan dos estrategias:

1. **`setStyle()` en código Java**: tiene la máxima prioridad en JavaFX. Usado en `Principal.initialize()` y `Login.initialize()` para los botones de navegación y los controles de ventana.
2. **`sidebar.css` inyectado en la escena**: cargado una sola vez en `Principal.initialize()` mediante `sceneProperty().addListener()`. Cubre scrollbars, TextArea, DatePicker, ComboBox y CheckBox.

### sidebar.css — qué cubre
- `.scroll-bar` y subcomponentes → scrollbars dark.
- `.text-area` y `.text-area .content` → TextArea dark.
- `.date-picker` y subcomponentes → DatePicker dark.
- `.combo-box` y `.combo-box-popup` → ComboBox dark con dropdown dark.
- `.check-box` y `.check-box .box` → CheckBox dark con tick azul.
- `.sidebar-toggle` → estilo del botón ☰.

---

## 10. Reglas de negocio del TCG

La app implementa las reglas oficiales del One Piece TCG:

| Regla | Implementación |
|---|---|
| Máximo 4 copias de una carta (no líder) | `MAX_COPIAS_NORMAL = 4` en `MazosController` y `CardDetailController`. |
| Máximo 1 líder | `MAX_COPIAS_LIDER = 1`. Si ya hay un líder distinto, se bloquea y muestra alerta. |
| Máximo 51 cartas por mazo | `MAX_CARTAS_MAZO = 51`. El delta se recorta si no hay hueco. |
| Solo 1 o 2 colores por mazo | `limitarSeleccionColores()` en `MazosController` desmarca el 3er color. |
| Colores del líder → filtro de colección | Al añadir un líder, `actualizarColoresMazoSegunLider()` parsea sus colores y actualiza el mazo en BD y memoria. La colección solo muestra cartas de esos colores. |
| Cartas multicolor | El filtro usa `.contains()` sobre el campo `color` para manejar valores como `"RED/GREEN"`. |

---

## 11. Flujos principales de usuario

### Registro e inicio de sesión
```
LoginFXML → usernameField + passwordField
  → [Crear cuenta] → registrarNuevoUsuario() → BCrypt.hashpw() → INSERT usuario
  → [Iniciar sesión] → IniciarSesion()
      → buscarUsuarioEnBD() → BCrypt.checkpw()
      → Login.sesionUsuario = usuario encontrado
      → MazosController.cargarMazosDesdeBD()
      → iraPanrallaPrincipal() → carga principal.fxml en el mismo Stage
```

### Marcar carta como poseída
```
ColeccionController.createCard(carta)
  → click izquierdo (sin mazo activo)
  → registrarEnColeccion(idCarta)
      → INSERT INTO coleccion ON CONFLICT DO NOTHING
      → idsPoseidos.add(idCarta)
      → actualizarEstiloCarta() → borde dorado
```

### Añadir carta a un mazo
```
Principal.loadColeccion()
  → MazosController.mazoSeleccionado != null
  → ColeccionController muestra solo cartas de los colores del mazo
  → click izquierdo en carta
      → puedeAnadirAlMazo() → valida líder único
      → abrirSelectorDeCopias() → abre cardPopup.fxml (modal)
          → CardDetailController.cargarDatos()
          → [botón +] → calcularDeltaPermitido() → guardarEnBD() → UPSERT deck_carta
          → [cerrar] → cargarCartasDelMazo() → actualizarContadorMazo() → filtrarLocalmente()
```

### Crear mazo
```
MazosController (mazos.fxml)
  → deckNameField + selección de 1 o 2 colores
  → [+ Crear mazo] → createDeck()
      → INSERT INTO deck
      → Deck nuevo → misMazos.add()
      → refreshDeckList() → botón con gradiente de colores
```

### Registrar evento
```
EventosController (eventos.fxml)
  → nameField + datePicker + locationField
  → [Guardar] → saveEvent()
      → INSERT INTO eventos
      → listaEventos.add(new Event())
      → renderEvents() → createEventCard() → tarjeta con color según urgencia
```

---

## 12. Mini-proyecto: Importador de Cartas

### 12.1 Propósito y contexto

La base de datos de la aplicación contiene más de 3.100 cartas del juego One Piece TCG, cada una con 15 campos (ID, nombre, tipo, color, rareza, coste, poder, contador, atributo, vida, subtipos, texto de habilidad, imagen y metadatos del set). Introducir estos datos manualmente sería inviable.

Para resolver este problema se desarrolló un **mini-proyecto Java independiente** (`importadorcartas/`) cuya única responsabilidad es conectarse a una API pública de cartas, deserializar la respuesta JSON y volcarla íntegramente a la base de datos PostgreSQL en Supabase mediante JDBC.

Este proyecto vive en la rama `feature/importador-cartas` del repositorio y no forma parte del artefacto final de la aplicación; es una herramienta de desarrollo de ejecución única (o puntual cuando salen nuevos sets).

---

### 12.2 Estructura del mini-proyecto

```
importadorcartas/
└── src/main/java/com/jp/
    ├── Database.java   — Singleton de conexión JDBC a Supabase
    ├── Carta.java      — POJO mapeado desde JSON con anotaciones Jackson
    └── TestAPI.java    — Lógica principal: fetch → deserialización → upsert en BD
```

---

### 12.3 Tecnologías utilizadas y justificación

#### 12.3.1 `java.net.URL` + `URI.create()` — HTTP sin cliente externo

```java
URL url = URI.create("https://optcgapi.com/api/allSetCards/").toURL();
mapper.readValue(new InputStreamReader(url.openStream()), Carta[].class);
```

**Por qué se usa:** Java 11+ expone `java.net.http.HttpClient` y la clase clásica `java.net.URL` para realizar peticiones HTTP directamente desde la JVM, sin añadir dependencias como OkHttp o Apache HttpClient. Se eligió `URL.openStream()` porque la API devuelve un array JSON en una sola respuesta, sin paginación ni autenticación, por lo que un cliente HTTP complejo sería sobredimensionado.

**Justificación frente a lo visto en clase:** En el currículo de DAM las peticiones de red se abordan de forma básica (sockets). El uso de `URI.create().toURL()` sigue la recomendación oficial de Java moderno (evitar el constructor `new URL(String)` deprecado en Java 20) y demuestra conocimiento de la evolución del API estándar.

---

#### 12.3.2 Jackson ObjectMapper — Deserialización JSON

```java
// Dependencia Maven:
// com.fasterxml.jackson.core : jackson-databind : 2.x

ObjectMapper mapper = new ObjectMapper();
Carta[] cartas = mapper.readValue(new InputStreamReader(url.openStream()), Carta[].class);
```

```java
// Carta.java — POJO con anotaciones de mapeo
@JsonIgnoreProperties(ignoreUnknown = true)
public class Carta {
    @JsonProperty("card_set_id")   public String cardId;
    @JsonProperty("card_name")     public String name;
    @JsonProperty("card_type")     public String type;
    @JsonProperty("card_color")    public String color;
    @JsonProperty("card_cost")     public String cost;
    @JsonProperty("card_power")    public String power;
    @JsonProperty("counter_amount")public String counter;
    @JsonProperty("card_image")    public String imageUrl;
    // ... 15 campos en total
}
```

**Por qué se usa:** La API devuelve un array JSON con nombres de campo en `snake_case` que no coinciden con las convenciones Java (`camelCase`). Jackson permite mapear automáticamente cada campo JSON a su atributo Java mediante `@JsonProperty`, eliminando la necesidad de parsear el JSON manualmente con `JSONObject` o `JSONArray`.

La anotación `@JsonIgnoreProperties(ignoreUnknown = true)` hace que el deserializador ignore campos de la API que no estén declarados en el POJO, protegiendo el importador frente a cambios futuros en la API sin romper la ejecución.

**Justificación frente a lo visto en clase:** Jackson es la librería de serialización/deserialización JSON más utilizada en el ecosistema Java empresarial (Spring Boot la incluye por defecto). No se estudia en DAM, pero su uso es imprescindible en proyectos reales. El dominio de `ObjectMapper` y las anotaciones `@JsonProperty` / `@JsonIgnoreProperties` es una competencia profesional demandada.

---

#### 12.3.3 Transacciones JDBC manuales con rollback

```java
conn.setAutoCommit(false);   // desactiva commit automático por operación

try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
     PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {

    for (Carta c : cartas) {
        // ... insertar o actualizar cada carta
    }

    conn.commit();   // confirma todos los cambios de una vez

} catch (Exception e) {
    conn.rollback(); // si algo falla, la BD queda exactamente como estaba
    throw e;
}
```

**Por qué se usa:** Importar 3.100+ cartas con `autoCommit=true` (el comportamiento por defecto de JDBC) significa un `COMMIT` por cada operación individual, lo que tiene un coste enorme en round-trips a la base de datos remota (Supabase está en AWS eu-west-1). Al desactivar el autocommit y agrupar todo en una sola transacción:

- El rendimiento mejora drásticamente (1 commit vs. 3.100+).
- La atomicidad garantiza que si la importación falla a mitad (error de red, campo inesperado...) la base de datos **no queda en un estado inconsistente** con la mitad de las cartas importadas.

**Justificación frente a lo visto en clase:** El manejo explícito de transacciones (`setAutoCommit`, `commit`, `rollback`) no se suele ver en los ejercicios básicos de JDBC de DAM, donde se trabaja con operaciones aisladas. En entornos de producción con bases de datos remotas es una práctica fundamental para garantizar integridad y rendimiento.

---

#### 12.3.4 `PreparedStatement` reutilizado y `ON CONFLICT DO NOTHING`

```java
// SQL de inserción con cláusula upsert de PostgreSQL
String sqlInsert = """
    INSERT INTO carta (id_carta, nombre, tipo, color, ...)
    VALUES (?, ?, ?, ?, ...)
    ON CONFLICT (id_carta) DO NOTHING
    """;

// El PreparedStatement se prepara UNA VEZ y se ejecuta 3.100 veces
try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
    for (Carta c : cartas) {
        insertarCarta(stmtInsert, c);  // rellena los ? y ejecuta
    }
}
```

**Por qué se usa — `PreparedStatement` reutilizado:** Preparar un `PreparedStatement` supone que el servidor de base de datos compila y planifica la query una sola vez. Reutilizar el mismo objeto para los 3.100+ registros elimina esa sobrecarga repetida. Además, los `?` parametrizados previenen inyección SQL, fundamental cuando los valores vienen de una API externa.

**Por qué se usa — `ON CONFLICT DO NOTHING`:** Esta cláusula es específica de PostgreSQL (no existe en SQL estándar ni en MySQL con esa sintaxis). Permite ejecutar el importador múltiples veces sin duplicar datos: si una carta ya existe en la BD (por su PK `id_carta`), la inserción se ignora silenciosamente. Esto convierte el importador en una herramienta **idempotente**: ejecutarla 10 veces produce el mismo resultado que ejecutarla una.

**Justificación frente a lo visto en clase:** En DAM se enseña el SQL estándar. `ON CONFLICT` es una extensión de PostgreSQL (también llamada "upsert") que no forma parte del currículo estándar pero es ampliamente usada en proyectos reales con PostgreSQL y Supabase.

---

#### 12.3.5 Text Blocks de Java (Java 15+)

```java
String sqlInsert = """
    INSERT INTO carta (id_carta, nombre, tipo, color, rareza, set_nombre, set_id, texto,
                       coste, poder, contador, atributo, imagen_url, vida, subtipos)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (id_carta) DO NOTHING
    """;
```

**Por qué se usa:** Los Text Blocks (bloques de texto multilínea con `"""`) se introdujeron como feature estable en Java 15. Permiten escribir cadenas SQL largas con sangría visual sin concatenaciones con `+` ni caracteres de escape. El resultado es código más legible y mantenible, especialmente con queries complejas de 15 parámetros.

**Justificación frente a lo visto en clase:** El currículo de DAM suele cubrir Java 8/11. Los Text Blocks son una característica moderna (Java 15+) que mejora significativamente la legibilidad del código SQL embebido en Java.

---

#### 12.3.6 Switch Expressions (Java 14+)

```java
public static String normalizarTipo(String tipo) {
    if (tipo == null) return "UNKNOWN";
    return switch (tipo.toLowerCase()) {
        case "character" -> "PERSONAJE";
        case "event"     -> "EVENTO";
        case "stage"     -> "STAGE";
        case "leader"    -> "LIDER";
        default          -> tipo.toUpperCase();
    };
}

public static String normalizarColor(String color) {
    if (color == null) return "UNKNOWN";
    return switch (color.toLowerCase()) {
        case "red"    -> "ROJO";
        case "blue"   -> "AZUL";
        case "green"  -> "VERDE";
        case "purple" -> "MORADO";
        case "black"  -> "NEGRO";
        case "yellow" -> "AMARILLO";
        default       -> color.toUpperCase();
    };
}
```

**Por qué se usa:** La API devuelve los tipos y colores en inglés (`"character"`, `"red"`...) mientras que la base de datos de la aplicación los almacena en español mayúscula (`"PERSONAJE"`, `"ROJO"`...) para coherencia con el juego en castellano. Las funciones de normalización traducen estos valores en el momento de la importación.

Los Switch Expressions (flecha `->`) son la sintaxis moderna de Java 14+ que elimina el `break` implícito, devuelve un valor directamente y hace que cada caso sea una expresión en lugar de una sentencia, reduciendo el riesgo de fall-through accidental.

**Justificación frente a lo visto en clase:** El `switch` clásico con `break` se enseña en los primeros módulos. El Switch Expression con `->` es una mejora sintáctica de Java 14 que produce código más conciso y seguro, evitando bugs clásicos del `switch` tradicional.

---

#### 12.3.7 Compatibilidad con PgBouncer (`prepareThreshold=0`)

```java
// Database.java
private static final String URL =
    "jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
```

**Por qué se usa:** Supabase utiliza **PgBouncer** como connection pooler en modo `transaction`, lo que significa que la conexión lógica puede cambiar de conexión física entre transacciones. PostgreSQL gestiona los `PreparedStatement` a nivel de conexión física; si PgBouncer cambia de conexión, el statement preparado ya no existe en el nuevo servidor y la ejecución falla con error `prepared statement does not exist`.

El parámetro `prepareThreshold=0` deshabilita el "server-side prepare" del driver JDBC de PostgreSQL: todas las queries se envían como texto plano (`simple query protocol`) en lugar de como statements preparados en el servidor. Esto sacrifica una pequeña optimización de parseo en el servidor pero garantiza la compatibilidad con cualquier connection pooler en modo transacción.

**Justificación frente a lo visto en clase:** La gestión de connection poolers, el protocolo extendido vs. simple de PostgreSQL y los parámetros de conexión JDBC son conceptos de administración de bases de datos en entornos de producción que no forman parte del currículo estándar de DAM. Su uso aquí demuestra capacidad de diagnóstico y resolución de problemas en infraestructura cloud real.

---

### 12.4 Flujo completo de importación

```
[Ejecución: mvn exec:java]
        │
        ▼
URI.create("https://optcgapi.com/api/allSetCards/").toURL()
        │  HTTP GET
        ▼
API optcgapi.com  ──►  JSON array (~3.100 objetos)
        │
        ▼
ObjectMapper.readValue(InputStreamReader, Carta[].class)
        │  @JsonProperty mapea snake_case → camelCase
        ▼
Carta[]  (array en memoria con todos los campos)
        │
        ▼
Database.conectar()  ──►  JDBC → Supabase PostgreSQL (AWS eu-west-1)
conn.setAutoCommit(false)
        │
        ▼
Para cada Carta c:
    ├── actualizarCarta(stmtUpdate, c)
    │       └── UPDATE ... WHERE imagen_url=? AND id_carta IS NULL
    │           ¿filas afectadas > 0?
    │              Sí → carta actualizada (tenía URL pero no ID)
    │              No → insertarCarta(stmtInsert, c)
    │                     └── INSERT ... ON CONFLICT (id_carta) DO NOTHING
    │
    └── cada 100 cartas → log de progreso en consola
        │
        ▼
conn.commit()   ──►  Un solo round-trip confirma todo
        │  (si Exception → conn.rollback())
        ▼
"Importación completada — actualizadas: X, insertadas: Y"
```

---

### 12.5 Resultado

Tras ejecutar el importador, la tabla `carta` de Supabase queda poblada con la totalidad del catálogo disponible en la API (más de 3.100 cartas en el momento del desarrollo), incluyendo todos los sets publicados hasta la fecha. La aplicación principal lee esta tabla en el arranque (`App.cargarDatosGlobales()`) y la carga en memoria para su uso en filtros, mazos y colección.
