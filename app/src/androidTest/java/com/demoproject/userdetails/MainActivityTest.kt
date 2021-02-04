package com.demoproject.userdetails

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.demoproject.restapi.UserDetailsCloud
import com.demoproject.restapi.models.Data
import com.demoproject.restapi.models.UsersDetails
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.demoproject.userdetails", appContext.packageName)
    }

    @Test
    @Throws(IOException::class)
    fun testUsersList() {
        val expected = Data()
        val detailsList = ArrayList<UsersDetails>()
        val detail1 = UsersDetails("0F8JIqi4zwvb77FGz6Wt", "Fiedler", "Heinz-Georg", "heinz-georg.fiedler@example.com")
        detailsList.add(detail1)
        expected.usersDetails = detailsList
        val apiService = UserDetailsCloud.getInstance(InstrumentationRegistry.getInstrumentation().targetContext).apiService
        val response = apiService.getUsersDetails(appId).execute()
        val data = response.body()
        Assert.assertNotNull(data)
        Assert.assertEquals(expected.usersDetails[0].id, data!!.usersDetails[0].id)
        Assert.assertEquals(expected.usersDetails[0].lastName, data.usersDetails[0].lastName)
        Assert.assertEquals(expected.usersDetails[0].firstName, data.usersDetails[0].firstName)
        Assert.assertEquals(expected.usersDetails[0].email, data.usersDetails[0].email)
    }

    companion object {
        const val appId = "601438f53eb3e6fd3d65936f"
    }
}