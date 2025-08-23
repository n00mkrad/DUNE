package org.jellyfin.androidtv.ui.livetv;
import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.preference.SystemPreferences;
import org.jellyfin.androidtv.ui.ProgramGridCell;
import org.jellyfin.androidtv.ui.itemhandling.ItemRowAdapter;
import org.jellyfin.androidtv.ui.presentation.MutableObjectAdapter;
import org.jellyfin.androidtv.util.DateTimeExtensionsKt;
import org.jellyfin.androidtv.util.TimeUtils;
import org.jellyfin.androidtv.util.Utils;
import org.jellyfin.androidtv.util.apiclient.EmptyResponse;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.koin.java.KoinJavaComponent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import timber.log.Timber;

// TV manager class
public class TvManager {
    // Channel and program data
    private static List<BaseItemDto> allChannels;
    private static UUID[] channelIds;
    private static HashMap<UUID,ArrayList<BaseItemDto>> mProgramsDict=new HashMap<>();
    private static LocalDateTime needLoadTime;
    private static boolean forceReload;

    //  last live TV channel
    public static UUID getLastLiveTvChannel() {
        return Utils.uuidOrNull(KoinJavaComponent.<SystemPreferences>get(SystemPreferences.class).get(SystemPreferences.Companion.getLiveTvLastChannel()));
    }

    //  last live TV channel
    public static void setLastLiveTvChannel(UUID id) {
        SystemPreferences systemPreferences=KoinJavaComponent.<SystemPreferences>get(SystemPreferences.class);
        systemPreferences.set(SystemPreferences.Companion.getLiveTvPrevChannel(),systemPreferences.get(SystemPreferences.Companion.getLiveTvLastChannel()));
        systemPreferences.set(SystemPreferences.Companion.getLiveTvLastChannel(),id.toString());
        updateLastPlayedDate(id);
        fillChannelIds();
    }

    //  previous live TV channel
    public static UUID getPrevLiveTvChannel() {
        return Utils.uuidOrNull(KoinJavaComponent.<SystemPreferences>get(SystemPreferences.class).get(SystemPreferences.Companion.getLiveTvPrevChannel()));
    }

    //  all channels
    public static List<BaseItemDto> getAllChannels() {
        return allChannels;
    }

    // Force reload
    public static void forceReload() {
        forceReload=true;
    }

    //  In case reload is needed
    public static boolean shouldForceReload() {
        return forceReload;
    }

