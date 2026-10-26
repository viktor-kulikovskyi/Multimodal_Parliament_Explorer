<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Suche - Parlaments-Explorer</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <nav class="navbar">
        <div class="nav-brand">Parlaments-Explorer</div>
        <div class="nav-links">
            <a href="/">Start</a>
            <a href="/visualisierung">Visualisierung</a>
            <a href="/suche" class="active">Suche</a>
            <a href="/export">Export</a>
        </div>
    </nav>

    <div class="container">
        <h1>Reden durchsuchen</h1>

        <form method="get" action="/suche" class="search-form">
            <div class="search-bar">
                <input type="text" name="q" id="searchInput" placeholder="Suchbegriff eingeben..." value="${q!}">
                <button type="submit" class="btn btn-primary">Suchen</button>
            </div>
            <div class="search-filters">
                <div class="filter-group">
                    <label for="fraktion">Fraktion:</label>
                    <select name="fraktion" id="fraktion">
                        <option value="">Alle Fraktionen</option>
                        <#if fraktionen??>
                            <#list fraktionen as f>
                                <option value="${f}" <#if (fraktion!"")==f>selected</#if>>${f}</option>
                            </#list>
                        </#if>
                    </select>
                </div>
            </div>
        </form>

        <div class="search-results">
            <#if results??>
                <div class="results-info">
                    ${results?size} Ergebnis(se) gefunden
                </div>
                <#if (results?size > 0)>
                    <#list results as rede>
                        <div class="result-card">
                            <div class="result-header">
                                <strong>${(rede.sprecherName)!"Unbekannt"}</strong>
                                <#if (rede.sprecherFraktion)??>
                                    <span class="badge">${rede.sprecherFraktion}</span>
                                </#if>
                            </div>
                            <div class="result-text">
                                <#if rede.text??>
                                    <#if (rede.text?length > 300)>
                                        ${rede.text?substring(0, 300)}...
                                    <#else>
                                        ${rede.text}
                                    </#if>
                                <#else>
                                    <em>Kein Text verf&uuml;gbar</em>
                                </#if>
                            </div>
                            <a href="/rede/${rede["_id"]}" class="btn btn-sm">
                                Rede anzeigen
                            </a>
                        </div>
                    </#list>
                <#else>
                    <div class="no-results">
                        Keine Reden gefunden.
                    </div>
                </#if>
            <#else>
                <div class="no-results">
                    Bitte Suchbegriff eingeben und suchen.
                </div>
            </#if>
        </div>
    </div>


</body>
</html>
