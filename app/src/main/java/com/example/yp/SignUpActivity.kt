package com.example.yp

import DBHelper
import android.os.Bundle
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var passwordConfirm: EditText

     fun registrationUser(email: String, password:  String, passwordConfirm:  String){
        if (password != passwordConfirm){
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }
        if (dbHelper.getByEmail(email) != null) {
            Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
            return
        }
         dbHelper.save(
                 email,
                 password
             )
         )

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
            registrationUser(email.toString(), password.toString(), passwordConfirm.toString())
        }
    }
}