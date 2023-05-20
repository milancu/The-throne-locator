package cz.cvut.fel.thethronelocator.repository

import android.content.ContentValues
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.model.Toilet
import java.util.UUID

class ToiletRepository {

    fun getToilets(onSuccess: (List<Toilet>) -> Unit, onError: (String) -> Unit) {
        val dbReference = FirebaseDatabase.getInstance().getReference("toilets")
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "Start fetching data  toilet point")

                val toiletList: MutableList<Toilet> = ArrayList()
                for (childSnapshot in dataSnapshot.children) {
                    val toilet = childSnapshot.getValue(Toilet::class.java)
                    toilet!!.id = childSnapshot.key
                    toiletList.add(toilet)

                }
                Log.d(ContentValues.TAG, "Data fetched")
                onSuccess(toiletList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error while fetching: ${databaseError.message}")
                onError(databaseError.message)
            }
        })
    }

    fun getToiletById(id: String, onSuccess: (Toilet) -> Unit, onError: (String) -> Unit) {
        val toiletDataRef = FirebaseDatabase.getInstance().getReference("toilets/${id}")
        toiletDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var toilet = dataSnapshot.getValue(Toilet::class.java)
                if (toilet != null) {
                    toilet.id = dataSnapshot.key
                }

                val ratingsList: MutableList<Rating> = mutableListOf()
                val ratingsDataSnapshot = dataSnapshot.child("ratings")

                for (entrySnapshot in ratingsDataSnapshot.children) {
                    val rating = entrySnapshot.getValue(Rating::class.java)
                    ratingsList.add(rating!!)
                }
                if (toilet != null) {
                    toilet.ratingList = ratingsList
                }
                onSuccess(toilet!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error while fetching: ${databaseError.message}")
                onError(databaseError.message)
            }
        })
    }

    fun addToiletRating(toiletId: String, rating: Rating) {
        val toiletDataRef = FirebaseDatabase.getInstance().getReference("toilets/${toiletId}")
        toiletDataRef.child("ratings").push().setValue(rating)
    }

    fun saveToilet(toilet: Toilet) {
        val toiletDataRef =
            FirebaseDatabase.getInstance().getReference("toilets/${UUID.randomUUID()}")
        toiletDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toiletDataRef.child("name").setValue(toilet.name)
                toiletDataRef.child("openingTime").setValue(toilet.openingTime)
                toiletDataRef.child("img").setValue(toilet.img)
                toiletDataRef.child("latitude").setValue(toilet.latitude)
                toiletDataRef.child("longitude").setValue(toilet.longitude)
                toiletDataRef.child("type").setValue(toilet.type)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(ContentValues.TAG, "onCancelled: ${databaseError.message}")
            }
        })
    }
}