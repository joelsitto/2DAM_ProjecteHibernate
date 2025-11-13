# Documentació Spring Boot amb Hibernate (Jakarta EE)

## Tutorial: Migrar d'Hibernate a Spring Boot amb Spring Tool Suite (STS)

### Pas 0: Instal·lar Spring Tool Suite (STS)

1. Anar a https://spring.io/tools
2. Descarregar **Spring Tools 4 for Eclipse** per al vostre sistema operatiu
3. Descomprimir el fitxer descarregat
4. Executar `SpringToolSuite4.exe` (Windows) o l'equivalent per al vostre SO
5. Seleccionar el workspace on volem treballar

### Pas 1: Crear un nou projecte Maven bàsic

1. **Obrir Spring Tool Suite (STS)**

2. Anar a `File > New > Maven Project`

3. **Configurar el projecte nou**


### Pas 2: Configurar el pom.xml per Spring Boot
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Parent de Spring Boot: proporciona configuració i versions -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <!-- Dades del nostre projecte -->
    <groupId>com.exemple</groupId>
    <artifactId>gestioestudiants</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>GestioEstudiants</name>
    <description>Projecte de gestió d'estudiants amb Spring Boot</description>
    
    <!-- Propietats -->
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <!-- Dependències -->
    <dependencies>
        <!-- Spring Boot Starter Web: per crear APIs REST -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Starter Data JPA: inclou Hibernate -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- Base de dades H2 (en memòria, per proves) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
      
        
        <!-- Spring Boot DevTools: recàrrega automàtica durant el desenvolupament -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Starter Test: per fer tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- Plugin de Spring Boot per empaquetar l'aplicació -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Pas 3: Crear l'estructura de packages

Crear els següents packages dins de `src/main/java`:

1. Clic dret sobre `src/main/java` → `New > Package`
2. Crear els següents packages:
   - `com.exemple.gestioestudiants` (package base/root)
   - `com.exemple.gestioestudiants.model` (per les entitats)
   - `com.exemple.gestioestudiants.repository` (per les interfícies Repository)
   - `com.exemple.gestioestudiants.service` (per les classes Service)
   - `com.exemple.gestioestudiants.controller` (per les classes Controller)

### Pas 4: Crear la classe principal de Spring Boot

1. Dins del package `com.exemple.gestioestudiants`, crear una nova classe Java:
   - Clic dret sobre el package → `New > Class`
   - **Name**: `GestioEstudiantsApplication`
   
2. Afegir el següent codi:

```java
package com.exemple.gestioestudiants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestioEstudiantsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestioEstudiantsApplication.class, args);
    }
}
```

**Nota**: `@SpringBootApplication` és una anotació que combina:
- `@Configuration`: Marca la classe com a font de configuració
- `@EnableAutoConfiguration`: Habilita l'autoconfiguració de Spring Boot
- `@ComponentScan`: Escaneja automàticament tots els components del package i subpackages

### Pas 5: Crear el fitxer application.properties

1. Si no existeix, crear el directori `src/main/resources`:
   - Clic dret sobre `src/main` → `New > Folder`
   - **Folder name**: `resources`

2. Crear el fitxer `application.properties`:
   - Clic dret sobre `src/main/resources` → `New > File`
   - **File name**: `application.properties`

### Pas 6: Migrar les entitats

1. **Crear el package per les entitats:**
   - Clic dret sobre `src/main/java/com.exemple.nomdelprojecte`
   - `New > Package`
   - Nom: `com.exemple.nomdelprojecte.model` (o `.entity`)

2. **Copiar les vostres classes d'entitat** existents a aquest package

### Pas 7: Esborrar configuració antiga d'Hibernate

❌ **Eliminar o no copiar:**
- El fitxer `persistence.xml` (ja no cal!)
- Les classes de configuració manual del `SessionFactory`
- Les classes DAO tradicionals (seran substituïdes per Repositories de Spring Data JPA)
- Llibreries manuals de Hibernate al Build Path (Maven ho gestiona tot)

### Pas 8: Crear els Repositories

Per cada entitat, crear una interfície Repository. Per exemple, per a una entitat `Estudiant`:

1. Dins del package `com.exemple.gestioestudiants.repository`, crear una nova **Interface**:
   - Clic dret → `New > Interface`
   - **Name**: `EstudiantRepository`

2. Afegir el codi:

