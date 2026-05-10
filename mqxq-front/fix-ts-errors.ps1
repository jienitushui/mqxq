# Fix common TypeScript errors

# Fix unused parameters by prefixing with underscore
$files = @(
    "src/router/index.ts",
    "src/pages/profile/ProfileSettings.vue",
    "src/pages/announcements/AnnouncementDetail.vue"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        
        # Fix unused parameters
        $content = $content -replace '\(to, from, savedPosition\)', '(_to, _from, savedPosition)'
        $content = $content -replace '\(to, from, next\)', '(to, _from, next)'
        $content = $content -replace 'validator: \(rule,', 'validator: (_rule,'
        $content = $content -replace '\.filter\(item =>', '.filter((item: any) =>'
        
        Set-Content $file $content -NoNewline
    }
}

Write-Host "Fixed common TypeScript errors"
