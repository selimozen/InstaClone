package com.example.instaclone.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaclone.R
import com.example.instaclone.adapter.FeedRecyclerAdapter

import com.example.instaclone.databinding.ActivityFeedBinding
import com.example.instaclone.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFeedBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var postArray : ArrayList<Post>
    private lateinit var feedAdapter: FeedRecyclerAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        postArray = ArrayList<Post>()

        getData()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        feedAdapter = FeedRecyclerAdapter(postArray)
        binding.recyclerView.adapter = feedAdapter

    }

    private fun getData(){
        firestore.collection("Posts").addSnapshotListener { value, error ->
            if(error != null){
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
            }else{
                if(value != null){
                    if(!value.isEmpty){
                        val documents = value.documents
                        for (document in documents){
                            val userMail = document.get("userMail") as String
                            val comment = document.get("comment") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            val post = Post(userMail, comment, downloadUrl)

                            postArray.add(post)
                        }
                        feedAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }
    //Menumüzden, hesaptan çıkışmı yapılacak, yoksa post mu atılacak, onu kontrol ediyoruz.
    //Bu fonksiyon içerisinde bağlama işlemini yapıyoruz.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuinflater = menuInflater
        menuinflater.inflate(R.menu.insta_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        //Bu menü içerisinde ise bağlanan menüde neler olacağını kontrol ediyoruz.
        if(item.itemId == R.id.add_post){
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }else if(item.itemId == R.id.sing_out){
            //Burada ise hesaptan çıkış işlemini kontrol edeceğiz.
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}