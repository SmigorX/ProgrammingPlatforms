-- Switch all data symbols to Alpha Vantage tickers.
-- Alpha Vantage supports ETFs and equities listed on US exchanges;
-- the stooq_symbol column continues to hold whichever external symbol
-- the fetch service needs.  Commodity futures require the =F suffix.

UPDATE assets SET stooq_symbol = 'GLD',
                  name         = 'Gold (SPDR ETF)',
                  description  = 'SPDR Gold Shares (NYSE: GLD) — tracks the price of gold bullion'
WHERE symbol = 'GOLD';

UPDATE assets SET stooq_symbol = 'SLV',
                  name         = 'Silver (iShares ETF)',
                  description  = 'iShares Silver Trust (NYSE: SLV) — tracks the price of silver bullion'
WHERE symbol = 'SILVER';

UPDATE assets SET stooq_symbol = 'BNO',
                  name         = 'Brent Crude Oil (ETF)',
                  description  = 'United States Brent Oil Fund (NYSE: BNO) — tracks Brent crude oil futures'
WHERE symbol = 'OIL_BRENT';

UPDATE assets SET stooq_symbol = 'USO',
                  name         = 'WTI Crude Oil (ETF)',
                  description  = 'United States Oil Fund (NYSE: USO) — tracks WTI crude oil futures'
WHERE symbol = 'OIL_WTI';

UPDATE assets SET stooq_symbol = 'UNG',
                  name         = 'Natural Gas (ETF)',
                  description  = 'United States Natural Gas Fund (NYSE: UNG) — tracks Henry Hub natural gas futures'
WHERE symbol = 'NAT_GAS';

UPDATE assets SET stooq_symbol = 'MU',
                  name         = 'Micron Technology',
                  description  = 'Micron Technology (NASDAQ: MU) — DRAM / NAND market proxy'
WHERE symbol = 'MU';
