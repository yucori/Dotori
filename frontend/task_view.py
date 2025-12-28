import streamlit as st
import api_service
from datetime import datetime, time, timedelta

def render_tasks():
    token = st.session_state.access_token
    user = st.session_state.user_info

    # --- 1. 세션 상태 및 콜백 설정 (시간 자동 계산) ---
    if 'duration_val' not in st.session_state:
        st.session_state.duration_val = 60
    if 'start_time_val' not in st.session_state:
        st.session_state.start_time_val = time(9, 0)
    if 'end_time_val' not in st.session_state:
        st.session_state.end_time_val = time(10, 0)

    def sync_end_time():
        start_dt = datetime.combine(datetime.today(), st.session_state.start_time_val)
        new_end_dt = start_dt + timedelta(minutes=st.session_state.duration_val)
        st.session_state.end_time_val = new_end_dt.time()

    def sync_duration():
        start_dt = datetime.combine(datetime.today(), st.session_state.start_time_val)
        end_dt = datetime.combine(datetime.today(), st.session_state.end_time_val)
        if end_dt <= start_dt:
            end_dt += timedelta(days=1)
        diff = end_dt - start_dt
        st.session_state.duration_val = int(diff.total_seconds() / 60)

    # 상세 설명이 포함된 우선순위 맵
    priority_options = {
        1: "1순위\n(중요도↑ / 집중도↑)",
        2: "2순위\n(중요도↑ / 집중도↓)",
        3: "3순위\n(중요도↓ / 집중도↑)",
        4: "4순위\n(중요도↓ / 집중도↓)"
    }

    # --- 2. 사이드바 구성 ---
    st.sidebar.title(f"🌰 {user['nickname']}님 환영해요!")
    if st.sidebar.button("로그아웃"):
        st.session_state.access_token = None
        st.session_state.user_info = None
        st.rerun()

    # --- 3. 새 도토리 추가 섹션 ---
    st.header("📥 새 도토리 줍기")
    with st.container(border=True):
        title = st.text_input("어떤 일을 하실 건가요?", placeholder="예: 스프링 부트 심화 학습")
        
        is_fixed = st.toggle("고정 스케줄 여부 (잠, 식사, 정규 수업 등)", value=False)
        
        col_t1, col_t2, col_t3 = st.columns(3)
        with col_t1:
            st.time_input("시작 시간", key="start_time_val", on_change=sync_end_time)
        with col_t2:
            st.number_input("소요 시간 (분)", min_value=0, step=10, key="duration_val", on_change=sync_end_time)
        with col_t3:
            st.time_input("종료 시간", key="end_time_val", on_change=sync_duration)

        # --- 우선순위 상세 설명 토글 버튼 ---
        st.write("**업무 우선순위 선택**")
        priority = st.radio(
            "우선순위 선택",
            options=[1, 2, 3, 4],
            format_func=lambda x: priority_options[x],
            horizontal=True,
            label_visibility="collapsed" # 라벨 중복 제거
        )

        selected_days = st.multiselect(
            "반복 요일",
            ["월", "화", "수", "목", "금", "토", "일"],
            default=[] if not is_fixed else ["월"]
        )

        if st.button("🌰 도토리 보관함에 넣기", use_container_width=True):
            if not title:
                st.error("업무명을 입력해주세요!")
            else:
                payload = {
                    "title": title,
                    "priorityType": priority,
                    "durationMinutes": st.session_state.duration_val,
                    "isFixed": bool(is_fixed),
                    "startTime": st.session_state.start_time_val.strftime("%H:%M"),
                    "endTime": st.session_state.end_time_val.strftime("%H:%M"),
                    "daysOfWeek": selected_days
                }
                res = api_service.create_task(token, payload)
                if res.status_code == 200:
                    st.success("새로운 도토리를 획득했습니다!")
                    st.rerun()
                else:
                    st.error(f"저장 실패: {res.text}")

    st.markdown("---")

    # --- 4. 도토리 목록 표시 섹션 ---
    st.header("📅 나의 도토리 계획")
    
    with st.spinner("보관함을 확인하는 중..."):
        res = api_service.get_auto_plan(token)
    
    if res.status_code == 200:
        tasks = res.json()
        if not tasks:
            st.info("보관함이 비어있습니다. 오늘 할 일을 추가해보세요!")
        else:
            for task in tasks:
                t_id = task.get('id')
                t_title = task.get('title')
                t_fixed = task.get('fixed') or task.get('isFixed')
                t_start = task.get('startTime')
                t_end = task.get('endTime')
                t_p_type = task.get('priorityType')
                t_days = task.get('daysOfWeek') # "월,화" 형태의 문자열 혹은 리스트
                t_duration = task.get('durationMinutes')
                
                with st.container(border=True):
                    c1, c2, c3 = st.columns([5, 3, 2])
                    with c1:
                        icon = "📌" if t_fixed else "🌰"
                        st.write(f"**{icon} {t_title}**")
                        
                        # 요일 정보 가공
                        day_info = ""
                        if t_days:
                            # 데이터가 리스트면 합치고, 문자열이면 그대로 사용
                            day_str = ", ".join(t_days) if isinstance(t_days, list) else t_days
                            day_info = f"🔄 매주 [{day_str}]"

                        # 시간 및 반복 정보 표시
                        if t_fixed and t_start:
                            time_str = f"🕒 {t_start[:5]} ~ {t_end[:5]}"
                            st.caption(f"{time_str} | {day_info}")
                        else:
                            # 일반 할 일인데 반복 설정이 있는 경우 포함
                            p_desc = priority_options.get(t_p_type, '').replace('\n', ' ')
                            st.caption(f"💡 {p_desc} | {day_info if day_info else '일회성'}")
                    
                    with c2:
                        st.write(f"**{t_p_type}순위**")
                        st.write(f"⏳ {t_duration}분")
                    
                    with c3:
                        # 미루기 버튼
                        if st.button("미루기", key=f"postpone_{t_id}"):
                            risk_res = api_service.check_postpone_risk(token, t_id)
                            if risk_res.status_code == 200:
                                risk_data = risk_res.json()
                                st.warning(f"{risk_data['riskProbability']}% 위험")
                                st.toast(risk_data['message'])
    else:
        st.error("목록을 불러오지 못했습니다.")