package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun CreateAccountDialog(
    onDismiss: () -> Unit,
    onCreateAccount: (nome: String, cognome: String, email: String, password: String, (Boolean, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessEmailSent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isSuccessEmailSent) "Verifica Email" else "Crea Account Aura",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp).testTag("btn_close_create_account")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (isSuccessEmailSent) {
                // Success state: email verification sent
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Email di verifica inviata!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Abbiamo inviato un'email di verifica all'indirizzo:\n$email\n\nApri il messaggio e clicca sul link di conferma per completare l'attivazione del tuo profilo Aura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                // Form input state: Nome, Cognome, Email, Password, Conferma Password, Checkbox Obbligatorie
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Nome Field
                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            errorMessage = null
                        },
                        label = { Text("Nome") },
                        placeholder = { Text("Es. Mario") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_nome")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cognome Field
                    OutlinedTextField(
                        value = cognome,
                        onValueChange = {
                            cognome = it
                            errorMessage = null
                        },
                        label = { Text("Cognome") },
                        placeholder = { Text("Es. Rossi") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_cognome")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email") },
                        placeholder = { Text("mario.rossi@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_email")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password (min. 6 caratteri)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostra password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_password")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Conferma Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_confirm_password")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // CHECKBOX 1 (OBBLIGATORIA): Termini e Condizioni
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = {
                                termsAccepted = it
                                errorMessage = null
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("checkbox_accept_terms")
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        val termsAnnotatedString = buildAnnotatedString {
                            append("Facendo la registrazione, dichiaro di accettare i  ")
                            pushStringAnnotation(tag = "URL", annotation = "https://privacy.aurastudioitalia.it/tos")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Termini e Condizioni")
                            }
                            pop()
                            append(" *")
                        }

                        ClickableText(
                            text = termsAnnotatedString,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                            onClick = { offset ->
                                termsAnnotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    } ?: run {
                                        termsAccepted = !termsAccepted
                                        errorMessage = null
                                    }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // CHECKBOX 2 (OBBLIGATORIA): Privacy Policy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = privacyAccepted,
                            onCheckedChange = {
                                privacyAccepted = it
                                errorMessage = null
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("checkbox_accept_privacy")
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        val privacyAnnotatedString = buildAnnotatedString {
                            append("Facendo la registrazione, dichiaro di accettare la ")
                            pushStringAnnotation(tag = "URL", annotation = "https://privacy.aurastudioitalia.it/privacypolicy")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            pop()
                            append(" *")
                        }

                        ClickableText(
                            text = privacyAnnotatedString,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                            onClick = { offset ->
                                privacyAnnotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    } ?: run {
                                        privacyAccepted = !privacyAccepted
                                        errorMessage = null
                                    }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            if (isSuccessEmailSent) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("btn_close_success_email")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ho Capito", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val cleanNome = nome.trim()
                        val cleanCognome = cognome.trim()
                        val cleanEmail = email.trim()
                        val cleanPassword = password

                        if (cleanNome.isBlank()) {
                            errorMessage = "Inserisci il tuo nome."
                            return@Button
                        }
                        if (cleanCognome.isBlank()) {
                            errorMessage = "Inserisci il tuo cognome."
                            return@Button
                        }
                        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                            errorMessage = "Inserisci un indirizzo email valido."
                            return@Button
                        }
                        if (cleanPassword.length < 6) {
                            errorMessage = "La password deve contenere almeno 6 caratteri."
                            return@Button
                        }
                        if (cleanPassword != confirmPassword) {
                            errorMessage = "Le password inserite non coincidono."
                            return@Button
                        }
                        if (!termsAccepted) {
                            errorMessage = "È obbligatorio accettare i termini e condizioni per registrarsi."
                            return@Button
                        }
                        if (!privacyAccepted) {
                            errorMessage = "È obbligatorio accettare la privacy policy per registrarsi."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        onCreateAccount(
                            cleanNome,
                            cleanCognome,
                            cleanEmail,
                            cleanPassword
                        ) { success, msg ->
                            isLoading = false
                            if (success) {
                                isSuccessEmailSent = true
                            } else {
                                errorMessage = msg ?: "Errore durante la creazione dell'account."
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_create_account")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creazione in corso...", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Registrati e Verifica Email", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (!isSuccessEmailSent) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.testTag("btn_cancel_create_account")
                ) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}
