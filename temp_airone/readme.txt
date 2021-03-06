=======================================================================================================================================

** 앱 배포 버전 및 수정사항에 대한 정리 **

** 마켓 등록 앱정보 Version 03.08.00 / versionCode 29 / 마켓등록일 (2020/00/00) / 업데이트 예정 **
    - 프리즘 마켓 등록

** 마켓 등록 앱정보 Version 03.07.00 / versionCode 28 / 마켓등록일 (2020/04/16) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 03.06.00 / versionCode 26 / 마켓등록일 (2020/03/11) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 03.05.00 / versionCode 26 / 마켓등록일 (2020/03/03) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 3.4.0 / versionCode 25 / 마켓등록일 (2020/01/09) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 3.3.0 / versionCode 24 / 마켓등록일 (2019/10/31) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 3.2.0 / versionCode 23 / 마켓등록일 (2019/10/23) / 업데이트 완료 **
** 마켓 등록 앱정보 Version 3.1.0 / versionCode 22 / 마켓등록일 (2019/10/17) / 업데이트 완료 **
    - 듀얼링크 마켓 등록

** 마켓 등록 앱정보 Version 2.3.0 / versionCode 21 / 마켓등록일 (2019/05/08) / 업데이트 완료 **

=======================================================================================================================================

** 버전 변경 내역 **

- Version 03.07.01 / versionCode 29 (2020/04/16)
    * 듀얼링크와 버전 동기화하기 위해 버전 번경
    * 프리즘 제습 청정 적용
    * 에어모니터 밝기 4단계 변경
    * 공기리포트 그래프 수치 변경(40,60,80,100)

- Version 03.07.00 / versionCode 28 (2020/04/09) (CleanVentilation_2nd_03_07_00_live_20200409.apk - QE 검증)
    * AP 검색시 "SSID" 비정상 목룍 표시 현상 수정
    * 검색된 AP 감도 미표시 오류 최저값 설정 수정
    * 위치 설정시 지도 미표시 (네트워크 연결 후 인터넷 연결이 되지 않은 상태일 수 있어 맺 초기화 시간 딜레이 추가)
    * 알람 노티에 소스내용 표시 (알람 채널 설정시 메시지 내용으로 채널을 설정해서 발생된 문제로 채널 이름 변경)

- Version 03.07.00 / versionCode 28 (2020/04/07) (CleanVentilation_2nd_03_07_00_live_20200407.apk - QE 검증)
    * 앱이름 변경 (에어원 -> 나비엔 에어원)
    * 회원가입시 아이디 조회, 생성, AP 검색 오류 문구 세분화
    * 회원가입 및 HF 모듈 연동시 command 요청 처리 변경 (AP 리스트 조회시 +ok command 3회 재시도 추가)
    * 기존 회원가입시 아이디조회 -> 아이디생성 -> AP 목록 조회 순에서 아이디 생성 -> AP 목록 조회 처리하도록 변경

- Version 3.06.00 / versionCode 27 (2020/03/11)
    * 개인정보 처리방침 문구 변경

- Version 3.05.00 / versionCode 26 (2020/03/03)
    * 개인정보 처리방침 및 이용약관 분리
    * 제품 연동시 AP 암호화 방식 null일 경우 예외처리

- Version 3.4.0 / versionCode 25 (2020/01/09)
    * 생활리포트 주 그래프 표시 오류 수정 (매달 1일이 이전달 마지막주와 겹치는 경우 그래프 표시 오류 수정)
    * 개인정보처리방침 내용 수정

- Version 3.3.0 / versionCode 24 (2019/10/31)
    * 나비엔 하우스 웹페이지 개선으로인한 청정환기 필터 구매 URL 변경으로 인한 URL 수 (http://www.navienhouse.com/product/?code=A01) (디자인팀 전명헌 대리 요청)

- Version 3.2.0 / versionCode 23 (2019/10/24)
    * 생활리포트 실내, 실외 일별 데이터 23시까지 표시하도록 수정
    * 갤럭시폴드, 샤오미 mi9 그래프 표시되도록 수정
    * 1차 버전 앱에서 "실내청정" 탭에서 숙면모드 표시되지 않도록 수정 (숙면 모드는 듀얼링크에서 표시)

- Version 3.1.0 / versionCode 22 (2019/08/27)
    * 운영서버 연동
    * 에어모니터 연결 해제시 더보기 - 제품정보 - 에어모니터 선택시 표시되는 팝업에서 에어모니터 연결 화면으로 이동시 1차버전 화면으로 이동하여 분기처리
    * EID CMD에 대한 에러코트 파싱 처리 오류 (상하위 바이트 순서 변경)
    * 메인화면 실외데이터 pm2.5, pm10 표시 반대로 표시되는 문제 수정

- Version 3.0.9 / versionCode 22 (2019/08/26)
    * 운영서버 연동 URL 변경하여 신뢰성 전달
    * 버전 변경사항 없음

- Version 3.0.9 / versionCode 22 (2019/08/20)
    * 실외 온도 JSON 파싱시 long -> double 형으로 변경 및 실외 및 실내 정보 DATA 클래스 double 형으로 변경

- Version 3.0.8 / versionCode 22 (2019/08/16)

- Version 3.0.7 / versionCode 22 (2019/08/08)
    * 신뢰성 7차 피드백에 대한 수정
    * 앱초기화 버튼 삭제 (신뢰성에서 앱초기화 기능에 대해 초기화 진행시 초기화 정보 나열에 대한 지속적인 피드백으로 디자이너와 협의하여 제거)
    * OS 9 버전에서 회원가입 불가 문제 수정 (OS 9부터 AP SSID를 확인하기 위해 위치 활성화 되어있어야함)
        // 2019-03-22 추가 하였으나 익스모바일에 프로젝트 전달 과정에서 삭제된 것으로 추정
    * 필터관리 "필터 수명" %값 계산시 소수점 올림으로 수정

=======================================================================================================================================

** 신뢰성 이력에 대한 정리 **

* 청정환기 2차 듀얼링크 신뢰성 검증 완료일 2019/08/28

=======================================================================================================================================