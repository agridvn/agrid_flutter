[![Package on pub.dev][pubdev_badge]][pubdev_link]

# Agrid Flutter

## Hướng dẫn tích hợp Agrid cho Flutter (tham khảo PostHog Flutter)

Tài liệu này mô phỏng cấu trúc và nội dung từ hướng dẫn chính thức của PostHog Flutter, được điều chỉnh cho `agrid_flutter`. Tham khảo tài liệu gốc tại: [PostHog Flutter docs](https://posthog.com/docs/libraries/flutter).

### Nội dung

- Cài đặt
- Cấu hình
- Android setup
- iOS/macOS setup
- Web setup
- Khởi tạo trong Dart (manual)
- Gửi sự kiện (capture)
- Xác định người dùng (identify, alias)
- Distinct ID hiện tại
- Sự kiện ẩn danh vs. định danh (personProfiles)
- Super properties
- Group analytics
- Feature flags
- Experiments (A/B tests)
- Error tracking
- Session replay
- Surveys
- Hành vi offline
- Opt-out / Opt-in
- Debug mode

### Cài đặt

Thêm `agrid_flutter` vào `pubspec.yaml` của bạn:

```yaml
dependencies:
  flutter:
    sdk: flutter
  agrid_flutter: ^<phiên_bản_mới_nhất>
```

Sau đó chạy:

```bash
flutter pub get
```

### Cấu hình tổng quan

SDK sử dụng hàng đợi nội bộ để gửi sự kiện không chặn UI, đồng thời batch và flush bất đồng bộ. Hỗ trợ Android, iOS, macOS và Web.

Bạn có thể khởi tạo:
- Tự động: cấu hình qua `AndroidManifest.xml` (Android) và `Info.plist` (iOS/macOS).
- Thủ công: tắt `AUTO_INIT` và tự gọi `Agrid().setup(...)` trong Dart để có nhiều quyền kiểm soát.

Lưu ý host mặc định: `https://us.i.agrid.com`. Hãy thay bằng host phù hợp môi trường của bạn.

> Tham khảo cấu trúc và hướng dẫn tương tự từ tài liệu PostHog: `https://posthog.com/docs/libraries/flutter`.

### Android setup

Có 2 cách khởi tạo: Tự động hoặc Thủ công.

Tự động: thêm cấu hình vào `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="your.package.name">
  <application>
    <!-- ... other configuration ... 
    agrid_project_api_key là API key của dự án Agrid của bạn
    agrid_host là host Agrid bạn dùng, có thể dùng host demo: https://gw.track-asia.vn
    -->
    <meta-data android:name="com.agrid.agrid.API_KEY" android:value="<agrid_project_api_key>" />
    <meta-data android:name="com.agrid.agrid.AGRID_HOST" android:value="agrid_host" />
    <meta-data android:name="com.agrid.agrid.TRACK_APPLICATION_LIFECYCLE_EVENTS" android:value="false" />
    <meta-data android:name="com.agrid.agrid.DEBUG" android:value="false" />
      
    <!-- Bật thủ công nếu bạn muốn tự setup trong Dart -->
    <!-- <meta-data android:name="com.agrid.agrid.AUTO_INIT" android:value="false" /> -->
  </application>
</manifest>
```

Yêu cầu `minSdkVersion 21` trong `android/app/build.gradle`:

```gradle
defaultConfig {
  minSdkVersion 21
  // ...
}
```

Thủ công (nhiều tùy chỉnh hơn): đặt `AUTO_INIT = false` trong `AndroidManifest.xml`, sau đó khởi tạo trong Dart (xem mục “Khởi tạo trong Dart”).

> Với Session replay và Surveys, khuyến nghị dùng manual init (tắt `AUTO_INIT`) để chủ động cấu hình. Tham khảo thêm hướng dẫn PostHog: `https://posthog.com/docs/libraries/flutter`.

#### Cấu hình trong `android/settings.gradle`

1. Trong thẻ `plugins`, đảm bảo plugin `com.android.application` dùng phiên bản `8.2.2` trở lên.
2. Thêm các lệnh ở cuối file để ưu tiên sử dụng `posthog-android` từ Pub cache, nếu không có sẽ dùng trực tiếp từ Maven Central:

```groovy
def pubCachePath = System.getenv("PUB_CACHE") ?: "${System.getProperty("user.home")}/.pub-cache"
def posthogAndroidPathFromPubCache = new File("${pubCachePath}/hosted/pub.dev/agrid_flutter-5.9.6/android/posthog-android")
if (posthogAndroidPathFromPubCache.exists()) {
    includeBuild(posthogAndroidPathFromPubCache.absolutePath) {
        dependencySubstitution {
            substitute module('com.posthog:posthog-android') using project(':posthog-android')
        }
    }
}
```

### iOS/macOS setup

Thêm cấu hình vào `ios/Runner/Info.plist` (iOS) hoặc file Info.plist tương ứng (macOS):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <!-- phần cấu hình khác 
  agrid_project_api_key là API key của dự án Agrid của bạn
  agrid_host là host Agrid bạn dùng, có thể dùng host demo: https://gw.track-asia.vn
  -->
  <key>com.agrid.agrid.API_KEY</key>
  <string>agrid_project_api_key</string>
  <key>com.agrid.agrid.AGRID_HOST</key>
  <string>agrid_host</string>
  <key>com.agrid.agrid.CAPTURE_APPLICATION_LIFECYCLE_EVENTS</key>
  <true/>
  <key>com.agrid.agrid.DEBUG</key>
  <false/>
    
  <!-- Tùy chọn manual init
  <key>com.agrid.agrid.AUTO_INIT</key>
  <false/> 
  -->
</dict>
</plist>
```

Thiết lập platform tối thiểu trong `ios/Podfile`:

```ruby
platform :ios, '14.0'
```

### Web setup

Với Web, thêm `Web snippet` vào thẻ `<head>` của `web/index.html`. Ví dụ:

```html
<!-- phần cấu hình khác 
agrid_project_api_key là API key của dự án Agrid của bạn
agrid_host là host Agrid bạn dùng, có thể dùng host demo: https://gw.track-asia.vn
-->
<script async>
  !function(t,e){var o,n,p,r;e.__SV||(window.agrid=e,e._i=[],e.init=function(i,s,a){function g(t,e){var o=e.split(".");2==o.length&&(t=t[o[0]],e=o[1]),t[e]=function(){t.push([e].concat(Array.prototype.slice.call(arguments,0)))}}(p=t.createElement("script")).type="text/javascript",p.crossOrigin="anonymous",p.async=!0,p.src=s.api_host+"/static/array.js",(r=t.getElementsByTagName("script")[0]).parentNode.insertBefore(p,r);var u=e;for(void 0!==a?u=e[a]=[]:a="agrid",u.people=u.people||[],u.toString=function(t){var e="agrid";return"agrid"!==a&&(e+="."+a),t||(e+=" (stub)"),e},u.people.toString=function(){return u.toString(1)+".people (stub)"},o="capture identify alias people.set people.set_once set_config register register_once unregister opt_out_capturing has_opted_out_capturing opt_in_capturing reset isFeatureEnabled onFeatureFlags getFeatureFlag getFeatureFlagPayload reloadFeatureFlags group updateEarlyAccessFeatureEnrollment getEarlyAccessFeatures getActiveMatchingSurveys getSurveys getNextSurveyStep onSessionId".split(" "),n=0;n<o.length;n++)g(u,o[n]);e._i.push([i,s,a])},e.__SV=1)}(document,window.agrid||[]);
  agrid.init('agrid_project_api_key', {
    api_host: 'agrid_host',
    // defaults, debug, v.v...
  })
</script>
```

`agrid_flutter` trên Web sẽ gọi sang `window.agrid` (JS SDK).

### Khởi tạo trong Dart (manual init)

Chỉ cần khi bạn tắt `AUTO_INIT`. Ví dụ:

```dart
import 'package:flutter/material.dart';
import 'package:agrid_flutter/agrid_flutter.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  /*
  agrid_project_api_key là API key của dự án Agrid của bạn
  agrid_host là host Agrid bạn dùng, có thể dùng host demo: https://gw.track-asia.vn
  */
  final config = AgridConfig('agrid_project_api_key');
  config.host = 'agrid_host';
  config.debug = true;
  config.captureApplicationLifecycleEvents = true;
  // Bật Session replay và Surveys khi cần
  config.sessionReplay = false; // bật true nếu muốn
  config.surveys = true;

  await Agrid().setup(config);

  runApp(MyApp());
}
```

Khuyến nghị gắn `AgridObserver` để theo dõi màn hình và hiển thị Surveys:

```dart
MaterialApp(
  navigatorObservers: const [AgridObserver()],
  // ...
)
```

### Gửi sự kiện (capture)

```dart
await Agrid().capture(eventName: 'user_signed_up');
```

Gợi ý đặt tên theo mẫu “đối_tượng hành_động” (ví dụ: `video played`, `account deleted`).

### Xác định người dùng (identify, alias)

```dart
await Agrid().identify(
  userId: 'user_123',
  userProperties: {'email': 'a@b.com'},
);

