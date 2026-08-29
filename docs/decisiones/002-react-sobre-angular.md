# 002 — React en lugar de Angular

Estado: aceptada · 2026-08-28

## Contexto
El plan inicial era Angular, por paralelismo con GestionER y por aprovechar experiencia previa.

## Decisión
React.

## Alternativas descartadas

**Angular.** Trae router, formularios, HTTP y estado de serie, y mantenía el paralelismo con el backend. Descartada por ser menos flexible para lo que se quiere aquí.

**Full JS (Next.js + Prisma).** Un solo lenguaje y despliegue más simple, pero tira por tierra el backend Spring Boot, que es parte del objetivo de aprendizaje.

## Consecuencias
- Hay que elegir router, gestión de estado y librería de formularios: en Angular venían dados. Decisiones pendientes.
- El paralelismo con GestionER se mantiene en el backend, se pierde en el front.

> **Corrección posterior:** esta nota decía que `lumen-app` ya era React y que eso abarataba migrar [[kuiper]]. Resultó que `lumen-app` nunca se terminó y todo el front se escribe de cero, así que ese beneficio no existe. **La decisión se mantiene por el motivo original: React es más flexible para lo que se quiere aquí.**
