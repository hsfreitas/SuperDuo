package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by hamilton.freitas on 2015-11-09.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] FOOTBALL_COLUMNS = {
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


    // these indices must match the projection
    static final int INDEX_ID = 0;
    static final int INDEX_DATE = 1;
    static final int INDEX_TIME = 2;
    static final int INDEX_HOME = 3;
    static final int INDEX_AWAY = 4;
    static final int INDEX_LEAGUE = 5;
    static final int INDEX_HOME_GOALS = 6;
    static final int INDEX_AWAY_GOALS = 7;
    static final int INDEX_MATCH_DAY = 8;
    static final int INDEX_MATCH_ID = 9;



    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                String today = Utilies.getTodayFormatted();

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                Uri matchesId = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(matchesId, FOOTBALL_COLUMNS, null, new String[]{today}, null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_provider_layout);
                double matchId = data.getInt(INDEX_ID);
                String teamHomeName = data.getString(INDEX_HOME);


                String teamAwayName = data.getString(INDEX_AWAY);
                String date = data.getString(INDEX_DATE);
                int homeGoals = data.getInt(INDEX_HOME_GOALS);
                int awaysGoals = data.getInt(INDEX_AWAY_GOALS);

                //Set icon Home
                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(teamHomeName));

                //Set home name
                views.setTextViewText(R.id.home_name, teamHomeName);


                //Set icon Away
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(teamAwayName));

                //Set home name
                views.setTextViewText(R.id.away_name, teamAwayName);

                //Set Match date label
                views.setTextViewText(R.id.data_textview, date);

                views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awaysGoals));


                final Intent fillInIntent = new Intent();

                String today = Utilies.getTodayFormatted();

                Uri matchesId = DatabaseContract.scores_table.buildScoreWithDate();
               // Cursor cursor = getContentResolver().query(matchesId, FOOTBALL_COLUMNS, null, new String[]{"2015-11-15"}, null);

                fillInIntent.setData(matchesId);
                views.setOnClickFillInIntent(R.id.widget, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_provider_layout);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
