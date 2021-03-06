package com.bigteeti.sanhgame.ui.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.bigteeti.sanhgame.R
import com.bigteeti.sanhgame.databinding.ActivityMainBinding
import com.bigteeti.sanhgame.ui.adapter.DashboardAdapter
import com.bigteeti.sanhgame.ui.fragment.DashboardFragment
import com.bigteeti.sanhgame.ui.fragment.DashboardFragmentDirections
import com.dakuinternational.common.domain.model.DataContent
import com.dakuinternational.common.domain.model.Response
import com.dakuinternational.common.ui.ActivityViewModel
import com.dakuinternational.common.ui.base.BaseActivity
import com.dakuinternational.common.ui.binding.viewBinding
import com.dakuinternational.common.ui.dialog.AlertUtils
import com.dakuinternational.common.ui.utils.showToast
import com.dakuinternational.common.ui.utils.writeLog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(), DashboardAdapter.OnItemClickListener  {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val viewModel by viewModels<ActivityViewModel>()

    private val sharedViewModel by viewModels<SharedViewModel>()

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.navigation_host) as NavHostFragment
    }

    private val navController get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getData(DATABASE_NAME)
        viewModel.uiState.observe(this){
            when(it){
                is Response.Loading -> showLoading(it.isLoading)
                is Response.Error -> showToast(it.message)
                is Response.Success ->  sharedViewModel.sendData(it.data)
                else -> {
                    //no - op
                }
            }
        }
    }

    companion object{
        const val DATABASE_NAME = "black_jack"

        fun createIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onItemClick(data: DataContent) {
        val direction = DashboardFragmentDirections.toDetails(Gson().toJson(data))
        navController.navigate(direction)
    }


    override fun onBackPressed() {
        if(navHostFragment.childFragmentManager.backStackEntryCount == 0) {
            AlertUtils.alertExit(this){ p0, p1 ->
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> super.onBackPressed()
                    else ->  p0.dismiss()
                }
            }.show()
        }else super.onBackPressed()
    }

}