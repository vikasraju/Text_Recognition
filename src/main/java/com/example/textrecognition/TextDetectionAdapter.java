package com.example.textrecognition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

public class TextDetectionAdapter extends RecyclerView.Adapter<TextDetectionAdapter.ViewHolder> {

    private List<TextDetectionModel> textDetectionModelList;
    private Context context;

    public TextDetectionAdapter(List<TextDetectionModel> textDetectionModelList, Context context) {
        this.textDetectionModelList = textDetectionModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public TextDetectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_text_detection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextDetectionAdapter.ViewHolder holder, int position) {

        TextDetectionModel textDetectionModel = textDetectionModelList.get(position);
        //holder.text1.setText(MessageFormat.format("Object{0}", String.valueOf(objectDetectionModel.getId())));
        //holder.text1.setText(MessageFormat.format("Object {0}", textDetectionModel.getId()));
        holder.text1.setText(MessageFormat.format("{0}", textDetectionModel.getText()));

    }

    @Override
    public int getItemCount() {
        return textDetectionModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView text1;
        public TextView text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.item_text_detection_text_view1);
            text2 = itemView.findViewById(R.id.item_text_detection_text_view2);
        }
    }
}
