# FESC Synapse — Red Colaborativa de Estudio en Tiempo Real

**FESC Synapse** es una aplicación móvil Android diseñada para conectar en tiempo real a estudiantes universitarios dentro del campus que están preparando las mismas materias o exámenes parciales. Elimina la frustración de estudiar en solitario permitiendo localizar mesas de estudio activas en la biblioteca o áreas comunes y emitir alertas urgentes ("SOS de Parcial") para recibir ayuda inmediata de compañeros cercanos.

---

## Decisiones Confirmadas y Revisión del Usuario

> [!IMPORTANT]
> **Decisiones de Producto Confirmadas:**
> - **Descubrimiento Espacial en el Campus:** Mapa interactivo de zonas del campus (Biblioteca, Cubículos, Áreas Comunes, Laboratorios, Cafetería) con indicadores de actividad en vivo, filtro por materia y estado de estudio de cada grupo.
> - **Herramientas Nucleares de Colaboración:**
>   1. **Mesas de Estudio Abiertas con Cupos en Vivo:** Creación, exploración y reserva de asiento en mesas presenciales con indicador de capacidad actual/máxima y nivel de enfoque.
>   2. **Alertas SOS de Dudas Urgentes por Materia:** Emisión de señales de ayuda prioritaria con cuenta regresiva para el parcial, ubicación exacta en el campus y canal rápido de respuesta/asistencia ("¡Voy en camino a explicarte!" o respuesta directa).
> - **Identidad Visual:** Estética **Moderno Académico Oscuro** (*Deep Synapse Dark*) con superficies azul medianoche profundo y acentos vibrantes (cian eléctrico para conexiones/mesas, violeta sináptico para materias y coral neón para alertas SOS urgentes).
> - **Persistencia y Sincronización en Tiempo Real (Recomendado):** Respaldo en la nube mediante **Firebase Firestore** y **Autenticación con Google** (Credential Manager) para que las mesas de estudio, cupos ocupados y alertas SOS se actualicen instantáneamente entre todos los estudiantes del campus.

---

## 1. Visión General y Concepto Central

- **Qué Hace:** Permite a cualquier estudiante en el campus visualizar en segundos quién está estudiando su misma materia, en qué zona y mesa exacta se encuentra, cuántos cupos libres quedan en esa mesa, y quién necesita ayuda urgente con un tema específico antes de un examen parcial.
- **Público Objetivo / Persona:** Estudiantes universitarios de la FESC (Ingeniería de Software, Diseño Gráfico, Administración, Negocios Internacionales, Logística, Financiera, etc.) que se encuentran en la biblioteca, pasillos o zonas de estudio preparando quices, talleres o parciales.
- **Valor Principal:** Transforma el estudio individual y estresante previo a un examen en una red de apoyo presencial e inmediata, conectando a quien tiene una duda puntual con el compañero que está a pocos metros estudiando el mismo tema.

---

## 2. Experiencia de Usuario y Diseño Visual

### Flujos Principales de Usuario

1. **Ingreso y Estado Sináptico Rápido:**
   - Autenticación rápida con cuenta de Google y configuración de perfil estudiantil (carrera, semestre y materias activas).
   - Selector de estado en vivo en la cabecera: *"En el campus — Buscando grupo"*, *"En mesa abierta — Acepto compañeros"*, *"Modo Mentor — Disponible para resolver dudas"*, o *"En alerta SOS"*.
2. **Mapa Interactivo del Campus (Radar Sináptico):**
   - Lienzo interactivo estilizado del campus FESC dividido por nodos arquitectónicos (**Biblioteca Central**, **Salas de Cómputo / Bloque A**, **Áreas Comunes / Plazoleta**, **Cubículos de Estudio**, **Cafetería Central**).
   - Cada zona brilla con anillos de pulso según la cantidad de mesas activas y alertas SOS vigentes. Al tocar una zona o filtrar por materia (ej. *Cálculo Diferencial*, *Estructura de Datos*, *Contabilidad*, *Estadística*), se despliega el panel inferior con las mesas y estudiantes en ese punto.