await Agrid().alias(alias: 'legacy_user_id_456');
```

### Lấy distinct ID hiện tại

```dart
final id = await Agrid().getDistinctId();
```

### Sự kiện ẩn danh vs. định danh (personProfiles)

Thiết lập qua `AgridConfig.personProfiles`:
- `AgridPersonProfiles.identifiedOnly` (mặc định): gửi ẩn danh, chỉ định danh sau khi `identify/alias/group`.
- `AgridPersonProfiles.always`: luôn gửi sự kiện định danh.
- `AgridPersonProfiles.never`: luôn gửi ẩn danh.

```dart
final config = AgridConfig('<key>');
config.personProfiles = AgridPersonProfiles.identifiedOnly;
```

### Super properties

```dart
await Agrid().register('team_id', 22);
await Agrid().unregister('team_id'); // hoặc Agrid().reset() khi logout
```

Super properties gắn vào mọi sự kiện sau thời điểm đăng ký.

### Group analytics

```dart
await Agrid().group(groupType: 'company', groupKey: 'company_id_in_your_db');

await Agrid().group(
  groupType: 'company',
  groupKey: 'company_id_in_your_db',
  groupProperties: {'name': 'ACME Corp'},
);
```

### Feature flags

```dart
final enabled = await Agrid().isFeatureEnabled('flag-key');
if (enabled) {
  // logic khi bật cờ
  final payload = await Agrid().getFeatureFlagPayload('flag-key');
}

