# Piko NewX KR

[![원본 자동 동기화](https://github.com/tokyodiver/Piko-NewX-KR/actions/workflows/sync-upstream.yml/badge.svg)](https://github.com/tokyodiver/Piko-NewX-KR/actions/workflows/sync-upstream.yml)

[**➕ Morphe에 Piko NewX KR 추가**](https://morphe.software/add-source?github=tokyodiver/Piko-NewX-KR&name=Piko%20NewX%20KR)

[패치 파일 다운로드](https://github.com/tokyodiver/Piko-NewX-KR/releases/latest)

원본 [crimera/piko-newx v3.65.0](https://github.com/crimera/piko-newx/releases/tag/v3.65.0)과 동일한 Piko x-lite `0dd6dcea52b0973e630ed568a58d2de047ae3417` 기반의 비공식 수정본입니다. 원본처럼 NewX 전용으로 빌드합니다.

- NewX 설정 한국어 문자열 490개
- 라이트·다크 색상 개별 설정, 추천 색상·사각형 색상판·색조 막대·접이식 HEX 입력
- 일반 반응 아이콘·숫자의 중립색과 원래 강조색 단계 복원
- 색상 저장·초기화 후 X 재시작 확인창
- 설정 라디오의 선택 색상에 커스텀 강조색 적용
- 외관 → 작성 화면 → 모든 사람 버튼 숨기기 (기본 켜짐, X 12.37.2-prod.01 지원)
- 사용자 지정 Material You 사용/해제 및 초기화
- 정확한 HCT 팔레트 생성이 아닌 기존 색상 역할의 색조·채도 변환

## 적용

1. Android에 [Morphe Manager](https://github.com/MorpheApp/morphe-manager/releases/latest)를 설치합니다.
2. 위 **Morphe에 한국어 패치 추가** 링크를 휴대폰에서 열고 소스 추가를 승인합니다.
3. NewX를 선택하고 이 한국어 소스를 사용합니다. 기존 공식 Piko 소스와 중복 적용하지 않습니다.
4. 원본 X **12.37.2-prod.01 APKM**을 선택해 패치하고 설치합니다. APKM은 미리 병합하거나 변형하지 않습니다.
5. 사용자 지정 강조색을 저장하고 사용 옵션을 켠 뒤 X를 재실행합니다. 색상을 저장한 모드에서는 테마 선택보다 사용자 강조색이 우선하며, 원래 타래 연결선·글자·미선택 액션·배경을 유지합니다. 사용자 강조색을 끄면 선택한 테마로 돌아갑니다.

링크로 앱이 열리지 않으면 Morphe → Sources → ＋ → Remote에서 `https://github.com/tokyodiver/Piko-NewX-KR`를 입력합니다.

현재 소스가 선언한 지원 버전: 12.34.0-prod.01, 12.35.0-prod.01, 12.36.1-prod.01, 12.37.0-prod.01, 12.37.1-prod.01, 12.37.2-prod.01.
실제 기기에서 패치 적용 및 기능 동작은 아직 검증하지 않았습니다. 이 링크는 패치 소스 등록용이며 X 설치까지 자동으로 진행하지 않습니다.

## 버전 표기

원본 버전은 `3.65.0`로 유지하고 수정 차수를 `-v1`, `-v2`처럼 붙입니다. 현재 수정본은 `3.65.0-v2`, 파일명은 `piko-newx-kr-3.65.0-v2.mpp`입니다.

## 배포

수정 소스, 빌드 및 Morphe 배포 정보는 main 브랜치를 사용합니다. Actions의 Build Korean custom theme 작업은 NewX 전용 빌드 후 `gradle.properties`의 버전으로 GitHub Release와 MPP를 게시합니다. 새 버전을 배포할 때 `gradle.properties`와 `patches-bundle.json`의 버전·다운로드 URL·시각을 함께 갱신합니다. 기존 릴리스는 덮어쓰지 않습니다.

## 원본 자동 업데이트 확인

- 매일 오전 9시(한국 시간, GitHub 사정에 따라 지연 가능)에 원본 Piko NewX의 정식 릴리스를 확인합니다. PC를 켜둘 필요는 없습니다.
- 새 릴리스가 있으면 배포본이 가리키는 Piko `x-lite` 커밋까지의 변경을 3-way 병합합니다. 기존 한국어·사용자 강조색 수정은 유지하며, 충돌하면 자동 선택하지 않고 중단합니다. 새 원본 문구는 영어로 표시될 수 있습니다.
- 빌드·자동 테스트를 통과한 소스만 main에 반영하고 새 KR 릴리스로 배포합니다. 충돌·빌드 실패 시 기존 main과 릴리스를 유지합니다. APK 실기기 동작 검증은 자동으로 수행하지 않습니다.
- **확인:** 위 상태 배지 또는 [Actions → Sync upstream Piko NewX](https://github.com/tokyodiver/Piko-NewX-KR/actions/workflows/sync-upstream.yml) → 최신 실행 → Summary. 초록색 실행도 업데이트가 없었을 수 있으므로 요약의 결과와 원본/KR 버전을 확인하세요. 빨간색 실행은 실패한 단계에서 로그를 볼 수 있습니다.
- **수동 실행:** 같은 화면의 Run workflow. `check_only`를 켜면 업데이트 유무만 확인하며 소스를 바꾸거나 배포하지 않습니다. 배포된 파일은 [Releases](https://github.com/tokyodiver/Piko-NewX-KR/releases/latest)에서 확인합니다.

원본 저작자와 기여자의 권리는 LICENSE(GPLv3), NOTICE를 따릅니다. 한국어·테마 수정 설명은 CUSTOM_KO_THEME_README.md를 참고하세요.
