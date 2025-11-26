# MediGO - Plataforma de Telemedicina

## 📋 Descripción
MediGO es una plataforma integral de telemedicina diseñada para facilitar la conexión entre pacientes y médicos, permitiendo agendar citas, realizar consultas virtuales, mantener historiales médicos digitales y gestionar pagos de manera segura.

## 🎯 Objetivo
Brindar una solución tecnológica accesible y eficiente que mejore la atención médica remota, eliminando barreras geográficas y optimizando el tiempo de ambos profesionales de la salud y pacientes.

## 🚀 Características Principales

### 👥 Gestión de Usuarios
- **Registro y autenticación** con JWT (JSON Web Tokens)
- **Tres tipos de usuarios**: Pacientes, Médicos y Administradores
- **Perfiles personalizados** con información detallada
- **Validación de datos** y seguridad robusta

### 🩺 Gestión Médica
- **Búsqueda de médicos** por especialidad, nombre y ubicación
- **Agendamiento de citas** con disponibilidad en tiempo real
- **Historial médico digital** para cada paciente
- **Especialidades médicas** categorizadas

### 💻 Videollamadas
- **Consultas virtuales** integradas con Whereby
- **Salas de video** seguras y privadas
- **Gestión de sesiones** de videollamada

### 💳 Sistema de Pagos
- **Integración con Stripe** para pagos seguros
- **Comisiones de plataforma** configurables
- **Transacciones rastreables** y reportes

### 📧 Notificaciones
- **Emails automáticos** para confirmaciones de citas
- **Plantillas HTML** personalizadas con Thymeleaf
- **Notificaciones de estado** de cuenta

### 🔐 Seguridad
- **Autenticación JWT** con tokens de acceso y refresh
- **Autorización basada en roles** (RBAC)
- **Validación de datos** con Bean Validation
- **Manejo de excepciones** global

## 🛠 Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **Spring Mail** - Envío de emails
- **Spring Web** - API REST

### Base de Datos
- **PostgreSQL** - Base de datos principal
- **Hibernate** - ORM

### Seguridad y Autenticación
- **JWT (JSON Web Tokens)** - Autenticación stateless
- **Spring Security** - Seguridad de la aplicación

### Servicios Externos
- **Stripe** - Procesamiento de pagos
- **Whereby** - Servicio de videollamadas
- **Gmail SMTP** - Envío de emails

### Herramientas de Desarrollo
- **Lombok** - Reducción de código boilerplate
- **ModelMapper** - Mapeo de objetos
- **Maven** - Gestión de dependencias
- **Thymeleaf** - Motor de plantillas

## 🏗 Arquitectura del Sistema

### Patrón de Arquitectura
- **Arquitectura en capas** (Layered Architecture)
- **Separación de responsabilidades** clara
- **Inyección de dependencias** con Spring

### Capas de la Aplicación
```
┌─────────────────────────────────────┐
│           Controller Layer          │ ← Controladores REST
├─────────────────────────────────────┤
│            Service Layer            │ ← Lógica de negocio
├─────────────────────────────────────┤
│           Repository Layer          │ ← Acceso a datos
├─────────────────────────────────────┤
│            Domain Layer             │ ← Entidades del dominio
└─────────────────────────────────────┘
```

### Entidades Principales
- **Usuario** (Base) → Paciente, Medico, Admin
- **Cita** - Gestión de citas médicas
- **HistorialMedico** - Registros médicos
- **PaymentTransaction** - Transacciones de pago
- **VideoRoom** - Salas de videollamada
- **Especialidad** - Categorías médicas

## ⚙️ Configuración del Proyecto

### Variables de Entorno
Para ejecutar la aplicación correctamente, debes configurar las siguientes variables de entorno en un archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=medigo_db
DB_USER=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_ACCESS=3600000
JWT_EXPIRATION_REFRESH=86400000

# Gmail SMTP
MAIL_SMPT_USERNAME=your_email@gmail.com
MAIL_SMPT_PASSWORD=your_app_password

# Stripe
STRIPE_API_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Whereby (antes Daily.co)
WHEREBY_API_KEY=your_whereby_api_key
```

### Instalación y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/MediGO.git
   cd MediGO
   ```

2. **Configurar variables de entorno:**
   Crear un archivo `.env` en la raíz del proyecto con las variables mencionadas anteriormente.

3. **Construir el proyecto:**
   ```bash
   ./mvnw clean install
   ```

4. **Ejecutar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Acceder a la aplicación:**
   La aplicación estará disponible en `http://localhost:8080`

## 🧪 Pruebas

Para ejecutar las pruebas unitarias y de integración:

```bash
./mvnw test
```

## 📚 Documentación Adicional

- [Documentación de la API](postman_collection.json) - Colección de Postman
- [Diagrama de la Base de Datos](docs/database-diagram.png) - Visualización del modelo de datos

## 👥 Equipo de Desarrollo

- **Sebastián Reategui** - Desarrollador Full Stack
- **Carlos García** - Desarrollador Backend
- **María López** - Desarrolladora Frontend

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 📞 Contacto

Para más información, contacta al equipo de desarrollo en:
- **Email:** contacto@medigo.com
- **Sitio Web:** https://medigo.com