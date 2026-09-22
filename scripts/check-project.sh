#!/usr/bin/env bash
set -euo pipefail

echo "== MPorT Maps project structure check =="

required=(
  "settings.gradle.kts"
  "build.gradle.kts"
  "app/build.gradle.kts"
  "app/src/main/AndroidManifest.xml"
  "app/src/main/java/id/mport/maps/MainActivity.kt"
  "app/src/main/java/id/mport/maps/ui/home/HomeScreen.kt"
  "app/src/main/java/id/mport/maps/viewmodel/SurveyViewModel.kt"
  "app/src/main/res/values/strings.xml"
)

missing=0
for f in "${required[@]}"; do
  if [ -f "$f" ]; then
    echo "  OK  $f"
  else
    echo "  MISSING  $f"
    missing=1
  fi
done

# Package must be id.mport.maps
if grep -q 'applicationId = "id.mport.maps"' app/build.gradle.kts; then
  echo "  OK  applicationId = id.mport.maps"
else
  echo "  FAIL applicationId"
  missing=1
fi

if grep -q 'app_name">MPorT Maps' app/src/main/res/values/strings.xml; then
  echo "  OK  app_name = MPorT Maps"
else
  echo "  WARN app_name string"
fi

if [ "$missing" -ne 0 ]; then
  echo "Structure check FAILED"
  exit 1
fi
echo "Structure check PASSED"
