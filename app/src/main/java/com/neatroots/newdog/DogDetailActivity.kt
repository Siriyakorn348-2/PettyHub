package com.neatroots.newdog

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Model.Dog
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class DogDetailActivity : AppCompatActivity() {

    private lateinit var dogId: String
    private lateinit var dogNameTextView: TextView
    private lateinit var breedDogTextView: TextView
    private lateinit var ageDogTextView: TextView
    private lateinit var sexDogTextView: TextView
    private lateinit var dogImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_detail)


        dogNameTextView = findViewById(R.id.nameDog)
        breedDogTextView = findViewById(R.id.breedDog)
        ageDogTextView = findViewById(R.id.ageDog)
        sexDogTextView = findViewById(R.id.sexDog)
        dogImageView = findViewById(R.id.dogImage)


        dogId = intent.getStringExtra("dogId") ?: ""


        loadDogDetails()
    }

    private fun loadDogDetails() {
        val dogRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)

        dogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val dog = dataSnapshot.getValue(Dog::class.java)

                    if (dog != null) {
                        Log.d("DogDetailActivity", "Dog details: $dog")

                        dogNameTextView.text = "Name: ${dog.getDogName()}"
                        breedDogTextView.text = "Breed: ${dog.getDogBreed()}"
                        ageDogTextView.text = "Age: ${dog.getDogAge()} years"
                        sexDogTextView.text = "Sex: ${dog.getDogGender()}"

                        val imageUrl = dog.getDogImage()
                        Log.d("DogDetailActivity", "Image URL: $imageUrl")

                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .placeholder(R.drawable.dogdog)
                                .into(dogImageView, object : Callback {
                                    override fun onSuccess() {
                                        Log.d("YourTag", "Image loaded successfully")
                                    }

                                    override fun onError(e: Exception?) {
                                        Log.e(
                                            "YourTag",
                                            "Failed to load image. Exception: ${e?.message}"
                                        )
                                    }
                                })
                        } else {
                            dogImageView.setImageResource(R.drawable.dogdog)
                        }
                    }
                } else {
                    // Handle the case where dog details do not exist
                    Log.d("YourTag", "Dog details do not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("YourTag", "Database error: ${error.message}")
            }
        })
    }
}
