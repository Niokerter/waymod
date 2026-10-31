# Генератор текстур Way Mod (16x16, PNG через System.Drawing).
# Запуск: powershell -ExecutionPolicy Bypass -File tools/make_textures.ps1
Add-Type -AssemblyName System.Drawing

$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root "src\main\resources\assets\waymod\textures\block"
$itemDir = Join-Path $root "src\main\resources\assets\waymod\textures\item"
if (!(Test-Path $dir)) {
	New-Item -ItemType Directory -Force -Path $dir | Out-Null
}
if (!(Test-Path $itemDir)) {
	New-Item -ItemType Directory -Force -Path $itemDir | Out-Null
}

function Save-Texture {
	param([System.Drawing.Bitmap]$Bitmap, [string]$Name)
	$path = Join-Path $dir $Name
	$Bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
	$Bitmap.Dispose()
	Write-Host "OK $path"
}

function Save-ItemTexture {
	param([System.Drawing.Bitmap]$Bitmap, [string]$Name)
	$path = Join-Path $itemDir $Name
	$Bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
	$Bitmap.Dispose()
	Write-Host "OK $path"
}

# Полупрозрачный пиксель (для предметных текстур, где фон прозрачный).
function New-Argb {
	param([int]$A, [int]$R, [int]$G, [int]$B)
	return [System.Drawing.Color]::FromArgb($A, $R, $G, $B)
}

function New-Rgb {
	param([int]$R, [int]$G, [int]$B)
	return [System.Drawing.Color]::FromArgb(255, $R, $G, $B)
}

# ---------- way_block.png: тёмно-фиолетовый портал с бирюзовым вихрем ----------
$wayBlock = New-Object System.Drawing.Bitmap 16, 16
$base = New-Rgb 47 18 76
$edge = New-Rgb 26 9 44
$swirlA = New-Rgb 128 62 198
$swirlB = New-Rgb 187 143 255
$sparkle = New-Rgb 168 236 255

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		$isEdge = ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15)
		if ($isEdge) {
			$color = $edge
		}
		else {
			# спираль из центра: чем ближе к центру, тем чаще светлые точки
			$dx = $x - 7.5
			$dy = $y - 7.5
			$dist = [math]::Sqrt($dx * $dx + $dy * $dy)
			$swirl = [math]::Floor(($dx * 2.4 + $dy * 1.7 + [math]::Sin($dist * 1.9) * 1.5) + 6) % 9
			if ($swirl -eq 0) { $color = $sparkle }
			elseif ($swirl -lt 3 -and $dist -lt 6.5) { $color = $swirlB }
			elseif ($swirl -lt 5 -or $dist -lt 2.2) { $color = $swirlA }
			else { $color = $base }
		}
		$wayBlock.SetPixel($x, $y, $color)
	}
}
Save-Texture $wayBlock "way_block.png"

# ---------- craftable_spawner.png: клетка, похожая на ванильный спавнер, с оком Эндера ----------
$spawner = New-Object System.Drawing.Bitmap 16, 16
$bg = New-Rgb 26 26 30
$bar = New-Rgb 118 118 126
$barDark = New-Rgb 78 78 86
$eyeA = New-Rgb 106 190 122
$eyeB = New-Rgb 60 120 84
$pupil = New-Rgb 20 44 30

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		$inEye = ($x -ge 6 -and $x -le 9 -and $y -ge 6 -and $y -le 9)
		if ($inEye) {
			$color = $eyeA
			if (($x -eq 7 -or $x -eq 8) -and ($y -eq 7 -or $y -eq 8)) { $color = $pupil }
			elseif (($x -eq 6 -or $x -eq 9) -and ($y -eq 6 -or $y -eq 9)) { $color = $eyeB }
		}
		else {
			$isBar = (($x % 4) -eq 0 -or ($y % 4) -eq 0)
			if ($isBar) {
				$color = $bar
				if (($x % 4) -eq 0 -and ($y % 4) -eq 0) { $color = $barDark }
			}
			else {
				$color = $bg
			}
		}
		$spawner.SetPixel($x, $y, $color)
	}
}
Save-Texture $spawner "craftable_spawner.png"