```java
package com.exemple.gestioestudiants.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.exemple.gestioestudiants.model.Estudiant;

@Repository
public interface EstudiantRepository extends JpaRepository<Estudiant, Long> {
    // Spring implementa automàticament els mètodes CRUD:
    // save(), findById(), findAll(), deleteById(), etc.
}
```


---

## Introducció

Spring Boot simplifica enormement la configuració d'aplicacions amb Hibernate. Mentre que amb Hibernate "pur" havíem de configurar manualment el `persistence.xml`, el `SessionFactory`, etc., Spring Boot ho autoconfigura tot per nosaltres.

## Dependències necessàries

Al `pom.xml` només cal afegir:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Nota**: `spring-boot-starter-data-jpa` ja inclou Hibernate com a proveïdor JPA amb Jakarta EE.

## Configuració

Al fitxer `application.properties`:

```properties
# Configuració de la base de dades
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=

# Configuració de Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### Opcions de `ddl-auto`:
- `none`: No fa res
- `validate`: Valida l'esquema
- `update`: Actualitza l'esquema (recomanat per desenvolupament)
- `create`: Crea l'esquema cada vegada
- `create-drop`: Crea i esborra en tancar

## Diferències clau amb Hibernate tradicional

| Hibernate tradicional | Spring Boot + Hibernate |
|----------------------|-------------------------|
| `SessionFactory` manual | Autoconfiguració |
| `persistence.xml` obligatori | `application.properties` |
| Gestió manual de transaccions | `@Transactional` |
| `session.beginTransaction()` | Automàtic amb Spring |
| `session.save()` / `session.get()` | `repository.save()` / `repository.findById()` |

## Arquitectura en capes

Spring Boot segueix una arquitectura en 3 capes:

```
Controller (API/Web) → Service (Lògica de negoci) → Repository (Accés a dades) → Base de dades
```

Cada capa té una responsabilitat específica i ben definida.

## Què és una API REST?

Una **API REST (Representational State Transfer)** és una manera estandarditzada de permetre que diferents aplicacions es comuniquin entre elles a través d'HTTP.

**Conceptes clau:**
- **Recurs**: Una entitat del nostre sistema (per exemple, un Estudiant)
- **Endpoint**: Una URL que representa un recurs (per exemple, `/api/estudiants`)
- **Mètodes HTTP**: Operacions sobre els recursos
  - `GET`: Obtenir dades (llegir)
  - `POST`: Crear nous recursos
  - `PUT`: Actualitzar recursos complets
  - `DELETE`: Eliminar recursos
- **JSON**: Format estàndard per intercanviar dades

**Exemple pràctic:**
```
GET    /api/estudiants      → Obtenir tots els estudiants
GET    /api/estudiants/5    → Obtenir l'estudiant amb id=5
POST   /api/estudiants      → Crear un nou estudiant
PUT    /api/estudiants/5    → Actualitzar l'estudiant amb id=5
DELETE /api/estudiants/5    → Eliminar l'estudiant amb id=5
```

**Avantatges:**
- ✅ Estàndard universal (funciona amb qualsevol client: web, mòbil, etc.)
- ✅ Separa el frontend del backend
- ✅ Fàcil de testejar (amb Postman, curl, etc.)
- ✅ Escalable i mantenible

## Components principals

### 1. Entitats (@Entity)
Utilitzant **Jakarta Persistence**:

```java
package com.exemple.gestioestudiants.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiants")
public class Estudiant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(unique = true)
    private String email;
    
    private Integer edat;
    
    // Constructors
    public Estudiant() {}
    
    public Estudiant(String nom, String email, Integer edat) {
        this.nom = nom;
        this.email = email;
        this.edat = edat;
    }
    
    // Getters i Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getEdat() { return edat; }
    public void setEdat(Integer edat) { this.edat = edat; }
}
```

### 2. Repositoris (@Repository)

**Què són?** Els repositoris són la capa d'**accés a dades**. Són equivalents als DAOs (Data Access Objects) que coneixeu d'Hibernate tradicional, però molt més potents.

**Diferència amb DAOs tradicionals:**
- **DAO tradicional**: Havíem de crear una classe i implementar tots els mètodes (save, findById, findAll, delete...) utilitzant sessions d'Hibernate.
- **Repository de Spring Data JPA**: Només declarem una **interfície** i Spring implementa tots els mètodes automàticament!

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstudiantRepository extends JpaRepository<Estudiant, Long> {
    
    // Mètodes automàtics que ja proporciona JpaRepository:
    // - save(Estudiant e)           → Guarda o actualitza
    // - findById(Long id)           → Cerca per ID
    // - findAll()                   → Retorna tots
    // - deleteById(Long id)         → Elimina per ID
    // - count()                     → Compta registres
    // - existsById(Long id)         → Comprova existència
    
    // ========== QUERY METHODS ==========
    // Spring implementa aquests mètodes automàticament 
    // només pel nom del mètode!
    
    List<Estudiant> findByNom(String nom);
    
    Optional<Estudiant> findByEmail(String email);
    
    List<Estudiant> findByEdatGreaterThan(Integer edat);
    
    List<Estudiant> findByNomContainingIgnoreCase(String nom);
    
    List<Estudiant> findByEdatBetween(Integer edatMin, Integer edatMax);
    
    List<Estudiant> findByNomAndEdat(String nom, Integer edat);
    
    List<Estudiant> findByEdatLessThanEqual(Integer edat);
    
    // Amb @Query podem fer consultes JPQL personalitzades
    @Query("SELECT e FROM Estudiant e WHERE e.edat BETWEEN :edatMin AND :edatMax")
    List<Estudiant> trobarPerRangEdat(@Param("edatMin") Integer min, 
                                      @Param("edatMax") Integer max);
}
```

