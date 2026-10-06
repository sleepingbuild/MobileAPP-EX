package com.example.studentmanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanager.data.AppDatabase
import com.example.studentmanager.data.Student
import com.example.studentmanager.databinding.ActivityMainBinding
import com.example.studentmanager.ui.StudentAdapter
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.studentmanager.ui.EditStudentActivity
import coil.load
import android.widget.ImageView
import android.widget.TextView
import com.example.studentmanager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StudentAdapter
    private val dao by lazy { AppDatabase.getInstance(this).studentDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditStudentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            onItemClick = { showDetailDialog(it) },
            onEditClick = {
                val intent = Intent(this, EditStudentActivity::class.java)
                intent.putExtra(EditStudentActivity.EXTRA_STUDENT_ID, it.id)
                startActivity(intent)
            },
            onDeleteClick = { confirmDelete(it) }
        )
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            val students = dao.getAll()
            adapter.submitList(students)
            binding.tvEmpty.visibility =
                if (students.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
    private fun showDetailDialog(student: Student) {
        val view = layoutInflater.inflate(R.layout.dialog_student_detail, null)

        view.findViewById<TextView>(R.id.tvName).text = student.name
        view.findViewById<TextView>(R.id.tvCode).text =
            "${getString(R.string.student_code)}: ${student.studentCode}"
        view.findViewById<TextView>(R.id.tvEmail).text =
            "${getString(R.string.student_email)}: ${student.email}"
        view.findViewById<ImageView>(R.id.ivAvatar).load(student.avatar) {
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            fallback(R.drawable.ic_person)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.student_detail)
            .setView(view)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun confirmDelete(student: Student) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                lifecycleScope.launch {
                    dao.delete(student)
                    Toast.makeText(this@MainActivity, R.string.msg_deleted, Toast.LENGTH_SHORT).show()
                    loadStudents()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}