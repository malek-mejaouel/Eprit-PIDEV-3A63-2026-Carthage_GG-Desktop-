Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
$synth.SpeakAsync('This is a test to see if PowerShell speaks.') | Out-Null
while ($true) {
    $cmd = [Console]::ReadLine()
    if ($cmd -eq 'stop' -or $cmd -eq $null) { $synth.SpeakAsyncCancelAll(); break }
}