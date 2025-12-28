import requests

BASE_URL = "http://localhost:8080"

def login(email, password):
    return requests.post(f"{BASE_URL}/auth/login", json={"email": email, "password": password})

def signup(payload):
    return requests.post(f"{BASE_URL}/auth/signup", json=payload)

def get_my_info(token):
    headers = {"Authorization": f"Bearer {token}"}
    return requests.get(f"{BASE_URL}/users/me", headers=headers)

# --- Task 관련 함수 ---
def create_task(token, payload):
    headers = {"Authorization": f"Bearer {token}"}
    return requests.post(f"{BASE_URL}/api/tasks", json=payload, headers=headers)

def get_auto_plan(token):
    headers = {"Authorization": f"Bearer {token}"}
    return requests.get(f"{BASE_URL}/api/tasks/auto-plan", headers=headers)

def check_postpone_risk(token, task_id):
    headers = {"Authorization": f"Bearer {token}"}
    return requests.post(f"{BASE_URL}/api/tasks/{task_id}/postpone-risk", headers=headers)