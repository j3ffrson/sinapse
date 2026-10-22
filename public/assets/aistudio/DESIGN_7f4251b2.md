# Design System Tokens: FESC Synapse Mobile

Este documento especifica el sistema de diseño, colores y tipografía institucional basado en el Manual de Identidad Visual de la Fundación de Estudios Superiores Comfanorte (FESC).

---

## 1. Paleta Cromática Institucional (Brand Palette)

### Colores Primarios Institucionales
- **FESC Red Primary (Rojo Institucional):** `#E30513`
  - RGB: `(227, 6, 19)` | CMYK: `(0, 100, 100, 0)`
  - *Uso:* Botones principales (Primary CTA), insignias de urgencia, estados activos prioritarios y acentos clave.
- **FESC Burgundy (Vinotinto Institucional):** `#9C0F06`
  - RGB: `(156, 16, 6)` | CMYK: `(0, 100, 100, 40)`
  - *Uso:* Encabezados destacados, fondos de barras de navegación (App Bars) y estados de interacción (hover/pressed de botones principales).
- **FESC Deep Wine (Vinotinto Oscuro / Sombra):** `#630B00`
  - RGB: `(100, 12, 0)` | CMYK: `(0, 100, 100, 70)`
  - *Uso:* Gradientes de acento, bordes de alto contraste y delimitadores de tarjetas críticas.

### Colores Digitales y Plataformas (Aula Virtual & SIPAES)
- **Digital Alert Red (Rojo Digital Aula Virtual):** `#BF0C0C`
  - RGB: `(160, 19, 23)`
  - *Uso:* Notificaciones de dudas sin resolver y etiquetas de alta prioridad.
- **Digital Wine (Vinotinto Aula Virtual):** `#910F05`
  - RGB: `(119, 24, 19)`
  - *Uso:* Barras de herramientas secundarias y acentos de navegación.

### Neutros y Superficies (Escala de Grises y Negros Institucionales)
- **FESC Dark Neutral (Gris Corporativo Oscuro - 90% K):** `#3C3C3B`
  - RGB: `(60, 60, 60)` | CMYK: `(0, 0, 0, 90)`
  - *Uso:* Superficies secundarias de tarjetas elevadas y textos de alto contraste sobre fondos claros.
- **FESC Digital Black (Negro Base Aula Virtual):** `#1C1C1C`
  - RGB: `(33, 25, 21)`
  - *Uso:* Fondo de pantalla principal (Background) en Dark Mode y fondos de contenedor de pantalla completa.
- **FESC Surface Charcoal (Gris de Superficie para Cards):** `#242424`
  - *Uso:* Fondo de tarjetas de dudas (`DoubtCard`), modales y bottom sheets.
- **FESC Neutral Light Gray (Gris Medio Aula Virtual):** `#AAAAA8`
  - RGB: `(166, 165, 170)`
  - *Uso:* Textos secundarios, marcas de tiempo ("Hace 5 min"), íconos inactivos y bordes de inputs.
- **Pure White (Blanco Puro):** `#FFFFFF`
  - *Uso:* Textos principales sobre fondos oscuros o vinotinto, íconos principales y avatares.

### Semántica de Estados y Gamificación (Adaptación FESC)
- **Success / Gamificación (XP y Duda Resuelta):** `#2E7D32` (Verde institucional sobrio) o `#00A86B`
  - *Uso:* Indicador de dudas resueltas y contador de puntos de experiencia (+20 XP).
- **Warning / Fichas Rápidas:** `#E65100` (Naranja ámbar)
  - *Uso:* Etiquetas de escaneo OCR y recordatorios de repaso.
- **Info / El Radar:** `#D32F2F` (Variante de rojo FESC claro para zonas calientes del mapa) o `#1976D2` (Azul neutro para marcadores de usuario).

---

## 2. Tipografía Institucional

De acuerdo con el manual oficial:
- **Tipografía Primaria / Corporativa:** `Calibri`
  - Variantes: *Italic*, *Bold Italic* (utilizada para identidad institucional y títulos dinámicos).
- **Tipografía de Apoyo y Digital:** `Roboto` (Google Fonts)
  - Variantes: *Roboto Regular*, *Roboto Medium*, *Roboto Bold*.
  - *Uso en App:* Toda la interfaz gráfica digital (textos de lectura, botones, inputs y cuerpo del chat) para garantizar máxima legibilidad y compatibilidad con Material 3 / Compose.

### Jerarquía Tipográfica sugerida
- **Display / Título de Pantalla:** `Roboto Bold` 22sp, color `#FFFFFF`
- **Subtítulos / Categorías de Materia:** `Roboto Medium` 16sp, color `#E30513` o `#AAAAA8`
- **Cuerpo de Mensaje (Dudas / Apuntes):** `Roboto Regular` 14sp, color `#FFFFFF`
- **Metadatos y Horas (Timestamps):** `Roboto Regular` 12sp, color `#AAAAA8`
- **Botones y CTAs:** `Roboto Bold` 14sp (Mayúsculas o Title Case), color `#FFFFFF` sobre fondo `#E30513`

---

## 3. Reglas de Contraste y Aplicación Visual (Normas del Manual)

1. **Regla de Positivo y Negativo:**
   - Logotipo y elementos claros sobre fondos oscuros (`#1C1C1C` / `#242424`).
   - Elementos oscuros (`#3C3C3B` / `#1C1C1C`) o fondos de acento cuando se usen contenedores en blanco (`#FFFFFF`).
2. **Gama Estricta:**
   - La interfaz debe mantenerse dentro de la armonía de **rojo, vinotinto, gris oscuro, blanco y negro**, respetando la identidad heredada de la FESC y Comfanorte.
3. **Botones de Acción Primaria:**
   - Color de fondo: `#E30513` (FESC Red Primary).
   - Texto/Iconografía: `#FFFFFF` (White).
   - Estado presionado/Hover: `#9C0F06` (FESC Burgundy).
4. **Tarjetas de Dudas (`DoubtCard`):**
   - Fondo: `#242424`.
   - Borde: `#3C3C3B` (1dp). Si la duda es urgente/sin resolver, el borde izquierdo o el tag superior lleva `#E30513`.