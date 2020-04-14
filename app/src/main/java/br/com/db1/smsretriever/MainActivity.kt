package br.com.db1.smsretriever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSmsRetriever()
        setupEvents()
    }

    private fun setupSmsRetriever() {
        val smsRetrieverClient = SmsRetriever.getClient(this)
        val receiverTask = smsRetrieverClient.startSmsRetriever()

        receiverTask.addOnSuccessListener {
            Toast.makeText(this, R.string.successMessageSmsRetrieverStartup, Toast.LENGTH_LONG)
                .show()
        }

        receiverTask.addOnFailureListener {
            Toast.makeText(this, R.string.errorMessageSmsRetrieverStartup, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupEvents() {
        buttonValidate.setOnClickListener { validateCode() }
    }

    private fun validateCode() {
        progressBarValidate.visibility = View.VISIBLE

        GlobalScope.launch {
            delay(LOADING_TIME)

            if (editTextCode.text.toString() != MOCK_VALID_CODE) {
                runOnUiThread {
                    inputLayoutCode.error = getString(R.string.errorMessageInvalidCode)
                    progressBarValidate.visibility = View.INVISIBLE
                }
            } else {
                startActivity(ValidationResultActivity.buildIntent(this@MainActivity))
                finish()
            }
        }
    }

    private val smsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
                val status = intent.getParcelableExtra(SmsRetriever.EXTRA_STATUS) as Status

                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                        message?.let { getValidationCodeFromSms(it) }
                    }
                    CommonStatusCodes.TIMEOUT -> Toast.makeText(this@MainActivity, R.string.errorMessageSmsTimeout, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getValidationCodeFromSms(message: String) {
        val colonPosition = message.indexOf(CHAR_COLON) + CODE_OFFSET

        val token = message.substring(colonPosition, colonPosition + CODE_SIZE)

        editTextCode.setText(token)
        validateCode()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(smsBroadcastReceiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(smsBroadcastReceiver)
    }

    companion object {
        private const val LOADING_TIME = 1500L
        private const val CHAR_COLON = ":"
        private const val CODE_SIZE = 4
        private const val CODE_OFFSET = 2

        private const val MOCK_VALID_CODE = "1234"
    }

}
