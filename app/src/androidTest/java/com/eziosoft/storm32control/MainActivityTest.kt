package com.eziosoft.storm32control

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get: Rule
    val activityRule :ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun test_isActivityInView() {
//        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.main)).check(matches(isDisplayed()))
        onView(withId(R.id.textView)).check(matches(withText("sasa")))
    }

}