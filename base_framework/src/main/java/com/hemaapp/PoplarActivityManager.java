package com.hemaapp;

import android.app.Activity;

import java.util.ArrayList;

/**
 * activity Manager
 */
public class PoplarActivityManager {
    private static ArrayList<Activity> mActivities;

    /**
     * Find the Activity by class
     *
     * @param cl the class of Activity
     * @return an Activity who at the top of stack or null
     */
    public static Activity getActivityByClass(Class<?> cl) {
        ArrayList<Activity> temp = getActivitysByClass(cl);
        return (temp.size() != 0) ? temp.get(temp.size() - 1) : null;
    }

    /**
     * Find the Activitys by class
     *
     * @param cl the class of Activity
     * @return an ArrayList of Activity
     */
    public static ArrayList<Activity> getActivitysByClass(Class<?> cl) {
        ArrayList<Activity> temp = new ArrayList<Activity>();
        for (Activity ac : mActivities) {
            if (ac.getClass().equals(cl))
                temp.add(ac);
        }
        return temp;
    }

    /**
     * add activity
     *
     * @param activity
     */
    public static void add(Activity activity) {
        if (mActivities == null)
            mActivities = new ArrayList<Activity>();
        if (!mActivities.contains(activity))
            mActivities.add(activity);
    }

    /**
     * remove activity
     *
     * @param activity
     */
    public static void remove(Activity activity) {
        if (mActivities != null)
            mActivities.remove(activity);
    }

    /**
     * get the activity of the last add in
     *
     * @return
     */
    public static Activity getLastActivity() {
        int size = mActivities == null ? 0 : mActivities.size();
        if (size > 0) {
            return mActivities.get(size - 1);
        } else {
            return null;
        }
    }

    /**
     * finish all activity
     */
    public static void finishAll() {
        if (mActivities != null) {
            ArrayList<Activity> tempList = new ArrayList<Activity>();
            tempList.addAll(mActivities);
            for (Activity activity : tempList)
                activity.finish();
            mActivities.clear();
        }
    }

    /**
     * finish other activity
     */
    public static void finishOtherActivity(Activity activity) {
        if (mActivities != null) {
            ArrayList<Activity> tempList = new ArrayList<Activity>();
            tempList.addAll(mActivities);
            for (Activity other : tempList) {
                if (!other.getClass().equals(activity.getClass())) {
                    other.finish();
                    mActivities.remove(other);
                }
            }
        }
    }
}
