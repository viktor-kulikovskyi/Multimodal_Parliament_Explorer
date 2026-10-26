<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Parlaments-Explorer</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <nav class="navbar">
        <div class="nav-brand">
            Parlaments-Explorer
        </div>
        <div class="nav-links">
            <a href="/" class="active">Start</a>
            <a href="/visualisierung">Visualisierung</a>
            <a href="/suche">Suche</a>
            <a href="/export" >Export</a>
        </div>
    </nav>

    <div class="container">
        <div class="hero">
            <h1>Willkommen beim Parlaments-Explorer</h1>
            <p>Multimodale Analyse und Visualisierung von Bundestagsdebatten</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number">${redenCount!0}</div>
                <div class="stat-label">Reden in der Datenbank</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">${abgeordnetenCount!0}</div>
                <div class="stat-label">Abgeordnete erfasst</div>
            </div>
        </div>

        <div class="feature-grid">
            <a href="/visualisierung" class="feature-card">
                <h3>Visualisierung</h3>
                <p>Topics, POS, Sentiments und Named Entities als interaktive d3.js Diagramme (Radar, Sunburst, Line, Bar Chart)</p>
            </a>
            <a href="/suche" class="feature-card">
                <h3>Reden durchsuchen</h3>
                <p>Volltextsuche mit Filtern nach Fraktion und Zeitraum. Detailansicht mit NLP-Annotationen.</p>
            </a>
        </div>
    </div>


</body>
</html>
