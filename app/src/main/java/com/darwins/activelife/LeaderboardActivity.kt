package com.darwins.activelife

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Space
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.darwins.activelife.databinding.ActivityLeaderboardBinding
import com.darwins.activelife.dto.FriendLeaderboardEntry
import com.darwins.activelife.dto.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database("https://activelife-d04f1-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val boardArray = ArrayList<FriendLeaderboardEntry>()

        // Last Entry to add (me)
        firebaseAuth.uid?.let { uid ->
            database.child("users").child(uid).get().addOnSuccessListener {
                val usr = it.getValue(User::class.java)
                if (usr != null) {
                    boardArray.add(FriendLeaderboardEntry(uid, "Me", usr.totalDistance))

                    boardArray.sortBy { friendLeaderboardEntry -> friendLeaderboardEntry.walked }
                    boardArray.reverse()
                    var place = 1

                    // Display all entries
                    for (friendEntry in boardArray) {
                        val row = TableRow(this)
                        row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)

                        val plc = TextView(this)
                        plc.text = place.toString()
                        plc.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                        val sp1 = Space(this)
                        sp1.layoutParams = TableRow.LayoutParams(toPx(10), TableRow.LayoutParams.WRAP_CONTENT)
                        val nick = TextView(this)
                        nick.text = friendEntry.nickname
                        nick.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                        val sp2 = Space(this)
                        sp2.layoutParams = TableRow.LayoutParams(toPx(100), TableRow.LayoutParams.WRAP_CONTENT)
                        val dst = TextView(this)
                        dst.text = friendEntry.walked.toString()
                        dst.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

                        row.addView(plc)
                        row.addView(sp1)
                        row.addView(nick)
                        row.addView(sp2)
                        row.addView(dst)

                        binding.leaderboard.addView(row)
                        place += 1
                    }
                }
            } }

        binding.addFriendButton.setOnClickListener {

        }
    }

    private fun toPx(dp : Int): Int {
        val scale: Float = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}