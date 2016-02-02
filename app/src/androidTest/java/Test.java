/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.activities.SearchActivity;
import com.robotium.solo.Solo;

/**
 * Created by Andry on 11/12/15.
 */
public class Test extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public Test() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }
    public void testAlbum() throws Exception {
        //Unlock the lock screen
        solo.unlockScreen();
        solo.scrollViewToSide(solo.getView("pager"), Solo.RIGHT);
        //solo.drag(100,1000,0,0,1); //marche pas
        solo.takeScreenshot();
        solo.sleep(2000);

        solo.clickInRecyclerView(0);
        solo.sleep(2000);

        solo.takeScreenshot();
        solo.sleep(2000);
    }

    public void testEq() throws Exception {

        String eq = solo.getString(R.string.action_equalizer);
        solo.clickOnMenuItem(eq);
        solo.takeScreenshot();
        solo.sleep(2000);

        solo.goBack();

    }

    public void testPlayback() throws Exception {
        solo.takeScreenshot();
        solo.sleep(2000);

        solo.clickInRecyclerView(0);
        solo.takeScreenshot();
        solo.sleep(2000);

    }

    public void testPrefs() throws Exception {
        String prefs = solo.getString(R.string.preferences);
        solo.clickOnMenuItem(prefs);
        solo.takeScreenshot();
        solo.sleep(2000);

    }


    public void testScroll() throws Exception {
        int pageCount = 4;
        ViewPager pager = (ViewPager) solo.getView("pager");
        for(int i = 0; i < pageCount; i++) {
            solo.scrollViewToSide(pager, Solo.RIGHT);
            solo.takeScreenshot();
            solo.sleep(2000);
        }

    }


    public void testSearch() throws Exception {
        solo.clickOnActionBarItem(R.id.action_search);
        solo.waitForActivity(SearchActivity.class, 2000);
       // solo.enterText(0, "test");

        solo.takeScreenshot();
        solo.sleep(2000);
    }



}
