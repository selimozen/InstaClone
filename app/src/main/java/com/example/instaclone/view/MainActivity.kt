package com.example.instaclone.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.instaclone.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        //Eğer daha önceden giriş yapılmış ise burada kontrol ediyoruz.
        if(currentUser != null){
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun singInClick(view : View){
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "You must to enter mail and password", Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                //İnternet olmayabilir, başka bir sorun olabilir, sunucuya bağlanılamayabilinir. Kullanıcı adı
                //yaratırken başarılı olmasını  durumunu burada kontrol ediyoruz.
                val intent = Intent(this@MainActivity, FeedActivity::class.java)
                startActivity(intent)
                finish()

            }.addOnFailureListener {
                Toast.makeText(this@MainActivity,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun singUpClick(view : View){
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        //Kullanıcının mail ve şifre kısımlarını boş bırakma ihtimalini if ile kontrol ediyoruz.
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"You must to enter mail and password",Toast.LENGTH_LONG).show()
        }else{
            //Aksi durumda kayıt işlemimizi başlatıyoruz.
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            //İnternet olmayabilir, başka bir sorun olabilir, sunucuya bağlanılamayabilinir. Kullanıcı adı
            //yaratırken başarılı olmasını  durumunu burada kontrol ediyoruz.
                val intent = Intent(this@MainActivity, FeedActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
            //Başarısız olma durumunda ise burada bir mesaj döneceğiz.
                Toast.makeText(this@MainActivity,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }
}