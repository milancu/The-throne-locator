import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.cvut.fel.thethronelocator.auth.UserData

class UserViewModel: ViewModel() {

    private val _state = MutableLiveData<UserData>()
    val state: LiveData<UserData> = _state

    fun updateUserData(result: UserData) {
        Log.d("TAG", "UpdateUserData")
        _state.value = result
    }
}