package com.example.textrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.theartofdev.edmodo.cropper.CropImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FrameProcessor {

    private Facing cameraFacing = Facing.FRONT;
    private ImageView imageView;
    private CameraView textDetectionCameraView;
    private RecyclerView bottomSheetRecyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private ArrayList<TextDetectionModel> textDetectionModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textDetectionModels = new ArrayList<>();
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        imageView = findViewById(R.id.text_detection_image_view);
        textDetectionCameraView = findViewById(R.id.text_detection_camera_view);

        Button toggle = findViewById(R.id.text_detection_camera_toggle_button);
        FrameLayout bottomSheetButton = findViewById(R.id.bottom_sheet_button);
        bottomSheetRecyclerView = findViewById(R.id.bottom_sheet_recycler_view);

        textDetectionCameraView.setFacing(cameraFacing);
        textDetectionCameraView.setLifecycleOwner(MainActivity.this);
        textDetectionCameraView.addFrameProcessor(MainActivity.this);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFacing = (cameraFacing == Facing.FRONT) ? Facing.BACK : Facing.FRONT;
                textDetectionCameraView.setFacing(cameraFacing);
            }
        });

        bottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().start(MainActivity.this);

            }
        });

        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        bottomSheetRecyclerView.setAdapter(new TextDetectionAdapter(textDetectionModels, MainActivity.this));


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                assert result != null;
                Uri imageUri = result.getUri();
                try {
                    analyseImage(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void analyseImage(final Bitmap bitmap) {

        if(bitmap == null){
            Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
            return;
        }
        imageView.setImageBitmap(null);
        textDetectionModels.clear();

        Objects.requireNonNull(bottomSheetRecyclerView.getAdapter()).notifyDataSetChanged();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        showProgress();
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        detector.processImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {

                        Bitmap mutableImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        detectText(firebaseVisionText, mutableImage);

                        imageView.setImageBitmap(mutableImage);

                        hideProgress();
                        bottomSheetRecyclerView.getAdapter().notifyDataSetChanged();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                        hideProgress();

                    }
                });

    }

    private void detectText(FirebaseVisionText firebaseVisionText, Bitmap bitmap) {

        if(firebaseVisionText == null || bitmap == null){
            Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
            return;
        }

        Canvas canvas = new Canvas(bitmap);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(5f);

        //for(int i=0; i< firebaseVisionText)
        String resultText = firebaseVisionText.getText();

        int i=0;
        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {


            String blockText = block.getText();

            textDetectionModels.add(new TextDetectionModel(i, "Recognized text =" + blockText));

            Float blockConfidence = block.getConfidence();

            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            if(blockLanguages.size() != 0)
            {
                textDetectionModels.add(new TextDetectionModel(i, "text language= " + blockLanguages.get(0)));
            }


            //for(int j = 0; j< blockLanguages.size(); j++){
             //   textDetectionModels.add(new TextDetectionModel(i, "text language= " + blockLanguages.get(j)));
           // }

            canvas.drawRect(block.getBoundingBox(), textPaint);

            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }

            i++;
        }

    }

    private void showProgress() {
        findViewById(R.id.bottom_sheet_button_image).setVisibility(View.GONE);
        findViewById(R.id.bottom_sheet_button_progress_bar).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.bottom_sheet_button_image).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_sheet_button_progress_bar).setVisibility(View.GONE);
    }


    @Override
    public void process(@NonNull Frame frame) {

        int width = frame.getSize().getWidth();
        int height = frame.getSize().getHeight();

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(
                        (cameraFacing == Facing.FRONT)
                                ? FirebaseVisionImageMetadata.ROTATION_270 :
                                FirebaseVisionImageMetadata.ROTATION_90
                )
                .build();

    }
}
