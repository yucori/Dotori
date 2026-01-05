import streamlit as st
import api_service
import auth_view
import task_view
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

st.set_page_config(page_title="Dotori", layout="wide")

# 세션 관리
if "access_token" not in st.session_state:
    st.session_state.access_token = None
if "user_info" not in st.session_state:
    st.session_state.user_info = None

# 메인 로직
if not st.session_state.access_token:
    logger.debug("인증되지 않은 사용자 - 인증 화면 표시")
    auth_view.render_auth()
else:
    # 로그인 성공 후 유저 정보가 없으면 가져오기
    if not st.session_state.user_info:
        logger.debug("사용자 정보 없음 - 사용자 정보 조회 시도")
        try:
            res = api_service.get_my_info(st.session_state.access_token)
            if res.status_code == 200:
                st.session_state.user_info = res.json()
                logger.info(f"사용자 정보 로드 완료: userId={st.session_state.user_info.get('id')}, email={st.session_state.user_info.get('email')}")
            else:
                logger.warning(f"사용자 정보 조회 실패: status_code={res.status_code}")
        except Exception as e:
            logger.error(f"사용자 정보 조회 중 예외 발생: error={str(e)}")
    
    logger.debug(f"인증된 사용자 - 작업 화면 표시: userId={st.session_state.user_info.get('id') if st.session_state.user_info else 'unknown'}")
    task_view.render_tasks()