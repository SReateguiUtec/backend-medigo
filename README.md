# MediGO

**MediGO** es una plataforma de telemedicina desarrollada con Spring Boot que conecta pacientes con médicos especialistas para consultas médicas virtuales. La aplicación incluye funcionalidades de videollamadas, gestión de citas, historiales médicos, pagos integrados y administración de usuarios.

Proyecto desarrollado para el curso **CS 2031 Desarrollo Basado en Plataformas de la Universidad de Ingenieria y Tecnologia - UTEC**.

<div align="center">
  <img src="src/main/resources/static/medigo-logo-1.gif" alt="MediGO Logo" width="400" height="350">
</div>

## 📋 Tabla de Contenidos

- [Características Principales](#-características-principales)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Equipo de Desarrollo](#-equipo-de-desarrollo)

## 💡 Introducción

MediGO es una solución de telemedicina diseñada para modernizar y optimizar el acceso a la atención médica especializada a través de medios digitales.

### Contexto
En el contexto actual, la demanda de **acceso rápido y flexible** a la atención médica especializada es alta, mientras que las barreras geográficas y de tiempo siguen limitando la disponibilidad de consultas presenciales. La necesidad de herramientas digitales que permitan la **interacción segura y eficiente** entre médicos y pacientes, junto con la gestión integral de todo el proceso (citas, historial, pagos), se ha convertido en una prioridad para el sector salud.

### Objetivos del Proyecto
Los objetivos principales de MediGO son:
1.  **Eliminar las barreras geográficas** y de tiempo para el acceso a médicos especialistas.
2.  Proveer una **plataforma segura y confiable** para la realización de consultas médicas virtuales.
3.  Digitalizar y centralizar la **gestión de citas, historiales médicos y transacciones** de pago.
4.  Garantizar la **seguridad y privacidad** de la información del paciente y las comunicaciones.


## 🔍 Identificación del Problema o Necesidad

### Descripción del Problema
El principal problema que MediGO aborda es la **inaccesibilidad y la ineficiencia** del modelo tradicional de atención médica. Los pacientes a menudo enfrentan largas esperas, dificultades de traslado, y limitaciones geográficas para acceder a especialistas. Además, los médicos carecen de herramientas centralizadas y seguras para la gestión de sus agendas, historiales clínicos digitales y cobros de servicios, lo que fragmenta la atención y reduce la eficiencia operativa.

### Justificación
La solución a este problema es relevante porque:
* **Mejora la calidad de vida** del paciente al ofrecer comodidad, rapidez y un acceso más equitativo a la salud.
* **Optimiza el tiempo y recursos** tanto para pacientes como para profesionales médicos.
* Permite a los centros de salud y a los médicos **expandir su alcance** sin necesidad de incrementar su infraestructura física, adoptando un modelo de negocio digital más eficiente.


## 💻 Nuestra solución

MediGO es la solución integral que digitaliza el proceso de consulta médica especializada de principio a fin, proporcionando un ecosistema completo para la telemedicina.

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
- **Consultas virtuales** integradas con Daily.co
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
- **Daily.co** - Servicio de videollamadas
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

## 📦 Instalación y Configuración

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- PostgreSQL 12+
- Git

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd MediGO
```

2. **Configurar variables de entorno**
Crear archivo `.env` en la raíz del proyecto:
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=medigo_db
DB_USER=medigo_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-super-secret-jwt-key
JWT_EXPIRATION_ACCESS=3600
JWT_EXPIRATION_REFRESH=86400

# Email
MAIL_SMPT_USERNAME=your-email@gmail.com
MAIL_SMPT_PASSWORD=your-app-password

# Stripe
STRIPE_API_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Daily.co
DAILY_API_KEY=your-daily-api-key
DAILY_DOMAIN=your-domain.daily.co
```

3. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/example/medigo/
│   │   ├── auth/                    # Autenticación
│   │   ├── config/                  # Configuraciones
│   │   ├── controller/              # Controladores REST
│   │   ├── domain/                  # Entidades del dominio
│   │   ├── dto/                     # Data Transfer Objects
│   │   │   ├── request/            # DTOs de entrada
│   │   │   └── response/           # DTOs de salida
│   │   ├── email/                   # Servicio de emails
│   │   ├── events/                  # Eventos de la aplicación
│   │   ├── exceptions/              # Excepciones personalizadas
│   │   ├── repository/              # Repositorios JPA
│   │   ├── security/                # Configuración de seguridad
│   │   └── service/                 # Servicios de negocio
│   └── resources/
│       ├── application.properties   # Configuración de la app
│       ├── static/                  # Archivos estáticos
│       └── templates/               # Plantillas Thymeleaf
└── test/                            # Pruebas unitarias
```

## 👥 Equipo de Desarrollo

| Nombre | Código |
|--------|--------|
|   Sebastian Hernan Reategui Bellido     |   202410048     |
|   Juan Diego Mejia Armas     |    202410271    |
|   Samir Antony Mena Ramirez   | 202310289       |
|   Gabriel Raymi Vaccaro Ortiz   | 202210358      |


## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

**MediGO** - Conectando pacientes y médicos a través de la tecnología 🚀