    // Get channel index
    public static int getAllChannelsIndex(UUID id) {
        if(allChannels==null) return -1;
        for(int i=0;i<allChannels.size();i++) {
            if(allChannels.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    //  channel by index
    public static BaseItemDto getChannel(int ndx) {
        return allChannels.get(ndx);
    }

    // Update last played date
    public static void updateLastPlayedDate(UUID channelId) {
        if(allChannels!=null) {
            int ndx=getAllChannelsIndex(channelId);
            if(ndx>=0) {
                allChannels.set(ndx,TvManagerHelperKt.copyWithLastPlayedDate(allChannels.get(ndx),LocalDateTime.now()));
            }
        }
    }

    //  all channels
    public static void loadAllChannels(Fragment fragment,Function<Integer,Void> outerResponse) {
        TvManagerHelperKt.loadLiveTvChannels(fragment,channels->{
            if(channels!=null) {
                allChannels=new ArrayList<>(channels);
                outerResponse.apply(fillChannelIds());
            } else {
                outerResponse.apply(0);
            }
            return null;
        });
    }

    //  channel IDs
    private static int fillChannelIds() {
        int ndx=0;
        if(allChannels!=null) {
            channelIds=new UUID[allChannels.size()];
            UUID last=getLastLiveTvChannel();
            if(last==null) return ndx;
            int i=0;
            for(BaseItemDto channel:allChannels) {
                channelIds[i++]=channel.getId();
                if(channel.getId().equals(last.toString())) ndx=i;
            }
        }
        return ndx;
    }

    //  programs asynchronously
    public static void getProgramsAsync(Fragment fragment,int startNdx,int endNdx,final LocalDateTime startTime,LocalDateTime endTime,final EmptyResponse outerResponse) {
        LocalDateTime startTimeRounded=startTime.withMinute(startTime.getMinute()>=30?30:0).withSecond(0).withNano(0);
        LocalDateTime endTimeRounded=endTime.minusSeconds(1);
        if(forceReload||needLoadTime==null||startTimeRounded.isAfter(needLoadTime)||!mProgramsDict.containsKey(channelIds[startNdx])||!mProgramsDict.containsKey(channelIds[endNdx])) {
            forceReload=false;
            endNdx=endNdx>channelIds.length?channelIds.length:endNdx+1;
            TvManagerHelperKt.getPrograms(fragment,Arrays.copyOfRange(channelIds,startNdx,endNdx),startTimeRounded,endTimeRounded,programs->{
                if(programs!=null) {
                    Timber.d("*** About to build dictionary");
                    buildProgramsDict(programs,startTimeRounded);
                    Timber.d("*** Programs retrieval finished");
                    outerResponse.onResponse();
                } else {
                    outerResponse.onResponse();
                }
                return null;
            });
            Timber.d("*** About to get programs");
        } else {
            outerResponse.onResponse();
        }
    }

    //  programs dictionary
    private static void buildProgramsDict(Collection<BaseItemDto> programs,LocalDateTime startTime) {
        mProgramsDict=new HashMap<>();
        for(BaseItemDto program:programs) {
            UUID id=program.getChannelId();
            if(!mProgramsDict.containsKey(id)) mProgramsDict.put(id,new ArrayList<BaseItemDto>());
            if(program.getEndDate().isAfter(startTime))
                mProgramsDict.get(id).add(program);
        }
        needLoadTime=startTime.plusMinutes(29);
    }

    //  programs for channel with filters
    public static List<BaseItemDto> getProgramsForChannel(UUID channelId,GuideFilters filters) {
        if(!mProgramsDict.containsKey(channelId)) return new ArrayList<>();
        List<BaseItemDto> results=mProgramsDict.get(channelId);
        boolean passes=filters==null||!filters.any();
        if(passes) return results;
        for(BaseItemDto program:results) {
            passes|=filters.passesFilter(program);
        }
        return passes?results:new ArrayList<BaseItemDto>();
    }

    //  programs for channel
    public static List<BaseItemDto> getProgramsForChannel(UUID channelId) {
        return !mProgramsDict.containsKey(channelId)?new ArrayList<BaseItemDto>():mProgramsDict.get(channelId);
    }

    //  timeline row
    public static void setTimelineRow(Context context,LinearLayout timelineRow,BaseItemDto program) {
        timelineRow.removeAllViews();
        LocalDateTime local=program.getStartDate();
        TextView on=new TextView(context);
        on.setText(context.getResources().getString(R.string.lbl_on));
        timelineRow.addView(on);
        TextView channel=new TextView(context);
        channel.setText(program.getChannelName());
        channel.setTypeface(null,Typeface.BOLD);
        channel.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light));
        timelineRow.addView(channel);
        TextView datetime=new TextView(context);
        datetime.setText(new StringBuilder().append(TimeUtils.getFriendlyDate(context,local)).append(" @ ").append(DateTimeExtensionsKt.getTimeFormatter(context).format(local)).append(" (").append(DateUtils.getRelativeTimeSpanString(local.toInstant(ZoneOffset.UTC).toEpochMilli(),Instant.now().toEpochMilli(),0)).append(")"));
        timelineRow.addView(datetime);
    }

    //  focus parameters
    public static void setFocusParams(LinearLayout currentRow,LinearLayout otherRow,boolean up) {
        for(int currentRowNdx=0;currentRowNdx<currentRow.getChildCount();currentRowNdx++) {
            ProgramGridCell cell=(ProgramGridCell)currentRow.getChildAt(currentRowNdx);
            ProgramGridCell otherCell=getOtherCell(otherRow,cell);
            if(otherCell!=null) {
                if(up) {
                    cell.setNextFocusUpId(otherCell.getId());
                } else {
                    cell.setNextFocusDownId(otherCell.getId());
                }
            }
        }
    }

    //  other cell
    private static ProgramGridCell getOtherCell(LinearLayout otherRow,ProgramGridCell cell) {
        for(int otherRowNdx=0;otherRowNdx<otherRow.getChildCount();otherRowNdx++) {
            ProgramGridCell otherCell=(ProgramGridCell)otherRow.getChildAt(otherRowNdx);
            if(otherCell.getProgram().getEndDate()!=null&&cell.getProgram().getStartDate()!=null&&otherCell.getProgram().getEndDate().isAfter(cell.getProgram().getStartDate())) {
                return otherCell;
            }
        }
        return null;
    }

    // Get schedule rows asynchronously
    public static void getScheduleRowsAsync(Fragment fragment,String seriesTimerId,final Presenter presenter,final MutableObjectAdapter<Row> rowAdapter) {
        TvManagerHelperKt.getScheduleRows(fragment,seriesTimerId,timerMap->{
            for(Map.Entry<LocalDate,? extends List<BaseItemDto>> entry:timerMap.entrySet()) {
                addRow(fragment.getContext(),entry.getValue(),presenter,rowAdapter);
            }
            return null;
        });
    }

    // Add schedule row
    private static void addRow(Context context,List<BaseItemDto> timers,Presenter presenter,MutableObjectAdapter<Row> rowAdapter) {
        ItemRowAdapter scheduledAdapter=new ItemRowAdapter(context,timers,presenter,rowAdapter,true);
        scheduledAdapter.Retrieve();
        ListRow scheduleRow=new ListRow(new HeaderItem(TimeUtils.getFriendlyDate(context,timers.get(0).getStartDate(),true)),scheduledAdapter);
        rowAdapter.add(scheduleRow);
    }
}
