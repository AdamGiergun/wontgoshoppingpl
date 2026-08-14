#!/usr/bin/env python3
"""Narzędzie do palet: sRGB <-> CIELAB/LCh, kontrast WCAG, symulacja deuteranopii.

Napisane od nowa, bo poprzednie było doraźnym skryptem w Bashu i nie przetrwało.
Trzymane w repozytorium narzędzi sesji; jeśli paleta ma być kiedyś zmieniana,
zaczynać stąd zamiast liczyć na oko.
"""
import math

# ── sRGB ↔ liniowe RGB ────────────────────────────────────────────────────────

def _do_liniowego(c):
    c /= 255.0
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4

def _z_liniowego(c):
    c = 0.0 if c < 0 else (1.0 if c > 1 else c)
    v = c * 12.92 if c <= 0.0031308 else 1.055 * c ** (1 / 2.4) - 0.055
    return round(v * 255)

def hex_na_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

def rgb_na_hex(rgb):
    return "#" + "".join(f"{max(0, min(255, c)):02X}" for c in rgb)

# ── XYZ / Lab / LCh (D65) ─────────────────────────────────────────────────────

BIALY = (0.95047, 1.0, 1.08883)

def rgb_na_xyz(rgb):
    r, g, b = (_do_liniowego(c) for c in rgb)
    return (0.4124 * r + 0.3576 * g + 0.1805 * b,
            0.2126 * r + 0.7152 * g + 0.0722 * b,
            0.0193 * r + 0.1192 * g + 0.9505 * b)

def xyz_na_rgb(xyz):
    x, y, z = xyz
    return tuple(_z_liniowego(v) for v in (
        3.2406 * x - 1.5372 * y - 0.4986 * z,
        -0.9689 * x + 1.8758 * y + 0.0415 * z,
        0.0557 * x - 0.2040 * y + 1.0570 * z))

def _f(t):
    return t ** (1 / 3) if t > 0.008856 else 7.787 * t + 16 / 116

def _f_odw(t):
    return t ** 3 if t ** 3 > 0.008856 else (t - 16 / 116) / 7.787

def rgb_na_lab(rgb):
    x, y, z = (v / w for v, w in zip(rgb_na_xyz(rgb), BIALY))
    fx, fy, fz = _f(x), _f(y), _f(z)
    return (116 * fy - 16, 500 * (fx - fy), 200 * (fy - fz))

def lab_na_rgb(lab):
    L, a, b = lab
    fy = (L + 16) / 116
    fx, fz = fy + a / 500, fy - b / 200
    return xyz_na_rgb(tuple(_f_odw(f) * w for f, w in zip((fx, fy, fz), BIALY)))

def lab_na_lch(lab):
    L, a, b = lab
    return (L, math.hypot(a, b), math.degrees(math.atan2(b, a)) % 360)

def lch_na_lab(lch):
    L, C, h = lch
    r = math.radians(h)
    return (L, C * math.cos(r), C * math.sin(r))

def lch_na_hex(L, C, h):
    return rgb_na_hex(lab_na_rgb(lch_na_lab((L, C, h))))

def hex_na_lch(h):
    return lab_na_lch(rgb_na_lab(hex_na_rgb(h)))

# ── kontrast WCAG ─────────────────────────────────────────────────────────────

def luminancja(rgb):
    r, g, b = (_do_liniowego(c) for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b

def kontrast(h1, h2):
    l1, l2 = luminancja(hex_na_rgb(h1)), luminancja(hex_na_rgb(h2))
    jasniejszy, ciemniejszy = max(l1, l2), min(l1, l2)
    return (jasniejszy + 0.05) / (ciemniejszy + 0.05)

# ── deuteranopia (Viénot–Brettel–Mollon) ──────────────────────────────────────

def deuteranopia(h):
    r, g, b = (_do_liniowego(c) for c in hex_na_rgb(h))
    L = 17.8824 * r + 43.5161 * g + 4.11935 * b
    M = 3.45565 * r + 27.1554 * g + 3.86714 * b
    S = 0.0299566 * r + 0.184309 * g + 1.46709 * b
    M2 = 0.494207 * L + 1.24827 * S
    r2 = 0.080944 * L - 0.130504 * M2 + 0.116721 * S
    g2 = -0.0102485 * L + 0.0540194 * M2 - 0.113615 * S
    b2 = -0.000365294 * L - 0.00412163 * M2 + 0.693513 * S
    return rgb_na_hex(tuple(_z_liniowego(v) for v in (r2, g2, b2)))
