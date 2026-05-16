# MEMORIA DE PROYECTO FINAL DE CICLO
## Desarrollo de Aplicaciones Multiplataforma (DAM)

---

# PORTADA

**Título del proyecto:**
OnePieceCollectr — Aplicación de gestión de colecciones y mazos del juego de cartas One Piece TCG

**Integrantes del equipo:**
- Diego Cabello Del Río
- Javier Palomar Sanz
- Iker Núñez Berrocal

**Centro educativo:**
IES Gaspar Melchor de Jovellanos — Fuenlabrada (Madrid)

**Ciclo formativo:**
2.º Curso — Desarrollo de Aplicaciones Multiplataforma (DAM)

**Curso académico:** 2025 / 2026

**Tutora del proyecto:**
Saturnina Castro Cintas

**Fecha de entrega:** [FECHA DE ENTREGA]

---

## ÍNDICE

*(Generado automáticamente en la versión Word/PDF final)*

1. Resumen / Abstract
2. Introducción
3. Análisis de requisitos
4. Tecnologías utilizadas
5. Diseño del sistema
6. Base de datos
7. Desarrollo e implementación
8. Interfaz gráfica
9. Pruebas
10. Conclusiones
11. Trabajo futuro
12. Bibliografía y webgrafía
13. Anexos

---

## 1. RESUMEN

### Resumen (español)

OnePieceCollectr es una aplicación de escritorio desarrollada en Java con interfaz gráfica JavaFX, orientada a los jugadores y coleccionistas del juego de cartas One Piece TCG (Trading Card Game). El proyecto nace de la necesidad real de los jugadores de gestionar su colección física de cartas de forma digital: saber qué cartas tienen, cuáles les faltan, crear mazos respetando las reglas oficiales del juego y organizar su participación en eventos y torneos.

La aplicación permite registrar e iniciar sesión de forma segura mediante cifrado de contraseñas con BCrypt, consultar un catálogo de más de 3.000 cartas con un sistema de filtros avanzado, marcar las cartas poseídas, construir mazos con validación automática de las reglas del TCG y gestionar eventos y torneos. Además, incluye integración con la plataforma CardTrader para facilitar la compra de cartas que faltan en la colección.

El proyecto ha sido desarrollado por tres alumnos de 2.º de DAM del IES Gaspar Melchor de Jovellanos (Fuenlabrada) durante el curso 2025/2026, aplicando los conocimientos adquiridos a lo largo del ciclo en programación orientada a objetos, bases de datos relacionales, interfaces gráficas y control de versiones.

**Tecnologías principales:** Java 17, JavaFX 17, PostgreSQL (Supabase), Maven, Git/GitHub.

---

### Abstract (English)

OnePieceCollectr is a desktop application developed in Java with a JavaFX graphical interface, aimed at players and collectors of the One Piece Trading Card Game (TCG). The project addresses the real need of players to digitally manage their physical card collections: tracking which cards they own, which ones are missing, building decks according to the official game rules, and organizing their participation in events and tournaments.

The application allows users to securely register and log in using BCrypt password hashing, browse a catalogue of over 3,000 cards with an advanced filtering system, mark owned cards, build decks with automatic TCG rule validation, and manage events and tournaments. It also includes integration with the CardTrader platform to facilitate the purchase of missing cards.

The project was developed by three students from the second year of the DAM programme at IES Gaspar Melchor de Jovellanos (Fuenlabrada) during the 2025/2026 academic year, applying knowledge acquired throughout the cycle in object-oriented programming, relational databases, graphical interfaces and version control.

**Main technologies:** Java 17, JavaFX 17, PostgreSQL (Supabase), Maven, Git/GitHub.

---

## 2. INTRODUCCIÓN

### 2.1 Contexto del proyecto

One Piece TCG es un juego de cartas coleccionables basado en el manga y anime *One Piece*, lanzado por Bandai en 2022. En poco tiempo se ha convertido en uno de los juegos de cartas más populares del mundo, con más de 3.000 cartas únicas distribuidas en múltiples colecciones (sets), torneos oficiales y una comunidad muy activa en España y a nivel internacional.

Un jugador habitual de One Piece TCG se enfrenta a un problema cotidiano: gestionar una colección que puede superar las 500 o 1.000 cartas físicas, recordar qué cartas posee, construir mazos competitivos y organizarse para asistir a torneos locales. Actualmente no existe una aplicación de escritorio en español, gratuita y de código abierto, que cubra todas estas necesidades de forma integrada.

### 2.2 Motivación

Los tres integrantes del equipo somos aficionados al juego. Hemos experimentado en primera persona la dificultad de llevar el control de una colección creciente y la falta de herramientas específicas en español. Esto nos llevó a proponer un proyecto que, además de cumplir los requisitos académicos del TFG, tuviese utilidad real para nosotros y para la comunidad de jugadores.

### 2.3 Objetivos generales

- Desarrollar una aplicación de escritorio funcional, estable y visualmente cuidada con Java y JavaFX.
- Implementar un sistema de autenticación seguro con registro e inicio de sesión.
- Permitir la gestión completa de una colección de cartas sincronizada con una base de datos en la nube.
- Implementar un constructor de mazos que valide automáticamente las reglas oficiales del One Piece TCG.
- Gestionar eventos y torneos locales con seguimiento de favoritos.
- Integrar la aplicación con la plataforma CardTrader para facilitar la adquisición de cartas.
- Aplicar buenas prácticas de desarrollo: control de versiones con Git, arquitectura MVC, separación de responsabilidades y documentación del código.

### 2.4 Alcance

La aplicación es de escritorio (Windows), monousuario por sesión y conectada a una base de datos PostgreSQL alojada en la nube (Supabase). No es una aplicación web ni móvil, aunque el diseño modular facilitaría una futura migración.

---

## 3. ANÁLISIS DE REQUISITOS

### 3.1 Requisitos funcionales

