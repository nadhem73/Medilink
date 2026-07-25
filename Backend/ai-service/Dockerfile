# Stage 1: Build dependencies
FROM python:3.12-slim AS builder

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1 \
    libglib2.0-0 \
    tesseract-ocr \
    tesseract-ocr-fra \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Stage 2: Runtime
FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    tesseract-ocr \
    tesseract-ocr-fra \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system app && adduser --system --ingroup app app && \
    mkdir -p /home/app/.local && \
    chown -R app:app /home/app/.local

ENV PYTHONPATH=/app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

USER app

COPY --from=builder --chown=app:app /root/.local /home/app/.local

COPY --chown=app:app . .

ENV PATH="/home/app/.local/bin:$PATH"

EXPOSE 8092

ENV AI_SERVER_PORT=8092
ENV AI_SERVER_HOST=0.0.0.0

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8092/health || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8092"]