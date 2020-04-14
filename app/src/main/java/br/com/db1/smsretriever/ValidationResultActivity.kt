package br.com.db1.smsretriever

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ValidationResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validation_result)
    }

    companion object {
        fun buildIntent(context: Context) = Intent(context, ValidationResultActivity::class.java)
    }

}
