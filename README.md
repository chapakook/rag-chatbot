# 🔍 Spring Boot 기반 RAG 차트볼 데모

이 프로젝트는 **Kotlin + Spring Boot** 기반으로 구성된 **Retrieval-Augmented Generation (RAG)** 데모입니다.
OpenAI의 임벤딩 API와 Qdrant 벡터 DB를 활용하여, 문서 기반 질의응답 시스템을 구현합니다.

---

## 📚 기술 스택
**Language / Build**
- Kotlin 1.9.25
- Java 21
- Gradle (Kotlin DSL)

**Framework**
- Spring Boot 3.5.4
- Spring Web (REST API)
- Spring WebFlux (WebClient)

**Embedding / Vector**
- OpenAI Embedding API
- Qdrant (via Docker)

---

## ⚙️ 실행 방법

### 1. OpenAI API Key 설정

```bash
export OPENAI_API_KEY=your_api_key_here
```

또는 `.env`를 사용하는 경우 `.env` 파일 생성:

```
OPENAI_API_KEY={your_api_key_here}
```

### 2. Qdrant 실행 (Docker)

```bash
docker-compose -f ./docker/infra-compose.yml up
```

```yaml
# docker-compose.yml 예시
version: '3.9'
services:
  qdrant:
    image: qdrant/qdrant
    container_name: qdrant
    ports:
      - "6333:6333"
    volumes:
      - qdrant_data:/qdrant/storage

volumes:
  qdrant_data:

networks:
  default:
    driver: bridge
```

### 3. Spring Boot 애플리케이션 실행

```bash
./gradlew bootRun
```

---

## 📌 참고

* [OpenAI Embedding API Docs](https://platform.openai.com/docs/guides/embeddings)
* [Qdrant HTTP API Reference](https://qdrant.tech/documentation/)
* [Spring WebClient Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)