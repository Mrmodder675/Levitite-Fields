# Reset-Gradle.ps1
# Cleans Gradle and NeoForged caches, then regenerates IntelliJ project files

Write-Host "🧹 Cleaning Gradle and NeoForged caches..." -ForegroundColor Cyan

# Stop any running Gradle daemons
./gradlew --stop

# Clean project build outputs
Write-Host "🧨 Running Gradle clean..." -ForegroundColor Cyan
./gradlew clean

# Clean NeoForged project cache
Write-Host "🧨 Cleaning NeoForged cache..." -ForegroundColor Cyan
./gradlew cleanCache

# Refresh dependencies
Write-Host "🔄 Refreshing Gradle dependencies..." -ForegroundColor Cyan
./gradlew --refresh-dependencies

# Regenerate IntelliJ project files
Write-Host "🛠️ Regenerating IntelliJ IDEA project files..." -ForegroundColor Cyan
./gradlew idea

Write-Host "🎉 Gradle and IntelliJ environment reset complete!" -ForegroundColor Green
