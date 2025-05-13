# AI Service

Microservice cho các chức năng AI của ứng dụng Nihongo-IT, bao gồm:

- Tạo và xử lý text-to-speech (TTS)
- Phân tích và đánh giá giọng nói (Speech Analysis)
- Tương tác với LLM qua OpenAI API
- Dịch văn bản (Việt-Nhật, Nhật-Việt)

## Cài đặt

### Yêu cầu

- Java 21
- Gradle
- OpenAI API Key

### Cài đặt

```bash
# Clone repository (nếu chưa có)
git clone https://your-repository-url.git

# Di chuyển vào thư mục ai-service
cd ai-service

# Build project
./gradlew build
```

## Cấu hình

Các cấu hình chính nằm trong file `src/main/resources/application.yaml`. Bạn có thể tùy chỉnh:

- Port của ứng dụng (mặc định: 8082)
- OpenAI API Key thông qua biến môi trường `OPENAI_API_KEY`
- Cấu hình Eureka Client cho microservice
- Đường dẫn đến Python service

## Chạy ứng dụng

### Chạy độc lập

```bash
./gradlew bootRun
```

### Chạy với Docker

```bash
docker build -t ai-service .
docker run -p 8082:8082 -e OPENAI_API_KEY=your_openai_key ai-service
```

### Chạy với Eureka Service Registry

Đảm bảo Eureka Server đang chạy, sau đó:

```bash
./gradlew bootRun --args='--spring.cloud.discovery.enabled=true --eureka.client.enabled=true'
```

## API Endpoints

### Text-to-Speech

- `POST /api/v1/tts/generate` - Tạo audio từ text
- `GET /api/v1/tts/check` - Kiểm tra audio đã tồn tại
- `GET /api/v1/tts/audio` - Lấy audio đã được lưu trữ

### Speech Analysis

- `POST /api/v1/speech/analyze` - Phân tích audio
- `POST /api/v1/speech/analyze-sample` - Phân tích mẫu audio
- `GET /api/v1/speech/sample-audio/{sampleId}` - Lấy mẫu audio
- `POST /api/v1/speech/analyze-audio-enhanced` - Phân tích nâng cao

### AI Chat & Translation

- `GET /api/v1/ai/ask-ai` - Tương tác với GPT-4
- `POST /api/v1/ai/translate` - Dịch văn bản (chất lượng cao)
- `POST /api/v1/ai/translate/economy` - Dịch văn bản (tiết kiệm)
- `POST /api/v1/ai/vocabulary/list` - Lấy danh sách từ vựng
- `POST /api/v1/ai/vocabulary/explain` - Giải thích từ vựng

## Health Check

- `GET /api/v1/tts/health` - Kiểm tra sức khỏe TTS service
- `GET /api/v1/speech/health` - Kiểm tra sức khỏe Speech service
- `GET /api/v1/ai/health` - Kiểm tra sức khỏe AI service