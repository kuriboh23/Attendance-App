package com.example.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.CheckViewModel
import com.example.project.fragment.list.CheckAdapter


class Attendance : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var checkAdapter: CheckAdapter
    private lateinit var attendanceViewModel: CheckViewModel
    private lateinit var img: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        recyclerView = view.findViewById(R.id.RecView)
        img = view.findViewById(R.id.filterMouth)

        val userId = UserPrefs.loadUserId(requireContext())

        //RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        checkAdapter = CheckAdapter()
        recyclerView.adapter = checkAdapter

        attendanceViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        attendanceViewModel.getAllUserChecks(userId).observe(viewLifecycleOwner) { check ->
            checkAdapter.setData(check)
        }

        return view
    }

}