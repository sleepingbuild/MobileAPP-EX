package com.example.studentmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.studentmanager.R
import com.example.studentmanager.data.Student
import com.example.studentmanager.databinding.ItemStudentBinding

class StudentAdapter(
    private val onItemClick: (Student) -> Unit,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(DiffCallback()) {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvName.text = student.name
            binding.tvCode.text = student.studentCode
            binding.tvEmail.text = student.email

            binding.ivAvatar.load(student.avatar) {
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                fallback(R.drawable.ic_person)
            }

            binding.root.setOnClickListener { onItemClick(student) }
            binding.btnEdit.setOnClickListener { onEditClick(student) }
            binding.btnDelete.setOnClickListener { onDeleteClick(student) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(old: Student, new: Student) = old.id == new.id
        override fun areContentsTheSame(old: Student, new: Student) = old == new
    }
}