#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint agrid_flutter.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'agrid_flutter'
  s.version          = '0.0.1'
  s.summary          = 'The hassle-free way to add agrid to your Flutter app.'
  s.description      = <<-DESC
Postog flutter plugin
                       DESC
  s.homepage         = 'https://agrid.com/'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Agrid' => 'engineering@agrid.com' }
  s.source           = { :path => '.' }
  s.social_media_url = 'https://twitter.com/Agrid'

  s.source_files = 'Classes/**/*'
  s.resource_bundles = { "AgridFlutter" => "Resources/PrivacyInfo.xcprivacy" }
  
  s.ios.dependency 'Flutter'
  s.osx.dependency 'FlutterMacOS'

  # Use PostHog macOS SDK (version 3.x)
  s.dependency 'PostHog', '>= 3.32.0', '< 4.0.0'

  s.ios.deployment_target = '13.0'
  # PH iOS SDK 3.0.0 requires >= 10.15
  s.osx.deployment_target = '10.15'

  # Flutter.framework does not contain a i386 slice.
  s.ios.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.osx.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
  s.swift_version = '5.3'
end
