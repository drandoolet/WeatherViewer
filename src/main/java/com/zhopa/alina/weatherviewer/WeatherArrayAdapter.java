package com.zhopa.alina.weatherviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherArrayAdapter extends ArrayAdapter {
    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView, lowTextView, highTextView, humidityTextView;
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(strings[0]);

                connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream()){
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(strings[0], bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Map<String, Bitmap> bitmaps = new HashMap<>();

    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Weather day = (Weather) getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView = convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = convertView.findViewById(R.id.lowTextView);
            viewHolder.highTextView = convertView.findViewById(R.id.highTextView);
            viewHolder.humidityTextView = convertView.findViewById(R.id.humidityTextView);

            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        if (bitmaps.containsKey(day.iconURL)) {
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        } else {
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }

        Context context = getContext();
        viewHolder.dayTextView.setText(context.getString(R.string.day_description, day.dayOfWeek, day.description));
        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));
        viewHolder.highTextView.setText(context.getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));

        return convertView;
    }
}
