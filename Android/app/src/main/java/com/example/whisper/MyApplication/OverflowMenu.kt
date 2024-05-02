import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.LoginActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.R
import com.example.whisper.UserInfoActivity

class OverflowMenu (private val app: MyApplication){
    fun onCreateOptionsMenu(menu: Menu?, activity: AppCompatActivity): Boolean {
        activity.menuInflater.inflate(R.menu.overflowmenu, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem, activity: AppCompatActivity) {
        when (item.itemId) {
            R.id.timeline -> {

            }
            R.id.search -> {
                // Navigate to Search screen
            }
            R.id.whisper -> {
                // Navigate to Whisper screen
            }
            R.id.myprofile -> {
                // Navigate to My Profile screen
                val intent = Intent(activity, UserInfoActivity::class.java)
                intent.putExtra("userId", app.loginUserId)
                activity.startActivity(intent)
            }
            R.id.profileedit -> {
                // Navigate to Profile Edit screen
            }
            R.id.logout -> {
                // Clear loginUserId global variable
                app.loginUserId = ""
                // Navigate to Login screen and clear previous screen info
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
            }
        }
    }
}