#### Query Methods: Com funcionen?

Els **Query Methods** són una funcionalitat fantàstica de Spring Data JPA. Només cal declarar un mètode seguint una convenció de noms, i Spring **genera automàticament la consulta SQL**.

**Paraules clau principals:**

| Paraula clau | Exemple | Equivalent SQL |
|--------------|---------|----------------|
| `findBy` | `findByNom(String nom)` | `WHERE nom = ?` |
| `And` | `findByNomAndEdat(String nom, Integer edat)` | `WHERE nom = ? AND edat = ?` |
| `Or` | `findByNomOrEmail(String nom, String email)` | `WHERE nom = ? OR email = ?` |
| `GreaterThan` | `findByEdatGreaterThan(Integer edat)` | `WHERE edat > ?` |
| `LessThan` | `findByEdatLessThan(Integer edat)` | `WHERE edat < ?` |
| `Between` | `findByEdatBetween(Integer min, Integer max)` | `WHERE edat BETWEEN ? AND ?` |
| `Like` | `findByNomLike(String pattern)` | `WHERE nom LIKE ?` |
| `Containing` | `findByNomContaining(String text)` | `WHERE nom LIKE '%?%'` |
| `IgnoreCase` | `findByNomIgnoreCase(String nom)` | `WHERE UPPER(nom) = UPPER(?)` |
| `OrderBy` | `findByEdatOrderByNomAsc(Integer edat)` | `WHERE edat = ? ORDER BY nom ASC` |
| `StartingWith` | `findByNomStartingWith(String prefix)` | `WHERE nom LIKE '?%'` |
| `EndingWith` | `findByNomEndingWith(String suffix)` | `WHERE nom LIKE '%?'` |
| `IsNull` | `findByEmailIsNull()` | `WHERE email IS NULL` |
| `IsNotNull` | `findByEmailIsNotNull()` | `WHERE email IS NOT NULL` |

**Exemples pràctics:**

```java
// Cerca estudiants amb edat superior a 18
List<Estudiant> majorsEdat = repository.findByEdatGreaterThan(18);

// Cerca per nom (sense diferenciar majúscules/minúscules) i que contingui "mar"
List<Estudiant> ambMar = repository.findByNomContainingIgnoreCase("mar");
// Retornarà: Maria, Marta, Mariona, etc.

// Cerca estudiants entre 18 i 25 anys, ordenats per nom
List<Estudiant> joves = repository.findByEdatBetweenOrderByNomAsc(18, 25);

// Cerca per nom que comenci per "J"
List<Estudiant> ambJ = repository.findByNomStartingWith("J");
```

**Avantatges:**
- ✅ No cal escriure SQL ni JPQL
- ✅ Menys errors (si el nom està malament, error de compilació)
- ✅ Codi molt llegible i autoexplicatiu
- ✅ Spring fa la implementació automàticament

### 3. Serveis (@Service)

