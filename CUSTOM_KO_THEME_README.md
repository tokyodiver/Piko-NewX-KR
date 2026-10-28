# Piko NewX — 한국어 설정 / 사용자 지정 Material You (source patch)

기준: 사용자 제공 `piko-x-lite.zip`, `x-lite` snapshot (2026-10-25, archive commit eaa255276df4e5514cae8fc42eb014fca83ea786).

## 적용된 소스 변경

- NewX 설정 문자열 490개에 대한 한국어 `values-ko-rKR/newx/strings.xml` 추가 (영어 491개 중 `patch_version`은 `translatable=false`라 제외).
- `NewX: Theme` 패치에 사용자 지정 색상 사용 토글, 색상 설정 화면, 숨겨진 라이트/다크 색상 저장 설정을 추가.
- `CustomThemeFragment`: 라이트/다크 모드별 HSV 컬러 피커, HEX 입력, 색상 미리보기, 색상 초기화.
- `CustomMaterialColors`: HEX 검증, Piko 설정 레지스트리를 통한 저장/읽기, 기존 Material You 색상 리소스 호출 경로에만 사용자 선택 색상 적용.
- `ColorSeedTransform`: 시스템의 Material You 역할별 색상에서 색조 및 채도를 상대적으로 이동하여 밝기/투명도 보존. **정확한 Material Color Utilities HCT 팔레트 생성은 아님.**
- NewX 자체의 설정 UI 테마 색상도 가능한 동일한 리소스 경로에서 재매핑.

## UI에서 사용하는 방법 (실제 앱에서의 동작은 미검증)

1. 원본 글자·미선택 액션·배경을 유지하려면 테마를 `기본`으로 선택합니다. Material You·고대비는 전체 팔레트를 변경합니다. Dim에는 사용자 지정색을 적용하지 않습니다.
2. `사용자 지정 강조색 선택`에서 라이트/다크 각각 컬러 피커 또는 `#RRGGBB` HEX 입력.
3. `사용자 지정 강조색 사용` 활성화 후 X 재실행.
4. 기본 색상으로 돌리려면 `사용자 지정 강조색 사용`을 끄고 재실행. 별도로 `두 색상 초기화`를 선택하면 저장한 두 HEX도 비워짐.
5. 앱 내부의 설정 백업/복원은 등록한 두 문자열과 토글을 포함하도록 설계함. **실기기 검증 전**.

## 빌드

원본 프로젝트의 `./gradlew clean buildAndroid` (Windows: `gradlew.bat clean buildAndroid`) 명령. 성공하면 `patches/build/libs/patches-*.mpp` 확인. 이 프로젝트의 Gradle 9.8.2/Android SDK/사설 GitHub Packages(필요시 GITHUB_TOKEN) 의존성을 해결해야 함. 상위 `piko-newx` 빌드 스크립트는 별도 배포 자동화에 사용됨.

**현재 작업 환경에는 Gradle 9.8.2가 설치돼 있지 않고 네트워크로 설치할 수도 없어 실제 Gradle 컴파일, `.mpp` 생성, APK 적용 테스트를 하지 못했습니다.**

확인한 사항: 문자열 리소스 490개 XML 파싱/키/서식 지정자 검사 통과, 추가한 컬러 피커/설정 코드의 Java 스텁 컴파일 통과, 색상 변환에 대한 순수 Java 테스트 6개 통과. 앱 리소스 AAPT/Gradle/Kotlin DSL/단말 검사는 미실행.

개발자 소스는 GPLv3 기반이므로 원래 저장소의 LICENSE 및 NOTICE를 유지합니다.