| ID | Requisito funcional |
|---|---|
| RF-01 | El sistema permitirá registrar nuevos usuarios con nombre y contraseña. |
| RF-02 | El sistema permitirá iniciar sesión con credenciales válidas. |
| RF-03 | Las contraseñas se almacenarán cifradas con BCrypt. |
| RF-04 | El sistema cargará en memoria todas las cartas del catálogo (~3.100) al arrancar. |
| RF-05 | El usuario podrá visualizar todas las cartas en una cuadrícula con imagen, nombre y estado de posesión. |
| RF-06 | El usuario podrá marcar cartas como poseídas (click izquierdo) y desmarcarlas (click derecho). |
| RF-07 | El sistema permitirá filtrar cartas por: texto libre, tipo, colección (set), color, rareza, coste, counter y poder. |
| RF-08 | Los filtros serán combinables y aplicables simultáneamente. |
| RF-09 | El panel de filtros será colapsable para maximizar el espacio de visualización. |
| RF-10 | La colección se mostrará con paginación virtual (carga de 50 cartas, +50 al llegar al final del scroll). |
| RF-11 | El usuario podrá crear mazos con nombre y hasta 2 colores. |
| RF-12 | El sistema validará las reglas del TCG: máximo 51 cartas por mazo, máximo 4 copias de cada carta no-líder, máximo 1 líder. |
| RF-13 | Al añadir un líder a un mazo, el sistema actualizará automáticamente los colores permitidos del mazo. |
| RF-14 | La colección mostrará solo cartas de los colores del mazo cuando este esté activo. |
| RF-15 | El usuario podrá renombrar mazos (doble click sobre el nombre). |
| RF-16 | El usuario podrá eliminar mazos con confirmación previa. |
| RF-17 | El usuario podrá exportar el contenido de un mazo a un archivo HTML. |
| RF-18 | El sistema permitirá registrar eventos con nombre, fecha y lugar. |
| RF-19 | Los eventos pasados se mostrarán en rojo, los próximos en amarillo y los futuros en blanco. |
| RF-20 | El usuario podrá marcar eventos como favoritos y filtrar por favoritos. |
| RF-21 | El módulo de Mercado permitirá seleccionar cartas de la colección para exportarlas a CardTrader. |
| RF-22 | El Dashboard mostrará estadísticas: cartas poseídas, porcentaje de colección completado, número de mazos y próximo evento. |
| RF-23 | La barra lateral de navegación será colapsable. |
| RF-24 | La ventana será movible mediante arrastre y contará con controles de minimizar, maximizar y cerrar. |

### 3.2 Requisitos no funcionales

| ID | Requisito | Descripción |
|---|---|---|
| RNF-01 | **Seguridad** | Las contraseñas nunca se almacenan en texto plano. Se usa BCrypt con factor de coste aleatorio (gensalt). |
| RNF-02 | **Rendimiento** | Las ~3.100 cartas se cargan en un hilo secundario para no bloquear la interfaz. La paginación virtual evita renderizar más de 50 cartas a la vez. |
| RNF-03 | **Usabilidad** | La interfaz sigue una paleta de colores consistente (azul oscuro/dorado). Los colores comunican el estado: dorado = poseída, verde = en mazo, rojo = límite alcanzado. |
| RNF-04 | **Disponibilidad** | La base de datos está en Supabase (PostgreSQL gestionado en la nube), con alta disponibilidad. |
| RNF-05 | **Mantenibilidad** | El código sigue el patrón MVC con separación clara de modelos, vistas (FXML) y controladores. |
| RNF-06 | **Compatibilidad** | La aplicación funciona en Windows 10/11 con JDK 17 o superior instalado. |
| RNF-07 | **Escalabilidad** | El catálogo de cartas puede crecer sin modificar el código (solo añadir filas a la BD). |
| RNF-08 | **Trazabilidad** | Todas las operaciones relevantes (login, cambios en colección/mazos, errores) se registran en `One_piece.log`. |

---

## 4. TECNOLOGÍAS UTILIZADAS

### 4.1 Java 17

Java es el lenguaje de programación principal del proyecto. Elegimos Java 17 (LTS) por ser la versión estable de largo soporte en el momento del desarrollo, su total compatibilidad con JavaFX 17 y por ser el lenguaje que hemos estudiado durante el ciclo. Java nos ofrece orientación a objetos robusta, tipado estático y una amplia biblioteca estándar.

### 4.2 JavaFX 17

JavaFX es el framework de interfaces gráficas para Java que sustituye a Swing en aplicaciones modernas. Permite definir la interfaz en archivos FXML (similares a XML/HTML) y vincularlos a controladores Java. Elegimos JavaFX porque:
- Separación limpia entre diseño (FXML) y lógica (Java), facilitando el patrón MVC.
- Soporte para CSS para estilizar los componentes.
- Scene Builder permite diseñar interfaces de forma visual.
- Es el framework de escritorio que se estudia en el ciclo DAM.

La versión 17.0.2 se gestiona como dependencia Maven, junto con el plugin `javafx-maven-plugin` para lanzar la aplicación desde línea de comandos.

### 4.3 Maven

Apache Maven es la herramienta de gestión de dependencias y construcción del proyecto. Nos permite:
- Declarar todas las dependencias en `pom.xml` y que Maven las descargue automáticamente.
- Compilar, testear y empaquetar el proyecto con comandos estandarizados (`mvn clean compile`, `mvn package`).
- Garantizar que todos los miembros del equipo trabajan con las mismas versiones de librerías.

### 4.4 PostgreSQL y Supabase

La base de datos relacional del proyecto es PostgreSQL, una de las más robustas y populares del mundo. En lugar de instalar un servidor local, utilizamos **Supabase**, una plataforma BaaS (Backend as a Service) que ofrece PostgreSQL gestionado en la nube de forma gratuita. Esto nos ha permitido:
- Trabajar los tres miembros del equipo contra la misma base de datos en tiempo real.
- No necesitar configurar un servidor local en cada máquina.
- Acceder a los datos desde cualquier lugar con conexión a Internet.

La conexión se realiza mediante JDBC con el driver oficial de PostgreSQL (`org.postgresql:postgresql:42.7.2`).

### 4.5 jBCrypt

