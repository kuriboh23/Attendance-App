package com.example.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.databinding.FragmentAdminLeaveBinding


class AdminLeave : Fragment() {
    private lateinit var binding: FragmentAdminLeaveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminLeaveBinding.inflate(inflater, container, false)


        return binding.root
    }
}