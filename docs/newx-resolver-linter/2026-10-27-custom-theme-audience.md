# Custom theme and audience selector validation (2026-10-27)

User-reported issues: unselected reaction icons/counts received the chosen accent; color save did not offer restart; settings selection colors could ignore custom accent; hide composer audience selector requested, default on.

Target: com.twitter.android 12.37.2-prod.01, version code 312372001, user-supplied original APKM with two architectures and two densities.
Built source: 14de00b, GitHub Actions run 40514599435, buildAndroid successful.
Output bundle: piko-newx-kr-3.64.1-v3.mpp.

Scope: reverted inline action primary override and flattened accent ramp; ordinary on-surface roles retain original colors. Color save/reset opens existing shared DialogView restart prompt without changing enabled state (default remains false). Settings primary accent resolves saved custom color first when dynamic custom colors are enabled.

Audience resolver: resource name narrowcast_default_audience -> narrowcast label function -> merged Function2 label/chevron content -> isolated composer/ui button renderer. Rejects missing/ambiguous candidates. Gated to the production version actually checked. Hides renderer before its Compose group starts; audience model is not changed.

Validation command: PIKO_PATCH_SMOKE=true MORPHE_CLI_JAR=../tools/morphe-cli.jar PIKO_PATCH_OUTPUT=../x-original/v3-smoke-patched.apk bash ./patch-twitter.sh ../x-original/original.apkm
CLI: Morphe Desktop 1.19.1, Java 21; smoke output unsigned, no installation.
Result: Applied: NewX: Hide audience selector; Applied: NewX: Theme; rebuilt APK and Saved to v3-smoke-patched.apk, exit 0.
Final DEX: renderer com/x/composer/ui/n.p in classes16.dex begins with SettingsRegistry.getBooleanOrDefault guard and conditional return before original Compose code.
Injected SettingsRegistry.load: hide_audience registerToggle receives const/4 v5, 1 (default on); configureToggle receives reboot/visible true.
Existing translation sanitizer warning for piko_newx_settings_search_no_results did not prevent rebuilding.
Device UI, actual restart behavior and older targets are not validated. No phone was installed or controlled.

MPP SHA256: f53a3b0e32f450d73bb734b895199e9283e9649ee54348823c61b8b8bd70f018