jBCrypt es una librería Java que implementa el algoritmo de hash BCrypt para contraseñas. BCrypt es el estándar actual para almacenamiento seguro de contraseñas: incorpora un factor de coste (el tiempo de cómputo aumenta con cada versión), protege contra ataques de diccionario y de fuerza bruta, e incluye una sal aleatoria en cada hash. En el proyecto usamos `BCrypt.hashpw()` al registrar y `BCrypt.checkpw()` al verificar el login.

### 4.6 Git y GitHub

Git es el sistema de control de versiones distribuido que hemos usado para coordinar el trabajo en equipo. GitHub es la plataforma de alojamiento del repositorio remoto. El flujo de trabajo ha consistido en:
- Rama principal `main` para el código estable.
- Rama `por-si-se-rompe` para el desarrollo activo y pruebas.
- Commits frecuentes con mensajes descriptivos.
- `.gitignore` configurado para excluir artefactos de compilación (`target/`) y logs.

### 4.7 Scene Builder

Scene Builder es la herramienta visual de Gluon para diseñar archivos FXML de forma gráfica. Lo usamos para el diseño inicial de las ventanas, aunque la mayor parte del estilado final se realizó directamente en los archivos FXML y mediante `setStyle()` en Java para garantizar la prioridad sobre el CSS de JavaFX (`modena.css`).

---

## 5. DISEÑO DEL SISTEMA

### 5.1 Arquitectura

La aplicación sigue el patrón **MVC (Modelo-Vista-Controlador)**:

- **Modelo**: Clases POJO que representan las entidades del dominio (`Carta`, `Deck`, `Usuario`, `Event`, `Mercado`, `Coleccion`, `Deck_carta`). No contienen lógica de negocio ni de interfaz.
- **Vista**: Archivos FXML en `src/main/resources/view/` que definen la estructura de cada pantalla. El estilado se aplica mediante CSS (`sidebar.css`) y estilos en línea.
- **Controlador**: Clases `*Controller.java` vinculadas a cada FXML mediante el atributo `fx:controller`. Gestionan la lógica de negocio, la interacción con la BD y la actualización de la vista.

```
┌─────────────────────────────────────────────────────┐
│                     APLICACIÓN                       │
│                                                       │
│   Vista (FXML)  ←→  Controlador (Java)  ←→  Modelo  │
│                           ↕                           │
│                     Base de Datos                     │
│                   (PostgreSQL/Supabase)               │
└─────────────────────────────────────────────────────┘
```

### 5.2 Patrón de navegación — Shell único

La aplicación usa un **shell de navegación único** basado en `principal.fxml`. Un `StackPane` central (`contentArea`) actúa como contenedor donde se cargan y descargan las vistas de cada sección. El método estático `Principal.mostrarVista(Parent)` permite a cualquier controlador cambiar la vista activa sin necesitar una referencia directa al controlador principal.

```
Stage
└── Scene
    └── principal.fxml (StackPane raíz)
        ├── BorderPane
        │   ├── LEFT: VBox sidebar (colapsable)
        │   └── CENTER: StackPane contentArea  ← aquí se carga cada sección
        │         ↔ dashboard.fxml
        │         ↔ coleccion.fxml
        │         ↔ mazos.fxml / deckDetail.fxml
        │         ↔ eventos.fxml
        │         ↔ mercado.fxml
        └── HBox windowControls (overlay TOP_RIGHT)
```

### 5.3 Diagrama de casos de uso

```
                        ┌─────────────────────────────────┐
                        │          OnePieceCollectr        │
                        │                                  │
    ┌──────┐            │  ┌─────────────────────────┐    │
    │      │──Registrarse──▶  Crear cuenta            │    │
    │      │            │  └─────────────────────────┘    │
    │      │──Login──────▶  Iniciar sesión             │    │
    │      │            │                                  │
    │      │──Ver cartas▶  Consultar colección         │    │
    │      │            │  Filtrar cartas               │    │
    │Usuario│            │  Marcar/desmarcar poseídas   │    │
    │      │            │                                  │
    │      │──Mazos─────▶  Crear mazo                  │    │
    │      │            │  Añadir/quitar cartas         │    │
    │      │            │  Exportar lista               │    │
    │      │            │                                  │
    │      │──Eventos───▶  Crear/editar/eliminar        │    │
    │      │            │  Marcar favoritos             │    │
    │      │            │                                  │
    │      │──Mercado───▶  Seleccionar cartas           │    │
    └──────┘            │  Abrir CardTrader             │    │
                        └─────────────────────────────────┘
```

### 5.4 Diagrama de clases (simplificado)

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Usuario    │       │    Carta     │       │    Deck      │
│──────────────│       │──────────────│       │──────────────│
│ id: int      │       │ id_carta     │       │ id_deck      │
│ nombre       │       │ nombre       │       │ id_usuario   │
└──────┬───────┘       │ tipo         │       │ nombre_deck  │
       │               │ color        │       │ colores      │
       │ 1             │ rareza       │       │ cartas       │
       ▼ N             │ imagen_url   │       └──────┬───────┘
┌──────────────┐       │ texto        │              │
│  Coleccion   │       │ coste        │              │ N
│──────────────│       │ poder        │       ┌──────▼───────┐
│ id_usuario   │       │ contador     │       │  Deck_carta  │
│ id_carta     │       │ subtipos     │       │──────────────│
│ cantidad     │       │ atributo     │       │ id_deck      │
└──────────────┘       └──────────────┘       │ id_carta     │
                                              │ cantidad     │
┌──────────────┐       ┌──────────────┐       └──────────────┘
│    Event     │       │   Mercado    │
│──────────────│       │──────────────│
│ name         │       │ id_public.   │
│ date         │       │ carta        │
│ location     │       │ vendedor     │
│ favorite     │       │ precio       │
└──────────────┘       │ estado       │
                       └──────────────┘
