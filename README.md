# Aura Store Android App

Benvenuto nel repository ufficiale di **Aura Store**, un'applicazione Android per installare e gestire le altre app Aura con funzionalità avanzate (come aggiungere altre repository che seguono lo schema Aura Store), sviluppata utilizzando Android Studio.

## Descrizione
Aura Store è uno store digitale progettato per semplificare l'installazione e la gestione delle app Android di Aura. Puoi anche aggiungere repository esterni che seguono lo schema Aura Store.

## Requisiti di Sistema
Per compilare e avviare correttamente il progetto, assicurati di avere installato:
*   **Android Studio** (versione Jellyfish o superiore consigliata)
*   **Java Development Kit (JDK) 17+**
*   **Android SDK** (API Level 34 o superiore)
*   **Gradle 8.0+**

## Istruzioni per la Compilazione

Segui questi passaggi per configurare l'ambiente di sviluppo e compilare l'applicazione:

1.  **Clona il repository:**
    ```bash
    git clone https://github.com/AuraStudioItalia/store-android-app.git
    cd store-android-app
    ```

2.  **Apri il progetto:**
    *   Avvia Android Studio.
    *   Seleziona **File > Open** e naviga fino alla cartella del progetto clonata.
    *   Attendi che Gradle sincronizzi le dipendenze automaticamente.

3.  **Compilazione:**
    *   Dalla barra dei menu, clicca su **Build > Make Project**.
    *   In alternativa, usa il terminale integrato:
        ```bash
        ./gradlew assembleDebug
        ```

4.  **Esecuzione:**
    *   Collega il tuo dispositivo Android (con Debug USB abilitato) o avvia un emulatore.
    *   Clicca sul pulsante **Run** (icona Play verde) in Android Studio.

## Struttura del Progetto
*   `/app/src/main/java`: Codice sorgente (Kotlin/Java)
*   `/app/src/main/res`: Risorse (layout XML, immagini, icone)
*   `/app/build.gradle`: Configurazione delle dipendenze e build

## Licenza
Questo progetto è rilasciato sotto licenza AGPL. Consulta il file `LICENSE` per ulteriori dettagli.
