# Informe Técnico y Funcional: Sistema de Roles Dinámicos entre Pares (*Peer-to-Peer*) en FESC Synapse

**Aplicación:** FESC Synapse — Red Colaborativa de Estudio en Tiempo Real  
**Arquitectura de Roles:** Adaptativa basada en acciones (*Action-Driven Peer Roles*)  
**Plataforma:** Android (Kotlin + Jetpack Compose + Cloud Firestore)

---

## 1. Filosofía del Modelo de Roles
A diferencia de las plataformas académicas tradicionales con jerarquías estáticas (*Docente vs. Estudiante*), **FESC Synapse** implementa un **modelo de roles dinámicos entre pares (*Peer-to-Peer*)**.

El principio central es que **el conocimiento universitario es asimétrico y situacional**: un mismo estudiante puede necesitar auxilio urgente en *Cálculo Diferencial e Integral* a las 4:00 PM antes de un examen parcial (actuando como **Solicitante SOS**), y a las 5:30 PM liderar una mesa de estudio de *Estructura de Datos y Algoritmos* o acudir a explicar *Programación Orientada a Objetos* a otro compañero (actuando como **Anfitrión de Mesa** o **Mentor Par**).

---

## 2. Estados Operativos del Estudiante en el Campus (`StudentStatusMode`)
Cada perfil estudiantil (`StudentProfile`) mantiene un estado activo en tiempo real vinculado a una de las **5 zonas del campus FESC** (*Biblioteca Central*, *Cubículos Bloque B*, *Laboratorios de Cómputo*, *Plazoleta Principal* y *Cafetería Estudiantil*):

| Rol / Estado en Campus | Identificador (`id`) | Propósito Funcional | Modo de Activación |
| :--- | :--- | :--- | :--- |
| **Estudiando / Concentrado** | `STUDYING` | Indica que el estudiante se encuentra en sesión activa en un edificio del campus, visible en el radar de su zona. | Por defecto al iniciar sesión o al hacer *check-in* rápido en una zona del mapa. |
| **Buscando Grupo** | `SEEKING_GROUP` | Señaliza a la comunidad que el estudiante busca integrarse a una mesa de repaso o armar grupo para un parcial. | Selección manual desde el diálogo de **Perfil Estudiantil**. |
| **Mentor Par (Disponible para Dudas)** | `MENTOR` | Identifica a estudiantes disponibles para resolver dudas urgentes y apoyar presencialmente o por hilo de estudio. | **Automático por acción** al pulsar *«Voy en camino»* en una alerta SOS, o manual desde el perfil. |

---

## 3. Transiciones Automáticas Basadas en Acciones (Flujo Reactivo)
El sistema actualiza automáticamente el rol, la ubicación y las métricas de impacto del estudiante según las acciones que ejecuta en la aplicación:

### 3.1. Ascenso Automático a **Mentor Par (`MENTOR`)** al Responder una Alerta SOS
Cuando un estudiante visualiza una emergencia académica en el **Radar SOS** y pulsa **«Voy en camino»** (`respondInPersonToSos`):
1. **Registro en la Alerta (`SosAlert`)**:
   - Su nombre se añade a la lista de mentores en camino (`helperNames`).
   - El estado `isCurrentUserHelping` pasa a `true`.
   - El contador de respuestas (`responsesCount`) se incrementa en `+1`.
2. **Emisión Automática de Aviso Presencial (`CollaborationMessage`)**:
   - Se publica automáticamente un mensaje en el hilo de la alerta con la bandera `isGoingInPerson = true`, indicando el punto exacto de encuentro (`spotReference`).
3. **Evolución Automática del Perfil (`StudentProfile`)**:
   - El estado del estudiante cambia automáticamente a **`MENTOR`** (*Disponible para dudas*).
   - Su ubicación en el campus (`currentZone`) se actualiza automáticamente hacia el edificio donde se originó la alerta SOS.
   - Su contador histórico de impacto (**`helpedPeersCount`**) aumenta automáticamente en **`+1`**.

