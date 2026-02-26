package com.example.buildflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.buildflow.view.fragments.NewRequestFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NewRequestUITest {

    private void launchFragment() {
        Bundle args = new Bundle();
        args.putString("PROJECT_ID", "test_project_id");
        FragmentScenario.launchInContainer(NewRequestFragment.class, args, R.style.Theme_BuildFlow2);
    }
    @Test
    public void testSubmitWithoutTitle_ShowsError() {
        launchFragment();
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnCatPlumbing))
                .perform(scrollTo(), click());
        onView(withId(R.id.btnSaveDraft)).perform(scrollTo());
        onView(withId(R.id.btnUrgHigh)).perform(click());
        onView(withId(R.id.btnSubmit)).perform(click());
        onView(withId(R.id.etTitle)).check(matches(hasErrorText("Title is required")));
    }
    @Test
    public void testUserCanEnterDetails() {
        launchFragment();
        String titleToType = "Broken Pipe in Kitchen";
        String locationToType = "Floor 1, Apt 4";
        onView(withId(R.id.etTitle))
                .perform(typeText(titleToType), closeSoftKeyboard());
        onView(withId(R.id.btnSubmit)).perform(scrollTo());
        onView(withId(R.id.etLocation))
                .perform(typeText(locationToType), closeSoftKeyboard());
        onView(withId(R.id.etTitle)).check(matches(withText(titleToType)));
        onView(withId(R.id.btnSubmit)).perform(scrollTo());
        onView(withId(R.id.etLocation)).check(matches(withText(locationToType)));
    }
}