3. **Mesas de Estudio Abiertas (Cupos en Vivo):**
   - Tarjetas detalladas con: Materia, tema específico de estudio (ej. *"Repaso integrales por partes para parcial de las 6:00 PM"*), zona y referencia física (ej. *"Biblioteca - Mesa 4 junto al ventanal"*), ritmo de estudio (*Debate activo*, *Práctica de ejercicios*, *Resolución de taller*) y barra visual de cupos (`3/5 cupos ocupados`).
   - Acción de un toque **"Unirme a la Mesa"** (o **"Salir de la Mesa"**) que actualiza el cupo en tiempo real y abre el hilo de coordinación de la mesa.
4. **Alertas SOS de Dudas Urgentes:**
   - Botón de acción rápida **"Lanzar SOS"** donde el estudiante selecciona la materia, escribe su duda urgente (ej. *"No entiendo cómo balancear el flujo de caja en el punto 3 del taller"*), indica en cuánto tiempo es su parcial (`En 1 hora`, `En 3 horas`, `Mañana`) y su ubicación exacta.
   - Otros estudiantes ven la alerta destacada con borde coral pulsante, pueden enviar respuestas paso a paso en el hilo de la duda o pulsar **"Voy a ayudarte en persona"** para confirmar asistencia presencial, y el autor puede marcar la duda como **"¡Resuelta!"**.

### Identidad Visual y Tema (*Deep Synapse Dark*)

- **Dirección Estética:** **Moderno Académico Oscuro (Cyber-Academic & Tactical Focus)**. Inspirado en tableros de comando modernos y redes neuronales ("Synapse"), combinando alta legibilidad para sesiones nocturnas o de biblioteca con indicadores luminosos de actividad en tiempo real.
- **Paleta de Colores y Atmósfera:**
  - `--background`: `#0A0E17` (Obsidiana Académica — fondo principal de alto descanso visual).
  - `--surface` / `--card`: `#121826` y `#1A2234` (Pizarra Nocturna con bordes sutiles `#26324B`).
  - `--primary`: `#00E5FF` (Cian Sináptico — nodos del mapa, mesas activas, botones principales y enlaces de conexión).
  - `--secondary`: `#8B5CF6` (Violeta Eléctrico — etiquetas de materias, facultades y modo mentor).
  - `--urgent-sos`: `#FF3366` (Coral Neón — alertas SOS urgentes, cuentas regresivas de parciales).
  - `--success`: `#10B981` (Verde Esmeralda — cupos disponibles, dudas resueltas).
  - `--text-primary`: `#F8FAFC` / `--text-secondary`: `#94A3B8`.
- **Tipografía y Jerarquía:**
  - **Encabezados y Títulos (`Space Grotesk`):** Geométrica, tecnológica y contundente para nombres de materias, contadores de cupos y títulos de zonas del campus.
  - **Cuerpo y Lectura (`Plus Jakarta Sans`):** Limpia y altamente legible para descripciones de dudas, comentarios de estudio y referencias de ubicación.
- **Estilo de Componentes y Layout:**
  - **Mapa de Campus en Canvas Interactivo:** Grafo visual de zonas conectadas por líneas sinápticas animadas, con zoom/selección táctil de edificio, badges flotantes con el número de mesas y alertas SOS en cada zona.
  - **Navegación Inferior M3 con Safe Areas:** Barra de navegación con tres vistas principales (**Mapa & Radar**, **Mesas en Vivo**, **Alertas SOS**) más acceso al **Perfil Académico**, respetando `WindowInsets.navigationBars` y `WindowInsets.safeDrawing`.
- **Feedback Interactivo y Movimiento:**
  - Ondas de pulso animadas en el mapa para zonas con alertas SOS activas.
  - Transiciones fluidas al reservar un cupo en una mesa de estudio con indicador visual inmediato de los avatares de los integrantes.

---

## 3. Decisiones Clave de Producto y Trade-Offs

