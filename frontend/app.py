import streamlit as st
import requests

BASE_URL = "http://localhost:8080"  # 백엔드 Spring Boot 주소


st.set_page_config(page_title="Auth Demo", layout="centered")
st.title("🔐 인증 테스트 (Streamlit + Spring Boot)")


# -- Streamlit Session State for JWT Token --
if "access_token" not in st.session_state:
    st.session_state.access_token = None


# ----------------------- SIGNUP -----------------------
st.header("📌 회원가입")

with st.form("signup_form"):
    signup_email = st.text_input("이메일", key="signup_email")
    signup_password = st.text_input("비밀번호", type="password", key="signup_password")
    signup_name = st.text_input("이름", key="signup_name")
    signup_nickname = st.text_input("닉네임", key="signup_nickname")
    submitted_signup = st.form_submit_button("회원가입")

if submitted_signup:
    payload = {
        "email": signup_email,
        "password": signup_password,
        "name": signup_name,
        "nickname": signup_nickname
    }
    try:
        res = requests.post(f"{BASE_URL}/auth/signup", json=payload)
        if res.status_code == 201 or res.status_code == 200:
            st.success("🎉 회원가입 성공!")
        else:
            st.error(f"❌ 회원가입 실패: {res.text}")
    except Exception as e:
        st.error(f"서버 요청 실패: {e}")


st.markdown("---")

# ----------------------- LOGIN -----------------------
st.header("🔑 로그인")

with st.form("login_form"):
    login_email = st.text_input("이메일", key="login_email")
    login_password = st.text_input("비밀번호", type="password", key="login_password")
    submitted_login = st.form_submit_button("로그인")

if submitted_login:
    payload = {
        "email": login_email,
        "password": login_password
    }
    try:
        res = requests.post(f"{BASE_URL}/auth/login", json=payload)

        if res.status_code == 200:
            data = res.json()
            st.session_state.access_token = data["accessToken"]
            st.success("🎉 로그인 성공!")
        else:
            st.error(f"❌ 로그인 실패: {res.text}")
    except Exception as e:
        st.error(f"서버 요청 실패: {e}")


st.markdown("---")

# ----------------------- ME -----------------------
st.header("🙋 내 정보 조회")

if st.session_state.access_token:
    if st.button("내 정보 가져오기"):
        headers = {
            "Authorization": f"Bearer {st.session_state.access_token}"
        }
        try:
            res = requests.get(f"{BASE_URL}/users/me", headers=headers)
            if res.status_code == 200:
                st.json(res.json())
            else:
                st.error(f"❌ 조회 실패: {res.text}")
        except Exception as e:
            st.error(f"서버 요청 실패: {e}")
else:
    st.info("로그인이 필요합니다.")