```

### 5.5 Diagrama de flujo — Inicio de sesión

```
  [Arranque] → App.start()
       │
       ├── Hilo secundario: cargarDatosGlobales()
       │       └── SELECT * FROM carta → App.todasLasCartas
       │
       └── Hilo principal: cargar login.fxml
                 │
           [Login screen]
                 │
         ┌───────┴──────────┐
    [Iniciar sesión]   [Crear cuenta]
         │                  │
   buscarUsuarioEnBD()   hashpw() → INSERT usuario
         │
   BCrypt.checkpw()
         │
    ¿Correcto?
    ┌─────┴─────┐
   NO           SÍ
    │            │
  Alerta    sesionUsuario = usuario
             cargarMazosDesdeBD()
             iraPanrallaPrincipal()
                  │
            [principal.fxml]
            loadVista(dashboard.fxml)
```

---

## 6. BASE DE DATOS

### 6.1 Diseño de la base de datos

La base de datos está alojada en Supabase (PostgreSQL). Consta de 6 tablas que cubren todas las entidades del sistema.

### 6.2 Diagrama Entidad-Relación

```
┌──────────┐        ┌────────────┐        ┌──────────┐
│ USUARIO  │        │  COLECCION │        │  CARTA   │
│──────────│1      N│────────────│N      1│──────────│
│id_usuario├────────┤id_usuario  ├────────┤id_carta  │
│nombre    │        │id_carta    │        │nombre    │
│password  │        │cantidad    │        │tipo      │
└────┬─────┘        └────────────┘        │color     │
     │                                    │rareza    │
     │1                                   │imagen_url│
     │                                    │texto     │
     ▼N                                   │coste     │
┌──────────┐        ┌────────────┐        │poder     │
│   DECK   │        │ DECK_CARTA │        │contador  │
│──────────│1      N│────────────│N      1│subtipos  │
│id_deck   ├────────┤id_deck     ├────────┤atributo  │
│id_usuario│        │id_carta    │        └──────────┘
│nombre    │        │cantidad    │
│colores   │        └────────────┘
└──────────┘

┌──────────┐
│ EVENTOS  │
│──────────│
│id_usuario│──────── FK → USUARIO
│nombre    │
│fecha     │
│lugar     │
│favorito  │
└──────────┘
```

### 6.3 Descripción de tablas

#### Tabla `carta`
Catálogo completo de cartas del juego. Se importa desde fuentes externas y no la modifica el usuario.

| Columna | Tipo | Descripción |
|---|---|---|
| id_carta | VARCHAR (PK) | Identificador único. Formato: `OP01-001`, `EB01-012`… |
| nombre | VARCHAR | Nombre de la carta |
| tipo | VARCHAR | LIDER, PERSONAJE, EVENTO o STAGE |
| color | VARCHAR | Color(es) de la carta. Puede ser compuesto: `RED/GREEN` |
| rareza | VARCHAR | C, UC, R, SR, SEC, L, P o SP |
| imagen_url | VARCHAR | URL pública de la imagen de la carta |
| texto | VARCHAR | Texto de habilidad (puede ser NULL) |
| coste | INTEGER | Coste en puntos de vida para jugar (NULL en líderes) |
| poder | INTEGER | Poder de combate (NULL en eventos/stages) |
| contador | INTEGER | Valor del counter (NULL si no tiene) |
| subtipos | VARCHAR | Subtipos de la carta (NULL si no tiene) |
| atributo | VARCHAR | Atributo especial (NULL si no tiene) |

#### Tabla `usuario`

| Columna | Tipo | Descripción |
|---|---|---|
| id_usuario | SERIAL (PK) | Identificador auto-incremental |
| nombre | VARCHAR (UNIQUE) | Nombre de usuario único en el sistema |
| password | VARCHAR | Hash BCrypt de la contraseña |

#### Tabla `coleccion`
Registra qué cartas posee cada usuario.

| Columna | Tipo | Descripción |
|---|---|---|
| id_usuario | INTEGER (FK) | Referencia a `usuario` |
| id_carta | VARCHAR (FK) | Referencia a `carta` |
| cantidad | INTEGER | Número de copias físicas poseídas |

Constraint: `ON CONFLICT DO NOTHING` — una carta no puede duplicarse por usuario.

#### Tabla `deck`

| Columna | Tipo | Descripción |
|---|---|---|
| id_deck | SERIAL (PK) | Identificador auto-incremental |
| id_usuario | INTEGER (FK) | Dueño del mazo |
| nombre | VARCHAR | Nombre del mazo |
| colores | VARCHAR | Colores en hex separados por coma: `#e74c3c,#3498db` |

#### Tabla `deck_carta`
Cartas incluidas en cada mazo con su cantidad.

| Columna | Tipo | Descripción |
|---|---|---|
| id_deck | INTEGER (FK) | Referencia a `deck` |
| id_carta | VARCHAR (FK) | Referencia a `carta` |
| cantidad | INTEGER | Copias incluidas en el mazo (1–4) |

Constraint: `ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad` — permite actualizar la cantidad sin duplicar.

#### Tabla `eventos`

| Columna | Tipo | Descripción |
|---|---|---|
| id_usuario | INTEGER (FK) | Usuario que creó el evento |
| nombre | VARCHAR | Nombre del evento o torneo |
| fecha | VARCHAR | Fecha en formato `dd/MM/yyyy` |
| lugar | VARCHAR | Lugar de celebración |
| favorito | BOOLEAN | Si el usuario lo ha marcado como favorito |

### 6.4 Decisiones de diseño de la BD

- **Supabase en la nube**: Permite que los tres miembros del equipo trabajen contra la misma base de datos sin instalar nada. En producción, también permite a distintos usuarios de la app compartir la misma BD.
- **Colores como CSV en `deck.colores`**: Los colores del mazo se almacenan como cadena de hexadecimales separados por coma (`#e74c3c,#3498db`). Es una simplificación deliberada para evitar una tabla extra de relación mazo-colores que habría complicado las consultas sin beneficio práctico dado el número máximo de colores (2).
- **Carga global de cartas en memoria**: En lugar de consultar la BD cada vez que el usuario filtra, todas las cartas se cargan al arrancar en `App.todasLasCartas`. El filtrado posterior es puramente en memoria, lo que hace la búsqueda instantánea independientemente del tamaño del catálogo.