final variant = await Agrid().getFeatureFlag('flag-key');
if (variant == 'variant-key') {
  // logic cho biến thể
}

await Agrid().reloadFeatureFlags();
```

### Experiments (A/B tests)

Tận dụng feature flags:

```dart
if (await Agrid().getFeatureFlag('experiment-flag') == 'variant-name') {
  // Thực thi nhánh thử nghiệm
}
```

### Error tracking

Bật tự động thu thập lỗi Flutter/Dart/native trong `AgridConfig.errorTrackingConfig` (Android có hỗ trợ native Java/Kotlin):

```dart
final config = AgridConfig('<key>');
config.errorTrackingConfig.captureFlutterErrors = true;
config.errorTrackingConfig.capturePlatformDispatcherErrors = true;
config.errorTrackingConfig.captureNativeExceptions = true; // Android
```

Gửi exception thủ công:

```dart
try {
  // ...
} catch (e, st) {
  await Agrid().captureException(error: e, stackTrace: st, properties: {
    'module': 'checkout',
  });
}
```

### Session replay

```dart
final config = AgridConfig('<key>');
config.sessionReplay = true;
config.sessionReplayConfig.maskAllTexts = true;
config.sessionReplayConfig.maskAllImages = true;
```

Ẩn vùng nhạy cảm bằng widget mask:

```dart
import 'package:agrid_flutter/src/replay/mask/agrid_mask_widget.dart';

AgridMaskWidget(
  child: TextField(...),
)
```

Lưu ý: Bật “Record user sessions” trong cài đặt dự án Agrid/PostHog.

### Surveys

- Mặc định `config.surveys = true`.
- Cần cài `AgridObserver` để Surveys hiển thị theo điều kiện mục tiêu.
- Trên Web, Surveys dùng JS SDK.

### Hành vi offline

SDK vẫn xếp hàng sự kiện khi offline và flush khi online:
- `maxQueueSize`, `flushInterval`, `flushAt`, `maxBatchSize` có thể cấu hình trong `AgridConfig`.

### Opt-out / Opt-in

```dart
await Agrid().disable();    // opt-out
await Agrid().enable();     // opt-in
final isOut = await Agrid().isOptOut();
```

### Debug mode

```dart
/*
  agrid_project_api_key là API key của dự án Agrid của bạn
  agrid_host là host Agrid bạn dùng, có thể dùng host demo: https://gw.track-asia.vn
*/
final config = AgridConfig('agrid_project_api_key');
config.host = 'agrid_host';
config.debug = true;
await Agrid().setup(config);

// hoặc bật/tắt động:
await Agrid().debug(true);
```

### Liên kết tham khảo

- Tài liệu PostHog Flutter: `https://posthog.com/docs/libraries/flutter`

[pubdev_badge]: https://img.shields.io/pub/v/agrid_flutter
[pubdev_link]: https://pub.dev/packages/agrid_flutter