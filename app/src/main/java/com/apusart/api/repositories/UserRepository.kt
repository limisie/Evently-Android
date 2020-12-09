package com.apusart.api.repositories

import android.content.Context
import android.net.Uri
import com.apusart.api.Resource
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class UserRepository(private val context: Context) {
    private val gto = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .build()
    private val gsc = GoogleSignIn.getClient(context, gto)
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    suspend fun facebookLogIn(token: AccessToken): Resource<FirebaseUser> {
        val credential = FacebookAuthProvider.getCredential(token.token)
        Firebase.auth.signInWithCredential(credential).await()
        return user()
    }

    suspend fun googleLogIn(idToken: String): Resource<FirebaseUser> {
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credentials).await()
        return user()
    }

    suspend fun emailLogIn(email: String, password: String): Resource<FirebaseUser> {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
        return user()
    }

    suspend fun register(email: String, password: String): Resource<FirebaseUser> {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        return user()
    }

    private fun user(): Resource<FirebaseUser> {
        val currUser = Firebase.auth.currentUser
        return if (currUser != null) {
            Resource.success(currUser)
        } else {
            Resource.error("Cannot be logged")
        }
    }

    fun signOut(): Resource<Boolean> {
        gsc.signOut()
        Firebase.auth.signOut()
        LoginManager.getInstance().logOut()
        return Resource.success(true)
    }

    private fun getReference(path: String): StorageReference {
        return storage.reference.child(path)
    }

    suspend fun uploadProfilePhoto(uri: Uri, id: String, pictureName: String) {
        val file = getReference("users/${id}/${pictureName}.jpg")
        file.putFile(uri).await()
    }

    suspend fun getDownloadLink(id: String, path: String): Resource<Uri> {
        val file = getReference(path)
        return Resource.success(file.downloadUrl.await())
    }
}