---

## 7. DESARROLLO E IMPLEMENTACIÓN

### 7.1 Organización del trabajo en equipo

El proyecto se ha desarrollado usando Git con dos ramas principales: `main` (código estable) y `por-si-se-rompe` (desarrollo activo). El trabajo se ha organizado por módulos funcionales, asignando a cada miembro responsabilidades sobre distintas partes de la aplicación, con revisiones y fusiones periódicas.

### 7.2 Flujo de arranque de la aplicación

El punto de entrada es `App.java`, que extiende `javafx.application.Application`. Al arrancar:

1. Se lanza un **hilo secundario** que carga todas las cartas de la BD en la lista estática `App.todasLasCartas`. Esto evita bloquear la UI mientras se conecta con Supabase.
2. En el hilo principal se carga `login.fxml` y se muestra la ventana en modo `UNDECORATED` (sin barra nativa de Windows), con controles propios de ventana.

```java
@Override
public void start(Stage stage) throws Exception {
    new Thread(App::cargarDatosGlobales).start();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
    Scene scene = new Scene(loader.load(), 900, 600);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setTitle("One Piece Collectr");
    stage.setScene(scene);
    stage.show();
}
```

### 7.3 Gestión de la conexión a la base de datos

La clase `Login` centraliza la conexión JDBC. El método estático `getConexion()` implementa un patrón singleton: devuelve la conexión existente si está abierta, o la crea si no existe o ha sido cerrada. Esto evita abrir una nueva conexión en cada operación.

```java
public static Connection getConexion() {
    try {
        if (conexion == null || conexion.isClosed()) {
            conectar();
        }
    } catch (SQLException e) {
        registrarEnLog("Error al verificar conexión: " + e.getMessage());
    }
    return conexion;
}
```

### 7.4 Autenticación segura con BCrypt

Al registrar un usuario, la contraseña nunca se almacena en texto plano. Se genera un hash BCrypt con sal aleatoria:

```java
String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
// INSERT INTO usuario (nombre, password) VALUES (?, ?)
```

Al iniciar sesión, se verifica la contraseña contra el hash almacenado:

```java
if (BCrypt.checkpw(passwordIntroducido, hashGuardado)) {
    return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre"));
}
```

### 7.5 Paginación virtual de la colección

Con más de 3.100 cartas, renderizar todas a la vez bloquearía la interfaz y consumiría demasiada memoria. Implementamos una **paginación virtual basada en scroll**:

- Solo se renderizan las primeras 50 cartas al cargar la vista.
- Un listener detecta cuando el usuario llega al 85% del scroll y carga las siguientes 50.
- Un sistema de "tickets" (contador incremental) cancela renders obsoletos si el usuario cambia el filtro mientras carga.

```java
scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal.doubleValue() > 0.85) cargarMasCartas();
});
```

### 7.6 Filtrado en memoria

El filtrado se realiza íntegramente en memoria sobre `cartasCargadas` (copia local de `App.todasLasCartas`). Cada vez que cambia cualquier filtro, se recorre la lista completa aplicando todos los criterios en cascada:

```java
for (Carta c : cartasCargadas) {
    if (/* filtro color mazo */) continue;
    if (/* filtro texto */) continue;
    if (/* filtro posesión */) continue;
    if (/* filtros numéricos */) continue;
    if (/* filtro tipo */) continue;
    if (/* filtro set */) continue;
    if (/* filtro color libre */) continue;
    if (/* filtro rareza */) continue;
    filtradas.add(c);
}
```

Este enfoque es posible porque las cartas ya están en RAM, haciendo la búsqueda prácticamente instantánea.

### 7.7 Caché de imágenes

Las URLs de imagen de las cartas apuntan a servidores externos. Para no descargar la misma imagen múltiples veces, usamos un `HashMap` como caché:

```java
private static final Map<String, Image> imagenCache = new HashMap<>();

public static Image getImagen(String url) {
    return imagenCache.computeIfAbsent(url, u ->
        new Image(u, 105, 145, true, true, true));
}
```

El último parámetro `true` de `Image` activa la carga asíncrona: la UI no se bloquea mientras se descarga la imagen.

### 7.8 Validación de reglas del TCG

La lógica de validación del One Piece TCG está implementada en `CardDetailController` y `ColeccionController`. Las reglas son:

| Regla | Implementación |
|---|---|
| Máximo 4 copias de una carta normal | `MAX_COPIAS_NORMAL = 4` |
| Máximo 1 copia del líder | `MAX_COPIAS_LIDER = 1` |
| Solo 1 líder por mazo | Se comprueba si ya hay un líder distinto antes de añadir |
| Máximo 51 cartas por mazo | `MAX_CARTAS_MAZO = 51` |
| Solo 1 o 2 colores por mazo | Listener que desmarca el 3.er color si se seleccionan más de 2 |
| Colores del líder → colores del mazo | Al añadir un líder, `actualizarColoresMazoSegunLider()` parsea sus colores y actualiza el mazo |

```java
private int calcularDeltaPermitido(int deltaDeseado) {
    // 1. Regla líder único
    if (esLider() && hayLiderDistinto()) { mostrarAlerta(...); return 0; }
    // 2. Límite de copias de la carta
    int maxPorCopia = maxCopias() - cantidadEnMazo;
    // 3. Hueco disponible en el mazo
    int hueco = MAX_CARTAS_MAZO - mazo.getCartas().size();
    // Devuelve el mínimo entre lo pedido, el límite de copia y el hueco
    return Math.min(deltaDeseado, Math.min(maxPorCopia, hueco));
}
```

### 7.9 Sistema de estilos y problema con modena.css

JavaFX carga por defecto `modena.css` como hoja de estilos de usuario-agente con alta prioridad. Esto hace que los estilos definidos en CSS propio sean ignorados en muchos componentes (botones, ComboBox, CheckBox).

La solución que adoptamos fue aplicar los estilos directamente mediante `setStyle()` en Java, que tiene la máxima prioridad en JavaFX:

