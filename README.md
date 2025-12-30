# Masbro AI - Enterprise RAG Assistant

**Masbro AI** is a robust, locally hosted Retrieval-Augmented Generation (RAG) application designed for enterprise environments. It combines a secure **Spring Boot** backend, a modern **Angular** frontend, and an **Oracle 23ai** database to provide an AI assistant capable of analyzing large technical documents (PDFs up to 400+ pages) and executing technical tasks without censorship.

## Features

* **100% Local Inference:** Runs entirely offline using **Ollama** (supports Qwen 2.5, Llama 3, Dolphin).
* **Oracle 23ai Vector Store:** Utilizes the latest Oracle Database 23ai for storing document embeddings and chat history.
* **Large Context Support:** Configurable up to **128k context window** for analyzing massive documentation.
* **Uncensored Technical Persona:** "Jailbreak" system prompts ensure the AI executes server commands and scripts without refusal.
* **Persistent Sessions:** Chat history is saved reliably in the Oracle database.

---

## Prerequisites

* **Docker Desktop** (for running the database).
* **Java JDK 21** or later.
* **Node.js & npm** (LTS version).
* **Ollama** (for the AI model).
* **Maven** (for building the backend).

---

## Installation & Setup

### 1. Database Setup (Docker + Oracle 23ai)

We use the official Oracle Free 23ai image. Run the following command in your terminal to start the database container:

```bash
docker run -d --name oracle23ai-full \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=admin \
  -v oracle-volume:/opt/oracle/oradata \
  gvenzl/oracle-free:23

```

Once the container is running (it may take a minute to initialize), connect to the database (using SQL Developer, DBeaver, or SQLPlus) as `SYS` (password: `admin`) and run these commands to set up the application user:

```sql
-- 1. Create the Application User
CREATE USER rag_user IDENTIFIED BY "RagPass123";

-- 2. Grant Permissions
GRANT DB_DEVELOPER_ROLE TO rag_user;

-- 3. Grant Unlimited Quota
ALTER USER rag_user QUOTA UNLIMITED ON USERS;

-- NOTE: You do not need to create tables manually. 
-- The Spring Boot application (AiConfig.java) will automatically 
-- create the necessary tables for Vector Store and Chat Memory on startup.
CREATE TABLE chat_memory (
    chat_id VARCHAR2(100) PRIMARY KEY,
    messages CLOB CHECK (messages IS JSON) -- Requires Oracle 23ai
);

```

### 2. AI Model Setup (Ollama)

1. **Download & Install Ollama** from [ollama.com](https://ollama.com).
2. **Pull the AI Models:**
Open your terminal and pull the models you intend to use.
* **Option A: Uncensored & Fast (Default)**
```bash
ollama pull dolphin-llama3

```


* **Option B: High Intelligence & Coding (Requires 32GB+ RAM for full context)**
```bash
ollama pull qwen2.5:32b

```


* **Option C: Embedding Model (Required for RAG)**
```bash
ollama pull nomic-embed-text

```




3. **Start Ollama:**
Ensure Ollama is running in the background:
```bash
ollama serve

```



### 3. Backend Configuration

The application is configured via `src/main/resources/application.yml`. You can switch models or adjust the context window size here.

**Current Configuration (`application.yml`):**

```yaml
spring:
  application:
    name: enterprise-rag-backend
  
  # --- DATABASE CONFIGURATION (Oracle) ---
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/FREEPDB1
    username: rag_user
    password: RagPass123
    driver-class-name: oracle.jdbc.OracleDriver

  # --- AI CONFIGURATION ---
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        # CHANGE MODEL HERE
        model: dolphin-llama3   
        # model: qwen2.5:32b
        
        options:
          # CONTEXT WINDOW (Tokens)
          num-ctx: 8192
          # num-ctx: 131072  # Uncomment for 128k context (Requires Qwen + High RAM)
          # num-ctx: 32768   # 32k context

  # --- FILE UPLOAD LIMITS ---
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

# --- LANGCHAIN4J SPECIFIC CONFIG ---
langchain4j:
  ollama:
    chat-model:
      base-url: http://localhost:11434
      model-name: dolphin-llama3
      temperature: 0.0      # 0.0 = Precise (Docs), 1.0 = Creative
    embedding-model:
      base-url: http://localhost:11434
      model-name: nomic-embed-text

```

### 4. Frontend Setup (Angular)

1. Navigate to the frontend directory:
```bash
cd frontend

```


2. Install dependencies:
```bash
npm install

```


3. Start the development server:
```bash
ng serve -o

```


The app will open at `http://localhost:4200`.

---

## How to Run

1. **Start Docker Container:** Ensure `oracle23ai-full` is running.
2. **Start Backend:** Run the Spring Boot app (via IntelliJ or `mvn spring-boot:run`).
3. **Start Frontend:** Run `ng serve`.
4. **Use the App:**
* Click **"+ New Chat"**.
* Upload a PDF (Technical Manual, Log file, etc.).
* Ask questions like *"Extract all server error codes from the file"*.



---

## Troubleshooting

* **Ollama Connection Refused:** Make sure `ollama serve` is running and port `11434` is not blocked.
* **"Table or View does not exist":** Ensure the Spring Boot app fully started; it initializes the tables on the first run.
* **Out of Memory (OOM):** If using `qwen2.5:32b` with `131072` context, ensure your machine has at least 64GB RAM. If not, lower `num-ctx` to `32768` or `16384` in `application.yml`.