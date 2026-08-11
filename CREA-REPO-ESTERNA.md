# Creare una repo esterna per Aura Store

Questo documento descrive come strutturare e pubblicare una repo esterna contenente app per Aura Store, pronta per l’integrazione con Aura Store.

## Prerequisiti

- Account GitHub attivo.
- Accesso in write al repository in cui creare la struttura.
- Comunicazione chiara tra le app: ogni app deve includere tre elementi fondamentali:
  - icon.png: icona dell’app (preferibilmente 1024x1024 o comunque una dimensione coerente)
  - <nome-file-app>.apk: APK dell’app
  - data.json: metadati sull’app in Aura Store
- È consigliato calcolare e fornire lo SHA256 del file APK per garantire integrità: puoi calcolare su https://emn178.github.io/online-tools/sha256_checksum.html.

## Struttura della repo

La root della repository contiene una cartella principale per ogni app. Esempio tipico:

```
repo/
├── Nyra/
│   ├── icon.png
│   ├── nome-file-app.apk
│   └── data.json
├── Docs/
│   ├── icon.png
│   ├── nome-file-app.apk
│   └── data.json
└── ... (altre app)
```

Note:
- Ogni sotto-cartella rappresenta un’app e deve contenere esattamente i tre file indicati.
- I nomi dei file APK e gli elementi di data.json devono essere coerenti tra loro.

## Definizione dei file

- icon.png: icona dell’app. Assicurati che sia chiara, leggibile e non violi diritti di copyright.
- <nome-file-app>.apk: pacchetto APK dell’app. Usa un naming chiaro (es. myapp_v1.0.0.apk).
- data.json: metadati sull’app utilizzati da Aura Store per l’import e la visualizzazione.

## Esempio di data.json

Di seguito un esempio strutturato e completo. Modifica i valori reali secondo la tua app.

```json
{
  "app": {
    "name": "Nome app",
    "icon_url": "icon.png",
    "description": "Descrizione app",
    "version": "x.x.x",
    "changelog": [
      {
        "version": "x.x.x",
        "date": "xxxx-xx-xx",
        "changes": [
          "Prima pubblicazione",
          "Aggiunti miglioramenti minori",
          "Correzione di bug segnalati dagli utenti",
          "Modifica a tuo piacimento.."
        ]
      }
    ],
    "apk": {
      "file_name": "nome-file-app.apk",
      "size_mb": x,
      "min_android_version": "x.0",
      "target_sdk": "xx",
      "signature_sha256": "Inserisci qui SHA256",
      "package_name": "com.dominio.nome"
    }
  }
}
```

Note:
- “signature_sha256” dovrebbe essere calcolato sul file APK (es. utilizzando strumenti online o linee di comando affidabili) e inserito qui.
- Mantieni la data della changelog in formato YYYY-MM-DD e aggiorna la lista delle modifiche ad ogni rilascio.

## Procedura operativa

1. Crea la repo e le cartelle delle app:
   - repo/
     - Nyra/
     - Docs/
     - ...
2. Per ogni app, aggiungi i tre file:
   - icon.png
   - <nome-file-app>.apk
   - data.json (con i valori aggiornati)
3. Esegui commit e push su GitHub.
4. In Aura Store, vai a Profilo > Impostazioni > Aggiungi repository esterno. (github.com/nome-utente/progetto/repo)
5. Verifica che la struttura sia corretta. Se Aura Store non carica immediatamente:
   - Riapri l’app Aura Store oppure attendi 5-15 minuti (potrebbe essere il deploy di GitHub/ Aura Store in corso).
6. Monitora eventuali messaggi di errore e correggi eventuali incongruenze nei nomi file o nei percorsi.

## Validazione e controllo qualità

- Assicurati che:
  - Ogni app abbia icon.png, un APK valido e data.json.
  - I percorsi indicati nei data.json puntino ai file presenti nella stessa cartella.
  - Il JSON sia valido (usa un JSON Lint o editor che evidenzi errori di sintassi).
  - I campi min_android_version e target_sdk siano coerenti con l’APK.
  - Il file SHA256 sia corretto e corrisponda al APK caricato: Aura Store, se lo SHA256 dichiarato e lo SHA256 dell'app non sono coerenti, potrebbe bloccare l'installazione per motivi di sicurezza.
- Verifica la leggibilità: evita nomi di file troppo lunghi o contenuti sensibili.

## Best practice

- Versioning chiaro: usa versioni semanticamente significative (es. 1.0.0, 1.1.0, 1.1.1, …).
- Aggiornamenti del changelog: mantieni descrizioni concise e utili per gli utenti.
- Metadati coerenti: attenzione a “name”, “description” e “package_name” per evitare mismatch.
- Sicurezza: non includere dati sensibili nei file pubblici. Calcola e pubblica solo SHA256 sicuri.
- Licensing: includi licenze dove necessario e rispetta le policy di Aura Store.

## Risoluzione dei problemi comuni

- Aura Store non carica la repo dopo averla collegata:
  - Verifica la struttura della cartella; ogni app deve contenere icon.png, <apk>.apk, data.json.
  - Controlla che i nomi file nel data.json corrispondano ai file effettivamente presenti.
  - Attendi 5-15 minuti: potrebbero esserci ritardi di deploy da parte di GitHub o Aura Store.
- Errore di JSON invalido:
  - Incorpo correttamente i blocchi JSON e valida con uno strumento di json lint.
- SHA256 non valido:
  - Rigenera l’hash SHA256 del APK e aggiorna data.json con il nuovo valore.

## Contenuti consigliati

- Inserisci una breve descrizione per ogni app in data.json.
- Mantieni le dimensioni delle icone e i naming conventions coerenti tra tutte le app.