```java
// En lugar de CSS, aplicamos el estilo directamente en Java
for (Button b : new Button[]{btnDashboard, btnColeccion, ...}) {
    b.setStyle("-fx-background-color: #e8c96d; -fx-text-fill: #0d1b2a; ...");
    b.setOnMouseEntered(e -> b.setStyle(BTN_HOVER));
    b.setOnMouseExited(e -> b.setStyle(BTN_STYLE));
}
```

Para componentes que no se pueden controlar fácilmente en Java (scrollbars, ComboBox interno, DatePicker), cargamos `sidebar.css` directamente en la escena:

```java
sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
    if (newScene != null) {
        String css = getClass().getResource("/view/sidebar.css").toExternalForm();
        if (!newScene.getStylesheets().contains(css))
            newScene.getStylesheets().add(css);
    }
});
```

### 7.10 Problemas encontrados y soluciones

| Problema | Causa | Solución |
|---|---|---|
| `NullPointerException` al filtrar cartas | Campos opcionales de la `Carta` (color, texto, subtipos…) eran `null` y se llamaba `.toUpperCase()` sobre ellos | Null-guard en todos los campos antes de comparar |
| Los estilos CSS no se aplicaban | `modena.css` tenía mayor prioridad que los estilos del proyecto | Usar `setStyle()` en Java para los botones críticos; `sidebar.css` en escena para el resto |
| La paginación fallaba al buscar rápido | Dos búsquedas rápidas consecutivas producían renders entremezclados | Sistema de tickets (contador `ticketBusqueda`) que cancela renders de búsquedas obsoletas |
| Clases `.class` desactualizadas en el IDE | El IDE compilaba contra `.class` antiguos que no tenían el import añadido | `mvn clean compile` para forzar recompilación completa |
| La imagen de fondo del login no cargaba | Ruta incorrecta al recurso | Usar `getClass().getResource()` con ruta absoluta desde `resources/` |
| Cartas multicolor no filtraban bien | El campo `color` puede ser `"RED/GREEN"` y el filtro buscaba coincidencia exacta | Usar `.contains()` en lugar de `.equals()` para el color |

---

## 8. INTERFAZ GRÁFICA

### 8.1 Paleta de colores y diseño

La interfaz sigue una paleta de colores oscura inspirada en el universo de One Piece:

| Elemento | Color | Hex |
|---|---|---|
| Fondo principal | Azul marino oscuro | `#0d1b2a` |
| Paneles y tarjetas | Azul marino medio | `#1a2c42` |
| Bordes y separadores | Azul marino claro | `#2e4a6b` |
| Texto principal | Azul muy claro | `#c8dce8` |
| Acento azul | Azul cielo | `#7fb3d3` |
| Sidebar y botones principales | Dorado | `#e8c96d` |
| Hover de botones dorados | Dorado oscuro | `#c9a84c` |
| Carta en mazo (dentro) | Verde oscuro | `#27ae60` |
| Carta en límite | Rojo oscuro | `#c0392b` |

Los colores comunican información de estado: una carta con **borde dorado** es poseída, con **borde verde** está en el mazo activo y aún tiene hueco, con **borde rojo y opacidad reducida** ha alcanzado el límite de copias.

### 8.2 Pantallas de la aplicación

#### Pantalla de Login
Ventana de 900×600 píxeles con imagen de fondo de One Piece. Sobre ella, un formulario en tarjeta beige con campos de usuario y contraseña. Incluye botón de inicio de sesión y enlace para crear cuenta. Los controles de ventana (−/□/✕) flotan en la esquina superior derecha.

#### Dashboard (Home)
Pantalla principal tras el login. Muestra tres tarjetas de estadísticas:
- Cartas en colección (número total de cartas poseídas).
- Mazos creados (número de mazos del usuario).
- Eventos próximos (número de eventos registrados).

Y una sección de "Próximo evento" con el nombre y fecha del siguiente evento registrado.

#### Colección
Vista más compleja de la aplicación. Incluye:
- Barra de búsqueda por texto libre.
- Panel de filtros colapsable con 8 criterios de filtrado.
- Cuadrícula de cartas con imagen, nombre y estado visual (poseída/no poseída/en mazo/límite).
- Paginación virtual por scroll infinito.
- Contador del mazo activo en la esquina superior derecha.

#### Mazos
Dividida en dos fases:
1. **Lista de mazos**: muestra todos los mazos del usuario como botones con gradiente de color. Permite crear nuevos mazos con nombre y hasta 2 colores.
2. **Detalle del mazo**: cuadrícula de las cartas del mazo con multiplicador (xN), opciones de ordenación, contador de cartas y botones para editar cantidades o eliminar cartas.

#### Eventos
Lista de eventos con tarjetas de color según urgencia. Formulario para crear/editar eventos con DatePicker que bloquea fechas pasadas. Botón de toggle para ver solo favoritos.

#### Mercado
Panel de selección de cartas para exportar a CardTrader. Muestra un `TextArea` con los IDs de las cartas seleccionadas y un botón para abrir la web de CardTrader directamente desde la aplicación.

### 8.3 Navegación

La barra lateral (sidebar) es colapsable: al pulsar el botón ☰, desaparece completamente y aparece un botón flotante en la esquina superior izquierda para volver a mostrarla. La aplicación arranca con el sidebar oculto para maximizar el espacio de contenido.

---

## 9. PRUEBAS

### 9.1 Pruebas de autenticación

| ID | Prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P-01 | Iniciar sesión con credenciales correctas | Acceso a la aplicación | ✅ Correcto |
| P-02 | Iniciar sesión con contraseña incorrecta | Mensaje de error | ✅ Correcto |
| P-03 | Iniciar sesión con campos vacíos | Mensaje de error | ✅ Correcto |
| P-04 | Registrar usuario nuevo | Cuenta creada correctamente | ✅ Correcto |
| P-05 | Registrar usuario con nombre ya existente | Mensaje de error | ✅ Correcto |
| P-06 | Las contraseñas se almacenan hasheadas | Hash BCrypt en BD | ✅ Verificado en BD |

