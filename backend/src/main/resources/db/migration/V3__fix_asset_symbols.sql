-- rcb.f, cl.f, ngg.f all return "No data" on stooq — they do not carry those
-- futures tickers. Replace with large US energy stocks that track the
-- underlying commodity prices closely and are available via the confirmed
-- working .us suffix format.

UPDATE assets
SET stooq_symbol = 'xom.us',
    name         = 'ExxonMobil (oil proxy)',
    description  = 'ExxonMobil Corp (NYSE: XOM) — used as a Brent crude price proxy'
WHERE symbol = 'OIL_BRENT';

UPDATE assets
SET stooq_symbol = 'cvx.us',
    name         = 'Chevron (oil proxy)',
    description  = 'Chevron Corp (NYSE: CVX) — used as a WTI crude price proxy'
WHERE symbol = 'OIL_WTI';

UPDATE assets
SET stooq_symbol = 'eqt.us',
    name         = 'EQT Corp (gas proxy)',
    description  = 'EQT Corp (NYSE: EQT) — largest US natural gas producer, used as a gas price proxy'
WHERE symbol = 'NAT_GAS';
