// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
package vcmsa.projects.pcv1.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BudgetViewModel : ViewModel() {

    // Backing property for text
    private val _text = MutableLiveData<String>().apply {
        value = "This is budget Fragment"
    }
    val text: LiveData<String> = _text

}
