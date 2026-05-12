-- Switch all data symbols from stooq to Yahoo Finance tickers.
-- The column is still named stooq_symbol for schema stability;
-- it now holds whichever exchange symbol the fetch service needs.

UPDATE assets SET stooq_symbol = 'GC=F',
                  name         = 'Gold',
                  description  = 'Gold Futures (COMEX, continuous front-month)'
WHERE symbol = 'GOLD';

UPDATE assets SET stooq_symbol = 'SI=F',
                  name         = 'Silver',
                  description  = 'Silver Futures (COMEX, continuous front-month)'
WHERE symbol = 'SILVER';

UPDATE assets SET stooq_symbol = 'BZ=F',
                  name         = 'Brent Crude Oil',
                  description  = 'Brent Crude Oil Futures (ICE, continuous front-month)'
WHERE symbol = 'OIL_BRENT';

UPDATE assets SET stooq_symbol = 'CL=F',
                  name         = 'WTI Crude Oil',
                  description  = 'WTI Crude Oil Futures (NYMEX, continuous front-month)'
WHERE symbol = 'OIL_WTI';

UPDATE assets SET stooq_symbol = 'NG=F',
                  name         = 'Natural Gas',
                  description  = 'Natural Gas Futures (Henry Hub, NYMEX, continuous front-month)'
WHERE symbol = 'NAT_GAS';

UPDATE assets SET stooq_symbol = 'MU',
                  name         = 'Micron Technology',
                  description  = 'Micron Technology (NASDAQ: MU) — DRAM / NAND market proxy'
WHERE symbol = 'MU';
