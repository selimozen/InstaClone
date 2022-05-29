package com.example.instaclone.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.instaclone.databinding.ActivityUploadBinding
import com.example.instaclone.model.Post
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList


class UploadActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUploadBinding
    //Hem gidip veriyi almak için, hemde izin gerekliliğinden doalyı activity result launcher sınıfını kullanıyoruz.
    //Bu sınıfları oncreate altında kullanmak için bir fonksiyon oluşturuyoruz.
    private lateinit var activityResultLauncer : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var postArray : ArrayList<Post>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Fonskiyonumuzu çağırmazsak uygulama çöker.
        registerLauncher()
        //Firebase ve Storage'i alıyoruz.
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage





    }


    fun upload (view : View){

        //Storage kısmına yüklenen fotoğraflarımızın hepsinin birbiri ile açkışmayacak şekilde uygurma bir ıd olması için java imdadımıza yetişiyor.
        val uuid = UUID.randomUUID()
        val imageId = "$uuid.jpg"

        //Storage değişkenine seçilen fotoğrafımızı atıyoruz.
        //Storage'e atmamızın sebebi storage kısmının fotoğraflar gibi büyük verileri toplayabilmesidir.
        //yine en önemli noktalardan biri referanslardır.

        val reference = storage.reference
        val imageReferance = reference.child("images").child(imageId)
        if(selectedPicture != null){
           imageReferance.putFile(selectedPicture!!).addOnSuccessListener {
                //Upload işlemi başarı olduğu durumda ise burda url alacağız ve firestore'a kayıt edeceğiz.
               val uploadPictureReference = storage.reference.child("images").child(imageId)
               uploadPictureReference.downloadUrl.addOnSuccessListener {
                   val downloadUrl = it.toString()
                   //Buraya kadar download url'mizi aldık. Artık veri tabanına kaydını yapabiliriz.

                   val postMap = hashMapOf<String, Any>()
                   postMap.put("downloadUrl", downloadUrl)
                   postMap.put("userMail", auth.currentUser!!.email!!)
                   postMap.put("comment", binding.commentText.text.toString())
                   postMap.put("date", com.google.firebase.Timestamp.now())
                   //Verilerimiz hashmap şeklinde aldığımıza göre artık firestore'a kayıt edebilirz.

                   firestore.collection("Posts").add(postMap).addOnSuccessListener {
                        finish()
                   }.addOnFailureListener {
                       Toast.makeText(this@UploadActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                   }

               }
            }.addOnFailureListener {
                //Upload işlemi başarısız olursa bu kısımda kontrol ediyoruz.
                Toast.makeText(this,it.localizedMessage, Toast.LENGTH_LONG)
            }
        }


    }

    fun selectedimage(view : View){
        //Bu noktada daha önce izin almışmıyız onu kontrol ediyoruz. İzin alınmamışsa izin isteyeceğiz.
        //Bunun için android'de soruyoruz. ve android iznin mantığını gösteriyor.
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission Needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {  }).show()
                //İzin isteme
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }else{
                //İzin isteme
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            val intenttoGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncer.launch(intenttoGallery)
        }

    }

    private fun registerLauncher(){

        activityResultLauncer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromrResult = result.data
                if(intentFromrResult != null){
                    selectedPicture = intentFromrResult.data
                    selectedPicture?.let {
                        binding.imageView.setImageURI(it)
                    }
                }
            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intenttoGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncer.launch(intenttoGallery)
            }else{
                Toast.makeText(this@UploadActivity, "Permission Needed!", Toast.LENGTH_LONG).show()

            }

        }

    }
}