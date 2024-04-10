package com.example.spirala1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isAbove
import androidx.test.espresso.assertion.PositionAssertions.isRightOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*


class IntentInstrumentedTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MovieDetailActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }


    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.spirala1", appContext.packageName)
    }
    @Test
    fun testDetailActivityInstantiation(){

        val pokreniDetalje = Intent(ApplicationProvider.getApplicationContext(),MovieDetailActivity::class.java)
        pokreniDetalje.putExtra("movie_title","Pride and prejudice")
        val scenario = ActivityScenario.launch<MovieDetailActivity>(pokreniDetalje)

        onView(withId(R.id.movie_poster)).check(matches(withImage(R.drawable.drama)))
        onView(withId(R.id.movie_title)).check(matches(withText("Pride and prejudice")))
        onView(withId(R.id.movie_genre)).check(matches(withText("drama")))
        onView(withId(R.id.movie_overview)).check(
                matches(
                        withSubstring(
                                "pirited Elizabeth Bennet meets single, rich, and proud Mr. Darcy"
                        )
                )
        )
        scenario.close()
    }

    @Test
    fun testLinksIntent(){
        val pokreniDetalje = Intent(ApplicationProvider.getApplicationContext(),MovieDetailActivity::class.java)
        pokreniDetalje.putExtra("movie_title","Pride and prejudice")
        val scenario = ActivityScenario.launch<MovieDetailActivity>(pokreniDetalje)
        onView(withId(R.id.movie_website)).perform(click())
        Intents.intended(hasAction(Intent.ACTION_VIEW))
        scenario.close()
    }

    @Test
    fun testMovieTitleClickOpensQuery() {
        val pokreniDetalje = Intent(ApplicationProvider.getApplicationContext(), MovieDetailActivity::class.java)
        pokreniDetalje.putExtra("movie_title", "Pride and prejudice")
        val scenario = ActivityScenario.launch<MovieDetailActivity>(pokreniDetalje)
        onView(withId(R.id.movie_title)).perform(click())

        val recordedIntents = Intents.getIntents()
        recordedIntents.forEach { intent ->
            Log.d("RecordedIntent", intent.toString())
        }
        val expectedSearchQuery = "Pride and prejudice trailer"
        val expectedUri = Uri.parse("https://www.google.com/search?q=$expectedSearchQuery")

        var foundMatchingIntent = false
        for (intent in recordedIntents) {
            if (intent.action == Intent.ACTION_VIEW) {
                foundMatchingIntent = true
                break
            }
        }

        assertTrue("Expected intent to open query", foundMatchingIntent)
        scenario.close()
    }



    @Test
    fun testLayoutElements() {

        val pokreniDetalje = Intent(ApplicationProvider.getApplicationContext(),MovieDetailActivity::class.java)
        pokreniDetalje.putExtra("movie_title","Pride and prejudice")
        val scenario = ActivityScenario.launch<MovieDetailActivity>(pokreniDetalje)

        onView(withId(R.id.movie_title)).check(isAbove(withId(R.id.movie_release_date)))

        onView(withId(R.id.movie_genre)).check(isAbove(withId(R.id.movie_overview)))

        onView(withId(R.id.movie_title)).check(isRightOf(withId(R.id.movie_poster)))

        onView(withId(R.id.shareButton)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun testShareButtonOpensShareIntent() {
        val pokreniDetalje = Intent(ApplicationProvider.getApplicationContext(), MovieDetailActivity::class.java)
        pokreniDetalje.putExtra("movie_title", "Pride and prejudice")
        val scenario = ActivityScenario.launch<MovieDetailActivity>(pokreniDetalje)
        onView(withId(R.id.shareButton)).perform(click())
        intended(allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(Intent.EXTRA_INTENT, allOf(
                hasAction(Intent.ACTION_SEND),
                hasType("text/plain"),
                hasExtra(Intent.EXTRA_SUBJECT, "Movie Details")
            ))
        ))
    }

    @Test
    fun testFavoriteMoviesListDisplayed() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)


        val scenario = ActivityScenario.launch<MainActivity>(intent)

        onView(withId(R.id.favoriteMovies)).check(matches(isDisplayed()))

        onView(withId(R.id.favoriteMovies)).check(matches(hasMinimumChildCount(1)))
        scenario.close()
    }

    private fun withImage(@DrawableRes id: Int) = object : TypeSafeMatcher<View>(){
        override fun describeTo(description: Description) {
            description.appendText("Drawable does not contain image with id: $id")
        }

        override fun matchesSafely(item: View): Boolean {
            val context: Context? = item.context
            val bitmap: Bitmap? = context?.getDrawable(id)?.toBitmap()
            return item is ImageView && item.drawable.toBitmap().sameAs(bitmap)
        }

    }
}