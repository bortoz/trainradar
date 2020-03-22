package it.trainradar.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import it.trainradar.R;
import it.trainradar.core.Train;

public class TrainManager extends BaseManager {
    private static boolean isLoaded = false;
    private static List<Train> trains;
    private static int frecciarossa;
    private static int frecciargento;
    private static int frecciabianca;
    private static int intercity;
    private static Bitmap trainIcon;

    public static void load(Context context) {
        if (isLoaded) return;
        isLoaded = true;

        executeTask(() -> {
            trains = Arrays.asList(gson.fromJson(getRawResources(context, R.raw.trains), Train[].class));
            Collections.shuffle(trains);
        });

        frecciarossa = context.getColor(R.color.frecciarossa);
        frecciargento = context.getColor(R.color.frecciargento);
        frecciabianca = context.getColor(R.color.frecciabianca);
        intercity = context.getColor(R.color.intercity);

        Drawable trainDrawable = context.getDrawable(R.drawable.ic_train);
        trainIcon = Bitmap.createBitmap(trainDrawable.getIntrinsicWidth(), trainDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(trainIcon);
        trainDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        trainDrawable.draw(canvas);
    }

    public static Train getTrain(String agency, String cateogory, String name) {
        String fullName = String.format(Locale.ITALY, "%s %s %s", agency, cateogory, name);
        return trains.stream().filter(t -> t.getName().equals(fullName)).findAny().orElse(null);
    }

    public static List<Train> getTrains() {
        return trains;
    }

    public static List<String> getAgencies() {
        return trains.stream().map(Train::getAgency).distinct().sorted().collect(Collectors.toList());
    }

    public static List<String> getCategories() {
        return trains.stream().map(Train::getCategory).distinct().sorted().collect(Collectors.toList());
    }

    public static SpannableString getFormattedName(Train train) {
        SpannableString trainFormat = new SpannableString(train.getName());
        if (train.getCategory().equals("FR") || train.getCategory().equals("ITA")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciarossa), 0, trainFormat.length(), 0);
        } else if (train.getCategory().equals("FA")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciargento), 0, trainFormat.length(), 0);
        } else if (train.getCategory().equals("FB")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciabianca), 0, trainFormat.length(), 0);
        } else if (train.getCategory().equals("IC") || train.getCategory().equals("EC") ||
                train.getCategory().equals("ICN") || train.getCategory().equals("EN")) {
            trainFormat.setSpan(new ForegroundColorSpan(intercity), 0, trainFormat.length(), 0);
        }
        return trainFormat;
    }

    public static BitmapDescriptor getTrainIcon() {
        return BitmapDescriptorFactory.fromBitmap(trainIcon);
    }
}
