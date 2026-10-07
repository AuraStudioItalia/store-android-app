<div align="center">

# 🌌 Creare una repo esterna per Aura Store

**Guida per strutturare e pubblicare una repository con le tue app, pronta per l'integrazione con Aura Store.**

![Aura Store](https://img.shields.io/badge/Aura_Store-Repo_esterna-7C4DFF?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-APK-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-Hosting-181717?style=for-the-badge&logo=github&logoColor=white)

</div>

---

## 📑 Indice

- [📋 Prerequisiti](#-prerequisiti)
- [📁 Struttura della repo](#-struttura-della-repo)
- [🧩 Definizione dei file](#-definizione-dei-file)
- [🧾 Esempio di `data.json`](#-esempio-di-datajson)
- [🚀 Procedura operativa](#-procedura-operativa)
- [✅ Validazione e controllo qualità](#-validazione-e-controllo-qualità)
- [💡 Best practice](#-best-practice)
- [🛠️ Risoluzione dei problemi comuni](#️-risoluzione-dei-problemi-comuni)
- [📌 Contenuti consigliati](#-contenuti-consigliati)

---

## 📋 Prerequisiti

- ✔️ Account **GitHub** attivo.
- ✔️ Accesso in **write** al repository in cui creare la struttura.
- ✔️ Comunicazione chiara tra le app: ogni app deve includere **tre elementi fondamentali**:

| File | Descrizione |
|------|-------------|
| 🖼️ `icon.png` | Icona dell'app (preferibilmente **1024x1024** o comunque una dimensione coerente) |
| 📦 `<nome-file-app>.apk` | APK dell'app |
| 🧾 `data.json` | Metadati sull'app in Aura Store |

> [!TIP]
> È consigliato calcolare e fornire lo **SHA256** del file APK per garantirne l'integrità. Puoi calcolarlo su [emn178.github.io/online-tools/sha256_checksum.html](https://emn178.github.io/online-tools/sha256_checksum.html).

---

## 📁 Struttura della repo

La root della repository contiene **una cartella principale per ogni app**. Esempio tipico:

```text
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

> [!IMPORTANT]
> - Ogni sotto-cartella rappresenta **un'app** e deve contenere **esattamente i tre file** indicati.
> - I nomi dei file APK e gli elementi di `data.json` devono essere **coerenti tra loro**.

---

## 🧩 Definizione dei file

| File | Cosa è | Note |
|------|--------|------|
| 🖼️ `icon.png` | Icona dell'app | Deve essere chiara, leggibile e **non violare diritti di copyright** |
| 📦 `<nome-file-app>.apk` | Pacchetto APK dell'app | Usa un naming chiaro (es. `myapp_v1.0.0.apk`) |
| 🧾 `data.json` | Metadati dell'app | Usati da Aura Store per **import e visualizzazione** |

---

## 🧾 Esempio di `data.json`

Di seguito un esempio strutturato e completo. Modifica i valori con quelli reali della tua app.

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

> [!NOTE]
> - `signature_sha256` dovrebbe essere calcolato sul **file APK** (con strumenti online o linee di comando affidabili) e inserito qui.
> - Mantieni la data del changelog in formato **`YYYY-MM-DD`** e aggiorna la lista delle modifiche **ad ogni rilascio**.

---

## 🚀 Procedura operativa

### 1️⃣ Crea la repo e le cartelle delle app

```text
repo/
├── Nyra/
├── Docs/
└── ...
```

### 2️⃣ Aggiungi i tre file per ogni app

- [ ] `icon.png`
- [ ] `<nome-file-app>.apk`
- [ ] `data.json` (con i valori aggiornati)

### 3️⃣ Commit e push su GitHub

### 4️⃣ Collega la repo ad Aura Store

In Aura Store vai su **Profilo → Impostazioni → Aggiungi repository esterno** e inserisci l'URL:

```text
github.com/nome-utente/progetto/repo
```

### 5️⃣ Verifica che la struttura sia corretta

Se Aura Store non carica immediatamente:

- 🔄 Riapri l'app Aura Store
- ⏳ Oppure attendi **5-15 minuti** (potrebbe essere il deploy di GitHub / Aura Store in corso)

### 6️⃣ Monitora gli errori

Controlla eventuali messaggi di errore e correggi incongruenze nei **nomi dei file** o nei **percorsi**.

---

## ✅ Validazione e controllo qualità

Prima di pubblicare, assicurati che:

- [ ] Ogni app abbia `icon.png`, un **APK valido** e `data.json`.
- [ ] I percorsi indicati nei `data.json` puntino ai file presenti **nella stessa cartella**.
- [ ] Il JSON sia valido (usa un JSON Lint o un editor che evidenzi errori di sintassi).
- [ ] I campi `min_android_version` e `target_sdk` siano coerenti con l'APK.
- [ ] Lo SHA256 sia corretto e corrisponda all'APK caricato.

> [!WARNING]
> Se lo SHA256 dichiarato e lo SHA256 effettivo dell'app **non sono coerenti**, Aura Store potrebbe **bloccare l'installazione** per motivi di sicurezza.

- [ ] Verifica la leggibilità: evita nomi di file troppo lunghi o contenuti sensibili.

---

## 💡 Best practice

| | Consiglio | Dettaglio |
|---|-----------|-----------|
| 🔢 | **Versioning chiaro** | Usa versioni semanticamente significative (es. `1.0.0`, `1.1.0`, `1.1.1`, …) |
| 📝 | **Aggiornamenti del changelog** | Mantieni descrizioni concise e utili per gli utenti |
| 🧬 | **Metadati coerenti** | Attenzione a `name`, `description` e `package_name` per evitare mismatch |
| 🔐 | **Sicurezza** | Non includere dati sensibili nei file pubblici. Calcola e pubblica solo SHA256 sicuri |
| ⚖️ | **Licensing** | Includi licenze dove necessario e rispetta le policy di Aura Store |

---

## 🛠️ Risoluzione dei problemi comuni

<details>
<summary><b>❌ Aura Store non carica la repo dopo averla collegata</b></summary>

<br>

- Verifica la struttura della cartella: ogni app deve contenere `icon.png`, `<apk>.apk` e `data.json`.
- Controlla che i nomi file nel `data.json` corrispondano ai file effettivamente presenti.
- Attendi **5-15 minuti**: potrebbero esserci ritardi di deploy da parte di GitHub o Aura Store.

</details>

<details>
<summary><b>❌ Errore di JSON invalido</b></summary>

<br>

- Incolla correttamente i blocchi JSON e valida il file con uno strumento di JSON Lint.

</details>

<details>
<summary><b>❌ SHA256 non valido</b></summary>

<br>

- Rigenera l'hash SHA256 dell'APK e aggiorna `data.json` con il nuovo valore.

</details>

---

## 📌 Contenuti consigliati

- 📝 Inserisci una **breve descrizione** per ogni app in `data.json`.
- 🎨 Mantieni **dimensioni delle icone** e **naming conventions** coerenti tra tutte le app.

---

<div align="center">

Fatto con 🩷 dal team **Aura**

</div>
