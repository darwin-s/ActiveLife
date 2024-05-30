package com.darwins.activelife

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darwins.activelife.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
    }
}