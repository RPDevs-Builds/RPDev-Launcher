package app.lawnchair.compatlib.fourteen;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.Intent;
import android.graphics.Rect;
import android.view.IRecentsAnimationController;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationTarget;
import android.window.TaskSnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.lawnchair.compatlib.ActivityManagerCompat;
import app.lawnchair.compatlib.RecentsAnimationRunnerCompat;

import java.util.Collections;
import java.util.List;

@RequiresApi(34)
public class ActivityManagerCompatVU implements ActivityManagerCompat {

    @Override
    public void invalidateHomeTaskSnapshot(final Activity homeActivity) {
        try {
            if (homeActivity != null) {
                ActivityTaskManager.getService().getClass()
                        .getMethod("invalidateHomeTaskSnapshot", android.os.IBinder.class)
                        .invoke(ActivityTaskManager.getService(), homeActivity.getActivityToken());
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void startRecentsActivity(
            Intent intent, long eventTime, RecentsAnimationRunnerCompat runnerCompat) {
        try {
            IRecentsAnimationRunner runner = runnerCompat != null ? new IRecentsAnimationRunner.Stub() {
                @Override
                public void onAnimationStart(
                        IRecentsAnimationController controller,
                        RemoteAnimationTarget[] apps,
                        RemoteAnimationTarget[] wallpapers,
                        Rect homeContentInsets,
                        Rect minimizedHomeBounds) {
                    runnerCompat.onAnimationStart(
                            controller, apps, wallpapers, homeContentInsets, minimizedHomeBounds);
                }

                @Override
                public void onAnimationCanceled(int[] taskIds, TaskSnapshot[] taskSnapshots) {
                    runnerCompat.onAnimationCanceled(taskIds, taskSnapshots);
                }

                @Override
                public void onTasksAppeared(RemoteAnimationTarget[] apps) {
                    runnerCompat.onTasksAppeared(apps);
                }
            } : null;

            ActivityTaskManager.getService().startRecentsActivity(intent, eventTime, runner);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    @Override
    public TaskSnapshot getTaskSnapshot(
            int taskId, boolean isLowResolution, boolean takeSnapshotIfNeeded) {
        try {
            return ActivityTaskManager.getService().getTaskSnapshot(
                    taskId, isLowResolution, takeSnapshotIfNeeded);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    @Override
    public ActivityManager.RunningTaskInfo getRunningTask(boolean filterOnlyVisibleRecents) {
        List<ActivityManager.RunningTaskInfo> tasks = getRunningTasks(filterOnlyVisibleRecents);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    @NonNull
    @Override
    public List<ActivityManager.RunningTaskInfo> getRunningTasks(boolean filterOnlyVisibleRecents) {
        try {
            return ActivityTaskManager.getInstance().getTasks(
                    NUM_RECENT_ACTIVITIES_REQUEST, filterOnlyVisibleRecents);
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    @NonNull
    @Override
    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int numTasks, int userId) {
        try {
            return ActivityTaskManager.getInstance().getRecentTasks(
                    numTasks, ActivityManager.RECENT_IGNORE_UNAVAILABLE, userId);
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }
}
