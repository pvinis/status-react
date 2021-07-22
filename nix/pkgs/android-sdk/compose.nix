#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ stdenv, config, callPackage, androidenv, openjdk, mkShell }:

# The "android-sdk-license" license is accepted
# by setting android_sdk.accept_license = true.
androidenv.composeAndroidPackages {
  toolsVersion = "26.1.1";
  platformToolsVersion = "31.0.2";
  buildToolsVersions = [ "30.0.3" ];
  platformVersions = [ "29" ];
  cmakeVersions = [ "3.18.1" ];
  ndkVersion = "22.1.7171670";
  includeNDK = true;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
