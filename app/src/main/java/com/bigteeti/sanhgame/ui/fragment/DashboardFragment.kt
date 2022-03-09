package com.bigteeti.sanhgame.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigteeti.sanhgame.R
import com.bigteeti.sanhgame.databinding.FragmentDashboardBinding
import com.bigteeti.sanhgame.ui.activity.AppEvent
import com.bigteeti.sanhgame.ui.activity.SharedViewModel
import com.bigteeti.sanhgame.ui.adapter.DashboardAdapter
import com.dakuinternational.common.domain.model.DataContent
import com.dakuinternational.common.ui.base.BaseFragment
import com.dakuinternational.common.ui.binding.viewBinding
import com.dakuinternational.common.ui.utils.writeLog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : BaseFragment(R.layout.fragment_dashboard){

    private val binding by viewBinding(FragmentDashboardBinding::bind)

    private val adapter by lazy { DashboardAdapter(requireActivity() as DashboardAdapter.OnItemClickListener ) }

    lateinit var sharedViewModel: SharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]


        sharedViewModel.uiEvent.observe(viewLifecycleOwner){
            if(it is AppEvent.DataReceived) adapter.setList(it.list)
        }

        setupViews()
    }

    private fun setupViews() {
        binding.dashboardAdapter.adapter = adapter
        binding.dashboardAdapter.layoutManager = LinearLayoutManager(requireContext())
    }

}