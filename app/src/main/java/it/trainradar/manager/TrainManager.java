package it.trainradar.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.trainradar.R;
import it.trainradar.core.Train;

public class TrainManager extends JsonManager {
    private static List<Train> trains;
    private static int frecciarossa;
    private static int frecciargento;
    private static int frecciabianca;
    private static int intercity;
    private static Bitmap trainIcon;

    public static void load(Context context) {
        trains = Arrays.asList(gson.fromJson(getRawResources(context, R.raw.trains), Train[].class));

        Collections.shuffle(trains);
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

    public static List<Train> getTrains() {
        return trains;
    }

    public static SpannableString getFormattedName(Train train) {
        SpannableString trainFormat = new SpannableString(train.getName());
        if (train.getName().startsWith("FR ")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciarossa), 0, trainFormat.length(), 0);
        } else if (train.getName().startsWith("FA ")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciargento), 0, trainFormat.length(), 0);
        } else if (train.getName().startsWith("FB ")) {
            trainFormat.setSpan(new ForegroundColorSpan(frecciabianca), 0, trainFormat.length(), 0);
        } else if (train.getName().startsWith("IC ") || train.getName().startsWith("IN ") ||
                train.getName().startsWith("EC ") || train.getName().startsWith("EN ") ||
                train.getName().startsWith("ICN ") || train.getName().startsWith("ECN ")) {
            trainFormat.setSpan(new ForegroundColorSpan(intercity), 0, trainFormat.length(), 0);
        }
        return trainFormat;
    }

    public static Bitmap getTrainIcon() {
        return trainIcon;
    }
}
