package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.ScoresAdapter;

/**
 * Created by hamilton.freitas on 2015-11-03.
 */
public class FootballWidgetIntentService extends IntentService {

    public FootballWidgetIntentService(){
        super("FootballWidgetIntentService");
    }

    private static final String[] MATCHES_COLUMNS ={
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY
    };



    private static final int FOOTBALL_ID = 0;
    private static final int FOOTBALL_HOME_COL = 3;
    private static final int FOOTBALL_AWAY_COL = 4;
    private static final int FOOTBALL_MATCH_DAY = 9;
    private static final int FOOTBALL_HOME_GOALS_COL = 6;
    private static final int FOOTBALL_AWAY_GOALS_COL = 7;



    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                FootballWidgetProvider.class));

        int footballArtResourceId = R.drawable.ic_launcher;

        String today = Utilies.getTodayFormatted();

        Uri matchesId = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor cursor = getContentResolver().query(matchesId, MATCHES_COLUMNS, null, new String[]{"2015-15-11"}, null);

        if(cursor == null) {
            return;
        }

        if(!cursor.moveToFirst()){
            cursor.close();
            return;
        }

        double match_id = cursor.getDouble(ScoresAdapter.COL_ID);
        String clubHomeName = cursor.getString(ScoresAdapter.COL_HOME);
        String clubAwayName = cursor.getString(ScoresAdapter.COL_AWAY);
        String date = cursor.getString(ScoresAdapter.COL_DATE);
        int homeGoals = cursor.getInt(ScoresAdapter.COL_HOME_GOALS);
        int awaysGoals = cursor.getInt(ScoresAdapter.COL_AWAY_GOALS);

        for(int appWidgetId: appWidgetIds){
            int layoutId = R.layout.widget_provider_layout;
            RemoteViews views = new RemoteViews(getPackageName(),layoutId);

            //Set icon Home
            views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(clubHomeName));

            //Set home name
            views.setTextViewText(R.id.home_name, clubHomeName);


            //Set icon Away
            views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(clubAwayName));

            //Set home name
            views.setTextViewText(R.id.away_name, clubAwayName);

            //Set Match date label
            views.setTextViewText(R.id.data_textview, date);

            views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awaysGoals));



            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, clubHomeName);
                setRemoteContentDescription(views, clubAwayName);
                setRemoteContentDescription(views, date);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.home_crest, description);
        views.setContentDescription(R.id.away_crest, description);
        views.setContentDescription(R.id.data_textview, description);
    }
}
