package com.bunchofstring.precisiontime

import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bunchofstring.precisiontime.core.TimestampProvider
import com.bunchofstring.precisiontime.core.UnreliableTimeException
import com.bunchofstring.test.LifecycleTestRule
import com.bunchofstring.test.flaky.FlakyTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class CoreUiTest {

    private val HOST_VALUE = "HOST_VALUE_TEST"

    private val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var mockNtpTimestampProvider: TimestampProvider// = Mockito.spy(NtpTimestampProvider::class.java)
    private lateinit var activity: MainActivity

    @Rule
    @JvmField
    var ruleChain = RuleChain.emptyRuleChain()
        .around(FlakyTestRule())
        .around(activityRule)
        .around(object: LifecycleTestRule(){
            override fun before() {
                activityRule.scenario
                    .moveToState(Lifecycle.State.CREATED)
                    .onActivity {
                        activity = it

                        //Inject the mock
                        mockNtpTimestampProvider = Mockito.spy(activity.timestampProvider)
                        activity.timestampProvider = mockNtpTimestampProvider
                    }
            }
            override fun after() {
                //No implementation
            }
        })

    //TODO: Figure out how to let frame-by-frame UI updates occur before the assertion

    @Test
    fun test_DisplayReliableTime(){
        //Arrange
        simulateReliableTime(mockNtpTimestampProvider)

        //Act
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        //Assert
        val view: TextView = activity.findViewById(R.id.current_time)
        Assert.assertTrue(
            "Could not display reliable time. Instead, found "+view.text+".",
            containsNumber(view.text)
        )
    }

    @Test
    fun test_SyncInProgress(){
        //Arrange
        Mockito.doReturn(true)
            .`when`(mockNtpTimestampProvider)
            .isSyncInProgress

        //Act
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        //Assert
        val view = activity.findViewById(R.id.status_actively_syncing) as View
        Assert.assertEquals(
            "Does not appear to be syncing.",
            View.VISIBLE, view.visibility
        )
    }

    @Test
    fun test_SyncNotInProgress(){
        //Arrange
        Mockito.doReturn(false)
            .`when`(mockNtpTimestampProvider)
            .isSyncInProgress

        //Act
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        //Assert
        val view = activity.findViewById(R.id.status_actively_syncing) as View
        Assert.assertEquals(
            "Appears to be actively syncing.",
            View.GONE, view.visibility
        )
    }

    @Test
    fun test_HideUnreliableTime(){
        //Arrange
        simulateUnreliableTime(mockNtpTimestampProvider)

        //Act
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        //Assert
        val view: TextView = activity.findViewById(R.id.current_time)
        Assert.assertTrue(
            "Displayed unreliable time " + view.text,
            LabelMaker().undefinedTimeLabel.equals(view.text)
        )
    }

    @Test
    fun test_NtpHostFieldAction(){
        //Arrange
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        Mockito.clearInvocations(mockNtpTimestampProvider)

        //Act
        val view: EditText = activity.findViewById(R.id.ntp_host)
        view.setText(HOST_VALUE)
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
//Espresso.onView(ViewMatchers.withId(R.id.ntp_host))
//    .perform(ViewActions.typeText(HOST_VALUE))
//    .perform(ViewActions.pressImeActionButton())
//    .perform(ViewActions.closeSoftKeyboard())

        //Assert
        Mockito.verify(mockNtpTimestampProvider).source = HOST_VALUE
    }

    private fun simulateReliableTime(mockNtpTimestampProvider: TimestampProvider) {
        Mockito.doReturn(Date().time).`when`(mockNtpTimestampProvider).timestamp
        Mockito.doReturn(10L).`when`(mockNtpTimestampProvider).secondsToSync
        Mockito.doReturn(100L).`when`(mockNtpTimestampProvider).secondsSinceLastSync
    }

    private fun simulateUnreliableTime(mockNtpTimestampProvider: TimestampProvider) {
        val ute = UnreliableTimeException()
        Mockito.doThrow(ute).`when`(mockNtpTimestampProvider).timestamp
        Mockito.doThrow(ute).`when`(mockNtpTimestampProvider).secondsToSync
        Mockito.doThrow(ute).`when`(mockNtpTimestampProvider).secondsSinceLastSync
    }

    private fun containsNumber(s: CharSequence): Boolean {
        return Regex(".*\\d.*").matches(s)
    }
}