# ---------- spawner_2000_pro.png: золотая клетка «Pro» с фиолетовым сиянием ----------
$pro = New-Object System.Drawing.Bitmap 16, 16
$pBg = New-Rgb 22 20 14
$pBar = New-Rgb 212 175 55
$pBarDark = New-Rgb 122 94 28
$pGlowA = New-Rgb 146 86 220
$pGlowB = New-Rgb 196 146 255
$pCore = New-Rgb 246 222 120

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		$inGlow = ($x -ge 5 -and $x -le 10 -and $y -ge 5 -and $y -le 10)
		if ($inGlow) {
			$color = $pGlowA
			if (($x -ge 7 -and $x -le 8) -and ($y -ge 7 -and $y -le 8)) { $color = $pCore }
			elseif (($x -eq 5 -or $x -eq 10) -and ($y -eq 5 -or $y -eq 10)) { $color = $pGlowB }
			elseif (($x -eq 6 -or $x -eq 9) -and ($y -eq 5 -or $y -eq 10)) { $color = $pGlowB }
			elseif (($x -eq 5 -or $x -eq 10) -and ($y -eq 6 -or $y -eq 9)) { $color = $pGlowB }
		}
		else {
			$isBar = (($x % 4) -eq 0 -or ($y % 4) -eq 0)
			if ($isBar) {
				$color = $pBar
				if (($x % 4) -eq 0 -and ($y % 4) -eq 0) { $color = $pBarDark }
			}
			else {
				$color = $pBg
			}
		}
		$pro.SetPixel($x, $y, $color)
	}
}
Save-Texture $pro "spawner_2000_pro.png"

# ---------- auto_hopper.png: тёмная воронка с бирюзовой лентой-конвейером ----------
$hop = New-Object System.Drawing.Bitmap 16, 16
$hTop = New-Rgb 158 158 168
$hMetal = New-Rgb 104 104 114
$hDark = New-Rgb 58 58 66
$hTape = New-Rgb 64 214 200
$hTapeDark = New-Rgb 30 122 116
$hArrow = New-Rgb 226 255 250

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		if ($y -le 2) {
			$color = $hTop
			if ($y -eq 2) { $color = $hDark }
		}
		elseif ($y -le 10) {
			$color = $hMetal
			if ($y -eq 5 -or $y -eq 6) {
				$color = $hTape
				if (($x % 4) -eq 1) { $color = $hArrow }
				elseif (($x % 4) -eq 3) { $color = $hTapeDark }
			}
			if ($x -eq 0 -or $x -eq 15) { $color = $hDark }
		}
		else {
			# воронка: к низу сужается
			$inset = ($y - 10)
			$color = $hDark
			if (($x -ge $inset) -and ($x -le (15 - $inset))) {
				$color = $hMetal
				if ($x -eq $inset -or $x -eq (15 - $inset)) { $color = $hTapeDark }
			}
		}
		$hop.SetPixel($x, $y, $color)
	}
}
Save-Texture $hop "auto_hopper.png"

