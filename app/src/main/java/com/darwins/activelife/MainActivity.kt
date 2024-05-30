package com.darwins.activelife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.darwins.activelife.dto.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database("https://activelife-d04f1-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val regButton = findViewById<Button>(R.id.reg)

        regButton.setOnClickListener{
            val email = findViewById<AppCompatEditText>(R.id.emailReg).text.toString()
            val pass = findViewById<AppCompatEditText>(R.id.passReg).text.toString()
            val pass2 = findViewById<AppCompatEditText>(R.id.pass2Reg).text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && pass2.isNotEmpty()) {
                if (pass == pass2) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            firebaseAuth.uid?.let { it1 -> database.child("users").child(it1).setValue(User()) }

                            setResult(RESULT_OK)
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, it.exception?.message ?: "Could not register", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        val loginButton = findViewById<Button>(R.id.login_redir)

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}