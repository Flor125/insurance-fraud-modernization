# 🛡️ Insurance Claims Fraud Audit Modernization

> **Architecture Note & Scope:** *This project implements the core fraud audit workflow and documents the IBM Z integration points. Some components such as RACF, IBM MQ, CICS transactions and DFSORT are represented through mocks or architecture-level simulations when a full z/OS environment is not available.*

## 📖 Project Overview
Modernización legacy para detección y auditoría de fraude en reclamos de seguros. Este MVP demuestra la integración de una arquitectura moderna orientada a eventos con un core transaccional robusto, priorizando la seguridad, la transaccionalidad ACID y la auditabilidad end-to-end.

## 🏗️ Architecture & Business Flow
El sistema procesa reclamos de seguros mediante el siguiente flujo "de adentro hacia afuera":

1. **REST API (Modern Entry Point):** Una API en Spring Boot recibe el JSON del reclamo.
2. **COBOL/CICS Validation (The Core):** Un programa estructurado en COBOL valida las reglas de negocio (ej. verificación de montos y estados de póliza).
3. **RACF-style Security:** Se evalúa el perfil del operador. Operadores básicos que intentan procesar reclamos mayores a $10,000 USD disparan un estado de `UNDER_REVIEW`.
4. **Db2 Transactional Persistence:** Inserción del reclamo evaluando el `SQLCODE` para garantizar `COMMIT` en caso de éxito o `ROLLBACK` ante fallos críticos de integridad.
5. **IBM MQ Event Decoupling:** Si un reclamo es marcado como riesgo/fraude, se publica un evento asíncrono en una cola MQ para desacoplar el motor transaccional.
6. **Nightly Batch & Reporting (JCL/DFSORT & REXX):** Un Job orquesta el filtrado de eventos de fraude generados en el día, y una herramienta interactiva REXX en TSO/ISPF expone los resultados a los auditores.

## 💻 Tech Stack
* **Frontend/API:** Java Spring Boot, Postman
* **Mainframe Core:** Enterprise COBOL, CICS (Simulado), JCL, DFSORT, REXX
* **Data & Messaging:** IBM Db2 (Docker), IBM MQ (RabbitMQ Mock en contenedores)
* **Development:** VS Code, IBM Z Open Editor, GnuCOBOL

## In progress 
