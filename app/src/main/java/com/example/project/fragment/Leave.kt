package com.example.project.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.ApplyLeave
import com.example.project.R
import com.example.project.databinding.FragmentLeaveBinding


class Leave : Fragment() {
    lateinit var binding: FragmentLeaveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLeaveBinding.inflate(inflater, container, false)

        binding.addRequest.setOnClickListener {
            val intent = Intent(requireContext(), ApplyLeave::class.java)
            startActivity(intent)
        }

        return binding.root
    }

}