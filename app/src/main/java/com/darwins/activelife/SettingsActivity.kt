package com.darwins.activelife

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darwins.activelife.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
private lateinit var firebaseAuth: FirebaseAuth


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val sharedPref = getSharedPreferences("ActiveLife", Context.MODE_PRIVATE)
        if (!sharedPref.contains("METERS_MAX")) {
            binding.mMax.setText("500")
            with(sharedPref.edit()) {
                putInt("METERS_MAX", 500)
                apply()
            }
        } else {
            binding.mMax.setText(Integer.toString(sharedPref.getInt("METERS_MAX", 500)))
        }

        binding.backSettings.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            setResult(RESULT_OK)

            startActivity(intent)
            finish()
        }

        binding.saveSettings.setOnClickListener {
            with (sharedPref.edit()) {
                putInt("METERS_MAX", Integer.parseInt(binding.mMax.text.toString()))
                apply()
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        binding.useridText.text = firebaseAuth.currentUser?.uid ?: ""

        binding.copyUid.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Active Life User ID", binding.useridText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied the user id to the clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}