package com.example.AlejaGuidanceSystem.Utility;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class Utility {

    /**
     * The method saveObject can be used to save an arbitrary object in the internal storage.
     * How to use example:
     *      Integer integer = new Integer(42);
     *      Utility.saveObject(this, "test", integer);
     *
     * @param context: Context of the currend Activity
     * @param id: Name to save the object. Used as key to load it again
     * @param obj: The object which should be saved
     */
    public static void saveObject(Context context, String id, Object obj){
        ObjectOutput oos;
        try {
            FileOutputStream fos = context.openFileOutput(id + ".data", Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The method loadObject can be used to load an object in the internal storage. If there is no
     * saved file with the given id, the obj is unchanged.
     * How to use example:
     *      Integer integer = null;
     *      Utility.loadObject(this, "test", integer);
     *      if (integer == null){
     *          initialize
     *          integer = new Integer(4);
     *      }
     *
     * @param context: Context of the current Activity
     * @param id: The key to load the desired object
     * @param obj: The object reference to load the saved content
     */
    public static void loadObject(Context context, String id, Object obj){
        ObjectInput ois;
        try {
            FileInputStream fis = context.openFileInput(id +  ".data");
            ois = new ObjectInputStream(fis);
            obj = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
