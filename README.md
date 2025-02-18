# ARBITRAGE - 비트코인 차익거래 사이트

![main](https://github.com/user-attachments/assets/041bbeea-6eac-4bca-8da8-646570dc3812)

## 프로젝트 소개
'ARBITRAGE'는 비트코인에서 '김치 프리미엄'이라는 한국 거래소의 가격과 외국 거래소의 가격 차이를 통한 거래를 지원하는 웹 사이트 프로젝트입니다. <br>
이 프로젝트는 Spring Boot 기반으로 개발되었으며, Upbit(국내 거래소)와 Binance(해외 거래소)의 API 연동을 통해 구매 및 판매를 효율적으로 처리할 수 있습니다. <br>
또한 Google Finance의 환율 정보를 주기적으로 업데이트하여 환율의 등락 또한 24시간 확인할 수 있도록 구현했습니다.

#### 실제로 주문이 들어가기 때문에 Test 계정을 제공하지 못하는 점 양해 부탁드립니다. 🙏

## 링크
* #### [URL](https://www.trade-arbitrage.shop/)
* #### [GitHub](https://github.com/dlgusgh4608/arbitrage-java)
* #### [Notion](https://www.notion.so/1963d37dd95680379db6c2da7dd0364b)

## 개발 환경
* Front-end: Thymeleaf, Bootstrap, Jquery, Chart.js
* Back-end: Spring boot, Gradle, JPA
* Database: PostgreSQL(RDS), Redis(Local)
* API: Google, Kakao, Binance, Upbit
* Version: Github, AWS S3
* Cloud(AWS): EC2, S3, CodeDeploy, RDS, Route53, ACM, EIP, ALB

## 개발 기간
2024.10.29 - 2025.~ing

## 페이지별 기능
#### [ 다크모드 지원 ]
![dark-mode](https://github.com/user-attachments/assets/8d4988a7-2e98-40f9-afcf-619023f78502)

#### [ 로그인 및 회원가입 화면 ]
* Email, Google, Kakao를 통해 로그인 및 회원가입을 할 수 있습니다.
* OAuth(Google, Kakao)를 통하여 회원가입을 진행할 경우, 그에 연동되는 Email회원가입도 함께 진행해야합니다.
  
|로그인타입|이메일로그인|
|---|---|
|![register-main](https://github.com/user-attachments/assets/7e9f75a8-612a-418c-b4b6-ae1bbc2d9de9)|![register-email](https://github.com/user-attachments/assets/529c6fc8-5041-4782-8704-a1b1d4f83058)|

#### [ 홈 ]
* 각 심볼의 차트 페이지로 이동 가능할 수 있습니다.
* 각 심볼에 대한 Upbit(국내 거래소), Binance(해외 거래소), Premium(김치 프리미엄)의 가격정보가 실시간으로 업데이트 됩니다.
![home](https://github.com/user-attachments/assets/badf590f-8090-49d9-af27-dfc6afba3acb)

#### [ 내 정보 ]
* 현재 나의 티어, 등급, 닉네임이 표시 됩니다.
* 닉네임을 수정 할 수 있습니다.
* API키를 통한 거래소 연결 상태가 표시 됩니다.
* API키에 대한 각 거래소에 나의 지갑상태를 표시합니다.
* 페이지에 접속한 순간의 환율 정보를 표시합니다.
  
|ENV등록안됨|ENV등록됨+.닉네임변경|
|---|---|
|![profile](https://github.com/user-attachments/assets/6735c5d1-b500-44b4-bc58-1f45106cb2ef)|![edit-nickname](https://github.com/user-attachments/assets/3668004f-2edd-4e76-a7e5-e272931f18a5)|

#### [ 주문 내역 ]
* 모든 심볼에 대한 주문내역 확인
* 필터링을 통한 주문확인 (추가 예정)
![order-history](https://github.com/user-attachments/assets/d17ac4da-9d0a-4f2b-b51d-95ccfc512209)

#### [ API키 등록 및 수정 ]
* API키를 등록 및 수정
* 각 거래소별 가이드 제공
* API키를 등록 및 수정시 각 거래소의 API를 통해 사용 가능한 KEY인지 확인
![api-register](https://github.com/user-attachments/assets/e7476bab-a30b-43b8-9411-8650e0a4f7ac)

#### [ 자동거래 설정 ]
* 등급별 자동거래 및 청산방지를 on, off를 통해 컨트롤 가능 (STANDARD, BUSINESS, FIRST)
* STANDARD (해당 기능 이용 불가)
* BUSINESS (청산방지 이용 가능)
* FIRST (청산방지 및 자동거래 이용 가능)
![setting](https://github.com/user-attachments/assets/9334ad72-e334-436d-8540-ae25ec145aeb)

#### [ 차트 ]
* 각 심볼에 대한 김치 프리미엄 차트
* 실시간 환율 트래킹
* 거래소별 매물대(호가창) 트래킹
* 심볼에 대한 거래소 포지션 트래킹
* 심볼에 대한 주문내역
* Binance(해외 거래소)에 대한 MarginMode, Leverage 변경
* 가격, 심볼, 포지션에 따른 주문 개수 수정 및 주문
* 차트의 Zoom, Pan에 따른 Paging (추가 예정)
* 각종 분봉 (추가 예정)
  
|MarginMode + Leverage변경|매수 + 매도|
|---|---|
|![change-mode](https://github.com/user-attachments/assets/ed97e2ea-1e1a-4688-b561-9a5336678736)|![order](https://github.com/user-attachments/assets/2323b97b-4eb4-499f-bb62-91142c1238de)|

## 목표
* 컴퓨팅 파워를 통한 자동거래
* 거래 발생시 User의 SNS로 거래 사실 알림 (Telegram or Slack)
* 자동 거래 안정화 이후 결제 모듈 등록 후 구독요금제 출시