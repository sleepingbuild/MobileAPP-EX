package com.example.studentmanager.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.studentmanager.R
import com.example.studentmanager.data.AppDatabase
import com.example.studentmanager.data.Student
import com.example.studentmanager.databinding.ActivityEditStudentBinding
import kotlinx.coroutines.launch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.activity.result.PickVisualMediaRequest
import java.io.File

class EditStudentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "student_id"
    }

    private lateinit var binding: ActivityEditStudentBinding
    private val dao by lazy { AppDatabase.getInstance(this).studentDao() }

    private var studentId = -1          // -1 = thêm mới, khác -1 = đang sửa
    private var pickedUri: String? = null   // ảnh chọn từ thiết bị

    // Chọn ảnh từ thiết bị
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                val savedPath = copyImageToInternalStorage(uri)
                if (savedPath != null) {
                    pickedUri = savedPath
                    binding.etAvatarUrl.setText("")   // ưu tiên ảnh vừa chọn
                    showAvatar(savedPath)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editRoot) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        studentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)
        binding.toolbar.setTitle(
            if (studentId == -1) R.string.add_new_student else R.string.edit_student
        )
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.etAvatarUrl.doAfterTextChanged {
            val url = it.toString().trim()
            if (url.isNotEmpty()) showAvatar(url)
        }
        binding.btnSave.setOnClickListener { onSaveClicked() }

        if (studentId != -1) loadStudent()
    }

    private fun loadStudent() {
        lifecycleScope.launch {
            val s = dao.getById(studentId) ?: return@launch
            binding.etName.setText(s.name)
            binding.etCode.setText(s.studentCode)
            binding.etEmail.setText(s.email)
            if (s.avatar?.startsWith("http") == true) {
                binding.etAvatarUrl.setText(s.avatar)
            } else {
                pickedUri = s.avatar
            }
            showAvatar(s.avatar)
        }
    }

    private fun showAvatar(source: String?) {
        binding.ivAvatar.load(source) {
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            fallback(R.drawable.ic_person)
        }
    }
    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val file = File(filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }
    private val codeRegex = Regex("^B[A-Z]{2}2[2-6][0-9]{4}$")

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val code = binding.etCode.text.toString().trim().uppercase()
        val email = binding.etEmail.text.toString().trim()

        binding.tilName.error =
            if (name.isEmpty()) getString(R.string.error_name_required) else null

        binding.tilCode.error = when {
            code.isEmpty() -> getString(R.string.error_code_required)
            !codeRegex.matches(code) -> getString(R.string.error_code_invalid)
            else -> null
        }

        binding.tilEmail.error = when {
            email.isEmpty() -> getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            else -> null
        }

        return binding.tilName.error == null &&
                binding.tilCode.error == null &&
                binding.tilEmail.error == null
    }
    private fun onSaveClicked() {
        if (!validateInputs()) return

        val name = binding.etName.text.toString().trim()
        val code = binding.etCode.text.toString().trim().uppercase()
        val email = binding.etEmail.text.toString().trim()
        val url = binding.etAvatarUrl.text.toString().trim()

        if (name.isEmpty() || code.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.msg_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }

        val avatar = if (url.isNotEmpty()) url else pickedUri
        val student = Student(
            id = if (studentId == -1) 0 else studentId,
            name = name, studentCode = code, email = email, avatar = avatar
        )

        if (studentId == -1) {
            saveStudent(student)   // thêm mới: lưu luôn
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_edit_message)
                .setPositiveButton(R.string.yes) { _, _ -> saveStudent(student) }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun saveStudent(student: Student) {
        lifecycleScope.launch {
            if (studentId == -1) dao.insert(student) else dao.update(student)
            Toast.makeText(this@EditStudentActivity, R.string.msg_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
