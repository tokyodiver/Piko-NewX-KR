# Piko NewX 한국어 + 커스텀 테마

[**➕ Morphe에 Piko NewX KR 추가**](https://morphe.software/add-source?github=tokyodiver/piko_kr&name=Piko%20NewX%20KR)

[패치 파일 다운로드](https://github.com/tokyodiver/piko_kr/releases/latest)

원본 [crimera/piko-newx v3.64.1](https://github.com/crimera/piko-newx/releases/tag/v3.64.1)과 동일한 Piko x-lite `eaa255276df4e5514cae8fc42eb014fca83ea786` 기반의 비공식 수정본입니다. 원본처럼 NewX 전용으로 빌드합니다.

- NewX 설정 한국어 문자열 490개
- 라이트·다크 색상 개별 설정, 추천 색상·사각형 색상판·색조 막대·접이식 HEX 입력
- 버튼·강조 아이콘의 사용자 지정 색상을 기존 시스템 팔레트로 탁하게 만들지 않고 직접 적용
- 사용자 지정 Material You 사용/해제 및 초기화
- 정확한 HCT 팔레트 생성이 아닌 기존 색상 역할의 색조·채도 변환

## 적용

1. Android에 [Morphe Manager](https://github.com/MorpheApp/morphe-manager/releases/latest)를 설치합니다.
2. 위 **Morphe에 한국어 패치 추가** 링크를 휴대폰에서 열고 소스 추가를 승인합니다.
3. NewX를 선택하고 이 한국어 소스를 사용합니다. 기존 공식 Piko 소스와 중복 적용하지 않습니다.
4. 원본 X **12.37.2-prod.01 APKM**을 선택해 패치하고 설치합니다. APKM은 미리 병합하거나 변형하지 않습니다.
5. X의 Piko 설정 → 외관 → 테마에서 Material You를 선택하고 사용자 지정 색상을 설정한 뒤 앱을 재실행합니다.

링크로 앱이 열리지 않으면 Morphe → Sources → ＋ → Remote에서 `https://github.com/tokyodiver/piko_kr`를 입력합니다.

현재 소스가 선언한 지원 버전: 12.34.0-prod.01, 12.35.0-prod.01, 12.36.1-prod.01, 12.37.0-prod.01, 12.37.1-prod.01, 12.37.2-prod.01.
실제 기기에서 패치 적용 및 기능 동작은 아직 검증하지 않았습니다. 이 링크는 패치 소스 등록용이며 X 설치까지 자동으로 진행하지 않습니다.

## 버전 표기

원본 버전은 `3.64.1`로 유지하고 수정 차수를 `-v1`, `-v2`처럼 붙입니다. 현재 수정본은 `3.64.1-v1`, 파일명은 `piko-newx-ko-custom-3.64.1-v1.mpp`입니다.

## 배포

수정 소스, 빌드 및 Morphe 배포 정보는 main 브랜치를 사용합니다. Actions의 Build Korean custom theme 작업은 NewX 전용 빌드 후 `gradle.properties`의 버전으로 GitHub Release와 MPP를 게시합니다. 새 버전을 배포할 때 `gradle.properties`와 `patches-bundle.json`의 버전·다운로드 URL·시각을 함께 갱신합니다. 기존 릴리스는 덮어쓰지 않습니다.

원본 저작자와 기여자의 권리는 LICENSE(GPLv3), NOTICE를 따릅니다. 한국어·테마 수정 설명은 CUSTOM_KO_THEME_README.md를 참고하세요.