**Què són?** Els serveis són la capa de **lògica de negoci**. Aquí és on implementem les regles del nostre domini i coordinem les operacions amb els repositoris.

**Responsabilitats:**
- Contenir la lògica de negoci (validacions, càlculs, regles...)
- Coordinar múltiples repositoris si cal
- Gestionar transaccions amb `@Transactional`
- Aïllar els controladors de la capa de dades

**Per què no posem la lògica directament al controlador?**
- **Reutilització**: Un mateix servei pot ser utilitzat per diferents controladors
- **Testabilitat**: És més fàcil fer tests unitaris dels serveis
- **Mantenibilitat**: Separació de responsabilitats (SoC - Separation of Concerns)
- **Transaccions**: Els serveis gestionen les transaccions de manera eficient

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EstudiantService {
    
    @Autowired
    private EstudiantRepository repository;
    
    // @Transactional assegura que tot es fa en una transacció
    // Si hi ha error, fa rollback automàticament
    @Transactional
    public Estudiant guardar(Estudiant estudiant) {
        // Aquí podríem afegir validacions de negoci
        if (estudiant.getEdat() < 16) {
            throw new IllegalArgumentException("L'estudiant ha de tenir almenys 16 anys");
        }
        return repository.save(estudiant);
    }
    
    public List<Estudiant> obtenirTots() {
        return repository.findAll();
    }
    
    public Optional<Estudiant> obtenirPerId(Long id) {
        return repository.findById(id);
    }
    
    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
    
    public List<Estudiant> cercarPerNom(String nom) {
        return repository.findByNomContainingIgnoreCase(nom);
    }
    
    // Exemple de lògica de negoci més complexa
    @Transactional
    public Estudiant matricular(Long estudiantId, Long cursId) {
        // Aquí coordinaríem múltiples repositoris
        // i aplicaríem regles de negoci
        Estudiant estudiant = repository.findById(estudiantId)
            .orElseThrow(() -> new RuntimeException("Estudiant no trobat"));
        
        // Lògica de negoci...
        
        return repository.save(estudiant);
    }
}
```

### 4. Controladors (@RestController)

**Què són?** Els controladors són la capa de **presentació/API**. Gestionen les peticions HTTP i retornen les respostes. Són els que exposen la nostra API REST.

**Responsabilitats:**
- Rebre peticions HTTP (GET, POST, PUT, DELETE...)
- Validar els paràmetres d'entrada
- Cridar als serveis adequats
- Retornar respostes HTTP amb els codis d'estat correctes

**No haurien de:**
- ❌ Contenir lògica de negoci (això va als serveis)
- ❌ Accedir directament als repositoris (han d'usar serveis)
- ❌ Gestionar transaccions (això ho fan els serveis)

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estudiants")
public class EstudiantController {
    
    @Autowired
    private EstudiantService service;
    
    // POST /api/estudiants → Crear un estudiant
    @PostMapping
    public ResponseEntity<Estudiant> crear(@RequestBody Estudiant estudiant) {
        Estudiant nou = service.guardar(estudiant);
        return ResponseEntity.ok(nou);
    }
    
    // GET /api/estudiants → Llistar tots els estudiants
    @GetMapping
    public List<Estudiant> llistar() {
        return service.obtenirTots();
    }
    
    // GET /api/estudiants/5 → Obtenir estudiant amb id=5
    @GetMapping("/{id}")
    public ResponseEntity<Estudiant> obtenirPerId(@PathVariable Long id) {
        return service.obtenirPerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // PUT /api/estudiants/5 → Actualitzar estudiant amb id=5
    @PutMapping("/{id}")
    public ResponseEntity<Estudiant> actualitzar(@PathVariable Long id, 
                                                  @RequestBody Estudiant estudiant) {
        return service.obtenirPerId(id)
                .map(existent -> {
                    estudiant.setId(id);
                    return ResponseEntity.ok(service.guardar(estudiant));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // DELETE /api/estudiants/5 → Eliminar estudiant amb id=5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET /api/estudiants/cercar?nom=maria → Cercar per nom
    @GetMapping("/cercar")
    public List<Estudiant> cercar(@RequestParam String nom) {
        return service.cercarPerNom(nom);
    }
}
```

