package it.trainradar.manager;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class TrainRadarProvider {
    private static final Set<Class> loadedManagers = new HashSet<>();

    public static void load(Context context, Class<? extends Manager> managerClass) {
        try {
            if (!loadedManagers.contains(managerClass)) {
                managerClass.getMethod("load", Context.class).invoke(null, context);
                loadedManagers.add(managerClass);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }
}
