package aleksa.mosis.elfak.capturetheflag.profile

import aleksa.mosis.elfak.capturetheflag.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        et_edit_register_username.setText(intent.getStringExtra("username").toString())
        et_edit_register_name.setText(intent.getStringExtra("name").toString())
        et_edit_register_surname.setText(intent.getStringExtra("surname").toString())
        et_edit_register_phone.setText(intent.getStringExtra("phone").toString())

        btn_save.setOnClickListener{
            val intent = Intent()

            intent.putExtra("username",et_edit_register_username.text.toString())
            intent.putExtra("name",et_edit_register_name.text.toString())
            intent.putExtra("surname",et_edit_register_surname.text.toString())
            intent.putExtra("phone",et_edit_register_phone.text.toString())

            setResult(2000,intent);
            finish()
        }
    }
}