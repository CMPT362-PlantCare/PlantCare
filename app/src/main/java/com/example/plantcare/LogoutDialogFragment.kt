import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.plantcare.R
import com.example.plantcare.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogoutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setTitle(getString(R.string.logout_confirmation_title))
        alertDialogBuilder.setMessage(getString(R.string.logout_confirmation_message))

        alertDialogBuilder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            Firebase.auth.signOut()
            val loginActivityIntent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(loginActivityIntent)
            requireActivity().finish()
        }

        alertDialogBuilder.setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        return alertDialogBuilder.create()
    }
}
