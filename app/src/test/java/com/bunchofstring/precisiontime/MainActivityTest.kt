package com.bunchofstring.precisiontime

import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bunchofstring.precisiontime.core.NtpTimestampProvider
import com.bunchofstring.precisiontime.core.UnreliableTimeException
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var activity: MainActivity

    @Before
    fun setupForTest() {
        scenario = activityRule.scenario
        scenario.onActivity { activity -> this.activity = activity }
    }

    @Test
    fun test_VisibleNtpHost() {
        Assert.assertNotNull(
            "NTP host view not found",
            activity.findViewById(R.id.ntp_host)
        )
    }

    @Test
    fun test_DisplayReliableTime(){
        val view: TextView = activity.findViewById(R.id.current_time)
        val mockNtpTimestampProvider = getMockNtpTimestampProvider()
        injectTimestampProvider(mockNtpTimestampProvider)

        Assert.assertTrue(
            "Could not display reliable time",
            containsNumber(view.text)
        )
    }

    @Test
    fun test_HideUnreliableTime(){
        val view: TextView = activity.findViewById(R.id.current_time)
        val mockNtpTimestampProvider = getMockNtpTimestampProvider()
        Mockito.`when`(mockNtpTimestampProvider.timestamp).thenThrow(UnreliableTimeException())
        injectTimestampProvider(mockNtpTimestampProvider)

        Assert.assertTrue(
            "Displayed unreliable time",
            LabelMaker().undefinedTimeLabel.equals(view.text)
        )
    }

    @Test
    fun test_NtpHostFieldAction(){
        val hostValueTest = "HOST_VALUE_TEST"
        val view: EditText = activity.findViewById(R.id.ntp_host)
        val mockNtpTimestampProvider = getMockNtpTimestampProvider()

        activity.timestampProvider = mockNtpTimestampProvider
        view.setText(hostValueTest)
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        Mockito.verify(mockNtpTimestampProvider).source = hostValueTest
    }

    private fun getMockNtpTimestampProvider(): NtpTimestampProvider {
        val mockNtpTimestampProvider = Mockito.mock(NtpTimestampProvider::class.java)
        Mockito.`when`(mockNtpTimestampProvider.timestamp).thenReturn(Date().time)
        Mockito.`when`(mockNtpTimestampProvider.secondsToSync).thenReturn(10)
        Mockito.`when`(mockNtpTimestampProvider.secondsSinceLastSync).thenReturn(100)
        return mockNtpTimestampProvider
    }

    private fun containsNumber(s: CharSequence): Boolean {
        return Regex(".*\\d.*").matches(s)
    }

    private fun injectTimestampProvider(ntpTimestampProvider: NtpTimestampProvider){
        activity.timestampProvider = ntpTimestampProvider
        scenario.recreate()
    }
}
