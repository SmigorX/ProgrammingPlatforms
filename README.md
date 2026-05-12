Aplikacja do wizualizacji danych.

Backend w Javie, frontend w React.

Chcemy zrobić wizualizację do danych rynkowych, w tym wwypadku ropy i ram-u, ale z możliwością rozbudowy o inne dane, np. złoto, srebro, gaz itp.

Frontend ma pozwolić na:

- Rejestrację i logowanie użytkowników
- Wybór które wykresy pokazujemy i na jakim przedziale czasowym, wybory powninny być persistante pomiędzy sesjami, np. w local storage
- Wyświetlenie danych w formie wykresów, liczb, tabel itp.

Backend ma:

- Obsługiwać rejestrację i logowanie użytkowników
- Przechowywać dane o wykresach, (np. wykres słupkowy danych o ropie w kolorze czerwonym, wykres liniowy danych o ram-u w kolorze niebieskim itp. tabelka średniej ceny złota miesiąc temu, rok temu, 5 lat temu itp.)
- Dane do wykresów itp., np. ostatnie 5 lat cen ropy.
- Ściągać te dane z zewnętrznych API, np. stooq, investing itp. i pakować do lokalnej bazy czy cokolwiek innego jest darmowe

Aplikacja powinna być skonteneryzowana
