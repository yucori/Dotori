FROM python:3.11-slim
WORKDIR /app

# 필수 라이브러리 설치 (requirements.txt에 streamlit, requests 필수)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Streamlit 기본 포트
EXPOSE 8501

# 외부 접속 허용을 위해 0.0.0.0 주소 바인딩
ENTRYPOINT ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]