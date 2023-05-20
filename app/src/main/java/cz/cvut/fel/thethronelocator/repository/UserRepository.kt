package cz.cvut.fel.thethronelocator.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.model.User

class UserRepository {

    private val database = FirebaseDatabase.getInstance()
    fun createUser(
        googleAuthClient: GoogleAuthClient,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = googleAuthClient.getUser()

        if (currentUser != null) {
            val userDataRef = database.getReference("users/${currentUser.userId}")

            val currentUserRef = userDataRef.child(currentUser.userId)
            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        if (!dataSnapshot.exists()) {
                            userDataRef.child("name").setValue(currentUser.name)
                            userDataRef.child("imgLink").setValue(currentUser.imgUrl)
                        }
                        Log.d(
                            TAG,
                            "onCreate: added new user with id: ${currentUser.userId}, ${currentUser.name}"
                        )
                        onSuccess(true)
                    } catch (e: Exception) {
                        Log.d(TAG, "onCancelled: ${e.message}")
                        onError(e.message!!)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${databaseError.message}")
                    onError("Failed to login")
                }
            })
        }
    }

    fun getUserById(id: String, callback: (User?) -> Unit) {
        val userDataRef = database.getReference("users/${id}")
        Log.d(TAG, "getUserById: $id")
        userDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.record = user?.record ?: 0;
                    user!!.id = dataSnapshot.key
                    callback(user)
                } else {
                    Log.d(TAG, "User does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: ${databaseError.message}")
                callback(null)
            }
        })
    }


    fun getAllUsers(callback: (List<User>?) -> Unit) {
        val usersRef = database.getReference("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()

                for (childSnapshot in dataSnapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    user!!.id = childSnapshot.key
                    Log.d(TAG, "onDataChange: ${user!!.name} ${user.id}")
                    userList.add(user)
                }
                Log.d(TAG, "onDataChange: ${userList.size}")
                callback(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: ${databaseError.message}")
                callback(null)
            }
        })
    }

    fun setNewRecord(id: String, record: Int) {
        val userDataRef = database.getReference("users/$id")
        userDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userDataRef.child("record").setValue(record)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: ${databaseError.message}")
            }
        })
    }
}