- **Decisión 1: Mapa Esquematizado Interactivo del Campus en lugar de Mapa GPS Satelital Genérico**
  - *Enfoque Elegido:* Un mapa interactivo personalizado en Jetpack Compose Canvas que representa las zonas interiores reales del campus universitario (Biblioteca, Cubículos, Laboratorios, Cafetería, Plazoleta) con pines táctiles, conexiones sinápticas y tarjetas de detalle.
  - *Por Qué:* Dentro de un campus universitario, las coordenadas GPS estándar no distinguen entre el segundo piso de la biblioteca, los cubículos o la cafetería debajo, y suelen requerir claves de Google Maps externas. Un mapa esquemático por zonas permite identificar al instante el ambiente de estudio, tocar cada edificio y ver qué mesas y alertas SOS están activas allí mismo.
  - *Alternativas Consideradas:* Mapa vial tradicional (descartado porque no muestra espacios interiores ni mesas de biblioteca con claridad).

- **Decisión 2: Sincronización Colaborativa en Tiempo Real con Firebase Firestore + Google Sign-In**
  - *Enfoque Elegido:* Almacenamiento en la nube con **Firebase Firestore** usando flujos en tiempo real (`snapshots()`) y autenticación con **Google Sign-In** (Credential Manager).
  - *Por Qué:* Al ser una red colaborativa entre estudiantes, cuando alguien abre una mesa de estudio, ocupa un cupo o lanza una alerta SOS antes de un parcial, todos los demás compañeros deben verlo reflejado en vivo y poder responder o unirse con su identidad verificada.
  - *Alternativas Consideradas:* Base de datos puramente local en el dispositivo (descartada porque impediría la colaboración multiusuario y el propósito social de la app).

---

## 4. Arquitectura Técnica y Estrategia de Datos *(Referencia Técnica)*

### Diagrama de Arquitectura y Componentes

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        CAPA DE PRESENTACIÓN (Compose)                      │
│                                                                            │
│  ┌──────────────────────┐  ┌────────────────────────────────────────────┐  │
│  │     AuthGateScreen   │  │              MainSynapseScaffold           │  │
│  │  (Google Sign-In +   │  │  ┌────────────┬────────────┬────────────┐  │  │
│  │   Bienvenida FESC)   │  │  │ CampusMap  │ StudyTables│  SosRadar  │  │  │
│  └──────────┬───────────┘  │  │   Screen   │   Screen   │   Screen   │  │  │
│             │              │  └─────┬──────┴─────┬──────┴─────┬──────┘  │  │
│             │              │        └────────────┼────────────┘         │  │
│             │              │                     ▼                      │  │
│             │              │  ┌──────────────────────────────────────┐  │  │
│             │              │  │ Modales: Crear Mesa / Lanzar SOS /   │  │  │
│             │              │  │ Detalle de Mesa & Chat de Duda SOS   │  │  │
│             │              │  └──────────────────┬───────────────────┘  │  │
│             │              └─────────────────────┼──────────────────────┘  │
└─────────────┼────────────────────────────────────┼─────────────────────────┘
              │                                    │
              ▼                                    ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE ESTADO Y LÓGICA (ViewModel)                    │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                           SynapseViewModel                           │  │
│  │  • Flujos reactivos (StateFlow<UiState<...>>) de Mesas, Alertas SOS  │  │
│  │  • Filtros combinados por Zona del Campus, Materia y Urgencia        │  │
│  │  • Acciones: crearMesa, unirseAMesa, salirDeMesa, crearAlertaSos,    │  │
│  │    responderSos, confirmarAsistenciaPresencial, marcarSosResuelto    │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                   CAPA DE DATOS Y PERSISTENCIA (Firestore)                 │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                          SynapseRepository                           │  │
│  │  • Colección `/study_tables/{tableId}` (Cupos, zona, materia, host)  │  │
│  │  • Colección `/sos_alerts/{alertId}` (Duda, urgencia, respuestas)    │  │
│  │  • Colección `/users/{userId}` (Perfil académico, materias, estado)  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Modelo de Datos y Entidades

1. **Perfil de Estudiante (`StudentProfile` — `/users/{userId}`):**
   - Identificador (`uid`), nombre, correo, programa académico (ej. *Ingeniería de Software*, *Diseño Gráfico*, *Administración Financiera*), semestre, lista de materias inscritas, zona actual en el campus y estado de disponibilidad.
