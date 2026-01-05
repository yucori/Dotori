import os
import requests
import logging

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('app.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")

def login(email, password):
    logger.info(f"로그인 API 호출: email={email}")
    try:
        response = requests.post(f"{BACKEND_URL}/auth/login", json={"email": email, "password": password})
        if response.status_code == 200:
            logger.info(f"로그인 성공: email={email}")
        else:
            logger.warning(f"로그인 실패: email={email}, status_code={response.status_code}")
        return response
    except Exception as e:
        logger.error(f"로그인 API 호출 중 예외 발생: email={email}, error={str(e)}")
        raise

def signup(payload):
    logger.info(f"회원가입 API 호출: email={payload.get('email')}")
    try:
        response = requests.post(f"{BACKEND_URL}/auth/signup", json=payload)
        if response.status_code in [200, 201]:
            logger.info(f"회원가입 성공: email={payload.get('email')}")
        else:
            logger.warning(f"회원가입 실패: email={payload.get('email')}, status_code={response.status_code}")
        return response
    except Exception as e:
        logger.error(f"회원가입 API 호출 중 예외 발생: email={payload.get('email')}, error={str(e)}")
        raise

def get_my_info(token):
    logger.debug("사용자 정보 조회 API 호출")
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.get(f"{BACKEND_URL}/users/me", headers=headers)
        if response.status_code == 200:
            logger.debug("사용자 정보 조회 성공")
        else:
            logger.warning(f"사용자 정보 조회 실패: status_code={response.status_code}")
        return response
    except Exception as e:
        logger.error(f"사용자 정보 조회 API 호출 중 예외 발생: error={str(e)}")
        raise

# --- Task 관련 함수 ---
def create_task(token, payload):
    logger.info(f"작업 생성 API 호출: title={payload.get('title')}, priorityType={payload.get('priorityType')}")
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.post(f"{BACKEND_URL}/api/tasks", json=payload, headers=headers)
        if response.status_code == 200:
            logger.info(f"작업 생성 성공: title={payload.get('title')}")
        else:
            logger.warning(f"작업 생성 실패: title={payload.get('title')}, status_code={response.status_code}, response={response.text}")
        return response
    except Exception as e:
        logger.error(f"작업 생성 API 호출 중 예외 발생: title={payload.get('title')}, error={str(e)}")
        raise

def get_auto_plan(token):
    logger.debug("자동 계획 조회 API 호출")
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.get(f"{BACKEND_URL}/api/tasks/auto-plan", headers=headers)
        if response.status_code == 200:
            tasks = response.json()
            logger.info(f"자동 계획 조회 성공: taskCount={len(tasks)}")
        else:
            logger.warning(f"자동 계획 조회 실패: status_code={response.status_code}")
        return response
    except Exception as e:
        logger.error(f"자동 계획 조회 API 호출 중 예외 발생: error={str(e)}")
        raise

def check_postpone_risk(token, task_id):
    logger.info(f"미루기 리스크 조회 API 호출: taskId={task_id}")
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.post(f"{BACKEND_URL}/api/tasks/{task_id}/postpone-risk", headers=headers)
        if response.status_code == 200:
            risk_data = response.json()
            logger.info(f"미루기 리스크 조회 성공: taskId={task_id}, risk={risk_data.get('riskProbability')}%")
        else:
            logger.warning(f"미루기 리스크 조회 실패: taskId={task_id}, status_code={response.status_code}")
        return response
    except Exception as e:
        logger.error(f"미루기 리스크 조회 API 호출 중 예외 발생: taskId={task_id}, error={str(e)}")
        raise