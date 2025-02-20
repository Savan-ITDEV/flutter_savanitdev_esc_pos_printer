
#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_savanitdev_printer.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_savanitdev_printer'
  s.version          = '1.1.5'
  s.summary          = 'A new Flutter project.'
  s.description      = <<-DESC
A new Flutter project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  s.vendored_libraries = 'PrinterSDK/libPrinterSDK.a'
  # s.public_header_files = 'PrinterSDK/Headers/*.h'

  # s.preserve_paths = 'PrinterSDK/**/*'
  # s.xcconfig = { 
  #   'LIBRARY_SEARCH_PATHS' => '$(PODS_TARGET_SRCROOT)/PrinterSDK',
  #   'HEADER_SEARCH_PATHS' => '$(PODS_TARGET_SRCROOT)/PrinterSDK/Headers',
  # }

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES', 
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64',
    # 'HEADER_SEARCH_PATHS' => '${PODS_ROOT}/Headers/Public/flutter_savanitdev_printer/PrinterSDK/Headers' 
  }
  s.swift_version = '5.0'
end