### 9.2 Pruebas de colección

| ID | Prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P-07 | Click izquierdo sobre carta no poseída | Carta marcada como poseída | ✅ Correcto |
| P-08 | Click derecho sobre carta poseída | Carta eliminada de la colección | ✅ Correcto |
| P-09 | Búsqueda por texto | Solo se muestran cartas que coinciden | ✅ Correcto |
| P-10 | Filtro por tipo "LIDER" | Solo se muestran líderes | ✅ Correcto |
| P-11 | Múltiples filtros simultáneos | Intersección correcta de resultados | ✅ Correcto |
| P-12 | Limpiar filtros | Todos los combos vuelven a "Todos" | ✅ Correcto |
| P-13 | Scroll al 85% | Se cargan 50 cartas adicionales | ✅ Correcto |

### 9.3 Pruebas de mazos

| ID | Prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P-14 | Crear mazo sin color | Mensaje de error | ✅ Correcto |
| P-15 | Seleccionar 3 colores | El tercero se desmarca automáticamente | ✅ Correcto |
| P-16 | Añadir una 5.ª copia de una carta | Bloqueado con mensaje | ✅ Correcto |
| P-17 | Añadir un segundo líder | Bloqueado con mensaje | ✅ Correcto |
| P-18 | Añadir carta 52 al mazo | Bloqueado con mensaje | ✅ Correcto |
| P-19 | Añadir líder multicolor | Los colores del mazo se actualizan | ✅ Correcto |
| P-20 | Eliminar carta del mazo | Carta eliminada y contador actualizado | ✅ Correcto |
| P-21 | Renombrar mazo (doble click) | Nombre actualizado en BD y en UI | ✅ Correcto |
| P-22 | Eliminar mazo con confirmación | Mazo eliminado de BD y de la lista | ✅ Correcto |
| P-23 | Exportar mazo a HTML | Archivo HTML generado y abierto en navegador | ✅ Correcto |

### 9.4 Pruebas de eventos

| ID | Prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P-24 | Crear evento con todos los campos | Evento guardado y visible | ✅ Correcto |
| P-25 | Crear evento con campos vacíos | Mensaje de aviso | ✅ Correcto |
| P-26 | Seleccionar fecha pasada en DatePicker | Fecha bloqueada (en rosa) | ✅ Correcto |
| P-27 | Marcar evento como favorito | Estado actualizado en BD | ✅ Correcto |
| P-28 | Filtrar solo favoritos | Solo se muestran los marcados | ✅ Correcto |
| P-29 | Color de evento pasado | Tarjeta en rojo | ✅ Correcto |
| P-30 | Color de evento en 3 días | Tarjeta en amarillo | ✅ Correcto |

### 9.5 Pruebas de rendimiento

| ID | Prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P-31 | Carga inicial de ~3.100 cartas | No bloquea la UI | ✅ Hilo secundario correcto |
| P-32 | Búsqueda en tiempo real | Resultado inmediato (<100ms) | ✅ Filtrado en memoria |
| P-33 | Navegación entre secciones | Sin retardo perceptible | ✅ Caché de vistas |

---

## 10. CONCLUSIONES

### 10.1 Valoración del proyecto

OnePieceCollectr ha cumplido todos los objetivos planteados al inicio del proyecto. Hemos desarrollado una aplicación funcional, estable y visualmente cuidada que resuelve un problema real. Durante el proceso hemos aplicado prácticamente todos los conocimientos adquiridos a lo largo del ciclo de DAM.

### 10.2 Aprendizajes técnicos

- **JavaFX**: Hemos aprendido a trabajar con FXML, Scene Builder, CSS en JavaFX, el sistema de layout (StackPane, BorderPane, VBox, HBox, GridPane), listeners, bindings y el ciclo de vida de los controladores.
- **Patrones de diseño**: La implementación del patrón MVC y el uso del patrón singleton para la conexión a BD han mejorado la organización y mantenibilidad del código.
- **Base de datos relacional**: Hemos diseñado el esquema de la BD, escrito consultas SQL con joins, inserts con ON CONFLICT y updates. La conexión mediante JDBC nos ha dado experiencia real con el acceso a datos desde Java.
- **Seguridad**: La implementación de BCrypt para contraseñas nos ha concienciado de la importancia de no almacenar contraseñas en texto plano, algo que en el mundo profesional es imprescindible.
- **Git y trabajo en equipo**: Hemos aprendido a coordinar el desarrollo entre tres personas usando ramas, commits descriptivos y resolución de conflictos.
- **Optimización**: El sistema de paginación virtual y la caché de imágenes nos han enseñado a pensar en el rendimiento desde el diseño, no como un parche posterior.

### 10.3 Aprendizajes no técnicos

- **Gestión de un proyecto real**: Planificar módulos, repartir trabajo, respetar plazos y adaptarse a los imprevistos (errores inesperados, cambios de requisitos) forma parte del día a día del desarrollo de software.
- **Resolución de problemas**: Varios de los problemas encontrados (CSS de JavaFX, paginación con búsqueda rápida, campos NULL en la BD) no tenían solución en los apuntes del ciclo y requirieron investigación autónoma y pensamiento crítico.
- **Documentación**: Documentar el proyecto —tanto el código como la memoria— nos ha obligado a entender bien lo que habíamos hecho y a ser capaces de explicarlo con claridad.

---

## 11. TRABAJO FUTURO

La aplicación, tal como está, es funcional y cubre las necesidades básicas de un coleccionista de One Piece TCG. Sin embargo, hay muchas mejoras y extensiones posibles:

