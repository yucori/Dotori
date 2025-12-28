import streamlit as st
import api_service

def render_auth():
    st.title("🐿️ 도토리: 다시 시작하는 계획적인 삶")
    tab1, tab2 = st.tabs(["로그인", "회원가입"])

    with tab1:
        with st.form("login_form"):
            email = st.text_input("이메일")
            password = st.text_input("비밀번호", type="password")
            if st.form_submit_button("로그인"):
                res = api_service.login(email, password)
                if res.status_code == 200:
                    st.session_state.access_token = res.json()["accessToken"]
                    st.success("로그인 성공!")
                    st.rerun()
                else:
                    st.error("로그인 실패: 이메일 또는 비밀번호를 확인하세요.")

    with tab2:
        with st.form("signup_form"):
            new_email = st.text_input("이메일 (ID)")
            new_password = st.text_input("비밀번호", type="password")
            new_name = st.text_input("이름")
            new_nickname = st.text_input("닉네임")
            
            if st.form_submit_button("회원가입"):
                payload = {
                    "email": new_email,
                    "password": new_password,
                    "name": new_name,
                    "nickname": new_nickname
                }
                res = api_service.signup(payload)
                if res.status_code in [200, 201]:
                    st.success("🎉 회원가입 성공! 로그인 탭에서 로그인해주세요.")
                else:
                    st.error(f"회원가입 실패: {res.text}")