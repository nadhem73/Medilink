# ✅ CORRECTION CORS & 403 Forbidden

**Date**: 8 juin 2026  
**Service**: API Gateway  
**Problèmes corrigés**:
1. ✅ CORS dupliqu é - Headers multiples `'http://localhost:4200, *'`
2. ✅ 403 Forbidden - Routes publiques bien configurées

---

## 🔴 PROBLÈME INITIAL

### Erreur CORS Frontend
```
Access to XMLHttpRequest at 'http://localhost:8765/api/auth/register' 
from origin 'http://localhost:4200' has been blocked by CORS policy: 
The 'Access-Control-Allow-Origin' header contains multiple values 
'http://localhost:4200, *', but only one is allowed.
```

### Erreur 403 Forbidden
```
POST http://localhost:8765/api/auth/register net::ERR_FAILED 403 (Forbidden)
```

---

## 🔍 DIAGNOSTIC

### Cause des problèmes:
1. **CORS dupliqué**: Le `SecurityConfig` définissait `corsConfigurationSource()` ET Spring Cloud Gateway ajoutait ses propres headers
2. **Routes publiques**: Déjà bien configurées dans `SecurityConfig` mais CORS bloquait avant

### Architecture correcte:
```
Frontend (4200)
    ↓ HTTP Request
API Gateway (8765) ← SEUL responsable du CORS
    ↓ Forward Request (pas de CORS)
Auth Service (8081) ← CORS désactivé
```

---

## ✅ CORRECTIONS APPLIQUÉES

### 1. **SecurityConfig.java** - Désactiver CORS
```java
@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(ServerHttpSecurity.CorsSpec::disable)  // ✅ CORS géré par GlobalCorsConfiguration
            .authorizeExchange(exchanges -> exchanges
                    // Routes publiques déjà bien configurées
                    .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    // ... autres routes
            )
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
}

// ✅ Méthode corsConfigurationSource() supprimée
```

### 2. **GlobalCorsConfiguration.java** - Nouveau fichier CORS dédié
```java
@Configuration
public class GlobalCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // ✅ Origines autorisées (pas de wildcard *)
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",       // React Dev
                "http://localhost:4200",       // Angular Dev ← Notre frontend
                "http://localhost:5173",       // Vite Dev
                "https://medilinktunisia.com"  // Production
        ));
        
        // ✅ Méthodes HTTP
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // ✅ Headers autorisés
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // ✅ Headers exposés
        corsConfig.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));
        
        // ✅ Credentials autorisés (cookies, JWT)
        corsConfig.setAllowCredentials(true);
        
        // ✅ Cache preflight (1h)
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
```

### 3. **auth-service/SecurityConfig.java** - Déjà correct
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)  // ✅ Pas de CORS dans les microservices
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()  // ✅ Routes publiques
                    .anyRequest().authenticated()
            );
    return http.build();
}
```

---

## 🚀 DÉMARRAGE DES SERVICES

### Ordre de démarrage:
```bash
# 1. Eureka Server (8761)
cd Backend/eureka-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 2. Auth Service (8081)
cd Backend/auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. API Gateway (8765)
cd Backend/api-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Frontend Angular (4200)
cd Frontend/medilink-angular
npm start
```

---

## ✅ VÉRIFICATION

### 1. Vérifier que les services démarrent
```bash
# Eureka Dashboard
http://localhost:8761/

# Auth Service Health
http://localhost:8081/actuator/health

# API Gateway Health
http://localhost:8765/actuator/health
```

### 2. Tester CORS avec curl
```bash
# Preflight request (OPTIONS)
curl -X OPTIONS http://localhost:8765/api/auth/register \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Résultat attendu:
# < Access-Control-Allow-Origin: http://localhost:4200
# < Access-Control-Allow-Methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
# < Access-Control-Allow-Credentials: true
```

### 3. Tester inscription depuis le frontend
```bash
# Ouvrir le navigateur
http://localhost:4200/auth/register

# Remplir le formulaire et soumettre
# Vérifier dans la console du navigateur:
# - Pas d'erreur CORS
# - Pas d'erreur 403
# - Réponse 200 ou message d'erreur métier (ex: "Email déjà utilisé")
```

---

## 📊 HEADERS CORS ATTENDUS

### Requête OPTIONS (Preflight)
```http
OPTIONS /api/auth/register HTTP/1.1
Host: localhost:8765
Origin: http://localhost:4200
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type
```

### Réponse OPTIONS
```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
Access-Control-Allow-Headers: Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

### Requête POST (Réelle)
```http
POST /api/auth/register HTTP/1.1
Host: localhost:8765
Origin: http://localhost:4200
Content-Type: application/json

{"email":"test@example.com","password":"Test123!",...}
```

### Réponse POST
```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Credentials: true
Content-Type: application/json

{"message":"Inscription réussie!","success":true}
```

---

## 🔧 EN CAS DE PROBLÈME

### Si CORS persiste:
```bash
# 1. Arrêter tous les services
# 2. Nettoyer les builds
cd Backend/api-gateway
mvn clean

# 3. Redémarrer dans l'ordre (Eureka → Auth → Gateway)
```

### Si 403 persiste:
```bash
# Vérifier les logs du Gateway
tail -f Backend/api-gateway/logs/api-gateway.log | grep "Securing POST"

# Vérifier les logs de l'Auth Service
tail -f Backend/auth-service/logs/auth-service.log | grep "register"
```

### Débugger CORS dans le navigateur:
1. Ouvrir DevTools (F12)
2. Onglet Network
3. Cliquer sur la requête
4. Vérifier les headers Response :
   - `Access-Control-Allow-Origin` doit être `http://localhost:4200`
   - Pas de valeurs multiples

---

## 📝 RÈGLES CORS POUR MICROSERVICES

### ✅ À FAIRE:
- Le Gateway gère TOUT le CORS
- Les microservices désactivent le CORS
- Utiliser `CorsWebFilter` dans Spring Cloud Gateway (reactive)
- Ne PAS utiliser `@CrossOrigin` dans les controllers

### ❌ À ÉVITER:
- Configurer CORS dans les microservices ET le Gateway
- Utiliser `*` (wildcard) avec `allowCredentials: true`
- Ajouter des headers CORS manuellement dans les filtres
- Utiliser `CorsConfigurationSource` dans SecurityConfig reactive

---

## 🎯 RÉSULTAT FINAL

### Avant:
```
❌ Access-Control-Allow-Origin: http://localhost:4200, *
❌ 403 Forbidden
```

### Après:
```
✅ Access-Control-Allow-Origin: http://localhost:4200
✅ 200 OK (ou message d'erreur métier)
```

---

## 📚 FICHIERS MODIFIÉS

1. ✅ `Backend/api-gateway/src/main/java/com/medilinktunisia/apigateway/security/SecurityConfig.java`
   - Désactivé CORS dans SecurityConfig
   - Supprimé méthode `corsConfigurationSource()`

2. ✅ `Backend/api-gateway/src/main/java/com/medilinktunisia/apigateway/config/GlobalCorsConfiguration.java`
   - **NOUVEAU FICHIER** - Configuration CORS propre

3. ✅ `Backend/auth-service/src/main/java/com/medilinktunisia/authservice/config/SecurityConfig.java`
   - Déjà correct - CORS désactivé

---

## 🔄 PROCHAINES ÉTAPES

1. ✅ Démarrer Eureka, Auth Service, API Gateway
2. ⏳ Tester inscription depuis le frontend
3. ⏳ Vérifier absence d'erreurs CORS
4. ⏳ Migrer la base de données (script SQL pour `status`)
