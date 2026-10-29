# 이모지 글꼴 진단 — 2026-10-29

- 보고 증상: 사용자 지정 이모지 글꼴을 켜고 폰트 추가 및 재시작한 후에도 게시물 본문·작성창 모두 기본 Android 이모지로 표시.
- 대상: com.twitter.android 12.37.2-prod.01, versionCode 312372001, 원본 APKM. 사용자 설치 버전·One UI 버전 미확인.
- 배포 소스: main ebab7e2, v3.65.0-v3.
- 진단 소스: 79f3fcc, test/emoji-font-runtime. 제품 코드 변경 없음.
- 새 빌드: https://github.com/tokyodiver/Piko-NewX-KR/actions/runs/40816727661
- 빌드 명령: :patches:build buildAndroid :patches:lintNewxResolvers :extensions:newx:testDebugUnitTest --tests app.morphe.extension.newx.misc.EmojiFontRuntimeTest (기존 테마 테스트 2종도 실행).
- 진단 MPP와 정식 v3.65.0-v3의 모든 ZIP 항목(서명 메타데이터 제외) 일치. 실제 비교 결과 차이 0개.
- 테스트 폰트 SHA256:
  - AppleColorEmoji-Linux.ttf: e37c7af6265ac4a0af6d57bc65e86109a776d9966e8343334557f63da482516f
  - iOS-Emoji-Android.ttf: f06ab19cd58f6c42ee7e367205e54dd7931c1344b641b977a415a279cf11319f
- Robolectric native graphics, API 29·34: 실제 UpdateFont.loadFont → processComposeEmoji → ReplacementSpan.getSize/draw 실행. 두 파일 모두 5개 이모지 시퀀스 처리·컬러 렌더링 성공. 테스트 2개 실패 0개. 단, 레지스트리 설정은 기존 단위 테스트 스텁을 사용하므로 실기기 저장·초기화 경로 검증은 아님.
- 로컬 APK 검증: PIKO_PATCH_SMOKE=true PIKO_PATCH_OUTPUT=analysis/newx/builds/emoji-font-smoke.apk ./patch-twitter.sh ../apk-analysis/original-12.37.2.apkm
- Applied: NewX: Custom font, NewX: Hide audience selector, NewX: Theme. Saved to analysis/newx/builds/emoji-font-smoke.apk. 패치 18.4초.
- 최종 DEX: androidx.compose.ui.text.b 생성자 두 분기 모두 processComposeEmoji 호출 및 move-result-object 확인. 설정 ID와 로딩 코드도 존재.
- 진단 도구 첫 실행은 테스트용 BooleanSetting 스텁과 Utils.setContext 내부 BaseSettings의 생성자 불일치로 실패. 실제 앱 장애가 아님. 테스트 컨텍스트 주입을 분리한 후 통과.
- 원인 분류: 실기기 런타임 동작, 아직 미확정. 리졸버·카디널리티·린터 오류 증거 없음.
- 한계: 실제 Samsung 장치, Compose 전체 화면, 파일 선택기/설정 저장 및 설치된 패치 버전을 아직 검증하지 못함. 제품 수정·정식 배포 없음.
