# 💬 VirtualMessenger (VM)

VirtualMessenger is a modern reactive messenger built with Spring WebFlux and React that supports real-time messaging via WebSocket and Server-Sent Events (SSE).

---

##  Features

-  Registration/Login via phone number 
-  JWT Authentication
-  Fully reactive architecture (Spring WebFlux + R2DBC + Redis)
-  Real-time updates with WebSocket and SSE
-  Send text, photo, and video messages (stored via Cloudinary)
-  Premium feature: AI translation and message correction
-  Search user by phone number to start a new chat
-  Real-time updates for chat list and message history
- Status indicators: online / offline / typing / read / unread
-  Redis caching for faster chat access
-  Pagination for message history loading
-  Chat sharding (distributes chats across multiple tables)
-  Clean and modern UI built with React + TailwindCSS + HeroUI

---

## 🛠️ Tech Stack

### Backend:
- Java 21
- Spring Boot 3.x
- Spring WebFlux
- R2DBC + MySQL
- Redis (reactive)
- Cloudinary (for media)
- JWT
- SSE + WebSocket
- Paypal payment

### Frontend:
- React + Vite
- TailwindCSS
- Axios
- EventSource / WebSocket
- HeroUI


video about this messenger - https://youtu.be/YQU5zR_iHRk?si=6s3IAdMYdt-R2AT8 
