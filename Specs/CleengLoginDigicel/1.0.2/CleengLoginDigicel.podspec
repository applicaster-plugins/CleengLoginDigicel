Pod::Spec.new do |s|
  s.name         = 'CleengLoginDigicel'
  s.version      = '1.0.2'
  s.summary      = 'Cleeng login plugin fork for Digicel'
  s.license      = 'MIT'
  s.homepage     = 'https://github.com/applicaster-plugins/CleengLoginDigicel'
  s.author       = {"udi lumnitz" => "u.lumnitz@applicaster.com"}
  s.ios.deployment_target = '10.0'
  s.swift_version = '5.1'
  s.source       = { :git => "git@github.com:applicaster-plugins/CleengLoginDigicel.git", :tag => 'ios-' + s.version.to_s }
  s.source_files = 'iOS/CleengLogin/**/*.{swift,h,m}'
  s.requires_arc = true
  s.static_framework = true
  s.dependency 'ZappPlugins'
  s.dependency 'Alamofire'
  s.dependency 'CAM', '3.5.0'
  s.dependency 'ApplicasterSDK'
  s.dependency 'ZappLoginPluginsSDK'
  s.xcconfig =  { 'CLANG_ALLOW_NON_MODULAR_INCLUDES_IN_FRAMEWORK_MODULES' => 'YES',
    'ENABLE_BITCODE' => 'YES',
    'SWIFT_VERSION' => '5.1'
  }
end
