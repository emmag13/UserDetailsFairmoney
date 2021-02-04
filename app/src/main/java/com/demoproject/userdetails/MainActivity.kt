package com.demoproject.userdetails

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.demoproject.restapi.UserDetailsCloud
import com.demoproject.restapi.models.Data
import com.demoproject.restapi.models.UsersDetails
import com.demoproject.userdetails.MainActivity.UIStateViewModel.UIState.DATA_FOUND
import com.demoproject.userdetails.MainActivity.UIStateViewModel.UIState.DEFAULT
import com.demoproject.userdetails.MainActivity.UIStateViewModel.UIState.FAILED
import com.demoproject.userdetails.MainActivity.UIStateViewModel.UIState.LOADING
import com.demoproject.userdetails.MainActivity.UIStateViewModel.UIState.NO_DATA
import com.demoproject.userdetails.adapter.UsersRecyclerAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {
    var usersList: RecyclerView? = null
    var usersAdapter: UsersRecyclerAdapter? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var contentArea: LinearLayout? = null
    private var netError: LinearLayout? = null
    private var emptyElement: LinearLayout? = null
    private var mainContainer: FrameLayout? = null

    private var shimmerContainer: ShimmerFrameLayout? = null

    var data: Data? = null

    var userDetails: List<UsersDetails>? = null

    var uiStateViewModel: UIStateViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_main)

        uiStateViewModel = ViewModelProviders.of(this).get(UIStateViewModel::class.java)
        setUpViewModels()

        usersList = findViewById(R.id.users_list)
        mainContainer = findViewById(R.id.main_container)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        usersList?.layoutManager = layoutManager
        usersList?.setHasFixedSize(true)

        shimmerContainer = findViewById(R.id.shimmer_view_container)
        swipeRefreshLayout = findViewById(R.id.swipe_to_refresh)
        contentArea = findViewById(R.id.content_area)
        netError = findViewById(R.id.net_error)
        emptyElement = findViewById(R.id.emptyElement)

        swipeRefreshLayout?.post {
            swipeRefreshLayout?.isRefreshing = true
            loadUsersList()
        }

        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = (true)
            loadUsersList()
            uiStateViewModel?.setUIState(DEFAULT)
        }
    }

    /*** This function method controls the state of the UI ***/
    private fun setUpViewModels() {
        uiStateViewModel?.stateUI?.observe(this, { state ->
            when (state) {
                DEFAULT -> {
                    contentArea?.visibility = View.VISIBLE
                    netError?.visibility = (View.GONE)
                    emptyElement?.visibility = (View.GONE)
                }
                LOADING -> {
                    //determines the UI state when the app is loading data
                    shimmerContainer?.visibility = View.VISIBLE
                    contentArea?.visibility = View.GONE
                    shimmerContainer!!.startShimmer()
                }
                DATA_FOUND -> {
                    //determines the UI state when the app has found data
                    swipeRefreshLayout?.isRefreshing = false
                    shimmerContainer?.stopShimmer()
                    shimmerContainer?.visibility = View.GONE
                    contentArea?.visibility = View.VISIBLE
                    netError?.visibility = View.GONE
                    emptyElement?.visibility = View.GONE
                }
                NO_DATA -> {
                    //determines the UI state when there is no data to load.
                    contentArea?.visibility = View.GONE
                    emptyElement?.visibility = View.VISIBLE
                }
                FAILED -> {
                    //determines the UI state when the app failed to get data.
                    shimmerContainer!!.stopShimmer()
                    shimmerContainer?.visibility = View.GONE
                    val snackBar = Snackbar.make(mainContainer!!, "Network Connection Error", Snackbar.LENGTH_SHORT)
                    snackBar.show()
                    swipeRefreshLayout?.isRefreshing = false
                    contentArea?.visibility = View.GONE
                    netError?.visibility = View.VISIBLE
                }

            }
        })
    }


    class UIStateViewModel : ViewModel() {
        private val uiState = MutableLiveData<Int>()

        val stateUI: LiveData<Int> = uiState

        internal object UIState {
            const val DEFAULT = 0
            const val LOADING = 1
            const val DATA_FOUND = 2
            const val NO_DATA = 3
            const val FAILED = 4
        }

        fun setUIState(state: Int) {
            uiState.postValue(state)
            Log.d(TAG, "UI State: '$state' Set.")
        }

        companion object {
            private const val TAG = "UIStateVM"
        }

        init {
            uiState.postValue(DEFAULT)
        }
    }


    fun loadUsersList() {
        uiStateViewModel?.setUIState(LOADING)

        //make request to fetch users:
        val appId = "601438f53eb3e6fd3d65936f"
        UserDetailsCloud.getInstance(applicationContext).getUsersDetails(appId, object : Callback<Data?> {
            override fun onResponse(call: Call<Data?>, response: Response<Data?>) {
                if (response.code() == 200) {
                    data = response.body()
                    if (data!!.usersDetails != null) {

                        /*** Fetches users and displays them in the recyclerView ***/
                        if (data!!.usersDetails.isNotEmpty()) {
                            userDetails = data!!.usersDetails

                            usersAdapter = UsersRecyclerAdapter(this@MainActivity, userDetails)

                            usersList?.adapter = usersAdapter

                            uiStateViewModel?.setUIState(DATA_FOUND)

                            //No user was found:
                            if (usersAdapter!!.itemCount < 1) {
                                uiStateViewModel?.setUIState(NO_DATA)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Data?>, t: Throwable) {
                t.printStackTrace()
                uiStateViewModel?.setUIState(FAILED)
            }
        })
    }
}