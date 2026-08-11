package com.example.data.auth

import com.example.data.model.AuthState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "Utente Registrato"
            val uid = currentUser.uid
            _authState.value = AuthState.LoggedIn(email = email, uid = uid)
            fetchUserProfile(uid, email)
        } else {
            _authState.value = AuthState.LoggedOut
        }
    }

    fun signInWithEmail(input: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val cleanInput = input.trim()
        if (cleanInput.isBlank() || pass.isBlank()) {
            onResult(false, "Inserisci email/username e password validi.")
            return
        }

        if (cleanInput.contains("@")) {
            performFirebaseSignIn(cleanInput, pass, onResult)
        } else {
            resolveEmailFromUsername(cleanInput) { resolvedEmail ->
                if (resolvedEmail.isNullOrBlank()) {
                    onResult(false, "Impossibile trovare l'email associata allo username '$cleanInput'. Verifica lo username o accedi con la tua email.")
                } else {
                    performFirebaseSignIn(resolvedEmail, pass, onResult)
                }
            }
        }
    }

    private fun resolveEmailFromUsername(username: String, onResolved: (String?) -> Unit) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank()) {
            onResolved(null)
            return
        }

        val hasCalled = java.util.concurrent.atomic.AtomicBoolean(false)
        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        val safeCallback: (String?) -> Unit = { email ->
            if (hasCalled.compareAndSet(false, true)) {
                handler.removeCallbacksAndMessages(null)
                onResolved(email)
            }
        }

        // 3.5 seconds timeout to guarantee login flow never hangs infinitely
        handler.postDelayed({
            safeCallback(null)
        }, 3500L)

        // Query Firestore document nicknames/{cleanUser}
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("nicknames").document(cleanUser).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val email = doc.getString("email")
                        val uid = doc.getString("uid") ?: doc.getString("id")
                        if (!email.isNullOrBlank()) {
                            safeCallback(email)
                        } else if (!uid.isNullOrBlank()) {
                            fetchEmailByUidFirestore(uid, safeCallback)
                        } else {
                            tryLowercaseFirestore(cleanUser, safeCallback)
                        }
                    } else {
                        tryLowercaseFirestore(cleanUser, safeCallback)
                    }
                }
                .addOnFailureListener {
                    tryLowercaseFirestore(cleanUser, safeCallback)
                }
        } catch (e: Exception) {
            tryLowercaseFirestore(cleanUser, safeCallback)
        }
    }

    private fun tryLowercaseFirestore(username: String, safeCallback: (String?) -> Unit) {
        val lower = username.lowercase()
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("nicknames").document(lower).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val email = doc.getString("email")
                        val uid = doc.getString("uid") ?: doc.getString("id")
                        if (!email.isNullOrBlank()) {
                            safeCallback(email)
                        } else if (!uid.isNullOrBlank()) {
                            fetchEmailByUidFirestore(uid, safeCallback)
                        } else {
                            tryQueryFirestore(username, safeCallback)
                        }
                    } else {
                        tryQueryFirestore(username, safeCallback)
                    }
                }
                .addOnFailureListener {
                    tryQueryFirestore(username, safeCallback)
                }
        } catch (e: Exception) {
            tryQueryFirestore(username, safeCallback)
        }
    }

    private fun tryQueryFirestore(username: String, safeCallback: (String?) -> Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("nicknames")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { query ->
                    val doc = query.documents.firstOrNull()
                    if (doc != null) {
                        val email = doc.getString("email")
                        val uid = doc.getString("uid") ?: doc.getString("id")
                        if (!email.isNullOrBlank()) {
                            safeCallback(email)
                        } else if (!uid.isNullOrBlank()) {
                            fetchEmailByUidFirestore(uid, safeCallback)
                        } else {
                            tryRtdbFallback(username, safeCallback)
                        }
                    } else {
                        tryRtdbFallback(username, safeCallback)
                    }
                }
                .addOnFailureListener {
                    tryRtdbFallback(username, safeCallback)
                }
        } catch (e: Exception) {
            tryRtdbFallback(username, safeCallback)
        }
    }

    private fun fetchEmailByUidFirestore(uid: String, safeCallback: (String?) -> Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val email = doc.getString("email") ?: doc.getString("userEmail") ?: doc.getString("mail")
                        if (!email.isNullOrBlank()) {
                            safeCallback(email)
                            return@addOnSuccessListener
                        }
                    }
                    tryRtdbUidLookup(uid, safeCallback)
                }
                .addOnFailureListener {
                    tryRtdbUidLookup(uid, safeCallback)
                }
        } catch (e: Exception) {
            tryRtdbUidLookup(uid, safeCallback)
        }
    }

    private fun tryRtdbFallback(username: String, safeCallback: (String?) -> Unit) {
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("nicknames")
                .child(username)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val emailVal = snapshot.child("email").getValue(String::class.java)
                        val uidVal = snapshot.child("uid").getValue(String::class.java)
                            ?: snapshot.getValue(String::class.java)

                        if (!emailVal.isNullOrBlank()) {
                            safeCallback(emailVal)
                        } else if (!uidVal.isNullOrBlank()) {
                            tryRtdbUidLookup(uidVal, safeCallback)
                        } else {
                            safeCallback(null)
                        }
                    } else {
                        safeCallback(null)
                    }
                }
                .addOnFailureListener {
                    safeCallback(null)
                }
        } catch (e: Exception) {
            safeCallback(null)
        }
    }

    private fun tryRtdbUidLookup(uid: String, safeCallback: (String?) -> Unit) {
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val email = snapshot.child("email").getValue(String::class.java)
                    safeCallback(email)
                }
                .addOnFailureListener {
                    safeCallback(null)
                }
        } catch (e: Exception) {
            safeCallback(null)
        }
    }

    private fun performFirebaseSignIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userEmail = user?.email ?: email
                    val uid = user?.uid ?: ""
                    _authState.value = AuthState.LoggedIn(email = userEmail, uid = uid)
                    fetchUserProfile(uid, userEmail)
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Errore di autenticazione. Verifica email/username e password.")
                }
            }
    }

    private fun fetchUserProfile(uid: String, email: String) {
        if (uid.isBlank()) return

        // 1. Try Firebase Realtime Database at users/{uid}
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val nome = snapshot.child("nome").getValue(String::class.java)
                        val cognome = snapshot.child("cognome").getValue(String::class.java)
                        val profileImage = snapshot.child("profileImage").getValue(String::class.java)
                        val current = _authState.value
                        if (current is AuthState.LoggedIn && current.uid == uid) {
                            _authState.value = current.copy(
                                nome = nome ?: current.nome,
                                cognome = cognome ?: current.cognome,
                                profileImage = profileImage ?: current.profileImage
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Try Firestore at users/{uid}
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val nome = doc.getString("nome")
                        val cognome = doc.getString("cognome")
                        val profileImage = doc.getString("profileImage")
                        val current = _authState.value
                        if (current is AuthState.LoggedIn && current.uid == uid) {
                            _authState.value = current.copy(
                                nome = nome ?: current.nome,
                                cognome = cognome ?: current.cognome,
                                profileImage = profileImage ?: current.profileImage
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setIncognitoMode() {
        _authState.value = AuthState.Incognito
    }

    fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _authState.value = AuthState.LoggedOut
    }
}