**Anotacions HTTP:**
- `@GetMapping`: Per obtenir dades (equivalent a SELECT)
- `@PostMapping`: Per crear nous recursos (equivalent a INSERT)
- `@PutMapping`: Per actualitzar recursos complets (equivalent a UPDATE)
- `@PatchMapping`: Per actualitzar parcialment
- `@DeleteMapping`: Per eliminar recursos (equivalent a DELETE)

**Exemple de peticions amb Postman o curl:**

```bash
# GET - Obtenir tots els estudiants
GET http://localhost:8080/api/estudiants

# POST - Crear un estudiant nou
POST http://localhost:8080/api/estudiants
Content-Type: application/json
{
  "nom": "Joan",
  "email": "joan@example.com",
  "edat": 20
}

# GET - Obtenir estudiant per id
GET http://localhost:8080/api/estudiants/1

# PUT - Actualitzar estudiant
PUT http://localhost:8080/api/estudiants/1
Content-Type: application/json
{
  "nom": "Joan Garcia",
  "email": "joan.garcia@example.com",
  "edat": 21
}

# DELETE - Eliminar estudiant
DELETE http://localhost:8080/api/estudiants/1
```

## Resum de l'arquitectura

```
┌──────────────────────────────────────────────┐
│       CONTROLLER (API REST - HTTP)           │
│  - Gestiona peticions HTTP                   │
│  - Exposa endpoints (/api/estudiants)        │
│  - Valida entrada                            │
│  - Retorna respostes JSON                    │
└──────────────┬───────────────────────────────┘
               │ crida
               ↓
┌──────────────────────────────────────────────┐
│            SERVICE (Lògica)                  │
│  - Lògica de negoci                          │
│  - Validacions complexes                     │
│  - Coordina repositoris                      │
│  - Gestiona transaccions                     │
└──────────────┬───────────────────────────────┘
               │ crida
               ↓
┌──────────────────────────────────────────────┐
│         REPOSITORY (Accés a dades)           │
│  - Operacions CRUD                           │
│  - Query methods                             │
│  - Consultes personalitzades                 │
└──────────────┬───────────────────────────────┘
               │ accedeix
               ↓
┌──────────────────────────────────────────────┐
│           BASE DE DADES                      │
└──────────────────────────────────────────────┘
```

## Avantatges de Spring Boot amb Hibernate

1. **Menys configuració**: Tot s'autoconfigura
2. **Dependency Injection**: No cal instanciar objectes manualment (`@Autowired`)
3. **Gestió de transaccions**: Amb `@Transactional` (substitueix `session.beginTransaction()`)
4. **Spring Data JPA**: Mètodes CRUD sense implementar
5. **Query methods**: Consultes per convenció de noms
6. **API REST fàcil**: Integració natural amb controladors
7. **Compatibilitat Jakarta**: Versions modernes i suport actualitzat
8. **Arquitectura clara**: Separació en capes ben definides

## Conceptes importants

- **IoC (Inversion of Control)**: Spring gestiona el cicle de vida dels objectes
- **@Autowired**: Spring injecta les dependències automàticament
- **@Transactional**: Spring gestiona les transaccions (equivalent a `session.beginTransaction()` i `commit()`)
- **JpaRepository**: Interfície que proporciona operacions CRUD sense codi
- **Jakarta Persistence**: Nova nomenclatura (`jakarta.persistence.*` en lloc de `javax.persistence.*`)
- **Query Methods**: Mètodes que Spring implementa automàticament segons el seu nom
- **API REST**: Manera estandarditzada de comunicació entre aplicacions via HTTP

## Exemple complet d'ús

```java
// 1. Crear l'aplicació Spring Boot
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// 2. Testejar amb CommandLineRunner
@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private EstudiantRepository repository;
    
    @Override
    public void run(String... args) {
        // Crear estudiants
        repository.save(new Estudiant("Joan", "joan@example.com", 20));
        repository.save(new Estudiant("Maria", "maria@example.com", 22));
        repository.save(new Estudiant("Pere", "pere@example.com", 19));
        
        // Llistar tots
        System.out.println("=== Tots els estudiants ===");
        repository.findAll().forEach(System.out::println);
        
        // Testejar query methods
        System.out.println("=== Majors de 20 anys ===");
        repository.findByEdatGreaterThan(20).forEach(System.out::println);
        
        System.out.println("=== Que continguin 'ar' ===");
        repository.findByNomContainingIgnoreCase("ar").forEach(System.out::println);
    }
}
```