2. **Mesa de Estudio (`StudyTable` — `/study_tables/{tableId}`):**
   - `subject`: Materia principal (ej. *Cálculo Vectorial*, *Programación Orientada a Objetos*).
   - `topic`: Tema o actividad específica (ej. *"Simulacro primer parcial"*).
   - `campusZone`: Identificador de la zona del campus (*Biblioteca Central*, *Cubículos*, *Cafetería*, *Plazoleta*, *Laboratorios*).
   - `spotReference`: Referencia exacta para encontrarse (ej. *"Mesa 6 cerca de los enchufes"*).
   - `studyVibe`: Dinámica de la mesa (*Resolución de ejercicios*, *Debate teórico*, *Estudio intensivo*).
   - `maxSeats`, `participantIds`, `participantNames`: Control de cupos en tiempo real.
   - `hostId`, `hostName`, `isActive`, `createdAt`, `updatedAt`.
3. **Alerta SOS de Duda Urgente (`SosAlert` — `/sos_alerts/{alertId}`):**
   - `subject`: Materia de la duda.
   - `questionTitle` y `questionDetail`: Descripción clara de lo que el estudiante no entiende.
   - `examUrgency`: Nivel de urgencia (*Parcial en < 2 horas*, *Parcial hoy*, *Parcial mañana*).
   - `campusZone` y `spotReference`: Dónde está sentado el estudiante esperando ayuda.
   - `authorId`, `authorName`, `isResolved`, `helperIds`, `helperNames` (compañeros que van en camino o están ayudando), `responsesCount`, `createdAt`, `updatedAt`.
4. **Mensajes y Respuestas de Colaboración (`CollaborationMessage` — `/collaboration_messages/{messageId}`):**
   - Comentarios y explicaciones asociados tanto a una mesa de estudio como a una alerta SOS (`parentId`, `parentType`, `senderId`, `senderName`, `content`, `isGoingInPerson`, `createdAt`).

### Mapeo de Componentes Interactivos y Transiciones de Estado

- **Selector de Zonas en el Mapa Interactivo (`CampusMapScreen`):**
  - Tocar cualquier edificio/zona en el lienzo interactivo selecciona esa zona, resalta sus conexiones sinápticas y filtra instantáneamente las mesas de estudio y alertas SOS ubicadas allí. Incluye un botón para **"Hacer Check-in Aquí"** o **"Abrir Mesa en esta Zona"**.
- **Gestión de Cupos en Vivo (`StudyTablesScreen` & `TableDetailSheet`):**
  - El botón **"Unirme a la Mesa"** valida que `participantIds.size < maxSeats` y que el estudiante no esté ya inscrito; añade su `uid` y nombre a la mesa en Firestore y habilita el chat de coordinación de la mesa para acordar detalles ("¿Tienes calculadora?", "Estoy llegando con camisa negra").
  - Si el creador cierra la mesa o el participante sale, el cupo se libera inmediatamente para otros estudiantes en el campus.
- **Flujo de Emergencia Académica (`SosRadarScreen` & `SosDetailSheet`):**
  - El formulario **"Lanzar SOS"** valida título, materia, nivel de urgencia y ubicación en el campus.
  - En la vista de detalle de un SOS, cualquier compañero puede pulsar **"¡Voy a tu mesa a explicarte!"** (registrándose como mentor en camino y enviando un aviso automático al hilo) o escribir una explicación rápida en el chat.
  - El autor del SOS cuenta con el botón **"Marcar Duda como Resuelta"**, que actualiza el estado de la alerta y otorga reconocimiento a los compañeros que colaboraron.
- **Semilla de Contexto Inicial (Demo Académica FESC):**
  - Si un estudiante ingresa por primera vez y el campus aún no tiene sesiones activas, dispondrá de una acción rápida **"Cargar Escenario de Semana de Parciales FESC"** que poblará mesas de estudio y alertas SOS realistas (en Biblioteca, Cubículos y Plazoleta) asociadas a su cuenta para explorar y probar todas las interacciones inmediatamente.
