"""Generuje daty dni handlowych/niehandlowych wg ustawy o ograniczeniu handlu
w niedziele i święta (Dz.U. 2018 poz. 305, ze zm. obowiązującą od 1.02.2025).

Reguła niedziel handlowych:
  - ostatnia niedziela stycznia, kwietnia, czerwca i sierpnia,
  - niedziela bezpośrednio poprzedzająca Wielkanoc (Niedziela Palmowa),
  - trzy kolejne niedziele poprzedzające 24 grudnia.
"""
import datetime as dt
import json
import sys


def easter(year):
    """Meeus/Jones/Butcher — Wielkanoc gregoriańska."""
    a = year % 19
    b, c = divmod(year, 100)
    d, e = divmod(b, 4)
    f = (b + 8) // 25
    g = (b - f + 1) // 3
    h = (19 * a + b - d - g + 15) % 30
    i, k = divmod(c, 4)
    l = (32 + 2 * e + 2 * i - h - k) % 7
    m = (a + 11 * h + 22 * l) // 451
    month, day = divmod(h + l - 7 * m + 114, 31)
    return dt.date(year, month, day + 1)


def sundays(year):
    d = dt.date(year, 1, 1)
    d += dt.timedelta(days=(6 - d.weekday()) % 7)
    while d.year == year:
        yield d
        d += dt.timedelta(days=7)


def last_sunday_of(year, month):
    return max(s for s in sundays(year) if s.month == month)


def commerce_sundays(year):
    e = easter(year)
    result = {last_sunday_of(year, m) for m in (1, 4, 6, 8)}
    result.add(e - dt.timedelta(days=7))          # Niedziela Palmowa
    eve = dt.date(year, 12, 24)
    # trzy kolejne niedziele poprzedzające Wigilię
    last = eve - dt.timedelta(days=(eve.weekday() + 1) % 7 or 7)
    result.update(last - dt.timedelta(days=7 * n) for n in range(3))
    return sorted(result)


def holidays(year):
    e = easter(year)
    return [
        (dt.date(year, 1, 1), "new_year"),
        (dt.date(year, 1, 6), "epiphany"),
        (e, "easter_sunday"),
        (e + dt.timedelta(days=1), "easter_monday"),
        (dt.date(year, 5, 1), "labour_day"),
        (dt.date(year, 5, 3), "constitution_day"),
        (e + dt.timedelta(days=49), "whit_sunday"),
        (e + dt.timedelta(days=60), "corpus_christi"),
        (dt.date(year, 8, 15), "assumption"),
        (dt.date(year, 11, 1), "all_saints"),
        (dt.date(year, 11, 11), "independence_day"),
        # Od 2025 Wigilia jest ustawowo dniem wolnym od pracy, więc to święto,
        # a nie dzień niehandlowy (Dz.U. 2024 poz. 1965).
        (dt.date(year, 12, 24), "christmas_eve"),
        (dt.date(year, 12, 25), "christmas_day"),
        (dt.date(year, 12, 26), "second_day_of_christmas"),
    ]


def build(year):
    commerce = set(commerce_sundays(year))
    holiday_dates = {d for d, _ in holidays(year)}
    e = easter(year)

    holy_saturday = e - dt.timedelta(days=1)

    # Święta (w tym Wigilia, która bywa niedzielą, np. 2028) są pomijane wśród
    # niedziel niehandlowych, bo ta sama data trafiłaby do danych dwa razy
    # i przy imporcie jeden wpis nadpisałby drugi.
    non_commerce = [
        (s, "non_commerce_sunday", "00:00")
        for s in sundays(year)
        if s not in commerce and s not in holiday_dates
    ]
    # Wielka Sobota: ustawa dopuszcza handel do 14:00, ale podajemy 13:00 celowo.
    # 14:00 to godzina wyjścia pracowników, nie zamknięcia sklepu dla klientów —
    # sklepy zamykają kasy wcześniej, żeby zdążyć z operacjami zamknięcia dnia.
    # Lepiej, żeby klient przyszedł za wcześnie niż pocałował klamkę.
    non_commerce.append((holy_saturday, "holy_saturday", "13:00"))
    non_commerce.sort()

    wszystkie = [d for d, _, _ in non_commerce] + sorted(commerce) + sorted(holiday_dates)
    duplikaty = {d for d in wszystkie if wszystkie.count(d) > 1}
    if duplikaty:
        raise AssertionError(f"{year}: data powtórzona w danych: {sorted(duplikaty)}")

    return {
        "nonCommerceDays": [
            {"date": d.strftime("%Y_%m_%d"), "time": t, "key": k}
            for d, k, t in non_commerce
        ],
        "commerceSundays": [
            {"date": d.strftime("%Y_%m_%d"), "time": "00:00", "key": "commerce_sunday"}
            for d in sorted(commerce)
        ],
        "holidays": [
            {"date": d.strftime("%Y_%m_%d"), "time": "00:00", "key": k}
            for d, k in sorted(holidays(year))
        ],
    }


if __name__ == "__main__":
    years = [int(a) for a in sys.argv[1:]] or [2026, 2027]
    print(json.dumps({str(y): build(y) for y in years}, indent=2))

# Użycie:
#   python3 tools/gen_days.py 2028 2029 > app/src/main/res/raw/days.json
# albo, żeby wygenerować payload do wklejenia w parametr `days_data`
# w konsoli Firebase Remote Config (wtedy bez wydawania nowej wersji):
#   python3 tools/gen_days.py 2028 2029
