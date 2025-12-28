import streamlit as st
import api_service
import auth_view
import task_view

st.set_page_config(page_title="Dotori", layout="wide")

# 세션 관리
if "access_token" not in st.session_state:
    st.session_state.access_token = None
if "user_info" not in st.session_state:
    st.session_state.user_info = None

# 메인 로직
if not st.session_state.access_token:
    auth_view.render_auth()
else:
    # 로그인 성공 후 유저 정보가 없으면 가져오기
    if not st.session_state.user_info:
        res = api_service.get_my_info(st.session_state.access_token)
        if res.status_code == 200:
            st.session_state.user_info = res.json()
    
    task_view.render_tasks()