# ---------- hopper_transmitter.png: воронка-передатчик с портальным вихрем ----------
$tr = New-Object System.Drawing.Bitmap 16, 16
$tTop = New-Rgb 148 132 176
$tMetal = New-Rgb 96 84 128
$tDark = New-Rgb 44 36 66
$tSwirl = New-Rgb 168 96 231
$tSwirlB = New-Rgb 216 168 255
$tSpark = New-Rgb 233 224 255

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		if ($y -le 2) {
			$color = $tTop
			if ($y -eq 2) { $color = $tDark }
		}
		elseif ($y -le 10) {
			$color = $tMetal
			if ($x -ge 4 -and $x -le 11 -and $y -ge 4 -and $y -le 9) {
				$dx = $x - 7.5
				$dy = $y - 6.5
				$dist = [math]::Sqrt($dx * $dx + $dy * $dy)
				$swirl = [math]::Floor(($dx * 2.1 + $dy * 1.8 + [math]::Sin($dist * 2.2) * 1.4) + 6) % 7
				if ($swirl -eq 0) { $color = $tSpark }
				elseif ($swirl -lt 3) { $color = $tSwirlB }
				else { $color = $tSwirl }
			}
			if ($x -eq 0 -or $x -eq 15) { $color = $tDark }
		}
		else {
			$inset = ($y - 10)
			$color = $tDark
			if (($x -ge $inset) -and ($x -le (15 - $inset))) {
				$color = $tMetal
				if ($x -eq $inset -or $x -eq (15 - $inset)) { $color = $tSwirl }
			}
		}
		$tr.SetPixel($x, $y, $color)
	}
}
Save-Texture $tr "hopper_transmitter.png"

# ---------- предметные ядра (item): общий шаблон «сфера в оправе» ----------
function New-Core {
	param([string]$Name, [System.Drawing.Color]$Ring, [System.Drawing.Color]$Dark, [System.Drawing.Color]$Fill, [System.Drawing.Color]$Hi, [int[][]]$Symbol)
	$bmp = New-Object System.Drawing.Bitmap 16, 16
	for ($y = 0; $y -lt 16; $y++) {
		for ($x = 0; $x -lt 16; $x++) {
			$bmp.SetPixel($x, $y, (New-Argb 0 0 0 0)) # прозрачный фон
		}
	}
	for ($y = 1; $y -le 14; $y++) {
		for ($x = 1; $x -le 14; $x++) {
			$dx = $x - 7.5
			$dy = $y - 7.5
			$dist = [math]::Sqrt($dx * $dx + $dy * $dy)
			$color = $null
			if ($dist -le 4.9) { $color = $Fill }
			elseif ($dist -le 6.4) { $color = $Dark }
			elseif ($dist -le 7.2) { $color = $Ring }
			if ($null -ne $color) {
				# блик сверху-слева
				if (($dist -ge 4.0 -and $dist -le 6.0) -and ($x + $y) -lt 13) { $color = $Hi }
				$bmp.SetPixel($x, $y, $color)
			}
		}
	}
	foreach ($p in $Symbol) {
		$bmp.SetPixel($p[0], $p[1], (New-Argb 255 255 255 255))
	}
	Save-ItemTexture $bmp "$Name.png"
}

# speed_core: молния
$speedBolt = @(
	@(8,3),@(8,4),@(7,5),@(6,6),@(7,7),@(8,7),
	@(7,8),@(6,9),@(5,10),@(6,10),@(7,10),@(8,10),
	@(7,11),@(6,12),@(7,12)
)
New-Core "speed_core" (New-Rgb 22 92 110) (New-Rgb 12 48 60) (New-Rgb 74 204 228) (New-Rgb 151 233 246) $speedBolt

# capacity_core: три полосы-«стопка»
$capacityStack = @()
foreach ($row in 4,7,10) {
	for ($x = 4; $x -le 11; $x++) {
		$capacityStack += ,@($x, $row)
		$capacityStack += ,@($x, ($row + 1))
	}
}
New-Core "capacity_core" (New-Rgb 34 92 34) (New-Rgb 20 56 20) (New-Rgb 108 190 92) (New-Rgb 168 230 150) $capacityStack

# killer_core: косой крест «клинки»
$killerX = @()
foreach ($i in 4..11) {
	$killerX += ,@($i, $i)
	$killerX += ,@($i, (15 - $i))
}
New-Core "killer_core" (New-Rgb 108 26 26) (New-Rgb 66 12 14) (New-Rgb 214 74 74) (New-Rgb 244 156 150) $killerX
