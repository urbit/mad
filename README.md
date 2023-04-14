# Monthly Active Developers

This repository contains the source code used to generate our metrics on monthly
active developer activity. Here's a brief overview of how it works:

`src/main.clj` contains logic that fetches all commits from the set of GitHub
repositories defined in `src/data.clj` and loads them into a PostgreSQL
database for analysis.

`sql/ga-developers.sql` is drawn from [Tribe Capital's Growth Accounting framework](https://tribecap.co/a-quantitative-approach-to-product-market-fit/) and slightly modified for our purposes.

`resources/d3/mad.html` is what we use to generate a visualization. The fonts
referenced in this file and used in the visualization are not open source and
are thus not included.

NOTE: This code is messy and intended for our own internal use. You can run it
if you're familiar with Clojure and can set up a local Postgres database. Feel
free to reach out to `~wolref-podlex` if you need assistance.

