$f = 'c:\Users\WIN\Desktop\promodel-2026\SimEngine.java'
$c = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
$old = 'proc.operation.split("\n")'
$new = 'proc.operation.split("[\n|]")'
$c = $c.Replace($old, $new)
[System.IO.File]::WriteAllText($f, $c, [System.Text.Encoding]::UTF8)
Write-Host "Done: $($c -match [regex]::Escape($new))"