### 3.2. Activación como **Anfitrión de Mesa (*Host*)**
Cuando un estudiante crea una sesión desde **«Abrir Mesa»** (`createNewStudyTable`):
1. Asume el rol de **Anfitrión (`hostName`)** junto con su sello académico (`hostProgram`, ej. *Ingeniería de Software • 5° Sem*).
2. El sistema reserva automáticamente el primer asiento ocupado (`occupiedSeats = 1`, `isJoinedByCurrentUser = true`) e inicializa la lista `participantNames`.
3. Establece las condiciones del grupo:
   - **Materia y tema puntual** a estudiar.
   - **Zona del campus y referencia física exacta** (ej. *Biblioteca Piso 2 • Mesa 4 junto al ventanal*).
   - **Modalidad de trabajo (`studyVibe`)**: *Resolución de taller*, *Simulacro de parcial*, *Práctica en código* o *Debate y revisión*.
   - **Capacidad máxima (`maxSeats`)**: control de aforo en vivo entre 2 y 12 asientos.

### 3.3. Transición entre **Integrante de Mesa** y **Compañero en Campus**
Cuando un estudiante interactúa con una mesa abierta (`toggleJoinTable`):
1. **Al unirse (`Unirse a mesa`)**: Su nombre se incorpora a `participantNames`, el contador `occupiedSeats` se actualiza en vivo para todo el campus y la barra de progreso refleja el nuevo aforo.
2. **Al retirarse (`Liberar cupo`)**: Su asiento queda disponible al instante para otro estudiante que esté buscando grupo antes del examen.

### 3.4. Activación como **Solicitante SOS (*Autor de Alerta*)**
Cuando un estudiante tiene un bloqueo crítico antes de una evaluación y pulsa **«Lanzar SOS»** (`launchSosAlert`):
1. Publica una señal priorizada con **nivel de urgencia** (*Parcial en 1 hora*, *Parcial en 2 horas*, *Parcial hoy*) y **punto exacto de ubicación**.
2. La alerta incrementa el indicador rojo pulsante en el edificio correspondiente dentro del **Mapa Interactivo del Campus**.
3. Conserva el control para **«Marcar Resuelta»** o **«Reabrir»** (`toggleResolveSos`) la alerta una vez que un compañero ha acudido a explicarle.

---

## 4. Estructura Técnica de Datos que Soporta los Roles

### Entidad `StudentProfile` (`/users/{userId}`)
- `userId`: Identificador único del estudiante autenticado.
- `displayName`: Nombre visible en mesas, alertas y chat.
- `program` y `semester`: Carrera universitaria y semestre actual.
- `currentZone`: Edificio o zona activa (`LIBRARY`, `CUBICLES`, `LABS`, `PLAZA`, `CAFETERIA`).
- `statusMode`: Rol operativo actual (`STUDYING`, `SEEKING_GROUP`, `MENTOR`).
- `enrolledSubjects`: Lista de materias cursadas para filtrado rápido en el radar.
- `helpedPeersCount`: Métrica acumulativa de mentorías y ayudas brindadas.

### Entidad `StudyTable` (`/study_tables/{tableId}`)
- `hostName` y `hostProgram`: Identidad académica del anfitrión.
- `maxSeats`, `occupiedSeats`, `participantNames`: Control de cupos en tiempo real.
- `isJoinedByCurrentUser`: Estado de participación del estudiante actual.

### Entidad `SosAlert` (`/sos_alerts/{alertId}`)
- `authorName`, `authorProgram`, `examUrgency`: Contexto del solicitante y premura del examen.
- `helperNames`, `isCurrentUserHelping`, `responsesCount`: Registro en vivo de mentores pares que acudieron al llamado.
- `isResolved`: Estado de resolución de la duda.

### Entidad `CollaborationMessage` (`/collaboration_messages/{messageId}`)
- `parentType` (`TABLE` o `SOS`) y `parentId`: Vinculación con la mesa o alerta.
- `senderName` y `senderRole`: Nombre y rol académico del autor del mensaje.
- `isGoingInPerson`: Distintivo especial cuando el compañero confirma asistencia física en el lugar.

---

## 5. Beneficios del Modelo Dinámico para la Comunidad FESC
1. **Aprendizaje Horizontal sin Fricción**: Cualquier estudiante puede enseñar lo que domina y pedir apoyo en lo que se le dificulta dentro de la misma jornada de estudio.
2. **Coordinación Física Inmediata**: A diferencia de un foro web tradicional, cada rol está anclado a un edificio, piso y mesa real del campus universitario.
3. **Reconocimiento al Compañerismo (`helpedPeersCount`)**: Incentiva una cultura de apoyo mutuo entre estudiantes de distintos semestres y carreras antes de los exámenes parciales.
