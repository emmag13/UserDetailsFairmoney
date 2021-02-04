package com.demoproject.userdetails.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.demoproject.restapi.UserDetailsCloud
import com.demoproject.restapi.models.UserDetailsData
import com.demoproject.restapi.models.UsersDetails
import com.demoproject.userdetails.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UsersRecyclerAdapter(private val context: Context, usersDetails: List<UsersDetails>?) : RecyclerView.Adapter<UsersViewHolder>() {
    private var usersDetails: List<UsersDetails>? = null

    var shimmerContainer: ShimmerFrameLayout? = null
    private var contentArea: RelativeLayout? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return UsersViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {

        //bind the data to the view:
        if (usersDetails != null) {
            holder.userEmail.text = usersDetails!![position].email
            holder.userFullName.text = usersDetails!![position].lastName + " " + usersDetails!![position].firstName

            if (!TextUtils.isEmpty(usersDetails.toString())) {
                Picasso.get().load(usersDetails!![position].picture).into(holder.userProfileImage, object : Callback {
                    override fun onSuccess() {
                        holder.loadingIndicator.visibility = View.GONE
                        holder.userProfileImage.visibility = View.VISIBLE
                    }

                    override fun onError(e: Exception) {}
                })
            }

            //handle clicks:
            holder.setItemClickListener {

                //launch the bottom sheet that shows a user's detail:
                val nullParent: ViewGroup? = null
                val modelBottom: View = LayoutInflater.from(context).inflate(R.layout.fragment_bottomsheet, nullParent)

                val mBehavior: BottomSheetBehavior<*>?

                val dialog = BottomSheetDialog(context)
                dialog.setContentView(modelBottom)

                mBehavior = BottomSheetBehavior.from(modelBottom.parent as View)
                mBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
                shimmerContainer = modelBottom.findViewById(R.id.shimmer_view_container)
                contentArea = modelBottom.findViewById(R.id.content_area)

                if (usersDetails != null) {
                    shimmerContainer?.visibility = View.VISIBLE
                    contentArea?.visibility = View.GONE
                    shimmerContainer!!.startShimmer()

                    val appId = "601438f53eb3e6fd3d65936f"
                    UserDetailsCloud.getInstance(context).getUserDetailData(appId, usersDetails!![position].id, object : retrofit2.Callback<UserDetailsData?> {
                        @SuppressLint("SetTextI18n")
                        override fun onResponse(call: Call<UserDetailsData?>, response: Response<UserDetailsData?>) {
                            if (response.code() == 200) {
                                val userDetailsData: UserDetailsData? = response.body()

                                // set data to view
                                if (userDetailsData != null) {
                                    shimmerContainer?.stopShimmer()
                                    shimmerContainer?.visibility = View.GONE
                                    contentArea?.visibility = View.VISIBLE

                                    //load the user's profile picture:
                                    if (!TextUtils.isEmpty(userDetailsData.toString())) {
                                        Picasso.get().load(userDetailsData.picture).into(modelBottom.findViewById<View>(R.id.ProfileImage) as CircleImageView)
                                    }

                                    (modelBottom.findViewById<View>(R.id.FullName) as TextView).text = userDetailsData.firstName.capitalize(Locale.ROOT) + " " + userDetailsData.lastName.capitalize(Locale.ROOT)

                                    (modelBottom.findViewById<View>(R.id.PhoneNumber) as CardView).setOnClickListener {
                                        val callIntent = Intent(Intent.ACTION_CALL)
                                        callIntent.data = Uri.parse("tel:"+ "" +userDetailsData.phone)
                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CALL_PHONE),
                                                    10)
                                        } else {
                                            try {
                                                context.startActivity(callIntent)
                                            } catch (ex: ActivityNotFoundException) {
                                                Toast.makeText(context, "Error to open contact", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    (modelBottom.findViewById<View>(R.id.email) as CardView).setOnClickListener {
                                        val mailto = "mailto:" + "" + userDetailsData.email +
                                                "?cc=" +
                                                "&subject=" + Uri.encode("From FairMoney Test App") +
                                                "&body=" + Uri.encode("")
                                        val emailIntent = Intent(Intent.ACTION_SENDTO)
                                        emailIntent.data = Uri.parse(mailto)

                                        try {
                                            context.startActivity(emailIntent)
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, "Error to open email app", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    (modelBottom.findViewById<View>(R.id.Gender) as TextView).text = userDetailsData.gender.capitalize(Locale.ROOT)
                                    (modelBottom.findViewById<View>(R.id.DOB) as TextView).text = getDate(userDetailsData.dateOfBirth)
                                    (modelBottom.findViewById<View>(R.id.RegDate) as TextView).text = getDate(userDetailsData.registerDate)
                                    (modelBottom.findViewById<View>(R.id.Location) as TextView).text = userDetailsData.location.street + " " + userDetailsData.location.city + " " + userDetailsData.location.state + " " + userDetailsData.location.country
                                }
                            }
                        }

                        override fun onFailure(call: Call<UserDetailsData?>, t: Throwable) {
                            t.printStackTrace()
                            Toast.makeText(context, "Network Connection Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return usersDetails!!.size
    }

    init {
        if (usersDetails != null) {
            this.usersDetails = usersDetails
        }
    }


    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    private fun getDate(dd: String): String? {
        val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault())
        val sdfIn = SimpleDateFormat("yyyy-MM-dd")
        val date = sdfIn.parse(dd)
        return dateFormat.format(date)
    }
}