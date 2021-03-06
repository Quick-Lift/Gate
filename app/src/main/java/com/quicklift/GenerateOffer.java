package com.quicklift;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by adarsh on 6/6/18.
 */

public class GenerateOffer {
    static File logFile;

    public GenerateOffer() {
        logFile = new File(Environment.getExternalStorageDirectory()+"/QuickLift/offers.txt");
        if (!logFile.exists()){
            try{
                File log = new File(Environment.getExternalStorageDirectory()+"/QuickLift/");
                log.mkdir();
                logFile.createNewFile();
            }
            catch (IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    static public void appendLog(String text){
//        File logFile = new File(Environment.getExternalStorageDirectory()+"/QuickLift/log.txt");
//        if (!logFile.exists()){
//            try{
//                logFile.createNewFile();
//            }
//            catch (IOException e){
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        try{
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text+"\n");
            buf.newLine();
            buf.close();
        }
        catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        Log.v("Save","hi");
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss : ");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime())+"\t";
    }
}
