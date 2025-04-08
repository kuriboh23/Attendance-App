package com.example.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.AttendanceViewModel
import com.example.project.fragment.list.CheckAdapter


class Attendance : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var checkAdapter: CheckAdapter
    private lateinit var attendanceViewModel: AttendanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        recyclerView = view.findViewById(R.id.RecView)

        //RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        checkAdapter = CheckAdapter()
        recyclerView.adapter = checkAdapter

        attendanceViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        attendanceViewModel.allAttendances.observe(viewLifecycleOwner) { check ->
            checkAdapter.setData(check)
        }

        return view
    }

}