| Mejora | Descripción |
|---|---|
| **Aplicación móvil** | Portar la lógica a Android usando el mismo backend en Supabase. |
| **Sincronización en tiempo real** | Usar las capacidades de Supabase Realtime para ver actualizaciones de otros usuarios al instante. |
| **Marketplace integrado** | Implementar la funcionalidad de compra/venta entre usuarios dentro de la propia app (el módulo de Mercado está parcialmente preparado para esto). |
| **Estadísticas avanzadas** | Gráficos de distribución de la colección por set, tipo, color y rareza. Historial de adquisiciones. |
| **Importación por cámara** | Reconocimiento de cartas físicas mediante la cámara del móvil usando OCR o ML Kit. |
| **API propia** | Desarrollar una API REST propia con Spring Boot que sirva los datos de cartas, eliminando la dependencia de la conexión directa a Supabase. |
| **Sistema de torneos** | Gestión completa de torneos: inscripciones, emparejamientos, resultados y clasificación. |
| **Actualizaciones automáticas del catálogo** | Un importador automático que descargue las cartas de nuevos sets al lanzarse. |
| **Multiidioma** | Internacionalización de la interfaz para soportar inglés, francés y japonés. |

---

## 12. BIBLIOGRAFÍA Y WEBGRAFÍA

### Documentación oficial

- **Java 17 API Documentation** — docs.oracle.com/en/java/javase/17/docs/api/
- **JavaFX 17 Documentation** — openjfx.io/javadoc/17/
- **JavaFX CSS Reference** — openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html
- **PostgreSQL 15 Documentation** — postgresql.org/docs/15/
- **Supabase Documentation** — supabase.com/docs
- **Apache Maven Documentation** — maven.apache.org/guides/

### Librerías utilizadas

- **jBCrypt** — github.com/jeremyh/jBCrypt
- **PostgreSQL JDBC Driver** — jdbc.postgresql.org/documentation/
- **OpenJFX** — openjfx.io

### Recursos de aprendizaje

- **Jenkov.com — JavaFX Tutorial** — jenkov.com/tutorials/javafx/
- **Baeldung — Java & Spring Guides** — baeldung.com
- **Stack Overflow** — stackoverflow.com (resolución de problemas específicos de JavaFX, JDBC y PostgreSQL)
- **GitHub Docs — Git Handbook** — docs.github.com/en/get-started/using-git

### Datos del juego

- **One Piece Card Game — Catálogo oficial** — en.onepiece-cardgame.com
- **CardTrader — Marketplace** — cardtrader.com

---

## 13. ANEXOS

### Anexo A — Repositorio del proyecto

El código fuente completo del proyecto está disponible en GitHub:
`https://github.com/Diegapio/OnePieceCollectr`

### Anexo B — Estructura del repositorio

```
OnePieceCollectr/
├── tfg/                          ← Proyecto Maven principal
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/onepiececollectr/
│           │   ├── App.java
│           │   ├── Login.java
│           │   ├── Principal.java
│           │   ├── DashboardController.java
│           │   ├── ColeccionController.java
│           │   ├── MazosController.java
│           │   ├── CardDetailController.java
│           │   ├── EventosController.java
│           │   ├── MarketController.java
│           │   └── [modelos: Carta, Deck, Event, Mercado, Usuario…]
│           └── resources/
│               ├── view/
│               │   ├── login.fxml
│               │   ├── principal.fxml
│               │   ├── dashboard.fxml
│               │   ├── coleccion.fxml
│               │   ├── mazos.fxml
│               │   ├── deckDetail.fxml
│               │   ├── cardPopup.fxml
│               │   ├── eventos.fxml
│               │   ├── mercado.fxml
│               │   └── sidebar.css
│               └── cards/
│                   └── background.png
├── importadorcartas/             ← Script de importación del catálogo
├── DOCUMENTACION.md              ← Documentación técnica interna
└── .gitignore
```

### Anexo C — Script SQL de creación de tablas

```sql
-- Tabla de usuarios
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre     VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL
);

-- Catálogo de cartas
CREATE TABLE carta (
    id_carta   VARCHAR(20) PRIMARY KEY,
    nombre     VARCHAR(200) NOT NULL,
    tipo       VARCHAR(20),
    color      VARCHAR(50),
    rareza     VARCHAR(10),
    imagen_url VARCHAR(500),
    texto      TEXT,
    coste      INTEGER,
    poder      INTEGER,
    contador   INTEGER,
    subtipos   VARCHAR(200),
    atributo   VARCHAR(100)
);

-- Colección de cada usuario
CREATE TABLE coleccion (
    id_usuario INTEGER REFERENCES usuario(id_usuario),
    id_carta   VARCHAR(20) REFERENCES carta(id_carta),
    cantidad   INTEGER DEFAULT 1,
    PRIMARY KEY (id_usuario, id_carta)
);

-- Mazos
CREATE TABLE deck (
    id_deck    SERIAL PRIMARY KEY,
    id_usuario INTEGER REFERENCES usuario(id_usuario),
    nombre     VARCHAR(100) NOT NULL,
    colores    VARCHAR(200)
);

-- Cartas de cada mazo
CREATE TABLE deck_carta (
    id_deck  INTEGER REFERENCES deck(id_deck) ON DELETE CASCADE,
    id_carta VARCHAR(20) REFERENCES carta(id_carta),
    cantidad INTEGER DEFAULT 1,
    PRIMARY KEY (id_deck, id_carta)
);

-- Eventos y torneos
CREATE TABLE eventos (
    id_usuario INTEGER REFERENCES usuario(id_usuario),
    nombre     VARCHAR(200) NOT NULL,
    fecha      VARCHAR(10),
    lugar      VARCHAR(200),
    favorito   BOOLEAN DEFAULT FALSE
);
```

### Anexo D — Reglas oficiales del One Piece TCG implementadas

| Regla | Valor |
|---|---|
| Cartas por mazo | Mínimo 50, máximo 51 (50 + 1 líder) |
| Copias máximas de carta normal | 4 |
| Copias máximas del líder | 1 |
| Líderes por mazo | 1 |
| Colores por mazo | 1 o 2 |
| Colores válidos | Rojo, Azul, Verde, Amarillo, Morado, Negro |

---

*Documento elaborado por Diego Cabello Del Río, Javier Palomar Sanz e Iker Núñez Berrocal — IES Gaspar Melchor de Jovellanos — 2.º DAM 2025/2026*
