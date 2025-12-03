package com.example.yp.activity

import com.example.yp.database.DBHelper
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yp.R
import org.intellij.lang.annotations.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var passwordConfirm: EditText


     fun registrationUser(email: String, password:  String, passwordConfirm:  String): Boolean{
         if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {

             Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
             return false
         }
         if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
             Toast.makeText(this, "Почта введена в неверном формате", Toast.LENGTH_SHORT).show()
             return false
         }
         if (dbHelper.getByEmail(email) != null) {
             Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
             return false
         }
        if (password != passwordConfirm){
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return false
        }
         dbHelper.save(
                 email,
                 password
             )
         return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        dbHelper = DBHelper(this, null)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        signUpButton = findViewById(R.id.signUpButton)
        signInText = findViewById(R.id.signInText)
        email = findViewById(R.id.emailField)
        password = findViewById(R.id.password)
        passwordConfirm = findViewById(R.id.passwordConfirm)

        signUpButton.setOnClickListener { v ->
            val succes = registrationUser(
                email.text.toString().trim().lowercase(),
                password.text.toString().trim(),
                passwordConfirm.text.toString().trim()
            )
            if (succes){
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        signInText.setOnClickListener { v ->
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}