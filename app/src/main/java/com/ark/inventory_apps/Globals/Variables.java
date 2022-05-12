package com.ark.inventory_apps.Globals;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Variables {
    public static DatabaseReference referenceRoot = FirebaseDatabase.getInstance().getReference();

    // user data
    public static String uid = null;
    public static String username = null;
}
