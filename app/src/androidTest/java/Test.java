import android.test.ActivityInstrumentationTestCase2;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
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
        solo.clickInRecyclerView(0);
    }

    public void testEq() throws Exception {

        String eq = solo.getString(R.string.action_equalizer);
        solo.clickOnMenuItem(eq);
        solo.takeScreenshot();
        solo.goBack();

    }

    public void testPlayback() throws Exception {
        solo.takeScreenshot();
        solo.clickInRecyclerView(0);
        solo.takeScreenshot();
    }



    public void testSearch() throws Exception {
        String search = solo.getString(R.string.action_search);
        solo.clickOnMenuItem(search);
        solo.enterText(0,"test");
        solo.clickOnActionBarItem(R.id.action_search);
    }



}
